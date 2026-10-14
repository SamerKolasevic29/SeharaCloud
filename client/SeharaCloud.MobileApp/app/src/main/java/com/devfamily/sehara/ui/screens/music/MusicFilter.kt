package com.devfamily.sehara.ui.screens.music

sealed class MusicFilter {
    data class ByGenre(val id: String, val name: String) : MusicFilter()
    data class ByArtist(val id: String, val name: String) : MusicFilter()
}