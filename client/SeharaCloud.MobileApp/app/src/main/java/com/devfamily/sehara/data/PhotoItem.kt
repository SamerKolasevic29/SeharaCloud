package com.devfamily.sehara.data

data class PhotoItem(
    val id: String,
    val fileName: String,
    val thumbnailUrl: String?,
    val width: Int?,
    val height: Int?,
    val dateTaken: String?,
    val camera: String?,
    val sizeBytes: Long?
)