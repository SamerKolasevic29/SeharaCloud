package com.devfamily.sehara.ui.screens.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devfamily.sehara.data.SongItem
import com.devfamily.sehara.data.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

class MusicViewModel : ViewModel() {

    private val _musicList = MutableStateFlow<List<SongItem>>(emptyList())
    val musicList: StateFlow<List<SongItem>> = _musicList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _searchResults = MutableStateFlow<List<SongItem>>(emptyList())
    val searchResults: StateFlow<List<SongItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    fun searchMusic(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                _searchResults.value = RetrofitClient.instance.searchMusic(query)
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

    init {
        fetchMusic()
    }

    fun fetchMusic() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _musicList.value = RetrofitClient.instance.getMusic()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.toUserMessage()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

fun Throwable.toUserMessage(): String {
    return when (this) {
        is ConnectException -> "Unable to connect to the server. Please check your internet connection."
        is SocketTimeoutException -> "The server is taking too long to respond. Please try again later."
        is HttpException -> "Server error (${this.code()}). Please try again."
        is IOException -> "Network issue. Please verify your connection."
        else -> "An unexpected error occurred. Please try again."
    }
}