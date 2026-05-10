package com.yiqiu.shirohaquiz.state

import com.yiqiu.shirohaquiz.importer.model.Option
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizRepositoryExamTest {

    @After
    fun tearDown() {
        QuizRepository.banks.clear()
        QuizRepository.activeBankId = null
        QuizRepository.practiceIndex = 0
        QuizRepository.selectedAnswer = emptyList()
        QuizRepository.resetExam()
    }

    @Test
    fun `start exam should select active bank questions and initialize timer`() {
        seedBank()

        val started = QuizRepository.startExam(questionCount = 2, durationMinutes = 15)

        assertTrue(started)
        assertEquals(2, QuizRepository.examQuestions.size)
        assertEquals(900, QuizRepository.examRemainingSeconds)
        assertNotNull(QuizRepository.currentExamQuestion())
    }

    @Test
    fun `submit exam should count correct objective answers`() {
        seedBank()
        QuizRepository.startExam(questionCount = 2, durationMinutes = 10)

        QuizRepository.toggleExamAnswer("A", multiple = false)
        QuizRepository.nextExamQuestion()
        QuizRepository.toggleExamAnswer("A", multiple = true)
        QuizRepository.toggleExamAnswer("B", multiple = true)
        QuizRepository.submitExam()

        val summary = QuizRepository.examSummary()
        assertTrue(QuizRepository.examFinished)
        assertEquals(2, summary.answered)
        assertEquals(2, summary.correct)
    }

    @Test
    fun `tick exam should auto submit at zero`() {
        seedBank()
        QuizRepository.startExam(questionCount = 1, durationMinutes = 1)

        repeat(60) {
            QuizRepository.tickExam()
        }

        assertEquals(0, QuizRepository.examRemainingSeconds)
        assertTrue(QuizRepository.examFinished)
        assertTrue(QuizRepository.examAutoSubmitted)
    }

    @Test
    fun `start exam should fail when active bank has no questions`() {
        QuizRepository.banks.clear()
        QuizRepository.banks += QuizBank("empty", "空题库", emptyList())
        QuizRepository.activeBankId = "empty"

        val started = QuizRepository.startExam(questionCount = 10, durationMinutes = 20)

        assertFalse(started)
        assertTrue(QuizRepository.examQuestions.isEmpty())
    }

    private fun seedBank() {
        QuizRepository.banks.clear()
        QuizRepository.banks += QuizBank(
            id = "bank-1",
            name = "测试题库",
            questions = listOf(
                Question(
                    id = "q1",
                    number = "1",
                    type = QuestionType.SINGLE,
                    question = "安全帽的主要作用是（ ）",
                    options = listOf(
                        Option("A", "保护头部"),
                        Option("B", "装饰作用")
                    ),
                    answer = listOf("A"),
                    analysis = "安全帽用于保护头部。"
                ),
                Question(
                    id = "q2",
                    number = "2",
                    type = QuestionType.MULTIPLE,
                    question = "雨天驾驶应注意哪些事项（ ）",
                    options = listOf(
                        Option("A", "降低车速"),
                        Option("B", "加大跟车距离"),
                        Option("C", "急打方向")
                    ),
                    answer = listOf("A", "B"),
                    analysis = "雨天路滑，应控制车速并加大车距。"
                )
            )
        )
        QuizRepository.activeBankId = "bank-1"
        QuizRepository.practiceIndex = 0
        QuizRepository.selectedAnswer = emptyList()
        QuizRepository.resetExam()
    }
}
