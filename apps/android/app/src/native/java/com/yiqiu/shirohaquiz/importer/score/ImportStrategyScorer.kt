package com.yiqiu.shirohaquiz.importer.score

import com.yiqiu.shirohaquiz.importer.model.ImportWarning
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.importer.model.WarningLevel

object ImportStrategyScorer {
    fun score(questions: List<Question>, warnings: List<ImportWarning>): Int {
        if (questions.isEmpty()) return Int.MIN_VALUE / 2
        val answeredCount = questions.count { it.answer.isNotEmpty() }
        val hardErrors = warnings.count { it.level == WarningLevel.ERROR }
        val softWarnings = warnings.count { it.level == WarningLevel.WARNING }
        val analysisCount = questions.count { it.analysis.isNotBlank() }
        val answerCoverage = answeredCount.toDouble() / questions.size.toDouble()
        val objectiveCount = questions.count {
            it.type == QuestionType.SINGLE || it.type == QuestionType.MULTIPLE || it.type == QuestionType.JUDGE
        }
        val optionCoverage = if (objectiveCount == 0) 1.0 else {
            questions.count {
                (it.type == QuestionType.SINGLE || it.type == QuestionType.MULTIPLE || it.type == QuestionType.JUDGE) && it.options.size >= 2
            }.toDouble() / objectiveCount.toDouble()
        }
        val sectionBonus = if (questions.any { it.category.isNotBlank() }) 20 else 0
        val suspiciousFrontMatterCount = questions.count { question ->
            question.number == "00" || Regex("""^(?:说明|注意事项|密卷|绝密|祝各位考生|时间[:：]|考试时间[:：])""").containsMatchIn(question.question.trim())
        }
        val subjectiveCount = questions.count { it.type == QuestionType.SHORT || it.type == QuestionType.BLANK }
        val suspiciousSubjectivePenalty = if (questions.size >= 30 && subjectiveCount > questions.size / 2 && suspiciousFrontMatterCount > 0) 400 else 0

        return questions.size * 5 +
            answeredCount * 12 +
            analysisCount * 4 +
            (answerCoverage * 150).toInt() +
            (optionCoverage * 80).toInt() +
            sectionBonus -
            hardErrors * 35 -
            softWarnings * 6 -
            suspiciousFrontMatterCount * 1200 -
            suspiciousSubjectivePenalty
    }
}
