package com.yiqiu.shirohaquiz.importer.parser

import com.yiqiu.shirohaquiz.importer.model.QuestionType

data class ParsedAnswerEntry(
    val number: String,
    val answer: List<String>,
    val analysis: String = "",
    val type: QuestionType? = null,
    val sequence: Int = 0
)

object AnswerTokenParser {
    private val judgeTrueRegex = Regex("""^(正确|对|是|√|true|t)$""", RegexOption.IGNORE_CASE)
    private val judgeFalseRegex = Regex("""^(错误|错|否|×|x|false|f)$""", RegexOption.IGNORE_CASE)
    private val leadingChoiceRegex = Regex("""^\s*([A-Ga-g]{1,7})(?=\s*(?:[.、．:：)）;；\]\}]|[\u4e00-\u9fa5]|$))""")

    fun isObjectiveAnswerText(raw: String): Boolean {
        val value = cleanup(raw)
        if (value.isBlank()) return false
        if (isJudgeAnswerText(value)) return true
        if (leadingChoiceRegex.find(value) != null) return true
        val compact = value.replace(Regex("""[,，、;；/\\s]+"""), "").uppercase()
        return Regex("""^[A-G]{1,7}$""").matches(compact)
    }

    fun isJudgeAnswerText(raw: String): Boolean {
        val value = cleanup(raw)
        if (value.isBlank()) return false
        if (judgeTrueRegex.matches(value) || judgeFalseRegex.matches(value)) return true
        return Regex(
            """^[ABab]\s*[.、．:：)）]?\s*(?:正确|错误|对|错|是|否|√|×|True|False)$""",
            RegexOption.IGNORE_CASE
        ).matches(value)
    }

    fun parseObjectiveAnswers(raw: String): List<String> {
        val value = cleanup(raw)
        if (value.isBlank()) return emptyList()
        if (judgeTrueRegex.matches(value)) return listOf("A")
        if (judgeFalseRegex.matches(value)) return listOf("B")

        leadingChoiceRegex.find(value)?.let { match ->
            val letters = match.groupValues[1].uppercase()
            if (Regex("""^[A-G]{1,7}$""").matches(letters)) {
                return letters.map { it.toString() }.distinct()
            }
        }

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
        val value = cleanup(raw)
        return if (value.isBlank()) emptyList() else listOf(value)
    }

    private fun cleanup(raw: String): String {
        return raw.trim()
            .removePrefix("[")
            .removeSuffix("]")
            .removePrefix("(")
            .removeSuffix(")")
            .replace(Regex("""^\s*(?:答案|正确答案|参考答案|标准答案|答)\s*[:：]?\s*"""), "")
            .trim()
    }
}

object AnswerParser {
    private val inlineEntryRegex = Regex(
        """(?:第\s*)?(\d{1,4})\s*(?:题)?\s*[.、．:：]?\s*(?:答案\s*[:：]?\s*)?([A-Ga-g]{1,7}|对|错|正确|错误|√|×|True|False)(?=\s*(?:\d{1,4}\s*[.、．:：]|$|\[|【|解析|[;；]))""",
        RegexOption.IGNORE_CASE
    )
    private val answerAnalysisLineRegex = Regex(
        """^\s*(?:第\s*)?(\d{1,4})\s*(?:题)?\s*[.、．:：]\s*([A-Ga-g]{1,7}|对|错|正确|错误|√|×|True|False)\s*[.。]?\s*(?:\[?\s*解析\s*\]?\s*[:：]?|【\s*解析\s*】\s*[:：]?)?\s*(.*)$""",
        RegexOption.IGNORE_CASE
    )
    private val rangeEntryRegex = Regex(
        """^\s*(\d{1,4})\s*[-~～至]\s*(\d{1,4})\s*[:：]\s*(.+)$"""
    )

    fun parse(text: String): List<ParsedAnswerEntry> {
        val entries = mutableListOf<ParsedAnswerEntry>()
        var currentType: QuestionType? = null
        var sequence = 0

        text.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isBlank()) return@forEach

            SectionTitleParser.parse(line)?.let { section ->
                if (!section.isAnswerSection) currentType = section.forcedType
                return@forEach
            }

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
                            entries += ParsedAnswerEntry(
                                number = number.toString(),
                                answer = tokens[index],
                                type = currentType,
                                sequence = sequence++
                            )
                        }
                    }
                    return@forEach
                }
            }

            val lineEntries = inlineEntryRegex.findAll(line).mapNotNull { match ->
                val number = match.groupValues[1]
                val answer = AnswerTokenParser.parseObjectiveAnswers(match.groupValues[2])
                if (answer.isEmpty()) null else ParsedAnswerEntry(
                    number = number,
                    answer = answer,
                    type = currentType,
                    sequence = sequence++
                )
            }.toList()

            if (lineEntries.size >= 2) {
                entries += lineEntries
                return@forEach
            }

            answerAnalysisLineRegex.find(line)?.let { match ->
                val answer = AnswerTokenParser.parseObjectiveAnswers(match.groupValues[2])
                if (answer.isNotEmpty()) {
                    entries += ParsedAnswerEntry(
                        number = match.groupValues[1],
                        answer = answer,
                        analysis = match.groupValues[3].trim(),
                        type = currentType,
                        sequence = sequence++
                    )
                    return@forEach
                }
            }

            if (lineEntries.isNotEmpty()) entries += lineEntries
        }

        return entries
    }
}
