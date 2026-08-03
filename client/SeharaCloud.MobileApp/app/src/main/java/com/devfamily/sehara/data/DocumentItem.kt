package com.devfamily.sehara.data

data class DocumentItem(
    val id: String,
    val title: String?,
    val category: String?,
    val author: String?,
    val pageCount: Int?,
    val thumbnailPath: String?,
    val sizeBytes: Long?
)