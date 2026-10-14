package com.devfamily.sehara.ui.screens.music

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import com.devfamily.sehara.data.PlayerConnection
import com.devfamily.sehara.data.RetrofitClient
import com.devfamily.sehara.data.SongItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SongUiModel(
    val id: String = "",
    val title: String = "Unknown Title",
    val artist: String = "Unknown Artist",
    val durationMs: Long = 0L,
    val thumbnailUrl: String? = null,
    val genre: String? = null
)

@UnstableApi
class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentSong = MutableStateFlow(SongUiModel())
    val currentSong: StateFlow<SongUiModel> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _hasPrevious = MutableStateFlow(false)
    val hasPreviousFlow: StateFlow<Boolean> = _hasPrevious.asStateFlow()

    private val _hasNext = MutableStateFlow(false)
    val hasNextFlow: StateFlow<Boolean> = _hasNext.asStateFlow()

    private val _isShuffled = MutableStateFlow(false)
    val isShuffled: StateFlow<Boolean> = _isShuffled.asStateFlow()

    private val favoritesPrefs = application.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    private val _favoriteIds = MutableStateFlow(
        favoritesPrefs.getStringSet("song_ids", emptySet())?.toSet() ?: emptySet()
    )
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private var originalQueue: List<SongItem> = emptyList()

    private val _openArtist = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 1)
    val openArtist: SharedFlow<Pair<String, String>> = _openArtist.asSharedFlow()

    private val _openGenre = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 1)
    val openGenre: SharedFlow<Pair<String, String>> = _openGenre.asSharedFlow()

    private var queue: List<SongItem> = emptyList()
    private var currentQueueIndex: Int = -1

    val hasPrevious: Boolean get() = currentQueueIndex > 0
    val hasNext: Boolean get() = currentQueueIndex < queue.size - 1

    private var controller: MediaController? = null
    private var pendingLoad: (() -> Unit)? = null

    init {
        PlayerConnection.connect(application) { mediaController ->
            controller = mediaController
            mediaController.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }

                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        skipNext()
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    val index = mediaController.currentMediaItemIndex
                    if (index in queue.indices) {
                        currentQueueIndex = index
                        val item = queue[index]
                        _currentSong.value = SongUiModel(
                            id = item.id,
                            title = item.title ?: item.filename,
                            artist = item.artist ?: "Unknown Artist",
                            durationMs = (item.durationSec ?: 0) * 1000L,
                            thumbnailUrl = item.thumbnailUrl,
                            genre = item.genre
                        )
                        _currentPosition.value = 0L
                        _hasPrevious.value = index > 0
                        _hasNext.value = index < queue.size - 1
                    }
                }
            })
            pendingLoad?.invoke()
            pendingLoad = null
        }

        viewModelScope.launch {
            while (true) {
                delay(1000L)
                val player = controller
                if (player != null && (player.isPlaying || player.duration > 0L)) {
                    _currentPosition.value = player.currentPosition
                    val exoDuration = player.duration
                    if (exoDuration > 0L && _currentSong.value.durationMs <= 0L) {
                        _currentSong.value = _currentSong.value.copy(durationMs = exoDuration)
                    }
                }
            }
        }
    }

    fun loadSong(
        songId: String,
        title: String,
        artist: String,
        durationMs: Long,
        thumbnailUrl: String? = null,
        genre: String? = null
    ) {
        val player = controller
        if (player == null) {
            pendingLoad = { loadSong(songId, title, artist, durationMs, thumbnailUrl, genre) }
            return
        }

        if (_currentSong.value.id == songId && player.playbackState != Player.STATE_IDLE) return

        queue = emptyList()
        originalQueue = emptyList()
        _isShuffled.value = false
        currentQueueIndex = -1
        _hasPrevious.value = false
        _hasNext.value = false

        _currentSong.value = SongUiModel(
            id = songId,
            title = title,
            artist = artist,
            durationMs = durationMs,
            thumbnailUrl = thumbnailUrl,
            genre = genre
        )
        _currentPosition.value = 0L

        val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")
        val streamUrl = "$baseUrl/api/music/$songId/stream"

        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setArtworkUri(thumbnailUrl?.let { android.net.Uri.parse(it) })
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(streamUrl)
            .setMediaMetadata(metadata)
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        _isPlaying.value = true
    }

    private fun buildMediaItem(song: SongItem, baseUrl: String): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(song.title ?: song.filename)
            .setArtist(song.artist ?: "Unknown Artist")
            .setArtworkUri(song.thumbnailUrl?.let { android.net.Uri.parse(it) })
            .build()

        return MediaItem.Builder()
            .setUri("$baseUrl/api/music/${song.id}/stream")
            .setMediaMetadata(metadata)
            .build()
    }

    fun loadSongFromSearch(item: SongItem) {
        loadSong(
            item.id,
            item.title ?: item.filename,
            item.artist ?: "Unknown Artist",
            (item.durationSec ?: 0) * 1000L,
            item.thumbnailUrl,
            item.genre
        )

        val genreName = item.genre
        if (genreName.isNullOrBlank()) return

        viewModelScope.launch {
            try {
                val genre = RetrofitClient.instance.getGenres()
                    .firstOrNull { it.name.equals(genreName, ignoreCase = true) } ?: return@launch
                val list = RetrofitClient.instance.getMusicByGenre(genre.id)
                val index = list.indexOfFirst { it.id == item.id }
                val player = controller ?: return@launch

                if (index < 0 || queue.isNotEmpty() || _currentSong.value.id != item.id) return@launch

                val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")
                val before = list.subList(0, index).map { buildMediaItem(it, baseUrl) }
                val after = list.subList(index + 1, list.size).map { buildMediaItem(it, baseUrl) }

                if (after.isNotEmpty()) player.addMediaItems(after)
                if (before.isNotEmpty()) player.addMediaItems(0, before)

                queue = list
                currentQueueIndex = index
                _hasPrevious.value = index > 0
                _hasNext.value = index < list.size - 1
            } catch (e: Exception) {
            }
        }
    }

    fun loadSongFromQueue(item: SongItem, queueList: List<SongItem>) {
        val player = controller
        if (player == null) {
            pendingLoad = { loadSongFromQueue(item, queueList) }
            return
        }

        queue = queueList
        originalQueue = emptyList()
        _isShuffled.value = false
        currentQueueIndex = queueList.indexOfFirst { it.id == item.id }

        val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")

        val mediaItems = queueList.map { song -> buildMediaItem(song, baseUrl) }

        player.setMediaItems(mediaItems, currentQueueIndex, 0L)
        player.prepare()
        player.play()

        _currentSong.value = SongUiModel(
            id = item.id,
            title = item.title ?: item.filename,
            artist = item.artist ?: "Unknown Artist",
            durationMs = (item.durationSec ?: 0) * 1000L,
            thumbnailUrl = item.thumbnailUrl,
            genre = item.genre
        )
        _currentPosition.value = 0L
        _isPlaying.value = true
        _hasPrevious.value = currentQueueIndex > 0
        _hasNext.value = currentQueueIndex < queue.size - 1
    }

    fun openCurrentArtist() {
        val name = _currentSong.value.artist
        if (name.isBlank() || name == "Unknown Artist") return
        viewModelScope.launch {
            try {
                val artist = RetrofitClient.instance.getArtists().firstOrNull { it.name == name }
                if (artist != null) _openArtist.tryEmit(artist.id to artist.name)
            } catch (e: Exception) {
            }
        }
    }

    fun openCurrentGenre() {
        val name = _currentSong.value.genre
        if (name.isNullOrBlank()) return
        viewModelScope.launch {
            try {
                val genre = RetrofitClient.instance.getGenres().firstOrNull { it.name.equals(name, ignoreCase = true) }
                if (genre != null) _openGenre.tryEmit(genre.id to genre.name)
            } catch (e: Exception) {
            }
        }
    }

    fun toggleShuffle() {
        val player = controller ?: return
        if (queue.isEmpty() || currentQueueIndex !in queue.indices) return

        val current = queue[currentQueueIndex]
        val newQueue: List<SongItem>

        if (!_isShuffled.value) {
            originalQueue = queue
            newQueue = listOf(current) + queue
                .filterIndexed { i, _ -> i != currentQueueIndex }
                .shuffled()
            _isShuffled.value = true
        } else {
            newQueue = originalQueue.ifEmpty { queue }
            _isShuffled.value = false
        }

        val newIndex = newQueue.indexOfFirst { it.id == current.id }.coerceAtLeast(0)
        val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")

        queue = newQueue
        currentQueueIndex = newIndex

        val playerIndex = player.currentMediaItemIndex
        if (playerIndex < player.mediaItemCount - 1) {
            player.removeMediaItems(playerIndex + 1, player.mediaItemCount)
        }
        if (playerIndex > 0) {
            player.removeMediaItems(0, playerIndex)
        }

        val after = newQueue.subList(newIndex + 1, newQueue.size).map { buildMediaItem(it, baseUrl) }
        val before = newQueue.subList(0, newIndex).map { buildMediaItem(it, baseUrl) }

        if (after.isNotEmpty()) player.addMediaItems(after)
        if (before.isNotEmpty()) player.addMediaItems(0, before)

        _hasPrevious.value = newIndex > 0
        _hasNext.value = newIndex < newQueue.size - 1
    }

    fun toggleFavorite() {
        val id = _currentSong.value.id
        if (id.isEmpty()) return
        val current = _favoriteIds.value
        val updated = if (id in current) current - id else current + id
        _favoriteIds.value = updated
        favoritesPrefs.edit().putStringSet("song_ids", updated).apply()
    }

    fun thumbnailAt(offset: Int): String? {
        return queue.getOrNull(currentQueueIndex + offset)?.thumbnailUrl
    }

    fun skipNext() {
        controller?.seekToNext()
    }

    fun skipPrevious() {
        controller?.seekToPrevious()
    }

    fun closePlayer() {
        controller?.stop()
        controller?.clearMediaItems()
        _currentSong.value = SongUiModel()
        _currentPosition.value = 0L
        _isPlaying.value = false
        queue = emptyList()
        originalQueue = emptyList()
        _isShuffled.value = false
        currentQueueIndex = -1
    }

    fun pause() {
        controller?.pause()
        _isPlaying.value = false
    }

    fun togglePlayPause() {
        val player = controller ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekTo(position: Long) {
        controller?.seekTo(position)
        _currentPosition.value = position
    }

    fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
    }
}