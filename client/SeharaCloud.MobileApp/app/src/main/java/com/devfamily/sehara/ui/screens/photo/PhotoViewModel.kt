package com.devfamily.sehara.ui.screens.photo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devfamily.sehara.data.PhotoItem
import com.devfamily.sehara.data.RetrofitClient
import com.devfamily.sehara.data.resolvedDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class PhotoViewModel : ViewModel() {

    private val _photoList = MutableStateFlow<List<PhotoItem>>(emptyList())
    val photoList: StateFlow<List<PhotoItem>> = _photoList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchPhotos()
    }

    fun fetchPhotos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = RetrofitClient.instance.getImages()
                _photoList.value = result.sortedWith(
                    compareByDescending<PhotoItem> { it.resolvedDate() ?: LocalDate.MIN }
                )
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.toUserMessage()
            } finally {
                _isLoading.value = false
            }
        }
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