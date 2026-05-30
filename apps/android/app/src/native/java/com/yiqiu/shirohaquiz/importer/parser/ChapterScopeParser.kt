package com.yiqiu.shirohaquiz.importer.parser

object ChapterScopeParser {
    private val chapterRegex = Regex("""第\s*([一二三四五六七八九十百0-9]+)\s*章""")

    fun normalizeChapter(raw: String): String {
        val token = chapterRegex.find(raw)?.groupValues?.getOrNull(1).orEmpty()
        if (token.isBlank()) return ""
        val number = normalizeQuestionIndex(token)
        return if (number.isNotBlank()) "第${number}章" else "第${token}章"
    }

    fun hasChapter(raw: String): Boolean = normalizeChapter(raw).isNotBlank()
}
