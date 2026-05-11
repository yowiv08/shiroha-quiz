package com.yiqiu.shirohaquiz.importer.parser

import com.yiqiu.shirohaquiz.importer.model.QuestionType

data class SectionInfo(
    val title: String,
    val forcedType: QuestionType? = null,
    val isAnswerSection: Boolean = false
)

object SectionTitleParser {
    private val chineseIndex = "[一二三四五六七八九十百]+"
    private val leadingIndexRegex = Regex("""^\s*(?:第?${chineseIndex}[、.．、]?|第?\d{1,3}[、.．、]?|\(\s*${chineseIndex}\s*\)|（\s*${chineseIndex}\s*）)?\s*""")
    private val sectionLikeRegex = Regex(
        """^\s*(?:第[一二三四五六七八九十百0-9]+(?:部分|卷|章|节)|[一二三四五六七八九十百]+[、.．]\s*(?:常识判断|言语理解|数量关系|判断推理|资料分析|综合知识|专业知识|基础知识|安全知识|理论知识|综合能力).*)\s*$"""
    )
    private val nonTypeSectionKeywordRegex = Regex("""(部分|试卷|常识判断|言语理解|数量关系|判断推理|资料分析|综合知识|专业知识|基础知识|安全知识|理论知识|综合能力)""")
    private val answerSectionRegex = Regex(
        """(参考答案|标准答案|正确答案|答案(?:与|及)?解析|答案解析|试题答案|答题要点|参考要点|答案区|解析区|答案部分)"""
    )

    fun parse(rawLine: String): SectionInfo? {
        val title = rawLine.trim()
        if (title.isBlank()) return null
        if (title.length > 60) return null

        if (answerSectionRegex.containsMatchIn(title) && !isInlineAnswerLine(title)) {
            return SectionInfo(title = title, isAnswerSection = true)
        }

        val simplified = leadingIndexRegex.replace(title, "")
            .replace(Regex("""[\s:：]+$"""), "")

        val type = when {
            Regex("""^(?:单项选择题?|单选题?|选择题)""").containsMatchIn(simplified) -> QuestionType.SINGLE
            Regex("""^(?:多项选择题?|多选题?|不定项选择题?)""").containsMatchIn(simplified) -> QuestionType.MULTIPLE
            Regex("""^(?:判断题?|是非题|对错题)""").containsMatchIn(simplified) -> QuestionType.JUDGE
            Regex("""^(?:填空题?|补全题?)""").containsMatchIn(simplified) -> QuestionType.BLANK
            Regex("""^(?:简答题?|问答题?|名词解释|论述题?|案例分析题?|综合题)""").containsMatchIn(simplified) -> QuestionType.SHORT
            else -> null
        }

        if (type != null) return SectionInfo(title = title, forcedType = type)
        if (sectionLikeRegex.matches(title) && nonTypeSectionKeywordRegex.containsMatchIn(title)) {
            return SectionInfo(title = title)
        }
        return null
    }

    private fun isInlineAnswerLine(title: String): Boolean {
        return Regex("""^\s*(?:答案|正确答案|参考答案|标准答案|答)\s*[:：]\s*\S+""").containsMatchIn(title)
    }

    fun isSectionHeading(rawLine: String): Boolean = parse(rawLine) != null

    fun forcedTypeOf(rawLine: String): QuestionType? = parse(rawLine)?.forcedType

    fun isAnswerSectionHeading(rawLine: String): Boolean = parse(rawLine)?.isAnswerSection == true
}
