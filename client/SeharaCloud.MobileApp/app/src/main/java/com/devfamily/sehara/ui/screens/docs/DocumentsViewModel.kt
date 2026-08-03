package com.devfamily.sehara.ui.screens.docs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devfamily.sehara.data.DocumentItem
import com.devfamily.sehara.data.RetrofitClient
import com.devfamily.sehara.ui.screens.music.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DocumentsViewModel : ViewModel() {

    private val _docsList = MutableStateFlow<List<DocumentItem>>(emptyList())
    val docsList: StateFlow<List<DocumentItem>> = _docsList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchResults = MutableStateFlow<List<DocumentItem>>(emptyList())
    val searchResults: StateFlow<List<DocumentItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        fetchDocuments()
    }

    fun fetchDocuments() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _docsList.value = RetrofitClient.instance.getDocuments()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.toUserMessage()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchDocuments(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                _searchResults.value = RetrofitClient.instance.searchDocuments(query)
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