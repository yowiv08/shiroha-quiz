package com.yiqiu.shirohaquiz.importer.parser

data class ParsedAnswerEntry(
    val number: String,
    val answer: List<String>,
    val analysis: String = ""
)

object AnswerTokenParser {
    private val judgeTrueRegex = Regex("""^(正确|对|是|√|true)$""", RegexOption.IGNORE_CASE)
    private val judgeFalseRegex = Regex("""^(错误|错|否|×|false)$""", RegexOption.IGNORE_CASE)

    fun isObjectiveAnswerText(raw: String): Boolean {
        val value = raw.trim()
        if (value.isBlank()) return false
        if (judgeTrueRegex.matches(value) || judgeFalseRegex.matches(value)) return true
        val compact = value.replace(Regex("""[,，、;；/\\\s]+"""), "").uppercase()
        return Regex("""^[A-G]{1,7}$""").matches(compact)
    }

    fun parseObjectiveAnswers(raw: String): List<String> {
        val value = raw.trim()
        if (value.isBlank()) return emptyList()
        if (judgeTrueRegex.matches(value)) return listOf("A")
        if (judgeFalseRegex.matches(value)) return listOf("B")

        val compact = value.replace(Regex("""[,，、;；/\\\s]+"""), "").uppercase()
        if (Regex("""^[A-G]{1,7}$""").matches(compact)) {
            return compact.map { it.toString() }.distinct()
        }

        return value.split(Regex("""[,，、;；/\\]+"""))
            .map { it.trim().uppercase() }
            .filter { Regex("""^[A-G]$""").matches(it) }
            .distinct()
    }

    fun parseTextAnswer(raw: String): List<String> {
        val value = raw.trim()
        return if (value.isBlank()) emptyList() else listOf(value)
    }
}

object AnswerParser {
    private val inlineEntryRegex = Regex(
        """(\d{1,4})\s*[.、．:：]?\s*(?:答案\s*[:：]?\s*)?([A-Ga-g对错正确错误√×TF,，、/\\\s]+?)(?=(?:\s+\d{1,4}\s*[.、．:：]?)|$)"""
    )
    private val rangeEntryRegex = Regex(
        """^\s*(\d{1,4})\s*[-~～至]\s*(\d{1,4})\s*[:：]\s*(.+)$"""
    )

    fun parse(text: String): List<ParsedAnswerEntry> {
        val entries = mutableListOf<ParsedAnswerEntry>()

        text.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isBlank()) return@forEach

            val rangeMatch = rangeEntryRegex.find(line)
            if (rangeMatch != null) {
                val start = rangeMatch.groupValues[1].toInt()
                val end = rangeMatch.groupValues[2].toInt()
                val tokens = rangeMatch.groupValues[3]
                    .split(Regex("""\s+"""))
                    .mapNotNull { token ->
                        val parsed = AnswerTokenParser.parseObjectiveAnswers(token)
                        parsed.takeIf { it.isNotEmpty() }
                    }
                if (tokens.isNotEmpty() && end >= start) {
                    (start..end).forEachIndexed { index, number ->
                        if (index < tokens.size) {
                            entries += ParsedAnswerEntry(number.toString(), tokens[index])
                        }
                    }
                    return@forEach
                }
            }

            val lineEntries = inlineEntryRegex.findAll(line).mapNotNull { match ->
                val number = match.groupValues[1]
                val answer = AnswerTokenParser.parseObjectiveAnswers(match.groupValues[2])
                if (answer.isEmpty()) null else ParsedAnswerEntry(number = number, answer = answer)
            }.toList()

            if (lineEntries.isNotEmpty()) {
                entries += lineEntries
            }
        }

        return entries
            .groupBy { it.number }
            .map { (number, grouped) ->
                grouped.last().copy(number = number)
            }
            .sortedBy { it.number.toIntOrNull() ?: Int.MAX_VALUE }
    }
}
