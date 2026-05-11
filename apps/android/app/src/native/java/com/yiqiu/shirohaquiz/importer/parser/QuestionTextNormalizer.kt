package com.yiqiu.shirohaquiz.importer.parser

object QuestionTextNormalizer {
    fun normalize(raw: String): String {
        if (raw.isBlank()) return ""
        return raw
            .replace("\uFEFF", "")
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .replace('\u3000', ' ')
            .replace('\t', ' ')
            .let(::normalizeFullWidthAscii)
            .replace("（", "(")
            .replace("）", ")")
            .replace("：", ":")
            .replace("．", ".")
            .replace("，", ",")
            .replace("；", ";")
            .replace("【", "[")
            .replace("】", "]")
            .replace("✔", "√")
            .replace("✅", "√")
            .replace("☑", "√")
            .replace("✘", "×")
            .replace("✖", "×")
            .replace("❌", "×")
            .replace("❎", "×")
            .replace("√", "√")
            .replace("×", "×")
            .lineSequence()
            .map { it.trimEnd() }
            .filterNot { isNoiseLine(it.trim()) }
            .joinToString("\n")
            .replace(Regex("""[ \t]{2,}"""), " ")
            .replace(Regex("""\n{3,}"""), "\n\n")
            .trim()
    }

    private fun normalizeFullWidthAscii(text: String): String {
        return buildString(text.length) {
            text.forEach { ch ->
                val code = ch.code
                append(
                    when (code) {
                        0x3000 -> ' '
                        in 0xFF01..0xFF5E -> (code - 0xFEE0).toChar()
                        else -> ch
                    }
                )
            }
        }
    }

    private fun isNoiseLine(line: String): Boolean {
        if (line.isBlank()) return false
        if (Regex("""^第\s*\d+\s*页\s*(?:共\s*\d+\s*页)?$""").matches(line)) return true
        if (Regex("""^-?\d{8,}$""").matches(line)) return true
        if (Regex("""^(?:绝密|请勿外传|忽略此处\.\.)$""").matches(line)) return true
        return false
    }
}
