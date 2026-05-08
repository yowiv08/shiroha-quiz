# Web导入逻辑移植到原生安卓：实施清单

## 1. 当前目标

你现在不是从零开发安卓 App，而是在已有原生安卓框架中复现 Web 版的题目导入逻辑。

因此重点不是：

```text
安卓项目如何搭建
页面怎么写
Room 如何从零设计
```

而是：

```text
如何把 Web 版 app.js 中的导入解析逻辑，稳定迁移为 Kotlin 原生模块
```

核心目标：

```text
Web 版能导入的题库，安卓端也能导入
Web 版能识别的题型，安卓端也能识别
Web 版的异常 / 错误分级，安卓端保持一致
Web 版的自动推荐策略，安卓端保持同样逻辑
```

---

## 2. 推荐迁移方式

不要逐行翻译 `app.js`。

推荐做法是：

```text
先按功能拆解 Web 版导入逻辑
再用 Kotlin 重新实现每个模块
最后用相同测试题库对照 Web 版结果
```

迁移流程：

```text
app.js 现有逻辑
↓
整理成 parser-spec
↓
Kotlin parser core
↓
安卓导入页面调用 parser core
↓
写入本地题库数据库
```

---

## 3. Kotlin 模块拆分建议

建议在安卓项目中建立类似目录：

```text
importer/
├─ model/
│  ├─ Question.kt
│  ├─ Option.kt
│  ├─ QuestionType.kt
│  ├─ ImportResult.kt
│  ├─ ImportWarning.kt
│  └─ ImportDiagnostics.kt
│
├─ parser/
│  ├─ QuizImportParser.kt
│  ├─ QuestionTextNormalizer.kt
│  ├─ QuestionBlockSplitter.kt
│  ├─ StandardQuestionParser.kt
│  ├─ SectionQuestionParser.kt
│  ├─ CompactQuestionParser.kt
│  ├─ AnswerSectionParser.kt
│  └─ DualFileAnswerParser.kt
│
├─ merge/
│  ├─ AnswerMerger.kt
│  └─ DualFileMerger.kt
│
├─ validate/
│  ├─ ImportValidator.kt
│  └─ ImportWarningClassifier.kt
│
└─ score/
   └─ ImportStrategyScorer.kt
```

---

## 4. Web 版函数与 Kotlin 模块对应关系

### 4.1 文本标准化

Web 版相关逻辑：

```text
normalizeImportText()
cleanText()
preSplitVolumeAndCompactQuestions()
```

Kotlin 对应：

```text
QuestionTextNormalizer.kt
```

职责：

```text
统一换行
统一中文 / 英文标点
清理空行
清理页眉页脚
保留题号、选项、答案、解析等关键信息
```

---

### 4.2 题块切分

Web 版相关逻辑：

```text
splitQuestionBlocks()
isQuestionStart()
hasStrongQuestionNo()
```

Kotlin 对应：

```text
QuestionBlockSplitter.kt
```

职责：

```text
识别题号
识别题目边界
避免把答案解析区当成题目
避免把选项行误判成新题
```

需要支持的题号形式：

```text
1.
1．
1、
【1】
第1题
一、二、三 等分区标题
```

---

### 4.3 标准题解析

Web 版相关逻辑：

```text
parseTextQuestionsBase()
parseStructuredExamText()
parseBlock()
parseOptionsFromLine()
```

Kotlin 对应：

```text
StandardQuestionParser.kt
```

职责：

```text
解析标准题干
解析 A/B/C/D 选项
解析行内答案
解析题内解析
生成 Question 对象
```

适合：

```text
题干
A. 选项
B. 选项
C. 选项
D. 选项
答案：A
解析：……
```

---

### 4.4 分卷分区三层解析

Web 版相关逻辑：

```text
parseByVolumeAndSections()
getHeadingType()
mapType()
```

Kotlin 对应：

```text
SectionQuestionParser.kt
```

职责：

```text
识别大标题
识别题型分区
保留原始分区名称
处理每个分区内题号从 1 重新开始的情况
```

注意：

```text
“逻辑判断”“判断推理”不等于判断题
不能因为标题里有“判断”两个字，就把整区题型改成 judge
```

---

### 4.5 紧凑格式解析

Web 版相关逻辑：

```text
forceSplitCompactText()
parseTextQuestionsBase(forceSplitCompactText(text))
```

Kotlin 对应：

```text
CompactQuestionParser.kt
```

职责：

```text
处理题干、选项、答案挤在一行的情况
```

例如：

```text
1. 题干 A.选项一 B.选项二 C.选项三 D.选项四 答案：C
```

---

### 4.6 答案解析区合并解析

这是 Web 版当前最关键的导入增强点。

Web 版相关逻辑：

```text
hasAnswerAnalysisSignal()
isAnswerSectionHeading()
isAnswerAnalysisEntryLine()
parseAnswerAnalysisEntries()
stripAnswerAnalysisTextForQuestions()
parseDocumentWithAnswerSections()
mergeAnswerAnalysisEntries()
scoreAnswerSectionCandidate()
```

Kotlin 对应：

```text
AnswerSectionParser.kt
AnswerMerger.kt
ImportStrategyScorer.kt
```

职责：

```text
识别答案区 / 答案解析区
把题目区和答案区分开
题目区只解析题干和选项
答案区只提取题号、答案、解析
最后按题号、分区、顺序回填答案
```

必须支持的答案区标题：

```text
答案
答案解析
参考答案
标准答案
正确答案
答案及解析
答案与解析
试题解析
真题答案
```

必须支持的答案格式：

```text
1.【答案】A【解析】……
1.【解析】A。……
1.A.【解析】……
1.A 解析：……
分析：选D
答：选C
故答案选B
因此，本题答案为C
16.B352
20.A122/199
```

关键原则：

```text
答案解析区不能进入普通题目解析器
答案解析区只能作为答案源
```

---

### 4.7 判断题识别

Web 版相关逻辑：

```text
isJudgeSymbolAnswer()
isJudgeBlock()
extractTrailingAnswerFromText()
judgeOptionMap()
normalizeAnswer()
```

Kotlin 对应：

```text
QuestionTypeDetector.kt
AnswerNormalizer.kt
```

规则：

```text
无选项 + 题尾 √/×/对/错 → 判断题
明确判断题分区 → 判断题
有 C/D/E/F/G 选项 → 优先选择题
3 个及以上选项 → 优先选择题
A. 是 / B. 否 → 默认单选题
A. 正确 / B. 错误 → 默认单选题
```

判断题答案映射：

```text
正确 / 对 / √ / 是 / T / True → A
错误 / 错 / × / 否 / F / False → B
```

---

### 4.8 填空题 / 简答题识别

Web 版相关逻辑：

```text
guessType()
shouldGuessBlankFromNoOption()
splitTextAnswer()
splitAnswerByType()
```

Kotlin 对应：

```text
QuestionTypeDetector.kt
AnswerNormalizer.kt
```

规则：

```text
无选项 + 短答案 + 明确填空特征 → blank
无选项 + 长答案 / 多句 / 步骤性答案 → short
有 A/B/C/D 选项时，优先选择题，不要因为题干有横线就判为填空
```

特别注意：

```text
splitAnswer 只处理客观题答案
splitTextAnswer 处理填空 / 简答文本答案
```

---

### 4.9 双文件导入

Web 版相关逻辑：

```text
parseDualImport()
parseAnswerEntries()
parseAnswerEntriesByQuestionParse()
resolveDualAnswerCandidates()
mergeQuestionAnswers()
```

Kotlin 对应：

```text
DualFileAnswerParser.kt
DualFileMerger.kt
```

双文件导入分三层：

```text
题目文件解析
答案文件解析
题目与答案合并
```

答案文件解析候选：

```text
普通答案表提取
答案解析区提取
完整题库解析兜底
混合提取
```

答案对应方式：

```text
自动推荐
按题型分组 + 组内题号对应
智能按题号对应
按顺序对应
```

---

## 5. 自动推荐策略迁移

安卓端必须保留 Web 版的自动推荐思路：

```text
标准优先
复杂兜底
答案解析区作为结构化候选
```

推荐流程：

```kotlin
fun parse(text: String, strategy: ImportStrategy = ImportStrategy.AUTO): ImportResult {
    val normalized = normalizer.normalize(text)
    val profile = analyzer.analyze(normalized)

    val candidates = mutableListOf<ImportCandidate>()

    candidates += standardParser.parse(normalized)
    candidates += structuredParser.parse(normalized)

    if (profile.hasAnswerAnalysisSection) {
        candidates += answerSectionParser.parseDocumentWithAnswerSections(normalized)
    }

    val mainlineBest = scorer.pickBestStableMainline(candidates)

    if (scorer.needComplexFallback(mainlineBest, profile)) {
        candidates += sectionParser.parse(normalized)
        candidates += compactParser.parse(normalized)
    }

    return scorer.pickBest(candidates).toImportResult()
}
```

---

## 6. 候选策略评分

评分不要只看题量。

需要综合：

```text
有效题数
答案命中率
题量是否接近预期
题干是否残留答案 / 解析标记
答案是否超出选项范围
判断题是否错配
答案解析区是否被误识别成题目
红色错误数量
黄色异常数量
```

建议 Kotlin 中设计：

```kotlin
data class ImportCandidate(
    val name: String,
    val questions: List<Question>,
    val warnings: List<ImportWarning>,
    val score: Int
)
```

评分逻辑示例：

```text
有效题 +30
答案成功匹配 +40
解析成功回填 +10
题干残留答案标记 -120
答案超出选项范围 -150
答案解析区伪题 -200
红色错误 -120
黄色异常 -20
```

---

## 7. 数据模型建议

```kotlin
data class Question(
    val id: String = UUID.randomUUID().toString(),
    val number: String = "",
    val type: QuestionType,
    val question: String,
    val options: List<Option> = emptyList(),
    val answer: List<String> = emptyList(),
    val analysis: String = "",
    val category: String = "",
    val sourceIndex: Int = 0
)

data class Option(
    val key: String,
    val text: String
)

enum class QuestionType {
    SINGLE,
    MULTIPLE,
    JUDGE,
    BLANK,
    SHORT
}

data class ImportResult(
    val questions: List<Question>,
    val strategyName: String,
    val warnings: List<ImportWarning>,
    val diagnostics: ImportDiagnostics
)

data class ImportWarning(
    val level: WarningLevel,
    val questionNumber: String?,
    val message: String
)

enum class WarningLevel {
    NORMAL,
    WARNING,
    ERROR
}
```

---

## 8. 需要优先移植的解析能力

迁移优先级建议：

```text
第一优先级：
1. 文本标准化
2. 标准逐行解析
3. 题型判断
4. 答案识别
5. 异常 / 错误校验

第二优先级：
6. 分卷分区三层解析
7. 紧凑格式解析
8. 判断题题尾答案
9. 两选项单选保护

第三优先级：
10. 答案解析区合并解析
11. 双文件答案匹配
12. 候选策略评分
13. 导入诊断报告
```

如果要达到 Web 版当前效果，至少做到第三优先级。

---

## 9. 测试样例必须复用

不要只靠人工试。

建议把 Web 版测试样例整理成：

```text
parser-spec/
├─ samples/
└─ expected/
```

每个样例都对应一个预期 JSON：

```json
{
  "questionCount": 4,
  "typeStats": {
    "single": 2,
    "judge": 1,
    "short": 1
  },
  "questions": [
    {
      "number": "1",
      "type": "single",
      "answer": ["A"],
      "optionCount": 4
    }
  ]
}
```

安卓端单元测试：

```kotlin
@Test
fun parseStandardChoice_shouldMatchExpected() {
    val text = loadSample("standard_choice.txt")
    val expected = loadExpected("standard_choice.expected.json")
    val result = parser.parse(text)
    assertEquals(expected.questionCount, result.questions.size)
}
```

---

## 10. Android 文件读取建议

你已有安卓框架的话，导入文件层面可以这样接：

```text
文件选择器 / SAF
↓
读取文件 Uri
↓
根据扩展名提取文本
↓
调用 QuizImportParser.parse(text)
↓
显示导入预览
↓
用户确认
↓
写入数据库
```

先支持：

```text
txt
剪贴板文本
docx 文本提取
```

后续再支持：

```text
文字型 pdf
```

暂时不建议优先做：

```text
扫描版 PDF OCR
图片题识别
```

---

## 11. Kotlin 正则迁移注意

JS 正则不能完全照搬。

JS：

```javascript
/答案[:：]\s*([A-D])/i
```

Kotlin：

```kotlin
val regex = Regex(
    """答案[:：]\s*([A-D])""",
    RegexOption.IGNORE_CASE
)
```

建议 Kotlin 中多用 raw string：

```kotlin
"""正则内容"""
```

避免大量双重转义。

全局匹配：

```kotlin
regex.findAll(text).toList()
```

---

## 12. 最终接口设计

安卓端最终要实现：

```text
输入：题库文本 / 双文件文本
输出：ImportResult
```

也就是：

```kotlin
interface QuizImportParser {
    fun parseSingleFile(
        text: String,
        strategy: ImportStrategy = ImportStrategy.AUTO
    ): ImportResult

    fun parseDualFile(
        questionText: String,
        answerText: String,
        questionStrategy: ImportStrategy = ImportStrategy.AUTO,
        matchMode: DualMatchMode = DualMatchMode.AUTO
    ): ImportResult
}
```

这样 UI 层只负责：

```text
选择文件
显示预览
确认导入
写入数据库
```

解析逻辑完全独立，方便测试、维护和后续同步 Web 版规则。

---

## 13. 一句话总结

你的安卓框架已经搭好后，下一步最重要的是：

```text
把 Web 版 app.js 的导入逻辑整理成 Kotlin parser core，
并用同一批样例保证 Web 端和 Android 端解析结果一致。
```

不要把导入逻辑写散在页面里，应该做成独立模块。
