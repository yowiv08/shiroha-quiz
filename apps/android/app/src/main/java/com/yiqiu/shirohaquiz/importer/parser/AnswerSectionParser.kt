package com.yiqiu.shirohaquiz.importer.parser

object AnswerSectionParser {
    private val answerSectionHeadingRegex = Regex(
        """^(?:参考答案|答案解析|答案与解析|解析区|答案区)\s*$"""
    )
    private val answerPrefixRegex = Regex("""(?:\[?答案]?\s*[:：]?\s*)(.+)""")
    private val analysisPrefixRegex = Regex("""(?:\[?解析]?\s*[:：]?\s*)(.+)""")
    private val inlineAnswerAnalysisRegex = Regex(
        """(?:\[?答案]?\s*[:：]?\s*)(.+?)(?:\s*(?:\[?解析]?\s*[:：]?\s*)(.*))?$"""
    )

    fun hasAnswerSection(text: String): Boolean {
        return text.lineSequence().any { answerSectionHeadingRegex.matches(it.trim()) }
    }

    fun splitSections(text: String): Pair<String, String?> {
        val lines = text.lineSequence().toList()
        val startIndex = lines.indexOfFirst { answerSectionHeadingRegex.matches(it.trim()) }
        if (startIndex < 0) return text to null
        val questionArea = lines.take(startIndex).joinToString("\n").trim()
        val answerArea = lines.drop(startIndex + 1).joinToString("\n").trim()
        return questionArea to answerArea
    }

    fun parse(text: String): List<ParsedAnswerEntry> {
        val (_, answerArea) = splitSections(text)
        if (answerArea.isNullOrBlank()) return emptyList()

        val sectionText = answerArea
        val blocks = QuestionBlockSplitter.split(sectionText)

        return blocks.mapNotNull { block ->
            var answer = emptyList<String>()
            val analysisParts = mutableListOf<String>()

            block.lines.forEach { rawLine ->
                val line = rawLine.trim()
                val inlineMatch = inlineAnswerAnalysisRegex.find(line)
                val answerMatch = answerPrefixRegex.find(line)
                val analysisMatch = analysisPrefixRegex.find(line)

                when {
                    inlineMatch != null && line.contains("解析") -> {
                        val answerText = inlineMatch.groupValues[1].trim()
                        val analysisText = inlineMatch.groupValues.getOrElse(2) { "" }.trim()
                        if (answer.isEmpty()) {
                            answer = AnswerTokenParser.parseObjectiveAnswers(answerText)
                        }
                        if (analysisText.isNotBlank()) {
                            analysisParts += analysisText
                        }
                    }
                    answerMatch != null && answer.isEmpty() -> {
                        answer = AnswerTokenParser.parseObjectiveAnswers(answerMatch.groupValues[1])
                    }
                    analysisMatch != null -> {
                        analysisParts += analysisMatch.groupValues[1].trim()
                    }
                    answer.isNotEmpty() -> {
                        analysisParts += line
                    }
                    AnswerTokenParser.isObjectiveAnswerText(line) -> {
                        answer = AnswerTokenParser.parseObjectiveAnswers(line)
                    }
                }
            }

            if (answer.isEmpty()) {
                null
            } else {
                ParsedAnswerEntry(
                    number = block.number,
                    answer = answer,
                    analysis = analysisParts.joinToString("\n").trim()
                )
            }
        }
    }
}
