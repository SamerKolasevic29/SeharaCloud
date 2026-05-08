package com.devfamily.sehara.ui.screens.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devfamily.sehara.data.FileItem
import com.devfamily.sehara.data.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicListViewModel : ViewModel() {

    private val _songs = MutableStateFlow<List<FileItem>>(emptyList())
    val songs: StateFlow<List<FileItem>> = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun load(filter: MusicFilter) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _songs.value = when (filter) {
                    is MusicFilter.ByGenre -> RetrofitClient.instance.getMusicByGenre(filter.id)
                    is MusicFilter.ByArtist -> RetrofitClient.instance.getMusicByArtist(filter.id)
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}