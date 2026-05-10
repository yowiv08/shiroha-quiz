package com.yiqiu.shirohaquiz.importer.parser

import com.yiqiu.shirohaquiz.importer.model.ImportDiagnostics
import com.yiqiu.shirohaquiz.importer.model.ImportResult
import com.yiqiu.shirohaquiz.importer.model.ImportWarning
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.WarningLevel
import com.yiqiu.shirohaquiz.importer.score.ImportStrategyScorer
import com.yiqiu.shirohaquiz.importer.validate.ImportValidator

object QuizImportParser {
    fun parseStandardText(raw: String): ImportResult {
        val normalized = QuestionTextNormalizer.normalize(raw)
        val candidates = mutableListOf<Candidate>()

        val standardQuestions = QuestionParser.parseStandard(normalized)
        candidates += buildCandidate("标准优先解析", standardQuestions)

        if (QuestionParser.looksSectioned(normalized)) {
            val sectionedQuestions = QuestionParser.parseSectioned(normalized)
            candidates += buildCandidate("分区分卷解析", sectionedQuestions)
        }

        if (normalized.contains("A.") && normalized.contains("B.")) {
            val compactQuestions = QuestionParser.parseCompact(normalized)
            candidates += buildCandidate("紧凑格式兜底解析", compactQuestions)
        }

        if (AnswerSectionParser.hasAnswerSection(normalized)) {
            val (questionArea, _) = AnswerSectionParser.splitSections(normalized)
            val baseQuestions = if (QuestionParser.looksSectioned(questionArea)) {
                QuestionParser.parseSectioned(questionArea)
            } else {
                QuestionParser.parseStandard(questionArea)
            }
            val answerEntries = AnswerSectionParser.parse(normalized)
            val merged = DualFileMerger.mergeByNumber(baseQuestions, answerEntries)
            candidates += buildCandidate(
                name = "题目区 + 答案解析区合并",
                questions = merged.questions,
                extraWarnings = merged.warnings
            )
        }

        val best = candidates.maxByOrNull { it.score } ?: Candidate(
            name = "标准优先解析",
            questions = emptyList(),
            warnings = listOf(ImportWarning(WarningLevel.ERROR, null, "未识别到任何题目")),
            score = Int.MIN_VALUE
        )

        return ImportResult(
            questions = best.questions,
            strategyName = best.name,
            warnings = best.warnings,
            diagnostics = ImportDiagnostics(
                normalizedLength = normalized.length,
                blockCount = QuestionBlockSplitter.split(normalized).size,
                answeredCount = best.questions.count { it.answer.isNotEmpty() },
                candidateCount = candidates.size,
                notes = candidates.map { candidate ->
                    "${candidate.name}：${candidate.questions.size}题 / 答案${candidate.questions.count { it.answer.isNotEmpty() }} / 分数${candidate.score}"
                }
            )
        )
    }

    fun parseDualText(questionText: String, answerText: String): ImportResult {
        val normalizedQuestion = QuestionTextNormalizer.normalize(questionText)
        val normalizedAnswer = QuestionTextNormalizer.normalize(answerText)

        val questionCandidates = buildList {
            add("标准题目解析" to QuestionParser.parseStandard(normalizedQuestion))
            if (QuestionParser.looksSectioned(normalizedQuestion)) {
                add("分区题目解析" to QuestionParser.parseSectioned(normalizedQuestion))
            }
            add("紧凑格式题目解析" to QuestionParser.parseCompact(normalizedQuestion))
        }

        val answerCandidates = buildList {
            val plainAnswers = AnswerParser.parse(normalizedAnswer)
            if (plainAnswers.isNotEmpty()) add("普通答案表" to plainAnswers)
            val sectionAnswers = AnswerSectionParser.parse(normalizedAnswer)
            if (sectionAnswers.isNotEmpty()) add("答案解析区" to sectionAnswers)
            val mergedAnswers = (plainAnswers + sectionAnswers)
                .groupBy { it.number }
                .map { (_, entries) -> entries.last() }
                .sortedBy { it.number.toIntOrNull() ?: Int.MAX_VALUE }
            if (mergedAnswers.isNotEmpty()) add("混合答案来源" to mergedAnswers)
        }

        val mergedCandidates = mutableListOf<Candidate>()
        questionCandidates.forEach { (questionStrategy, questions) ->
            answerCandidates.forEach { (answerStrategy, answers) ->
                val merged = DualFileMerger.mergeByNumber(questions, answers)
                mergedCandidates += buildCandidate(
                    name = "$questionStrategy + $answerStrategy",
                    questions = merged.questions,
                    extraWarnings = merged.warnings
                )
            }
        }

        val best = mergedCandidates.maxByOrNull { it.score } ?: Candidate(
            name = "双文件按题号合并",
            questions = emptyList(),
            warnings = listOf(ImportWarning(WarningLevel.ERROR, null, "双文件导入未得到有效题目")),
            score = Int.MIN_VALUE
        )

        return ImportResult(
            questions = best.questions,
            strategyName = best.name,
            warnings = best.warnings,
            diagnostics = ImportDiagnostics(
                normalizedLength = normalizedQuestion.length + normalizedAnswer.length,
                blockCount = QuestionBlockSplitter.split(normalizedQuestion).size,
                answeredCount = best.questions.count { it.answer.isNotEmpty() },
                candidateCount = mergedCandidates.size,
                notes = mergedCandidates.map { candidate ->
                    "${candidate.name}：${candidate.questions.size}题 / 答案${candidate.questions.count { it.answer.isNotEmpty() }} / 分数${candidate.score}"
                }
            )
        )
    }

    private fun buildCandidate(
        name: String,
        questions: List<Question>,
        extraWarnings: List<ImportWarning> = emptyList()
    ): Candidate {
        val warnings = ImportValidator.validate(questions) + extraWarnings
        val score = ImportStrategyScorer.score(questions, warnings)
        return Candidate(name, questions, warnings, score)
    }

    private data class Candidate(
        val name: String,
        val questions: List<Question>,
        val warnings: List<ImportWarning>,
        val score: Int
    )
}
