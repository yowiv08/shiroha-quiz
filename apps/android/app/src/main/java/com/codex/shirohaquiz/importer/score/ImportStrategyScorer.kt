package com.codex.shirohaquiz.importer.score

import com.codex.shirohaquiz.importer.model.ImportIssue
import com.codex.shirohaquiz.importer.model.Question

object ImportStrategyScorer {
    fun score(questions: List<Question>, issues: List<ImportIssue>): Int {
        val hardErrors = issues.count { it.isHardError }
        val softErrors = issues.size - hardErrors
        val answered = questions.count { it.answer.isNotEmpty() }
        return answered * 100 - hardErrors * 120 - softErrors * 30
    }
}
