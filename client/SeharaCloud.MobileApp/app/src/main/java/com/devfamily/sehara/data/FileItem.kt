package com.devfamily.sehara.data

data class FileItem(
    val id: String,
    val filename: String,
    val thumbnailUrl: String?,
    val title: String?,
    val artist: String?,
    val album: String?,
    val genre: String?,
    val durationSec: Int?
)