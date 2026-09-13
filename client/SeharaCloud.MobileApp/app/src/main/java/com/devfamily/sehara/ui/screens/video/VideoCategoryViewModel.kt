package com.devfamily.sehara.ui.screens.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devfamily.sehara.data.VideoItem
import com.devfamily.sehara.data.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class VideoCategory { MOVIE, DOCUMENTARY, OTHER }

class VideoCategoryViewModel : ViewModel() {

    private val _videoList = MutableStateFlow<List<VideoItem>>(emptyList())
    val videoList: StateFlow<List<VideoItem>> = _videoList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchResults = MutableStateFlow<List<VideoItem>>(emptyList())
    val searchResults: StateFlow<List<VideoItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var currentCategory: VideoCategory = VideoCategory.MOVIE

    fun loadCategory(category: VideoCategory) {
        currentCategory = category
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _videoList.value = when (category) {
                    VideoCategory.MOVIE -> RetrofitClient.instance.getMovies()
                    VideoCategory.DOCUMENTARY -> RetrofitClient.instance.getDocumentaries()
                    VideoCategory.OTHER -> RetrofitClient.instance.getOtherVideos()
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.toUserMessage()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                _searchResults.value = when (currentCategory) {
                    VideoCategory.MOVIE -> RetrofitClient.instance.searchMovies(query)
                    VideoCategory.DOCUMENTARY -> RetrofitClient.instance.searchDocumentaries(query)
                    VideoCategory.OTHER -> _videoList.value.filter {
                        it.title.contains(query, ignoreCase = true) ||
                                it.filename.contains(query, ignoreCase = true)
                    }
                }
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
    }
}

private fun Throwable.toUserMessage(): String {
    return when (this) {
        is java.net.ConnectException -> "Unable to connect to the server. Please check your internet connection."
        is java.net.SocketTimeoutException -> "The server is taking too long to respond. Please try again later."
        is retrofit2.HttpException -> "Server error (${this.code()}). Please try again."
        is java.io.IOException -> "Network issue. Please verify your connection."
        else -> "An unexpected error occurred. Please try again."
    }
}