package com.yiqiu.shirohaquiz.importer.parser

import com.yiqiu.shirohaquiz.importer.model.Question

object QuestionParser {
    private val sectionHeadingRegex = Regex(
        """^\s*(?:第[一二三四五六七八九十0-9]+卷|[一二三四五六七八九十]+[、.]?(?:单选题|多选题|判断题|填空题|简答题)|单选题|多选题|判断题|填空题|简答题)\s*$"""
    )

    fun parseStandard(text: String): List<Question> {
        return StandardQuestionParser.parse(text)
    }

    fun parseCompact(text: String): List<Question> {
        val preprocessed = text
            .replace(Regex("""\s+([A-G][.、．:：])"""), "\n$1")
            .replace(Regex("""\s+(答案[:：])"""), "\n$1")
            .replace(Regex("""\s+(解析[:：])"""), "\n$1")
        return StandardQuestionParser.parse(preprocessed)
    }

    fun parseSectioned(text: String): List<Question> {
        val cleaned = text.lineSequence()
            .filterNot { sectionHeadingRegex.matches(it.trim()) }
            .joinToString("\n")
        return StandardQuestionParser.parse(cleaned)
    }

    fun looksSectioned(text: String): Boolean {
        return text.lineSequence().any { sectionHeadingRegex.matches(it.trim()) }
    }
}
