package com.yiqiu.shirohaquiz.importer.assets

/**
 * Canonical and compatibility helpers for internal DOCX image placeholders.
 *
 * DOCX extraction writes [[SHIROHA_IMAGE:img_0001]], but Word/normalization/editing paths may
 * leave visually equivalent variants such as [SHIROHA_IMAGE:img_0001], 【SHIROHA_IMAGE:img_0001】
 * or [【SHIROHA_IMAGE:img_0001】].  Binding and validation should treat all of them as the same
 * marker and must never expose these internal tokens to the user.
 */
object QuestionImageMarker {
    private const val prefix = "SHIROHA_IMAGE:"

    val markerRegex = Regex(
        pattern = """(?:\[\[\s*|\[\s*【\s*|【\s*|\[\s*)SHIROHA_IMAGE\s*:\s*(img_\d{4})(?:\s*\]\]|\s*】\s*\]|\s*】|\s*\])"""
    )

    fun canonical(id: String): String = "[[$prefix$id]]"

    fun canonicalFromMarker(marker: String): String? {
        val id = markerRegex.find(marker)?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() }
        return id?.let(::canonical)
    }

    fun canonicalMarkersIn(text: String): List<String> {
        return markerRegex.findAll(text)
            .mapNotNull { match -> match.groupValues.getOrNull(1)?.takeIf { it.isNotBlank() }?.let(::canonical) }
            .distinct()
            .toList()
    }

    fun rangesIn(text: String): List<IntRange> {
        return markerRegex.findAll(text).map { it.range }.toList()
    }

    fun contains(text: String): Boolean = markerRegex.containsMatchIn(text)

    fun clean(text: String): String {
        return text
            .replace(markerRegex, "")
            .replace(Regex("""\n{3,}"""), "\n\n")
            .trim()
    }
}
