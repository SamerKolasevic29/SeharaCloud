package com.devfamily.sehara.ui.screens.music

import android.app.Application
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SongUiModel(
    val id: String = "",
    val title: String = "Unknown Title",
    val artist: String = "Unknown Artist",
    val durationMs: Long = 0L,
    val thumbnailUrl: String? = null
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
                            thumbnailUrl = item.thumbnailUrl
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
        thumbnailUrl: String? = null
    ) {
        val player = controller
        if (player == null) {
            pendingLoad = { loadSong(songId, title, artist, durationMs, thumbnailUrl) }
            return
        }

        if (_currentSong.value.id == songId && player.playbackState != Player.STATE_IDLE) return

        queue = emptyList()
        currentQueueIndex = -1
        _hasPrevious.value = false
        _hasNext.value = false

        _currentSong.value = SongUiModel(
            id = songId,
            title = title,
            artist = artist,
            durationMs = durationMs,
            thumbnailUrl = thumbnailUrl
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

    fun loadSongFromQueue(item: SongItem, queueList: List<SongItem>) {
        val player = controller
        if (player == null) {
            pendingLoad = { loadSongFromQueue(item, queueList) }
            return
        }

        queue = queueList
        currentQueueIndex = queueList.indexOfFirst { it.id == item.id }

        val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")

        val mediaItems = queueList.map { song ->
            val metadata = MediaMetadata.Builder()
                .setTitle(song.title ?: song.filename)
                .setArtist(song.artist ?: "Unknown Artist")
                .setArtworkUri(song.thumbnailUrl?.let { android.net.Uri.parse(it) })
                .build()

            MediaItem.Builder()
                .setUri("$baseUrl/api/music/${song.id}/stream")
                .setMediaMetadata(metadata)
                .build()
        }

        player.setMediaItems(mediaItems, currentQueueIndex, 0L)
        player.prepare()
        player.play()

        _currentSong.value = SongUiModel(
            id = item.id,
            title = item.title ?: item.filename,
            artist = item.artist ?: "Unknown Artist",
            durationMs = (item.durationSec ?: 0) * 1000L,
            thumbnailUrl = item.thumbnailUrl
        )
        _currentPosition.value = 0L
        _isPlaying.value = true
        _hasPrevious.value = currentQueueIndex > 0
        _hasNext.value = currentQueueIndex < queue.size - 1
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