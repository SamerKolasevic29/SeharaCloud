package com.devfamily.sehara.ui.screens.music

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.devfamily.sehara.data.SongItem
import com.devfamily.sehara.data.RetrofitClient
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
    val thumbnailUrl: String?=null
)

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

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    skipNext()
                }
            }
        })
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                if (exoPlayer.isPlaying || exoPlayer.duration > 0L) {
                    _currentPosition.value = exoPlayer.currentPosition
                    val exoDuration = exoPlayer.duration
                    if (exoDuration > 0L && _currentSong.value.durationMs <= 0L) {
                        _currentSong.value = _currentSong.value.copy(durationMs = exoDuration)
                    }
                }
            }
        }
    }

    fun loadSong(songId: String, title: String, artist: String, durationMs: Long, thumbnailUrl: String? = null) {
        if (_currentSong.value.id == songId && exoPlayer.playbackState != Player.STATE_IDLE) return

        _currentSong.value = SongUiModel(
            id = songId,
            title = title,
            artist = artist,
            durationMs = durationMs,
            thumbnailUrl=thumbnailUrl
        )
        _currentPosition.value = 0L

        val streamUrl = "${RetrofitClient.BASE_URL}api/stream/$songId"
        val mediaItem = MediaItem.fromUri(streamUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
        _isPlaying.value = true
    }

    fun loadSongFromQueue(item: SongItem, queueList: List<SongItem>) {
        queue = queueList
        currentQueueIndex = queueList.indexOfFirst { it.id == item.id }
        _hasPrevious.value = currentQueueIndex > 0
        _hasNext.value = currentQueueIndex < queue.size - 1
        loadSong(
            item.id,
            item.title ?: item.filename,
            item.artist ?: "Unknown Artist",
            (item.durationSec ?: 0) * 1000L,
            item.thumbnailUrl
        )
    }

    fun skipNext() {
        if (queue.isEmpty()) return

        if (hasNext) {
            currentQueueIndex++
        } else {
            currentQueueIndex = 0
        }

        val item = queue[currentQueueIndex]
        _hasPrevious.value = currentQueueIndex > 0
        _hasNext.value = currentQueueIndex < queue.size - 1

        loadSong(
            item.id,
            item.title ?: item.filename,
            item.artist ?: "Unknown Artist",
            (item.durationSec ?: 0) * 1000L,
            item.thumbnailUrl
        )
    }

    fun skipPrevious() {
        if (hasPrevious) {
            currentQueueIndex--
            val item = queue[currentQueueIndex]
            _hasPrevious.value = currentQueueIndex > 0
            _hasNext.value = currentQueueIndex < queue.size - 1
            loadSong(
                item.id,
                item.title ?: item.filename,
                item.artist ?: "Unknown Artist",
                (item.durationSec ?: 0) * 1000L,
                item.thumbnailUrl
            )
        }
    }

    fun closePlayer() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _currentSong.value = SongUiModel()
        _currentPosition.value = 0L
        _isPlaying.value = false
        queue = emptyList()
        currentQueueIndex = -1
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
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
        exoPlayer.release()
    }
}