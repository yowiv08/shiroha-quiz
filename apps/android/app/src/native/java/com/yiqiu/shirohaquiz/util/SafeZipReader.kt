package com.yiqiu.shirohaquiz.util

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object SafeZipReader {
    fun normalizeEntryName(rawName: String): String {
        val name = rawName.trim()
        require(isSafeEntryName(name)) { "Unsafe ZIP entry name: $rawName" }
        return name
    }

    fun isSafeEntryName(rawName: String): Boolean {
        val name = rawName.trim()
        if (name.isBlank()) return false
        if (name.indexOf('\u0000') >= 0) return false
        if (name.contains('\\')) return false
        if (name.startsWith("/") || name.startsWith("./")) return false
        if (Regex("""^[A-Za-z]:""").containsMatchIn(name)) return false
        return name.split('/').all { segment ->
            segment.isNotBlank() && segment != "." && segment != ".."
        }
    }

    fun readEntryBytes(
        zip: ZipInputStream,
        entry: ZipEntry,
        maxSize: Long,
        maxTotalRemaining: Long? = null
    ): ByteArray {
        require(maxSize > 0) { "ZIP entry size limit must be positive." }
        maxTotalRemaining?.let {
            require(it > 0) { "ZIP total remaining size must be positive." }
        }
        if (entry.size > maxSize) {
            throw IllegalArgumentException("ZIP entry too large: ${entry.name}")
        }
        if (maxTotalRemaining != null && entry.size > maxTotalRemaining) {
            throw IllegalArgumentException("ZIP total size exceeded.")
        }

        val output = ByteArrayOutputStream(initialCapacity(entry, maxSize, maxTotalRemaining))
        val buffer = ByteArray(8192)
        var total = 0L
        while (true) {
            val read = zip.read(buffer)
            if (read <= 0) break
            if (total + read > maxSize) {
                throw IllegalArgumentException("ZIP entry too large: ${entry.name}")
            }
            if (maxTotalRemaining != null && total + read > maxTotalRemaining) {
                throw IllegalArgumentException("ZIP total size exceeded.")
            }
            output.write(buffer, 0, read)
            total += read
        }
        return output.toByteArray()
    }

    private fun initialCapacity(entry: ZipEntry, maxSize: Long, maxTotalRemaining: Long?): Int {
        val knownSize = entry.size.takeIf { it > 0 } ?: 8192L
        val boundedSize = listOfNotNull(knownSize, maxSize, maxTotalRemaining).minOrNull() ?: 8192L
        return boundedSize.coerceAtMost(64L * 1024L).toInt().coerceAtLeast(1024)
    }
}
