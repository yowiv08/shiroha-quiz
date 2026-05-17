package com.yiqiu.shirohaquiz.importer.parser

object AnswerSectionParser {
    private val answerEntryWithAnalysisRegex = Regex(
        """^\s*(?:第\s*)?\d{1,4}\s*(?:题)?\s*(?:[.、．:：]?\s*(?:[A-Ga-g]{1,7}|对|错|正确|错误|√|×|True|False)|[.、．:：]?\s*(?:[【\[]\s*(?:答案|正确答案|参考答案|标准答案|解析)\s*[】\]]|(?:答案|正确答案|参考答案|标准答案|解析)\s*[:：])\s*(?:[A-Ga-g]{1,7}|对|错|正确|错误|√|×|True|False))\s*[.。]?\s*(?:\[?\s*解析\s*\]?|【\s*解析\s*】|解析)?""",
        RegexOption.IGNORE_CASE
    )

    fun hasAnswerSection(text: String): Boolean {
        if (text.lineSequence().any { SectionTitleParser.isAnswerSectionHeading(it.trim()) }) return true
        return findImplicitAnswerStart(text.lineSequence().toList()) >= 0
    }

    fun splitSections(text: String): Pair<String, String?> {
        val lines = text.lineSequence().toList()
        val explicitIndex = lines.indexOfFirst { SectionTitleParser.isAnswerSectionHeading(it.trim()) }
        val startIndex = if (explicitIndex >= 0) explicitIndex else findImplicitAnswerStart(lines)
        if (startIndex < 0) return text to null
        val questionArea = lines.take(startIndex).joinToString("\n").trim()
        val answerArea = lines.drop(if (explicitIndex >= 0) startIndex + 1 else startIndex).joinToString("\n").trim()
        return questionArea to answerArea
    }

    fun parse(text: String): List<ParsedAnswerEntry> {
        val (_, answerArea) = splitSections(text)
        val sectionText = answerArea?.takeIf { it.isNotBlank() } ?: text
        val parsed = AnswerParser.parse(sectionText)
        if (parsed.isNotEmpty()) return parsed

        val blocks = QuestionBlockSplitter.split(sectionText, allowUnnumbered = false)
        return blocks.mapNotNull { block ->
            val combined = block.lines.joinToString("\n")
            val entries = AnswerParser.parse("${block.number}. $combined")
            entries.firstOrNull()
        }
    }

    private fun findImplicitAnswerStart(lines: List<String>): Int {
        if (lines.size < 8) return -1
        val startAt = (lines.size * 0.25).toInt().coerceAtLeast(0)
        for (index in startAt until lines.size) {
            if (!answerEntryWithAnalysisRegex.containsMatchIn(lines[index].trim())) continue
            val nextWindow = lines.drop(index).take(12)
            val hitCount = nextWindow.count { answerEntryWithAnalysisRegex.containsMatchIn(it.trim()) }
            if (hitCount >= 3) return index
        }
        return -1
    }
}
