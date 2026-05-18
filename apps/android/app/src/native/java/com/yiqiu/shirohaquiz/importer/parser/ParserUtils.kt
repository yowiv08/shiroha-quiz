package com.yiqiu.shirohaquiz.importer.parser

internal fun normalizeQuestionIndex(raw: String): String {
    val clean = raw.trim()
    if (clean.all { it.isDigit() }) return clean
    return chineseNumberToInt(clean)?.toString().orEmpty()
}

internal fun chineseNumberToInt(raw: String): Int? {
    if (raw.isBlank()) return null
    val digitMap = mapOf(
        '零' to 0, '〇' to 0, '一' to 1, '二' to 2, '两' to 2, '三' to 3, '四' to 4,
        '五' to 5, '六' to 6, '七' to 7, '八' to 8, '九' to 9
    )
    if ('百' in raw) {
        val parts = raw.split('百', limit = 2)
        val hundreds = parts.getOrNull(0)?.takeIf { it.isNotBlank() }?.let { digitMap[it.first()] } ?: 1
        val tail = parts.getOrNull(1).orEmpty()
        return hundreds * 100 + (chineseNumberToInt(tail) ?: 0)
    }
    if ('十' in raw) {
        val parts = raw.split('十', limit = 2)
        val tens = parts.getOrNull(0)?.takeIf { it.isNotBlank() }?.let { digitMap[it.first()] } ?: 1
        val ones = parts.getOrNull(1)?.takeIf { it.isNotBlank() }?.let { digitMap[it.first()] } ?: 0
        return tens * 10 + ones
    }
    return digitMap[raw.first()]
}
