package com.yiqiu.shirohaquiz.ai

import com.yiqiu.shirohaquiz.importer.model.Question
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

private const val DEFAULT_AI_TIMEOUT_SECONDS = 60

data class AiReviewSuggestion(
    val questionId: String,
    val status: String,
    val issueTypes: List<String>,
    val reason: String,
    val suggestion: String,
    val suggestedType: String?,
    val suggestedAnswer: List<String>,
    val needHumanReview: Boolean,
    val confidence: Double
)

data class AiAnalysisSuggestion(
    val questionId: String,
    val analysis: String,
    val needHumanReview: Boolean,
    val confidence: Double
)

object ShirohaAiClient {
    fun testConnection(
        apiBaseUrl: String,
        apiKey: String,
        modelName: String,
        timeoutSeconds: Int = DEFAULT_AI_TIMEOUT_SECONDS
    ): String {
        validateConfig(apiBaseUrl, apiKey, modelName)
        val content = requestChatCompletion(
            apiBaseUrl = apiBaseUrl,
            apiKey = apiKey,
            modelName = modelName,
            systemPrompt = "你是 Shiroha Quiz 的接口连通性测试助手，只返回简短确认。",
            userPayload = "请只回复 JSON：{\"ok\":true,\"message\":\"connected\"}",
            timeoutSeconds = timeoutSeconds.coerceIn(15, 180),
            maxTokens = 64
        )
        return if (content.isBlank()) "连接成功。" else "连接成功：${content.take(40)}"
    }

    fun reviewQuestions(
        apiBaseUrl: String,
        apiKey: String,
        modelName: String,
        questions: List<Question>,
        timeoutSeconds: Int = DEFAULT_AI_TIMEOUT_SECONDS
    ): List<AiReviewSuggestion> {
        validateConfig(apiBaseUrl, apiKey, modelName)
        if (questions.isEmpty()) return emptyList()
        val content = requestChatCompletion(
            apiBaseUrl = apiBaseUrl,
            apiKey = apiKey,
            modelName = modelName,
            systemPrompt = AiPrompts.AI_REVIEW_SYSTEM_PROMPT,
            userPayload = JSONObject()
                .put("task", "review_questions")
                .put("outputFormat", reviewOutputContract())
                .put("questions", questionsToJson(questions))
                .toString(),
            timeoutSeconds = timeoutSeconds.coerceIn(15, 180)
        )
        return parseReviewSuggestions(content)
    }

    fun generateAnalysis(
        apiBaseUrl: String,
        apiKey: String,
        modelName: String,
        questions: List<Question>,
        timeoutSeconds: Int = DEFAULT_AI_TIMEOUT_SECONDS
    ): List<AiAnalysisSuggestion> {
        validateConfig(apiBaseUrl, apiKey, modelName)
        if (questions.isEmpty()) return emptyList()
        val content = requestChatCompletion(
            apiBaseUrl = apiBaseUrl,
            apiKey = apiKey,
            modelName = modelName,
            systemPrompt = AiPrompts.AI_ANALYSIS_SYSTEM_PROMPT,
            userPayload = JSONObject()
                .put("task", "generate_analysis")
                .put("outputFormat", analysisOutputContract())
                .put("questions", questionsToJson(questions))
                .toString(),
            timeoutSeconds = timeoutSeconds.coerceIn(15, 180)
        )
        return parseAnalysisSuggestions(content)
    }

    private fun requestChatCompletion(
        apiBaseUrl: String,
        apiKey: String,
        modelName: String,
        systemPrompt: String,
        userPayload: String,
        timeoutSeconds: Int,
        maxTokens: Int? = null
    ): String {
        val endpoint = normalizeChatEndpoint(apiBaseUrl)
        val timeoutMillis = timeoutSeconds.coerceIn(15, 180) * 1000
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = timeoutMillis.coerceAtMost(30000)
            readTimeout = timeoutMillis
            doOutput = true
            useCaches = false
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
        }

        return try {
            val body = JSONObject()
                .put("model", modelName.trim())
                .put("temperature", 0.1)
                .put(
                    "messages",
                    JSONArray()
                        .put(JSONObject().put("role", "system").put("content", systemPrompt.trim()))
                        .put(JSONObject().put("role", "user").put("content", userPayload))
                )
            if (maxTokens != null) body.put("max_tokens", maxTokens)

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(body.toString())
            }

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val responseText = if (stream != null) {
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
            } else {
                ""
            }

            if (code !in 200..299) {
                throw IllegalStateException(formatHttpError(code, responseText))
            }

            val response = JSONObject(responseText)
            val choices = response.optJSONArray("choices") ?: JSONArray()
            val first = choices.optJSONObject(0)
                ?: throw IllegalStateException("AI 接口没有返回 choices，请检查模型名称或接口兼容性。")
            val message = first.optJSONObject("message")
            val content = message?.optString("content").orEmpty()
            if (content.isBlank()) {
                throw IllegalStateException("AI 接口返回内容为空，请检查模型是否支持 chat/completions。")
            }
            content
        } catch (error: IllegalStateException) {
            throw error
        } catch (error: SocketTimeoutException) {
            throw IllegalStateException("AI 请求超时，请检查网络或适当调大超时秒数。", error)
        } catch (error: UnknownHostException) {
            throw IllegalStateException("无法连接 AI 接口域名，请检查 API 地址或网络。", error)
        } catch (error: IOException) {
            throw IllegalStateException("AI 网络请求失败：${error.message ?: "请检查接口地址和网络"}", error)
        } catch (error: JSONException) {
            throw IllegalStateException("AI 接口返回格式无法解析，请确认接口是否兼容 OpenAI chat/completions。", error)
        } finally {
            connection.disconnect()
        }
    }

    private fun validateConfig(apiBaseUrl: String, apiKey: String, modelName: String) {
        require(apiBaseUrl.trim().isNotBlank()) { "请先填写 API 地址。" }
        require(apiKey.trim().isNotBlank()) { "请先填写 API Key。" }
        require(modelName.trim().isNotBlank()) { "请先填写模型名称。" }
    }

    private fun normalizeChatEndpoint(apiBaseUrl: String): String {
        val clean = apiBaseUrl.trim().trimEnd('/')
        require(clean.startsWith("http://") || clean.startsWith("https://")) {
            "API 地址必须以 http:// 或 https:// 开头。"
        }
        return if (clean.endsWith("/chat/completions")) clean else "$clean/chat/completions"
    }

    private fun formatHttpError(code: Int, responseText: String): String {
        val apiMessage = extractApiErrorMessage(responseText)
        val hint = when (code) {
            400 -> "请求格式或模型参数不被接口接受"
            401, 403 -> "API Key 无效、余额不足或无权限"
            404 -> "API 地址或模型路径不正确"
            429 -> "请求过于频繁或额度受限"
            in 500..599 -> "服务商接口暂时异常"
            else -> "接口返回异常"
        }
        return "AI 接口请求失败：HTTP $code，$hint${if (apiMessage.isNotBlank()) "：$apiMessage" else ""}"
    }

    private fun extractApiErrorMessage(responseText: String): String {
        if (responseText.isBlank()) return ""
        val parsed = runCatching { JSONObject(responseText) }.getOrNull() ?: return responseText.take(160)
        val error = parsed.optJSONObject("error")
        val message = error?.optString("message").orEmpty().ifBlank { parsed.optString("message") }
        return message.take(160)
    }

    private fun questionsToJson(questions: List<Question>): JSONArray {
        val array = JSONArray()
        questions.forEachIndexed { index, question ->
            val options = JSONArray()
            question.options.forEach { option ->
                options.put(
                    JSONObject()
                        .put("key", option.key)
                        .put("text", option.text)
                )
            }

            val answers = JSONArray()
            question.answer.forEach { answers.put(it) }

            array.put(
                JSONObject()
                    .put("index", index)
                    .put("questionId", question.id)
                    .put("number", question.number)
                    .put("type", question.type.name.lowercase())
                    .put("question", question.question)
                    .put("options", options)
                    .put("answer", answers)
                    .put("analysis", question.analysis)
                    .put("category", question.category)
            )
        }
        return array
    }

    private fun parseReviewSuggestions(content: String): List<AiReviewSuggestion> {
        val root = JSONObject(extractJsonObject(content))
        val items = root.optJSONArray("items") ?: JSONArray()
        return (0 until items.length()).mapNotNull { index ->
            val item = items.optJSONObject(index) ?: return@mapNotNull null
            val issueTypesJson = item.optJSONArray("issueTypes") ?: JSONArray()
            val suggestedAnswerJson = item.optJSONArray("suggestedAnswer") ?: JSONArray()
            AiReviewSuggestion(
                questionId = item.optString("questionId"),
                status = item.optString("status", "warning"),
                issueTypes = (0 until issueTypesJson.length()).map { issueTypesJson.optString(it) }.filter { it.isNotBlank() },
                reason = item.optString("reason"),
                suggestion = item.optString("suggestion"),
                suggestedType = item.optString("suggestedType").takeIf { it.isNotBlank() && it != "null" },
                suggestedAnswer = (0 until suggestedAnswerJson.length()).map { suggestedAnswerJson.optString(it) }.filter { it.isNotBlank() },
                needHumanReview = item.optBoolean("needHumanReview", true),
                confidence = item.optDouble("confidence", 0.0)
            ).takeIf { it.questionId.isNotBlank() }
        }
    }

    private fun parseAnalysisSuggestions(content: String): List<AiAnalysisSuggestion> {
        val root = JSONObject(extractJsonObject(content))
        val items = root.optJSONArray("items") ?: JSONArray()
        return (0 until items.length()).mapNotNull { index ->
            val item = items.optJSONObject(index) ?: return@mapNotNull null
            AiAnalysisSuggestion(
                questionId = item.optString("questionId"),
                analysis = item.optString("analysis"),
                needHumanReview = item.optBoolean("needHumanReview", false),
                confidence = item.optDouble("confidence", 0.0)
            ).takeIf { it.questionId.isNotBlank() && it.analysis.isNotBlank() }
        }
    }

    private fun extractJsonObject(content: String): String {
        val clean = content.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val start = clean.indexOf('{')
        val end = clean.lastIndexOf('}')
        if (start < 0 || end <= start) {
            throw IllegalStateException("AI 返回内容不是有效 JSON。")
        }
        return clean.substring(start, end + 1)
    }

    private fun reviewOutputContract(): JSONObject {
        return JSONObject()
            .put(
                "items",
                JSONArray().put(
                    JSONObject()
                        .put("questionId", "题目ID")
                        .put("status", "ok / warning / error")
                        .put("issueTypes", JSONArray().put("answer_mismatch"))
                        .put("reason", "发现问题的原因")
                        .put("suggestion", "建议如何人工核对或修改")
                        .put("suggestedType", "single / multiple / judge / blank / short / null")
                        .put("suggestedAnswer", JSONArray().put("A"))
                        .put("needHumanReview", true)
                        .put("confidence", 0.82)
                )
            )
    }

    private fun analysisOutputContract(): JSONObject {
        return JSONObject()
            .put(
                "items",
                JSONArray().put(
                    JSONObject()
                        .put("questionId", "题目ID")
                        .put("analysis", "生成的解析内容")
                        .put("needHumanReview", false)
                        .put("confidence", 0.86)
                )
            )
    }
}
