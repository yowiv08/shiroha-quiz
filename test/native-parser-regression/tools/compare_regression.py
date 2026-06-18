from __future__ import annotations

import json
import sys
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "manifest.json"
EXPECTED_DIR = ROOT / "expected"
ACTUAL_DIR = ROOT / "actual"


@dataclass
class CaseResult:
    case_id: str
    passed: bool = True
    messages: list[str] = field(default_factory=list)
    actual_summary: str = ""
    actual_details: list[str] = field(default_factory=list)

    def fail(self, message: str) -> None:
        self.passed = False
        self.messages.append(message)

    def note(self, message: str) -> None:
        self.messages.append(message)


def main() -> None:
    ACTUAL_DIR.mkdir(parents=True, exist_ok=True)
    expected_paths = load_manifest_expected_paths()
    results = [compare_case(path) for path in expected_paths]
    write_summary(results)
    write_report(results)

    failed = [result for result in results if not result.passed]
    print(f"cases={len(results)} passed={len(results) - len(failed)} failed={len(failed)}")
    for result in failed:
        print(f"FAIL {result.case_id}")
        for message in result.messages:
            print(f"  - {message}")
        for detail in result.actual_details:
            print(f"    {detail}")

    if failed:
        sys.exit(1)


def load_manifest_expected_paths() -> list[Path]:
    manifest = read_json(MANIFEST)
    cases = manifest.get("cases", [])
    if not isinstance(cases, list) or not cases:
        raise SystemExit("manifest.json 中没有 cases 用例")

    expected_paths: list[Path] = []
    seen_ids: set[str] = set()
    seen_expected: set[Path] = set()
    for case in cases:
        case_id = case.get("id")
        if not isinstance(case_id, str) or not case_id.strip():
            raise SystemExit("manifest.json 中存在缺少 id 的用例")
        if case_id in seen_ids:
            raise SystemExit(f"manifest.json 中存在重复用例 id：{case_id}")
        seen_ids.add(case_id)

        expected_value = case.get("expected")
        if not isinstance(expected_value, str) or not expected_value.strip():
            raise SystemExit(f"{case_id}: manifest 用例缺少 expected")
        expected_path = resolve_under_root(expected_value)
        if expected_path in seen_expected:
            raise SystemExit(f"manifest.json 中重复引用 expected：{expected_value}")
        if not expected_path.exists():
            raise SystemExit(f"{case_id}: expected 文件不存在：{expected_value}")
        expected_paths.append(expected_path)
        seen_expected.add(expected_path)

    all_expected = {path.resolve() for path in EXPECTED_DIR.glob("*.json")}
    declared_expected = {path.resolve() for path in expected_paths}
    orphan_expected = sorted(all_expected - declared_expected)
    if orphan_expected:
        names = ", ".join(path.name for path in orphan_expected)
        raise SystemExit(f"expected 目录存在未被 manifest 声明的孤立文件：{names}")

    return expected_paths


def resolve_under_root(relative_path: str) -> Path:
    path = (ROOT / relative_path).resolve()
    if not path.is_relative_to(ROOT):
        raise SystemExit(f"路径不能越过回归目录：{relative_path}")
    return path


def compare_case(expected_path: Path) -> CaseResult:
    expected_doc = read_json(expected_path)
    case_id = expected_doc["id"]
    result = CaseResult(case_id=case_id)
    actual_path = ACTUAL_DIR / f"{case_id}.json"
    if not actual_path.exists():
        result.fail(f"缺少实际输出：{actual_path.name}")
        return result

    actual = read_json(actual_path)
    expected = expected_doc.get("expected", {})
    questions = actual.get("questions", [])
    warnings = actual.get("warnings", [])
    result.actual_summary = summarize_actual(actual)

    assert_equal(result, "题目数量", actual.get("questionCount"), expected.get("questionCount"))
    assert_equal(result, "已识别答案数量", actual.get("answeredCount"), expected.get("answeredCount"))
    if "typeCounts" in expected:
        assert_equal(result, "题型分布", actual.get("typeCounts", {}), expected["typeCounts"])
    if "warningsMax" in expected:
        warning_count = len(warnings)
        if warning_count > expected["warningsMax"]:
            result.fail(f"警告数量过多：实际 {warning_count}，期望不超过 {expected['warningsMax']}")

    if "strategyNameContains" in expected:
        strategy_name = actual.get("strategyName", "")
        if expected["strategyNameContains"] not in strategy_name:
            result.fail(f"策略名缺少：{expected['strategyNameContains']}；实际 {strategy_name!r}")

    diagnostics = actual.get("diagnostics", {})
    if "candidateCountMin" in expected:
        actual_value = diagnostics.get("candidateCount", 0)
        if actual_value < expected["candidateCountMin"]:
            result.fail(f"候选数量不足：实际 {actual_value}，期望至少 {expected['candidateCountMin']}")
    if "errorCountMax" in expected:
        actual_value = diagnostics.get("errorCount", 0)
        if actual_value > expected["errorCountMax"]:
            result.fail(f"错误数量过多：实际 {actual_value}，期望不超过 {expected['errorCountMax']}")

    warning_text = "\n".join(warning.get("message", "") for warning in warnings)
    for expected_warning in expected.get("warningContains", []):
        if expected_warning not in warning_text:
            result.fail(f"警告信息缺少：{expected_warning}")
    for forbidden_warning in expected.get("warningNotContains", []):
        if forbidden_warning in warning_text:
            result.fail(f"警告信息不应包含：{forbidden_warning}")

    for forbidden in expected.get("mustNotCreateQuestionFrom", []):
        if any(forbidden in question.get("question", "") for question in questions):
            result.fail(f"不应把“{forbidden}”解析成题干")

    for forbidden in expected.get("stemMustNotContain", []):
        if any(forbidden in question.get("question", "") for question in questions):
            result.fail(f"题干不应包含分区标题“{forbidden}”")

    for forbidden in expected.get("optionMustNotContain", []):
        if any(forbidden in option_text for question in questions for option_text in question.get("options", {}).values()):
            result.fail(f"选项不应包含：{forbidden}")

    for forbidden in expected.get("analysisMustNotContain", []):
        if any(forbidden in question.get("analysis", "") for question in questions):
            result.fail(f"解析不应包含：{forbidden}")

    for expected_question in expected.get("questions", []):
        actual_question = find_question(questions, expected_question.get("number"))
        if actual_question is None:
            result.fail(f"缺少第 {expected_question.get('number')} 题")
            continue
        compare_question(result, expected_question, actual_question)

    if not result.passed:
        result.actual_details = summarize_actual_details(questions, warnings)

    return result


def compare_question(result: CaseResult, expected: dict[str, Any], actual: dict[str, Any]) -> None:
    number = expected.get("number", "?")
    for key, label in [("type", "题型"), ("optionCount", "选项数")]:
        if key in expected:
            assert_equal(result, f"第 {number} 题{label}", actual.get(key), expected[key])

    if "mustNotType" in expected and actual.get("type") == expected["mustNotType"]:
        result.fail(f"第 {number} 题不应识别为 {expected['mustNotType']}")

    if "answer" in expected:
        assert_equal(result, f"第 {number} 题答案", actual.get("answer", []), expected["answer"])

    if "blankAnswers" in expected:
        assert_equal(result, f"第 {number} 题逐空答案", actual.get("blankAnswers", []), expected["blankAnswers"])

    if "stemContains" in expected and expected["stemContains"] not in actual.get("question", ""):
        result.fail(f"第 {number} 题题干缺少：{expected['stemContains']}")

    for forbidden in expected.get("stemNotContains", []):
        if forbidden in actual.get("question", ""):
            result.fail(f"第 {number} 题题干不应包含：{forbidden}")

    if "analysisContains" in expected and expected["analysisContains"] not in actual.get("analysis", ""):
        result.fail(f"第 {number} 题解析缺少：{expected['analysisContains']}")

    if "answerTextContains" in expected:
        answer_text = "\n".join(actual.get("answer", []))
        if expected["answerTextContains"] not in answer_text:
            result.fail(f"第 {number} 题文本答案缺少：{expected['answerTextContains']}")

    for forbidden_answer in expected.get("mustNotAnswer", []):
        if forbidden_answer in actual.get("answer", []):
            result.fail(f"第 {number} 题答案不应被拆成：{forbidden_answer}")

    expected_options = expected.get("options", {})
    actual_options = actual.get("options", {})
    for key, value in expected_options.items():
        if actual_options.get(key) != value:
            result.fail(f"第 {number} 题选项 {key} 不匹配：实际 {actual_options.get(key)!r}，期望 {value!r}")


def find_question(questions: list[dict[str, Any]], number: Any) -> dict[str, Any] | None:
    if number is None:
        return None
    number = str(number)
    return next((question for question in questions if str(question.get("number")) == number), None)


def assert_equal(result: CaseResult, label: str, actual: Any, expected: Any) -> None:
    if expected is None:
        return
    if actual != expected:
        result.fail(f"{label}不匹配：实际 {actual!r}，期望 {expected!r}")


def summarize_actual(actual: dict[str, Any]) -> str:
    return (
        f"题数 {actual.get('questionCount')}，"
        f"答案 {actual.get('answeredCount')}，"
        f"题型 {actual.get('typeCounts')}，"
        f"警告 {len(actual.get('warnings', []))}，"
        f"策略 {actual.get('strategyName')}，"
        f"候选 {actual.get('diagnostics', {}).get('candidateCount')}"
    )


def summarize_actual_details(questions: list[dict[str, Any]], warnings: list[dict[str, Any]]) -> list[str]:
    details: list[str] = []
    if warnings:
        details.append("实际警告：" + "；".join(
            f"{warning.get('level', '')}/{warning.get('number', '')}: {warning.get('message', '')}"
            for warning in warnings[:5]
        ))
    if questions:
        details.append("实际题目摘录：")
        for question in questions[:3]:
            options = question.get("options", {})
            option_summary = "；".join(f"{key}.{value}" for key, value in list(options.items())[:4])
            details.append(
                "  - "
                f"第{question.get('number')}题 "
                f"type={question.get('type')} "
                f"answer={question.get('answer', [])} "
                f"options={{{option_summary}}} "
                f"stem={shorten(question.get('question', ''))} "
                f"analysis={shorten(question.get('analysis', ''))}"
            )
        if len(questions) > 3:
            details.append(f"  - 其余 {len(questions) - 3} 题略")
    else:
        details.append("实际题目摘录：无题目")
    return details


def shorten(value: str, limit: int = 80) -> str:
    value = value.replace("\n", " ").strip()
    if len(value) <= limit:
        return value
    return value[: limit - 1] + "…"


def write_summary(results: list[CaseResult]) -> None:
    payload = {
        "total": len(results),
        "passed": sum(1 for result in results if result.passed),
        "failed": sum(1 for result in results if not result.passed),
        "cases": [
            {
                "id": result.case_id,
                "passed": result.passed,
                "messages": result.messages,
                "actualSummary": result.actual_summary,
                "actualDetails": result.actual_details,
            }
            for result in results
        ],
    }
    (ACTUAL_DIR / "comparison-summary.json").write_text(
        json.dumps(payload, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )


def write_report(results: list[CaseResult]) -> None:
    lines = [
        "# 原生解析器外部回归核对报告",
        "",
        "本报告由 `tools/compare_regression.py` 生成，只对比 `actual/` 与 `expected/`，不修改源码。",
        "",
        f"- 总用例：{len(results)}",
        f"- 通过：{sum(1 for result in results if result.passed)}",
        f"- 未通过：{sum(1 for result in results if not result.passed)}",
        "",
        "## 明细",
        "",
    ]
    for result in results:
        status = "通过" if result.passed else "未通过"
        lines.append(f"### {result.case_id}：{status}")
        lines.append("")
        lines.append(f"- 实际摘要：{result.actual_summary or '无实际输出'}")
        if result.messages:
            lines.append("- 差异：")
            for message in result.messages:
                lines.append(f"  - {message}")
        else:
            lines.append("- 无差异")
        if result.actual_details:
            lines.append("- 失败定位：")
            for detail in result.actual_details:
                lines.append(f"  - {detail}")
        lines.append("")

    (ACTUAL_DIR / "REGRESSION_REPORT.md").write_text("\n".join(lines), encoding="utf-8")


def read_json(path: Path) -> dict[str, Any]:
    return json.loads(path.read_text(encoding="utf-8"))


if __name__ == "__main__":
    main()
