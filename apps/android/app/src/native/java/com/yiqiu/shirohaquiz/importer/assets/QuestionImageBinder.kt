package com.yiqiu.shirohaquiz.importer.assets

import com.yiqiu.shirohaquiz.importer.model.ImportResult
import com.yiqiu.shirohaquiz.importer.model.ImportWarning
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionImage
import com.yiqiu.shirohaquiz.importer.model.WarningLevel

object QuestionImageBinder {
    private val markerRegex = Regex("""\[\[SHIROHA_IMAGE:img_\d{4}]]""")
    private val materialRangeRegex = Regex("""回答\s*(\d+)\s*[~～\-—至到]\s*(\d+)\s*题""")

    fun attach(result: ImportResult, images: List<QuestionImportAssetExtractor.ExtractedImportImage>): ImportResult {
        if (images.isEmpty()) return result
        val markerToImage = images.associate { it.marker to it.image }
        val usedMarkers = mutableSetOf<String>()
        val attached = result.questions.map { question ->
            val markers = markersInQuestion(question)
            usedMarkers += markers
            val questionImages = markers.mapNotNull { markerToImage[it] }
            cleanQuestionMarkers(question).let { cleaned ->
                if (questionImages.isEmpty()) cleaned else cleaned.copy(images = mergeImages(cleaned.images, questionImages))
            }
        }.toMutableList()

        applyMaterialImageSharing(attached)

        val boundCount = attached.sumOf { it.images.size }
        val uniqueBound = attached.flatMap { it.images }.map { it.localPath }.distinct().size
        val unboundCount = images.count { it.marker !in usedMarkers }
        val imageWarnings = buildList {
            add(
                ImportWarning(
                    level = if (unboundCount > 0) WarningLevel.WARNING else WarningLevel.NORMAL,
                    questionNumber = null,
                    message = "检测到 ${images.size} 张图片，已绑定 $uniqueBound 张。建议在沉浸核对中筛选图片题检查。"
                )
            )
            if (unboundCount > 0) {
                add(
                    ImportWarning(
                        level = WarningLevel.WARNING,
                        questionNumber = null,
                        message = "有 $unboundCount 张图片未能明确绑定到题目，可能来自封面、说明页、答案解析区或复杂图表。"
                    )
                )
            }
        }

        return result.copy(
            questions = attached,
            warnings = result.warnings + imageWarnings,
            diagnostics = result.diagnostics.copy(
                notes = result.diagnostics.notes + "图片导入：检测 ${images.size} 张，题目引用 $boundCount 次，唯一绑定 $uniqueBound 张。"
            )
        )
    }

    private fun markersInQuestion(question: Question): List<String> {
        return buildList {
            addAll(markerRegex.findAll(question.question).map { it.value })
            question.options.forEach { option -> addAll(markerRegex.findAll(option.text).map { it.value }) }
            addAll(markerRegex.findAll(question.analysis).map { it.value })
        }.distinct()
    }

    private fun cleanQuestionMarkers(question: Question): Question {
        return question.copy(
            question = cleanText(question.question),
            options = question.options.map { it.copy(text = cleanText(it.text)) },
            analysis = cleanText(question.analysis)
        )
    }

    private fun cleanText(text: String): String {
        return text
            .replace(markerRegex, "")
            .replace(Regex("""\n{3,}"""), "\n\n")
            .trim()
    }

    private fun mergeImages(existing: List<QuestionImage>, incoming: List<QuestionImage>): List<QuestionImage> {
        return (existing + incoming).distinctBy { it.localPath }
    }

    private fun applyMaterialImageSharing(questions: MutableList<Question>) {
        questions.forEachIndexed { index, question ->
            if (question.images.isEmpty()) return@forEachIndexed
            val match = materialRangeRegex.find(question.question) ?: return@forEachIndexed
            val start = match.groupValues[1].toIntOrNull() ?: return@forEachIndexed
            val end = match.groupValues[2].toIntOrNull() ?: return@forEachIndexed
            if (end < start || end - start > 20) return@forEachIndexed
            val count = end - start + 1
            for (offset in 1 until count) {
                val targetIndex = index + offset
                if (targetIndex !in questions.indices) break
                questions[targetIndex] = questions[targetIndex].copy(
                    images = mergeImages(questions[targetIndex].images, question.images)
                )
            }
        }
    }
}
