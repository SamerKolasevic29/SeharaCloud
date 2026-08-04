package com.devfamily.sehara.data

import kotlinx.serialization.Serializable

@Serializable
data class VideoItem(
    val id: String,
    val filename: String,
    val thumbnailUrl: String?,
    val title: String,
    val category: String,
    val year: Int,
    val durationSec: Int?,
    val resolution: String?,
    val codec: String?
)