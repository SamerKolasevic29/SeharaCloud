package com.devfamily.sehara.data

data class ArtistItem(
    val id: String,
    val name: String,
    val songCount: Int,
    val thumbnailUrl: String? = null
)