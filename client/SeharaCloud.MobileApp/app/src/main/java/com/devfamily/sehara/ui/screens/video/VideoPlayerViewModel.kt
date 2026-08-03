package com.devfamily.sehara.ui.screens.video

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.devfamily.sehara.data.VideoItem
import com.devfamily.sehara.data.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class VideoPlayerMode {
    HIDDEN, FLOATING, PORTRAIT, FULLSCREEN
}

data class VideoUiModel(
    val id: String = "",
    val title: String = "Unknown Title",
    val category: String = "other",
    val durationMs: Long = 0L,
    val thumbnailUrl: String? = null
)

class VideoPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentVideo = MutableStateFlow(VideoUiModel())
    val currentVideo: StateFlow<VideoUiModel> = _currentVideo.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _playerMode = MutableStateFlow(VideoPlayerMode.HIDDEN)
    val playerMode: StateFlow<VideoPlayerMode> = _playerMode.asStateFlow()

    private val _hasPrevious = MutableStateFlow(false)
    val hasPreviousFlow: StateFlow<Boolean> = _hasPrevious.asStateFlow()

    private val _hasNext = MutableStateFlow(false)
    val hasNextFlow: StateFlow<Boolean> = _hasNext.asStateFlow()

    private val _categoryQueue = MutableStateFlow<List<VideoItem>>(emptyList())
    val categoryQueue: StateFlow<List<VideoItem>> = _categoryQueue.asStateFlow()

    private var queue: List<VideoItem> = emptyList()
    private var currentQueueIndex: Int = -1

    val hasPrevious: Boolean get() = currentQueueIndex > 0
    val hasNext: Boolean get() = currentQueueIndex < queue.size - 1

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    if (hasNext) {
                        skipNext()
                    } else {
                        _isPlaying.value = false
                        _currentPosition.value = 0L
                        exoPlayer.seekTo(0)
                        exoPlayer.pause()
                    }
                }
            }
        })

        viewModelScope.launch {
            while (true) {
                delay(500L)
                if (exoPlayer.isPlaying || exoPlayer.duration > 0L) {
                    _currentPosition.value = exoPlayer.currentPosition
                    val exoDuration = exoPlayer.duration
                    if (exoDuration > 0L && _currentVideo.value.durationMs <= 0L) {
                        _currentVideo.value = _currentVideo.value.copy(durationMs = exoDuration)
                    }
                }
            }
        }
    }

    fun loadVideo(item: VideoItem, queueList: List<VideoItem>) {
        queue = queueList
        currentQueueIndex = queueList.indexOfFirst { it.id == item.id }
        _hasPrevious.value = currentQueueIndex > 0
        _hasNext.value = currentQueueIndex < queue.size - 1

        val categoryItems = queueList.filter { it.category == item.category }
        _categoryQueue.value = categoryItems

        _currentVideo.value = VideoUiModel(
            id = item.id,
            title = if (item.title.isBlank()) item.filename else item.title,
            category = item.category,
            durationMs = (item.durationSec ?: 0) * 1000L,
            thumbnailUrl = item.thumbnailUrl
        )
        _currentPosition.value = 0L

        val streamUrl = "${RetrofitClient.BASE_URL}api/stream/${item.id}"
        val mediaItem = MediaItem.fromUri(streamUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
        _isPlaying.value = true
        _playerMode.value = VideoPlayerMode.PORTRAIT
    }

    fun skipNext() {
        if (hasNext) {
            currentQueueIndex++
            val item = queue[currentQueueIndex]
            _hasPrevious.value = currentQueueIndex > 0
            _hasNext.value = currentQueueIndex < queue.size - 1
            loadVideo(item, queue)
        }
    }

    fun skipPrevious() {
        if (hasPrevious) {
            currentQueueIndex--
            val item = queue[currentQueueIndex]
            _hasPrevious.value = currentQueueIndex > 0
            _hasNext.value = currentQueueIndex < queue.size - 1
            loadVideo(item, queue)
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
        _currentPosition.value = position
    }

    fun setMode(mode: VideoPlayerMode) {
        _playerMode.value = mode
    }

    fun closePlayer() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _currentVideo.value = VideoUiModel()
        _currentPosition.value = 0L
        _isPlaying.value = false
        _playerMode.value = VideoPlayerMode.HIDDEN
        queue = emptyList()
        currentQueueIndex = -1
        _categoryQueue.value = emptyList()
    }

    fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {

            "%02d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}