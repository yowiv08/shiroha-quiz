package com.yiqiu.shirohaquiz.state

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yiqiu.shirohaquiz.importer.model.Option
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionImage
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

data class WrongQuestionEntry(
    val bankId: String,
    val bankName: String,
    val question: Question,
    val lastAnswer: List<String>,
    val source: String,
    val timestamp: Long
)

data class StudyRecord(
    val id: String,
    val bankId: String?,
    val bankName: String,
    val source: String,
    val title: String,
    val total: Int,
    val correct: Int,
    val timestamp: Long,
    val durationSeconds: Int? = null,
    val autoSubmitted: Boolean = false
)

data class QuestionCheckResult(
    val question: Question,
    val userAnswer: List<String>,
    val correct: Boolean,
    val answerText: String
)

object QuizRepository {
    private const val PREFS_NAME = "shiroha_quiz_native"
    private const val KEY_BANKS = "banks"
    private const val KEY_ACTIVE_BANK_ID = "active_bank_id"
    private const val KEY_WRONG_BOOK = "wrong_book"
    private const val KEY_STUDY_RECORDS = "study_records"

    val banks = mutableStateListOf<QuizBank>()
    val wrongBook = mutableStateListOf<WrongQuestionEntry>()
    val studyRecords = mutableStateListOf<StudyRecord>()

    var activeBankId by mutableStateOf<String?>(null)
    var practiceIndex by mutableStateOf(0)
    var selectedAnswer by mutableStateOf<List<String>>(emptyList())
    var practiceLastResult by mutableStateOf<QuestionCheckResult?>(null)
        private set

    private var initialized by mutableStateOf(false)
    private var appContext: Context? = null

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
        appContext = context.applicationContext

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val banksJson = prefs.getString(KEY_BANKS, null)
        val restoredBanks = runCatching { parseBanksJson(banksJson) }.getOrDefault(emptyList())
        val restoredWrongBook = runCatching {
            parseWrongBookJson(prefs.getString(KEY_WRONG_BOOK, null))
        }.getOrDefault(emptyList())
        val restoredStudyRecords = runCatching {
            parseStudyRecordsJson(prefs.getString(KEY_STUDY_RECORDS, null))
        }.getOrDefault(emptyList())

        banks.clear()
        wrongBook.clear()
        studyRecords.clear()

        val sanitizedRestoredBanks = restoredBanks.map(::sanitizeBank)
        if (sanitizedRestoredBanks.isNotEmpty()) {
            banks.addAll(sanitizedRestoredBanks)
            activeBankId = prefs.getString(KEY_ACTIVE_BANK_ID, sanitizedRestoredBanks.firstOrNull()?.id)
        } else {
            banks += demoBank()
            activeBankId = "demo-bank"
        }

        wrongBook.addAll(restoredWrongBook)
        studyRecords.addAll(restoredStudyRecords)
        if (sanitizedRestoredBanks != restoredBanks) persist()
    }

    fun importBank(context: Context, name: String, questions: List<Question>) {
        appContext = context.applicationContext
        val bank = QuizBank(
            id = "bank_${System.currentTimeMillis()}",
            name = name.ifBlank { "导入题库" },
            questions = questions.map(::sanitizeQuestion)
        )
        banks += bank
        activeBankId = bank.id
        resetPracticeState()
        resetExam()
        persist()
    }

    fun setActiveBank(context: Context, bankId: String) {
        appContext = context.applicationContext
        activeBankId = bankId
        resetPracticeState()
        resetExam()
        persist()
    }

    fun deleteBank(context: Context, bankId: String) {
        appContext = context.applicationContext
        val removingActive = activeBankId == bankId
        banks.removeAll { it.id == bankId }
        wrongBook.removeAll { it.bankId == bankId }
        studyRecords.removeAll { it.bankId == bankId }

        if (banks.isEmpty()) {
            banks += demoBank()
        }
        if (removingActive || banks.none { it.id == activeBankId }) {
            activeBankId = banks.firstOrNull()?.id
        }
        resetPracticeState()
        resetExam()
        persist()
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
        practiceLastResult = null
    }

    fun previousQuestion() {
        val questions = activeBank()?.questions.orEmpty()
        if (questions.isEmpty()) return
        practiceIndex = (practiceIndex - 1).coerceAtLeast(0)
        selectedAnswer = emptyList()
        practiceLastResult = null
    }

    fun openWrongQuestion(entry: WrongQuestionEntry) {
        val bank = banks.firstOrNull { it.id == entry.bankId } ?: return
        val index = bank.questions.indexOfFirst { it.id == entry.question.id }
        if (index >= 0) {
            activeBankId = bank.id
            practiceIndex = index
            selectedAnswer = emptyList()
            practiceLastResult = null
        }
    }

    fun removeWrongQuestion(entry: WrongQuestionEntry) {
        wrongBook.removeAll { it.bankId == entry.bankId && it.question.id == entry.question.id }
        persist()
    }

    fun clearWrongBook() {
        wrongBook.clear()
        persist()
    }

    fun toggleAnswer(key: String, multiple: Boolean) {
        selectedAnswer = if (multiple) {
            if (selectedAnswer.contains(key)) selectedAnswer - key else selectedAnswer + key
        } else {
            listOf(key)
        }
    }

    fun submitPracticeQuestion(): QuestionCheckResult? {
        val question = currentPracticeQuestion() ?: return null
        val result = evaluateQuestion(question, selectedAnswer)
        practiceLastResult = result

        val bank = activeBank()
        recordPracticeResult(bank, question, result)
        if (!result.correct) {
            addWrongQuestion(
                bank = bank,
                question = question,
                userAnswer = result.userAnswer,
                source = "练习"
            )
        }
        persist()
        return result
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
        if (examQuestions.isEmpty() || examFinished) return
        examFinished = true
        examAutoSubmitted = autoSubmitted

        val summary = examSummary()
        val bank = activeBank()
        studyRecords.add(
            0,
            StudyRecord(
                id = "exam_${System.currentTimeMillis()}",
                bankId = bank?.id,
                bankName = bank?.name ?: "未命名题库",
                source = "考试",
                title = "原生考试",
                total = summary.total,
                correct = summary.correct,
                timestamp = System.currentTimeMillis(),
                durationSeconds = summary.durationSeconds - summary.remainingSeconds,
                autoSubmitted = autoSubmitted
            )
        )

        examQuestions.forEach { question ->
            val userAnswer = examAnswers[question.id].orEmpty()
            val result = evaluateQuestion(question, userAnswer)
            if (!result.correct) {
                addWrongQuestion(
                    bank = bank,
                    question = question,
                    userAnswer = userAnswer,
                    source = "考试"
                )
            }
        }
        persist()
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
        evaluateQuestion(question, examAnswers[question.id].orEmpty()).correct
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

    internal fun resetForTesting() {
        banks.clear()
        wrongBook.clear()
        studyRecords.clear()
        activeBankId = null
        practiceIndex = 0
        selectedAnswer = emptyList()
        practiceLastResult = null
        initialized = false
        appContext = null
        resetExam()
    }

    private fun resetPracticeState() {
        practiceIndex = 0
        selectedAnswer = emptyList()
        practiceLastResult = null
    }

    private fun sanitizeBank(bank: QuizBank): QuizBank {
        return bank.copy(questions = bank.questions.map(::sanitizeQuestion))
    }

    private fun sanitizeQuestion(question: Question): Question {
        val cleanQuestion = stripEmbeddedAnswerBracket(question.question, question.answer)
        return if (cleanQuestion == question.question) question else question.copy(question = cleanQuestion)
    }

    private fun stripEmbeddedAnswerBracket(stem: String, answer: List<String>): String {
        if (stem.isBlank() || answer.isEmpty()) return stem
        val expected = answer.map { it.trim().uppercase() }.filter { it.isNotBlank() }.sorted()
        if (expected.isEmpty()) return stem

        val match = Regex(
            """[（(]\s*([A-Ga-g]{1,7}|正确|错误|对|错|是|否|√|×|True|False)\s*[）)]""",
            RegexOption.IGNORE_CASE
        ).findAll(stem).toList().asReversed().firstOrNull { hit ->
            embeddedAnswerToKeys(hit.groupValues[1]) == expected
        } ?: return stem

        return stem.removeRange(match.range)
            .replace(Regex("""\s+([?？。；;，,])"""), "$1")
            .replace(Regex("""\s+"""), " ")
            .trim()
            .trimEnd('，', ',', '；', ';', ':', '：')
            .trim()
    }

    private fun embeddedAnswerToKeys(raw: String): List<String> {
        val value = raw.trim().uppercase()
        return when {
            value.matches(Regex("""^[A-G]{1,7}$""")) -> value.map { it.toString() }.distinct().sorted()
            value in listOf("正确", "对", "是", "√", "TRUE", "T") -> listOf("A")
            value in listOf("错误", "错", "否", "×", "X", "FALSE", "F") -> listOf("B")
            else -> emptyList()
        }
    }

    private fun evaluateQuestion(question: Question, userAnswer: List<String>): QuestionCheckResult {
        val correctAnswer = question.answer.sorted()
        val normalizedUserAnswer = userAnswer.sorted()
        val isObjective = question.type == QuestionType.SINGLE ||
            question.type == QuestionType.MULTIPLE ||
            question.type == QuestionType.JUDGE

        val correct = isObjective &&
            normalizedUserAnswer.isNotEmpty() &&
            normalizedUserAnswer == correctAnswer

        return QuestionCheckResult(
            question = question,
            userAnswer = userAnswer,
            correct = correct,
            answerText = question.answer.joinToString(" / ").ifBlank { "未识别答案" }
        )
    }

    private fun recordPracticeResult(bank: QuizBank?, question: Question, result: QuestionCheckResult) {
        studyRecords.add(
            0,
            StudyRecord(
                id = "practice_${question.id}_${System.currentTimeMillis()}",
                bankId = bank?.id,
                bankName = bank?.name ?: "未命名题库",
                source = "练习",
                title = question.question.take(24),
                total = 1,
                correct = if (result.correct) 1 else 0,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private fun addWrongQuestion(
        bank: QuizBank?,
        question: Question,
        userAnswer: List<String>,
        source: String
    ) {
        val bankId = bank?.id ?: return
        val bankName = bank.name
        wrongBook.removeAll { it.bankId == bankId && it.question.id == question.id }
        wrongBook.add(
            0,
            WrongQuestionEntry(
                bankId = bankId,
                bankName = bankName,
                question = question,
                lastAnswer = userAnswer,
                source = source,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private fun persist() {
        val context = appContext ?: return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_BANKS, banksToJson(banks))
            .putString(KEY_ACTIVE_BANK_ID, activeBankId)
            .putString(KEY_WRONG_BOOK, wrongBookToJson(wrongBook))
            .putString(KEY_STUDY_RECORDS, studyRecordsToJson(studyRecords))
            .apply()
    }

    private fun demoBank(): QuizBank {
        return QuizBank(
            id = "demo-bank",
            name = "示例题库",
            questions = emptyList()
        )
    }

    private fun banksToJson(banks: List<QuizBank>): String {
        val bankArray = JSONArray()
        banks.forEach { bank ->
            val bankJson = JSONObject()
            bankJson.put("id", bank.id)
            bankJson.put("name", bank.name)
            bankJson.put("questions", questionsToJsonArray(bank.questions))
            bankArray.put(bankJson)
        }
        return bankArray.toString()
    }

    private fun wrongBookToJson(entries: List<WrongQuestionEntry>): String {
        val array = JSONArray()
        entries.forEach { entry ->
            val item = JSONObject()
            item.put("bankId", entry.bankId)
            item.put("bankName", entry.bankName)
            item.put("source", entry.source)
            item.put("timestamp", entry.timestamp)
            item.put("lastAnswer", JSONArray(entry.lastAnswer))
            item.put("question", questionToJson(entry.question))
            array.put(item)
        }
        return array.toString()
    }

    private fun studyRecordsToJson(records: List<StudyRecord>): String {
        val array = JSONArray()
        records.forEach { record ->
            val item = JSONObject()
            item.put("id", record.id)
            item.put("bankId", record.bankId)
            item.put("bankName", record.bankName)
            item.put("source", record.source)
            item.put("title", record.title)
            item.put("total", record.total)
            item.put("correct", record.correct)
            item.put("timestamp", record.timestamp)
            item.put("durationSeconds", record.durationSeconds)
            item.put("autoSubmitted", record.autoSubmitted)
            array.put(item)
        }
        return array.toString()
    }

    private fun parseBanksJson(text: String?): List<QuizBank> {
        if (text.isNullOrBlank()) return emptyList()
        val bankArray = JSONArray(text)

        return buildList {
            for (i in 0 until bankArray.length()) {
                val bankJson = bankArray.optJSONObject(i) ?: continue
                add(
                    QuizBank(
                        id = bankJson.optString("id"),
                        name = bankJson.optString("name"),
                        questions = parseQuestionsArray(bankJson.optJSONArray("questions"))
                    )
                )
            }
        }
    }

    private fun parseWrongBookJson(text: String?): List<WrongQuestionEntry> {
        if (text.isNullOrBlank()) return emptyList()
        val array = JSONArray(text)
        return buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val questionJson = item.optJSONObject("question") ?: continue
                val answerArray = item.optJSONArray("lastAnswer") ?: JSONArray()
                val lastAnswer = buildList {
                    for (index in 0 until answerArray.length()) {
                        add(answerArray.optString(index))
                    }
                }
                add(
                    WrongQuestionEntry(
                        bankId = item.optString("bankId"),
                        bankName = item.optString("bankName"),
                        question = parseQuestion(questionJson),
                        lastAnswer = lastAnswer,
                        source = item.optString("source"),
                        timestamp = item.optLong("timestamp")
                    )
                )
            }
        }
    }

    private fun parseStudyRecordsJson(text: String?): List<StudyRecord> {
        if (text.isNullOrBlank()) return emptyList()
        val array = JSONArray(text)
        return buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                add(
                    StudyRecord(
                        id = item.optString("id"),
                        bankId = item.optString("bankId").ifBlank { null },
                        bankName = item.optString("bankName"),
                        source = item.optString("source"),
                        title = item.optString("title"),
                        total = item.optInt("total"),
                        correct = item.optInt("correct"),
                        timestamp = item.optLong("timestamp"),
                        durationSeconds = if (item.has("durationSeconds") && !item.isNull("durationSeconds")) {
                            item.optInt("durationSeconds")
                        } else {
                            null
                        },
                        autoSubmitted = item.optBoolean("autoSubmitted")
                    )
                )
            }
        }
    }

    private fun parseQuestionsArray(array: JSONArray?): List<Question> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val questionJson = array.optJSONObject(i) ?: continue
                add(parseQuestion(questionJson))
            }
        }
    }

    private fun parseQuestion(questionJson: JSONObject): Question {
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

        val imagesArray = questionJson.optJSONArray("images") ?: JSONArray()
        val images = buildList {
            for (k in 0 until imagesArray.length()) {
                val imageJson = imagesArray.optJSONObject(k) ?: continue
                val path = imageJson.optString("localPath")
                if (path.isBlank()) continue
                add(
                    QuestionImage(
                        id = imageJson.optString("id"),
                        localPath = path,
                        sourceName = imageJson.optString("sourceName"),
                        order = imageJson.optInt("order"),
                        width = if (imageJson.has("width") && !imageJson.isNull("width")) imageJson.optInt("width") else null,
                        height = if (imageJson.has("height") && !imageJson.isNull("height")) imageJson.optInt("height") else null,
                        sizeBytes = imageJson.optLong("sizeBytes")
                    )
                )
            }
        }

        return sanitizeQuestion(
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
                images = images,
                score = if (questionJson.has("score")) questionJson.optDouble("score") else null
            )
        )
    }

    private fun questionsToJsonArray(questions: List<Question>): JSONArray {
        val questionsArray = JSONArray()
        questions.forEach { question ->
            questionsArray.put(questionToJson(question))
        }
        return questionsArray
    }

    private fun questionToJson(question: Question): JSONObject {
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

        val imagesArray = JSONArray()
        question.images.forEach { image ->
            val imageJson = JSONObject()
            imageJson.put("id", image.id)
            imageJson.put("localPath", image.localPath)
            imageJson.put("sourceName", image.sourceName)
            imageJson.put("order", image.order)
            imageJson.put("width", image.width)
            imageJson.put("height", image.height)
            imageJson.put("sizeBytes", image.sizeBytes)
            imagesArray.put(imageJson)
        }
        questionJson.put("images", imagesArray)
        return questionJson
    }
}
