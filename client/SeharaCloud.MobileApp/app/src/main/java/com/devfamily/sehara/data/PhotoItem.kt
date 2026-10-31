package com.devfamily.sehara.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

data class PhotoItem(
    val id: String = "",
    val filename: String = "",
    val title: String? = null,
    val thumbnailUrl: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val dateTaken: String? = null,
    val camera: String? = null,
    val sizeBytes: Long? = null
)

private val FILENAME_DATE = Regex("(19\\d{2}|20\\d{2})(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])")

fun PhotoItem.displayName(): String = title?.takeIf { it.isNotBlank() } ?: filename

fun PhotoItem.resolvedDate(): LocalDate? {
    val raw = dateTaken
    if (!raw.isNullOrBlank()) {
        try {
            return OffsetDateTime.parse(raw).toLocalDate()
        } catch (e: Exception) {
        }
        try {
            return LocalDateTime.parse(raw).toLocalDate()
        } catch (e: Exception) {
        }
    }
    val match = FILENAME_DATE.find(filename) ?: return null
    return try {
        LocalDate.of(
            match.groupValues[1].toInt(),
            match.groupValues[2].toInt(),
            match.groupValues[3].toInt()
        )
    } catch (e: Exception) {
        null
    }
}