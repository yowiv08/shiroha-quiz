package com.yiqiu.shirohaquiz.importer.assets

/** Centralized helper for internal DOCX image placeholders. */
object QuestionImageMarker {
    private val compatibleMarkerRegex = Regex(
        pattern = """(?:\[\s*){0,2}SHIROHA_IMAGE\s*:\s*(img_\d{4})(?:\s*]){0,2}|(?:[【\[]\s*){1,2}SHIROHA_IMAGE\s*:\s*(img_\d{4})(?:\s*[】\]]){1,2}""",
        options = setOf(RegexOption.IGNORE_CASE)
    )

    fun canonical(id: String): String = "[[SHIROHA_IMAGE:${id.trim()}]]"

    fun markerId(raw: String): String? {
        val match = compatibleMarkerRegex.find(raw) ?: return null
        return match.groupValues.drop(1).firstOrNull { it.isNotBlank() }?.trim()
    }

    fun canonicalize(text: String): String {
        if (!contains(text)) return text
        return compatibleMarkerRegex.replace(text) { match ->
            markerId(match.value)?.let(::canonical).orEmpty()
        }
    }

    fun contains(text: String): Boolean = compatibleMarkerRegex.containsMatchIn(text)

    fun rangesIn(text: String): List<IntRange> {
        if (text.isBlank()) return emptyList()
        return compatibleMarkerRegex.findAll(text).map { it.range }.toList()
    }

    fun idsIn(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        return compatibleMarkerRegex.findAll(text)
            .mapNotNull { markerId(it.value) }
            .distinct()
            .toList()
    }

    fun removeAll(text: String): String {
        if (!contains(text)) return text.trim()
        return compatibleMarkerRegex.replace(text, "")
            .replace(Regex("""[ \t]{2,}"""), " ")
            .replace(Regex("""\n{3,}"""), "\n\n")
            .trim()
    }
}
