package com.yiqiu.shirohaquiz.importer.parser

import com.yiqiu.shirohaquiz.importer.model.QuestionType

object QuestionTypeLabelParser {
    data class LeadingTypeLabel(
        val type: QuestionType,
        val label: String,
        val remainder: String
    )

    private val wrappers = listOf(
        "【" to "】",
        "[" to "]",
        "（" to "）",
        "(" to ")",
        "〔" to "〕",
        "〖" to "〗",
        "《" to "》"
    )

    private val typeAliases = listOf(
        listOf("单项选择题", "单选题", "选择题", "单项选择", "单选") to QuestionType.SINGLE,
        listOf("不定项选择题", "多项选择题", "多选题", "不定项选择", "多项选择", "多选") to QuestionType.MULTIPLE,
        listOf("判断题", "是非题", "对错题", "判断", "是非", "对错") to QuestionType.JUDGE,
        listOf("填空题", "补全题", "填空", "补全") to QuestionType.BLANK,
        listOf(
            "结构化面试题", "公考面试题", "公务员面试题", "材料分析题", "案例分析题",
            "名词解释", "论述题", "简答题", "问答题", "面试题", "综合题",
            "结构化面试", "公考面试", "公务员面试", "材料分析", "案例分析",
            "论述", "简答", "问答", "面试", "综合"
        ) to QuestionType.SHORT
    )

    private val aliasesByLength = typeAliases
        .flatMap { (aliases, type) -> aliases.map { alias -> alias to type } }
        .sortedByDescending { it.first.length }

    fun parseLabel(raw: String): QuestionType? {
        val label = unwrap(raw).trim().trimEnd(':', '：')
        return aliasesByLength.firstOrNull { (alias, _) -> label == alias }?.second
    }

    fun extractLeading(raw: String): LeadingTypeLabel? {
        val text = raw.trimStart()
        if (text.isBlank()) return null

        wrappers.forEach { (open, close) ->
            if (text.startsWith(open)) {
                val end = text.indexOf(close, startIndex = open.length)
                if (end > open.length) {
                    val label = text.substring(open.length, end).trim()
                    val type = parseLabel(label)
                    if (type != null) {
                        val remainder = cleanLeadingRemainder(text.substring(end + close.length))
                        return LeadingTypeLabel(type = type, label = label, remainder = remainder)
                    }
                }
            }
        }

        aliasesByLength.forEach { (alias, type) ->
            if (text.startsWith(alias)) {
                val next = text.getOrNull(alias.length)
                if (next == null || next.isWhitespace() || next in listOf(':', '：', '-', '－', '—', '、', '.', '．', '(', '（', '[', '【', '〔', '〖', '《')) {
                    val remainder = cleanLeadingRemainder(text.substring(alias.length))
                    return LeadingTypeLabel(type = type, label = alias, remainder = remainder)
                }
            }
        }

        return null
    }

    fun hasLeading(raw: String): Boolean = extractLeading(raw) != null

    private fun unwrap(raw: String): String {
        val text = raw.trim()
        wrappers.forEach { (open, close) ->
            if (text.startsWith(open) && text.endsWith(close) && text.length > open.length + close.length) {
                return text.substring(open.length, text.length - close.length).trim()
            }
        }
        return text
    }

    private fun cleanLeadingRemainder(raw: String): String {
        return raw.trimStart()
            .trimStart(':', '：', '-', '－', '—')
            .trimStart()
    }
}
