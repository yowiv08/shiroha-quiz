package com.yiqiu.shirohaquiz.importer.parser

import com.yiqiu.shirohaquiz.importer.model.QuestionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizImportParserTest {

    @Test
    fun `standard single choice should parse stem options answer and analysis`() {
        val raw = """
            1. 安全帽的主要作用是（A）
            A. 保护头部
            B. 装饰作用
            C. 增加重量
            D. 无实际作用
            答案：A
            解析：安全帽用于减轻坠落物和碰撞对头部造成的伤害。
        """.trimIndent()

        val result = QuizImportParser.parseStandardText(raw)
        val question = result.questions.single()

        assertEquals("标准优先解析", result.strategyName)
        assertEquals(QuestionType.SINGLE, question.type)
        assertEquals("1", question.number)
        assertEquals(4, question.options.size)
        assertEquals(listOf("A"), question.answer)
        assertTrue(question.analysis.contains("减轻坠落物"))
    }

    @Test
    fun `two option correct or wrong should remain single choice`() {
        val raw = """
            1. 关于作业许可，下列说法正确的是（A）
            A. 正确
            B. 错误
            答案：A
        """.trimIndent()

        val result = QuizImportParser.parseStandardText(raw)
        val question = result.questions.single()

        assertEquals(QuestionType.SINGLE, question.type)
        assertEquals(listOf("A"), question.answer)
        assertTrue(result.warnings.any { it.message.contains("两选项单选题") })
    }

    @Test
    fun `trailing judge answer without options should parse as judge`() {
        val raw = "1. 国家安全生产方针是安全第一，预防为主。（对）"

        val result = QuizImportParser.parseStandardText(raw)
        val question = result.questions.single()

        assertEquals(QuestionType.JUDGE, question.type)
        assertEquals(listOf("A"), question.answer)
    }

    @Test
    fun `answer section should merge answer and analysis instead of becoming a new question`() {
        val raw = """
            1. 安全帽的主要作用是（ ）
            A. 保护头部
            B. 装饰作用
            
            答案解析
            1.【答案】A【解析】安全帽用于保护头部。
        """.trimIndent()

        val result = QuizImportParser.parseStandardText(raw)
        val question = result.questions.single()

        assertEquals(listOf("A"), question.answer)
        assertTrue(question.analysis.contains("保护头部"))
    }

    @Test
    fun `dual file merge should apply answers by question number`() {
        val questionText = """
            1. 安全帽的主要作用是（ ）
            A. 保护头部
            B. 装饰作用
            C. 增加重量
            D. 无实际作用
            2. 雨天驾驶时应注意哪些事项（ ）
            A. 降低车速
            B. 加大跟车距离
            C. 急打方向
            D. 紧急制动
        """.trimIndent()

        val answerText = """
            1. A
            2. AB
        """.trimIndent()

        val result = QuizImportParser.parseDualText(questionText, answerText)

        assertEquals(2, result.questions.size)
        assertEquals(listOf("A"), result.questions[0].answer)
        assertEquals(listOf("A", "B"), result.questions[1].answer)
    }

    @Test
    fun `answer parser should support multiple entries on one line`() {
        val parsed = AnswerParser.parse("1. A 2. AB 3. 对")

        assertEquals(3, parsed.size)
        assertEquals(listOf("A"), parsed[0].answer)
        assertEquals(listOf("A", "B"), parsed[1].answer)
        assertEquals(listOf("A"), parsed[2].answer)
    }

    @Test
    fun `answer parser should support range style answers`() {
        val parsed = AnswerParser.parse("1-3: A B C")

        assertEquals(3, parsed.size)
        assertEquals("1", parsed[0].number)
        assertEquals(listOf("A"), parsed[0].answer)
        assertEquals("3", parsed[2].number)
        assertEquals(listOf("C"), parsed[2].answer)
    }

    @Test
    fun `sectioned question text should not leak section heading into stem`() {
        val raw = """
            一、单选题
            1. 安全帽的主要作用是（A）
            A. 保护头部
            B. 装饰作用
            二、多选题
            2. 雨天驾驶应注意哪些事项（AB）
            A. 降低车速
            B. 加大跟车距离
            C. 急打方向
            答案：AB
        """.trimIndent()

        val result = QuizImportParser.parseStandardText(raw)
        assertEquals(2, result.questions.size)
        assertTrue(result.questions.none { it.question.contains("单选题") || it.question.contains("多选题") })
    }
}
