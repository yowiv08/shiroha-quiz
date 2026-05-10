package com.yiqiu.shirohaquiz.importer.score

import com.yiqiu.shirohaquiz.importer.model.ImportWarning
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.WarningLevel

object ImportStrategyScorer {
    fun score(questions: List<Question>, warnings: List<ImportWarning>): Int {
        val answeredCount = questions.count { it.answer.isNotEmpty() }
        val hardErrors = warnings.count { it.level == WarningLevel.ERROR }
        val softWarnings = warnings.count { it.level == WarningLevel.WARNING }
        val answerCoverageBonus = if (questions.isNotEmpty()) {
            (answeredCount * 100 / questions.size)
        } else {
            0
        }
        return answeredCount * 8 + questions.size * 2 + answerCoverageBonus - hardErrors * 12 - softWarnings * 4
    }
}
