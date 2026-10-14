package com.devfamily.sehara.ui.screens.music

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.devfamily.sehara.data.RetrofitClient
import com.devfamily.sehara.data.SongItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    private val _allSongs = MutableStateFlow<List<SongItem>>(emptyList())
    private val _favoriteIds = MutableStateFlow(readIds())

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _sortAscending = MutableStateFlow(true)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    private val _sortByArtist = MutableStateFlow(false)
    val sortByArtist: StateFlow<Boolean> = _sortByArtist.asStateFlow()

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "song_ids") _favoriteIds.value = readIds()
    }

    val favorites: StateFlow<List<SongItem>> = combine(
        _allSongs,
        _favoriteIds,
        _sortAscending,
        _sortByArtist
    ) { songs, ids, ascending, byArtist ->
        val collator = java.text.Collator.getInstance(java.util.Locale("bs")).apply {
            strength = java.text.Collator.TERTIARY
        }
        val textComparator = Comparator<String> { a, b -> collator.compare(a, b) }
        val comparator = if (byArtist) {
            compareBy<SongItem, String>(textComparator) { it.artist ?: "" }
                .thenBy(textComparator) { it.title ?: it.filename }
        } else {
            compareBy<SongItem, String>(textComparator) { it.title ?: it.filename }
        }
        songs
            .filter { it.id in ids }
            .sortedWith(if (ascending) comparator else comparator.reversed())
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
        fetchSongs()
    }

    private fun readIds(): Set<String> {
        return prefs.getStringSet("song_ids", emptySet())?.toSet() ?: emptySet()
    }

    fun fetchSongs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _allSongs.value = RetrofitClient.instance.getMusic()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.toUserMessage()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleOrder() {
        _sortAscending.value = !_sortAscending.value
    }

    fun toggleSortBy() {
        _sortByArtist.value = !_sortByArtist.value
    }

    override fun onCleared() {
        prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
        super.onCleared()
    }
}