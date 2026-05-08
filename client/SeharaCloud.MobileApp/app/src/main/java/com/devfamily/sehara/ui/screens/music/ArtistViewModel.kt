package com.devfamily.sehara.ui.screens.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devfamily.sehara.data.ArtistItem
import com.devfamily.sehara.data.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArtistViewModel : ViewModel() {

    private val _artists = MutableStateFlow<List<ArtistItem>>(emptyList())
    val artists: StateFlow<List<ArtistItem>> = _artists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchResults = MutableStateFlow<List<ArtistItem>>(emptyList())
    val searchResults: StateFlow<List<ArtistItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        fetchArtists()
    }

    fun fetchArtists() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _artists.value = RetrofitClient.instance.getArtists()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchArtists(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            val filteredList = _artists.value.filter { artist ->
                artist.name.contains(query, ignoreCase = true)
            }

            _searchResults.value = filteredList
            _isSearching.value = false
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
    }
}