package com.yiqiu.shirohaquiz.importer.parser

import com.yiqiu.shirohaquiz.importer.model.QuestionType

data class QuestionBlock(
    val number: String,
    val lines: List<String>,
    val category: String = "",
    val forcedType: QuestionType? = null,
    val sequence: Int = 0
)

object QuestionBlockSplitter {
    private val strictQuestionStartRegex = Regex(
        """^\s*(?:第\s*)?(\d{1,4})\s*(?:题)?\s*[.、．:：)）]\s*(.*)$"""
    )
    private val spacedQuestionStartRegex = Regex(
        """^\s*(\d{1,3})\s+(.+)$"""
    )
    private val gluedQuestionStartRegex = Regex(
        """^\s*(\d{1,2})(?=[\u4e00-\u9fa5A-Za-z])(.*)$"""
    )
    private val optionStartRegex = Regex("""^\s*[A-Ga-g]\s*[.、．:：)）]""")
    private val answerLineRegex = Regex("""^\s*\[?\s*(?:答案|正确答案|参考答案|标准答案|答)\s*[:：]""")
    private val analysisLineRegex = Regex("""^\s*\[?\s*(?:解析|答案解析|说明)\s*[:：]""")
    private val embeddedAnswerRegex = Regex("""\[\s*(?:答案|正确答案|参考答案|标准答案)\s*[:：]""")

    fun split(
        text: String,
        forcedType: QuestionType? = null,
        category: String = "",
        allowUnnumbered: Boolean = true
    ): List<QuestionBlock> {
        val blocks = mutableListOf<QuestionBlock>()
        var currentNumber: String? = null
        var currentLines = mutableListOf<String>()
        var syntheticNumber = 0
        var sequence = 0

        fun flush() {
            val number = currentNumber ?: return
            val cleanLines = currentLines.map { it.trim() }.filter { it.isNotBlank() }
            if (cleanLines.isNotEmpty()) {
                blocks += QuestionBlock(
                    number = number,
                    lines = cleanLines,
                    category = category,
                    forcedType = forcedType,
                    sequence = sequence++
                )
            }
            currentNumber = null
            currentLines = mutableListOf()
        }

        text.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isBlank()) return@forEach
            SectionTitleParser.parse(line)?.let { section ->
                if (!section.isAnswerSection) return@forEach
            }

            val explicitStart = parseQuestionStart(line)
            if (explicitStart != null) {
                flush()
                currentNumber = explicitStart.first
                currentLines = mutableListOf<String>().apply {
                    val remainder = explicitStart.second.trim()
                    if (remainder.isNotBlank()) add(remainder)
                }
                return@forEach
            }

            if (currentNumber == null) {
                if (allowUnnumbered && (isLikelyUnnumberedQuestionLine(line) || isLikelyTypedQuestionLine(line, forcedType))) {
                    syntheticNumber += 1
                    currentNumber = syntheticNumber.toString()
                    currentLines += line
                }
                return@forEach
            }

            if (allowUnnumbered && shouldStartNextSyntheticBlock(currentLines, line)) {
                flush()
                syntheticNumber += 1
                currentNumber = syntheticNumber.toString()
                currentLines += line
            } else {
                currentLines += line
            }
        }

        flush()
        return blocks
    }

    private fun parseQuestionStart(line: String): Pair<String, String>? {
        strictQuestionStartRegex.find(line)?.let { match ->
            return match.groupValues[1] to match.groupValues[2]
        }

        spacedQuestionStartRegex.find(line)?.let { match ->
            val number = match.groupValues[1]
            val rest = match.groupValues[2]
            if (number.length <= 3 && !looksLikeYearPrefix(number, rest)) {
                return number to rest
            }
        }

        gluedQuestionStartRegex.find(line)?.let { match ->
            val number = match.groupValues[1]
            val rest = match.groupValues[2]
            if (number.length <= 2 && rest.isNotBlank()) {
                return number to rest
            }
        }

        return null
    }

    private fun looksLikeYearPrefix(number: String, rest: String): Boolean {
        return number.length == 4 && rest.startsWith("年")
    }

    private fun shouldStartNextSyntheticBlock(currentLines: List<String>, nextLine: String): Boolean {
        if (!isLikelyUnnumberedQuestionLine(nextLine)) return false
        if (optionStartRegex.containsMatchIn(nextLine)) return false
        val hasOption = currentLines.any { optionStartRegex.containsMatchIn(it) || hasMultipleInlineOptions(it) }
        val hasAnswer = currentLines.any { embeddedAnswerRegex.containsMatchIn(it) || answerLineRegex.containsMatchIn(it) }
        return hasAnswer || hasOption
    }

    private fun isLikelyTypedQuestionLine(line: String, forcedType: QuestionType?): Boolean {
        if (forcedType == null) return false
        if (line.length < 2) return false
        if (optionStartRegex.containsMatchIn(line)) return false
        if (answerLineRegex.containsMatchIn(line) || analysisLineRegex.containsMatchIn(line)) return false
        if (SectionTitleParser.isSectionHeading(line)) return false
        return true
    }

    private fun isLikelyUnnumberedQuestionLine(line: String): Boolean {
        if (line.length < 4) return false
        if (optionStartRegex.containsMatchIn(line)) return false
        if (answerLineRegex.containsMatchIn(line) || analysisLineRegex.containsMatchIn(line)) return false
        if (SectionTitleParser.isSectionHeading(line)) return false
        if (embeddedAnswerRegex.containsMatchIn(line)) return true
        if (Regex("""[（(]\s*(?:[A-Ga-g]{1,7}|对|错|正确|错误|√|×|True|False)\s*[)）]""", RegexOption.IGNORE_CASE).containsMatchIn(line)) return true
        if (Regex("""[（(]\s*[)）]""").containsMatchIn(line)) return true
        if (Regex("""[?？。]$""").containsMatchIn(line)) return true
        return false
    }

    private fun hasMultipleInlineOptions(line: String): Boolean {
        return Regex("""[A-Ga-g]\s*[.、．:：)）]""").findAll(line).take(2).count() >= 2
    }
}
