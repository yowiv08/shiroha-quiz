package com.yiqiu.shirohaquiz.state

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yiqiu.shirohaquiz.importer.model.Option
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import org.json.JSONArray
import org.json.JSONObject

data class QuizBank(
    val id: String,
    val name: String,
    val questions: List<Question>
)

data class ExamSummary(
    val total: Int,
    val answered: Int,
    val correct: Int,
    val durationSeconds: Int,
    val remainingSeconds: Int,
    val autoSubmitted: Boolean
)

object QuizRepository {
    private const val PREFS_NAME = "shiroha_quiz_native"
    private const val KEY_BANKS = "banks"
    private const val KEY_ACTIVE_BANK_ID = "active_bank_id"

    val banks = mutableStateListOf<QuizBank>()
    var activeBankId by mutableStateOf<String?>(null)
    var practiceIndex by mutableStateOf(0)
    var selectedAnswer by mutableStateOf<List<String>>(emptyList())
    private var initialized by mutableStateOf(false)

    var examQuestions by mutableStateOf<List<Question>>(emptyList())
        private set
    var examIndex by mutableStateOf(0)
        private set
    var examDurationSeconds by mutableStateOf(0)
        private set
    var examRemainingSeconds by mutableStateOf(0)
        private set
    var examFinished by mutableStateOf(false)
        private set
    var examAutoSubmitted by mutableStateOf(false)
        private set
    val examAnswers = mutableStateMapOf<String, List<String>>()

    fun init(context: Context) {
        if (initialized) return
        initialized = true

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val banksJson = prefs.getString(KEY_BANKS, null)
        val restoredBanks = runCatching { parseBanksJson(banksJson) }.getOrDefault(emptyList())

        banks.clear()
        if (restoredBanks.isNotEmpty()) {
            banks.addAll(restoredBanks)
            activeBankId = prefs.getString(KEY_ACTIVE_BANK_ID, restoredBanks.firstOrNull()?.id)
        } else {
            banks += QuizBank(
                id = "demo-bank",
                name = "示例题库",
                questions = emptyList()
            )
            activeBankId = "demo-bank"
        }
    }

    fun importBank(context: Context, name: String, questions: List<Question>) {
        val bank = QuizBank(
            id = "bank_${System.currentTimeMillis()}",
            name = name.ifBlank { "导入题库" },
            questions = questions
        )
        banks += bank
        activeBankId = bank.id
        practiceIndex = 0
        selectedAnswer = emptyList()
        resetExam()
        persist(context)
    }

    fun setActiveBank(context: Context, bankId: String) {
        activeBankId = bankId
        practiceIndex = 0
        selectedAnswer = emptyList()
        resetExam()
        persist(context)
    }

    fun deleteBank(context: Context, bankId: String) {
        val removingActive = activeBankId == bankId
        banks.removeAll { it.id == bankId }
        if (banks.isEmpty()) {
            banks += QuizBank(
                id = "demo-bank",
                name = "示例题库",
                questions = emptyList()
            )
        }
        if (removingActive || banks.none { it.id == activeBankId }) {
            activeBankId = banks.firstOrNull()?.id
        }
        practiceIndex = 0
        selectedAnswer = emptyList()
        resetExam()
        persist(context)
    }

    fun activeBank(): QuizBank? = banks.firstOrNull { it.id == activeBankId } ?: banks.firstOrNull()

    fun currentPracticeQuestion(): Question? {
        val questions = activeBank()?.questions.orEmpty()
        if (questions.isEmpty()) return null
        val safeIndex = practiceIndex.coerceIn(0, questions.lastIndex)
        if (safeIndex != practiceIndex) practiceIndex = safeIndex
        return questions[safeIndex]
    }

    fun nextQuestion() {
        val questions = activeBank()?.questions.orEmpty()
        if (questions.isEmpty()) return
        practiceIndex = (practiceIndex + 1).coerceAtMost(questions.lastIndex)
        selectedAnswer = emptyList()
    }

    fun previousQuestion() {
        val questions = activeBank()?.questions.orEmpty()
        if (questions.isEmpty()) return
        practiceIndex = (practiceIndex - 1).coerceAtLeast(0)
        selectedAnswer = emptyList()
    }

    fun toggleAnswer(key: String, multiple: Boolean) {
        selectedAnswer = if (multiple) {
            if (selectedAnswer.contains(key)) selectedAnswer - key else selectedAnswer + key
        } else {
            listOf(key)
        }
    }

    fun startExam(questionCount: Int, durationMinutes: Int): Boolean {
        val source = activeBank()?.questions.orEmpty()
        if (source.isEmpty()) return false

        val count = questionCount.coerceIn(1, source.size)
        examQuestions = source.take(count)
        examIndex = 0
        examDurationSeconds = durationMinutes.coerceAtLeast(1) * 60
        examRemainingSeconds = examDurationSeconds
        examFinished = false
        examAutoSubmitted = false
        examAnswers.clear()
        return true
    }

    fun resetExam() {
        examQuestions = emptyList()
        examIndex = 0
        examDurationSeconds = 0
        examRemainingSeconds = 0
        examFinished = false
        examAutoSubmitted = false
        examAnswers.clear()
    }

    fun currentExamQuestion(): Question? {
        if (examQuestions.isEmpty()) return null
        val safeIndex = examIndex.coerceIn(0, examQuestions.lastIndex)
        if (safeIndex != examIndex) examIndex = safeIndex
        return examQuestions[safeIndex]
    }

    fun nextExamQuestion() {
        if (examQuestions.isEmpty()) return
        examIndex = (examIndex + 1).coerceAtMost(examQuestions.lastIndex)
    }

    fun previousExamQuestion() {
        if (examQuestions.isEmpty()) return
        examIndex = (examIndex - 1).coerceAtLeast(0)
    }

    fun toggleExamAnswer(key: String, multiple: Boolean) {
        val question = currentExamQuestion() ?: return
        val current = examAnswers[question.id].orEmpty()
        val updated = if (multiple) {
            if (current.contains(key)) current - key else current + key
        } else {
            listOf(key)
        }
        examAnswers[question.id] = updated
    }

    fun submitExam(autoSubmitted: Boolean = false) {
        if (examQuestions.isEmpty()) return
        examFinished = true
        examAutoSubmitted = autoSubmitted
    }

    fun tickExam() {
        if (examFinished || examQuestions.isEmpty()) return
        if (examRemainingSeconds > 0) {
            examRemainingSeconds -= 1
        }
        if (examRemainingSeconds <= 0 && !examFinished) {
            submitExam(autoSubmitted = true)
        }
    }

    fun examAnsweredCount(): Int = examQuestions.count { examAnswers[it.id].orEmpty().isNotEmpty() }

    fun examCorrectCount(): Int = examQuestions.count { question ->
        val userAnswer = examAnswers[question.id].orEmpty().sorted()
        val correctAnswer = question.answer.sorted()
        when (question.type) {
            QuestionType.SINGLE, QuestionType.MULTIPLE, QuestionType.JUDGE -> userAnswer.isNotEmpty() && userAnswer == correctAnswer
            else -> false
        }
    }

    fun examSummary(): ExamSummary {
        return ExamSummary(
            total = examQuestions.size,
            answered = examAnsweredCount(),
            correct = examCorrectCount(),
            durationSeconds = examDurationSeconds,
            remainingSeconds = examRemainingSeconds,
            autoSubmitted = examAutoSubmitted
        )
    }

    private fun persist(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_BANKS, banksToJson(banks))
            .putString(KEY_ACTIVE_BANK_ID, activeBankId)
            .apply()
    }

    private fun banksToJson(banks: List<QuizBank>): String {
        val bankArray = JSONArray()
        banks.forEach { bank ->
            val bankJson = JSONObject()
            bankJson.put("id", bank.id)
            bankJson.put("name", bank.name)

            val questionsArray = JSONArray()
            bank.questions.forEach { question ->
                val questionJson = JSONObject()
                questionJson.put("id", question.id)
                questionJson.put("number", question.number)
                questionJson.put("type", question.type.name)
                questionJson.put("question", question.question)
                questionJson.put("analysis", question.analysis)
                questionJson.put("category", question.category)
                if (question.score != null) questionJson.put("score", question.score)

                val optionsArray = JSONArray()
                question.options.forEach { option ->
                    val optionJson = JSONObject()
                    optionJson.put("key", option.key)
                    optionJson.put("text", option.text)
                    optionsArray.put(optionJson)
                }
                questionJson.put("options", optionsArray)

                val answersArray = JSONArray()
                question.answer.forEach { answersArray.put(it) }
                questionJson.put("answer", answersArray)

                questionsArray.put(questionJson)
            }

            bankJson.put("questions", questionsArray)
            bankArray.put(bankJson)
        }
        return bankArray.toString()
    }

    private fun parseBanksJson(text: String?): List<QuizBank> {
        if (text.isNullOrBlank()) return emptyList()
        val bankArray = JSONArray(text)

        return buildList {
            for (i in 0 until bankArray.length()) {
                val bankJson = bankArray.optJSONObject(i) ?: continue
                val questionsArray = bankJson.optJSONArray("questions") ?: JSONArray()

                val questions = buildList {
                    for (j in 0 until questionsArray.length()) {
                        val questionJson = questionsArray.optJSONObject(j) ?: continue
                        val optionsArray = questionJson.optJSONArray("options") ?: JSONArray()
                        val answersArray = questionJson.optJSONArray("answer") ?: JSONArray()

                        val options = buildList {
                            for (k in 0 until optionsArray.length()) {
                                val optionJson = optionsArray.optJSONObject(k) ?: continue
                                add(
                                    Option(
                                        key = optionJson.optString("key"),
                                        text = optionJson.optString("text")
                                    )
                                )
                            }
                        }

                        val answers = buildList {
                            for (k in 0 until answersArray.length()) {
                                add(answersArray.optString(k))
                            }
                        }

                        add(
                            Question(
                                id = questionJson.optString("id"),
                                number = questionJson.optString("number"),
                                type = runCatching {
                                    QuestionType.valueOf(questionJson.optString("type"))
                                }.getOrDefault(QuestionType.SINGLE),
                                question = questionJson.optString("question"),
                                options = options,
                                answer = answers,
                                analysis = questionJson.optString("analysis"),
                                category = questionJson.optString("category"),
                                score = if (questionJson.has("score")) questionJson.optDouble("score") else null
                            )
                        )
                    }
                }

                add(
                    QuizBank(
                        id = bankJson.optString("id"),
                        name = bankJson.optString("name"),
                        questions = questions
                    )
                )
            }
        }
    }
}
