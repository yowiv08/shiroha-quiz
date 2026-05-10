package com.yiqiu.shirohaquiz.importer.parser

import com.yiqiu.shirohaquiz.importer.model.ImportWarning
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.WarningLevel

data class MergeResult(
    val questions: List<Question>,
    val warnings: List<ImportWarning>
)

object DualFileMerger {
    fun mergeByNumber(
        questions: List<Question>,
        answers: List<ParsedAnswerEntry>
    ): MergeResult {
        val answerMap = answers.associateBy { it.number }
        val warnings = mutableListOf<ImportWarning>()

        val mergedQuestions = questions.map { question ->
            val matched = answerMap[question.number]
            if (matched == null) {
                warnings += ImportWarning(
                    level = WarningLevel.WARNING,
                    questionNumber = question.number,
                    message = "未匹配到对应答案"
                )
                question
            } else {
                question.copy(
                    answer = if (question.answer.isNotEmpty()) question.answer else matched.answer,
                    analysis = if (question.analysis.isNotBlank()) question.analysis else matched.analysis
                )
            }
        }

        answers.forEach { entry ->
            if (questions.none { it.number == entry.number }) {
                warnings += ImportWarning(
                    level = WarningLevel.WARNING,
                    questionNumber = entry.number,
                    message = "答案文件中存在未匹配题号"
                )
            }
        }

        return MergeResult(
            questions = mergedQuestions,
            warnings = warnings
        )
    }
}
