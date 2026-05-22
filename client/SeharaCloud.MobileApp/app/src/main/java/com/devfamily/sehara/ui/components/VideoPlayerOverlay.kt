package com.devfamily.sehara.ui.components

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.SurfaceView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.devfamily.sehara.R
import com.devfamily.sehara.ui.screens.video.VideoPlayerMode
import com.devfamily.sehara.ui.screens.video.VideoPlayerViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerOverlay(
    viewModel: VideoPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val video by viewModel.currentVideo.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val playerMode by viewModel.playerMode.collectAsState()
    val hasDuration = video.durationMs > 0L
    val hasPrevious by viewModel.hasPreviousFlow.collectAsState()
    val hasNext by viewModel.hasNextFlow.collectAsState()
    val context = LocalContext.current

    if (video.id.isEmpty() || playerMode == VideoPlayerMode.HIDDEN) return

    when (playerMode) {
        VideoPlayerMode.FULLSCREEN -> {
            var controlsVisible by remember { mutableStateOf(true) }

            BackHandler(enabled = true) {
                viewModel.setMode(VideoPlayerMode.FLOATING)
            }

            DisposableEffect(Unit) {
                val activity = context as? Activity
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                onDispose {
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }

            LaunchedEffect(controlsVisible, isPlaying) {
                if (controlsVisible && isPlaying) {
                    delay(3000)
                    controlsVisible = false
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .pointerInput(isPlaying) {
                        detectTapGestures(
                            onTap = {
                                controlsVisible = !controlsVisible
                            }
                        )
                    }
            ) {
                AndroidView(
                    factory = { ctx ->
                        SurfaceView(ctx).also {
                            viewModel.exoPlayer.setVideoSurfaceView(it)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                AnimatedVisibility(
                    visible = controlsVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    VideoControls(
                        modifier = Modifier.fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { controlsVisible = !controlsVisible })
                            },
                        isPlaying = isPlaying,
                        hasDuration = hasDuration,
                        currentPosition = currentPosition,
                        durationMs = video.durationMs,
                        hasPrevious = hasPrevious,
                        hasNext = hasNext,
                        title = video.title,
                        onPlayPause = {
                            viewModel.togglePlayPause()
                            controlsVisible = true
                        },
                        onSeek = {
                            viewModel.seekTo(it)
                            controlsVisible = true
                        },
                        onPrevious = {
                            viewModel.skipPrevious()
                            controlsVisible = true
                        },
                        onNext = {
                            viewModel.skipNext()
                            controlsVisible = true
                        },
                        onClose = { viewModel.closePlayer() },
                        onFloat = { viewModel.setMode(VideoPlayerMode.FLOATING) },
                        formatTime = { viewModel.formatTime(it) },
                        isFullscreen = true
                    )
                }
            }
        }

        VideoPlayerMode.FLOATING -> {
            var offsetX by remember { mutableFloatStateOf(40f) }
            var offsetY by remember { mutableFloatStateOf(200f) }

            LaunchedEffect(Unit) {
                val activity = context as? Activity
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .width(220.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            SurfaceView(ctx).also {
                                viewModel.exoPlayer.setVideoSurfaceView(it)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { viewModel.togglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { viewModel.setMode(VideoPlayerMode.FULLSCREEN) },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_fullscreen),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { viewModel.closePlayer() },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = video.title,
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        VideoPlayerMode.HIDDEN -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoControls(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    hasDuration: Boolean,
    currentPosition: Long,
    durationMs: Long,
    hasPrevious: Boolean,
    hasNext: Boolean,
    title: String,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    onFloat: () -> Unit,
    formatTime: (Long) -> String,
    isFullscreen: Boolean
) {
    Box(modifier = modifier.background(Color.Black.copy(alpha = 0.4f))) {
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isFullscreen) {
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onFloat() },
                    colorFilter = ColorFilter.tint(Color.White)
                )
            } else {
                Spacer(modifier = Modifier.size(28.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow_down),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onFloat() },
                    colorFilter = ColorFilter.tint(Color.White)
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onClose() },
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }

        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val prevInteraction = remember { MutableInteractionSource() }
            Image(
                painter = painterResource(id = R.drawable.ic_fast_rewind),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .indication(prevInteraction, ripple(bounded = true))
                    .clickable(
                        interactionSource = prevInteraction,
                        indication = null,
                        enabled = hasPrevious
                    ) { onPrevious() },
                colorFilter = ColorFilter.tint(
                    if (hasPrevious) Color.White else Color.White.copy(alpha = 0.3f)
                )
            )

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .clickable { onPlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }

            val nextInteraction = remember { MutableInteractionSource() }
            Image(
                painter = painterResource(id = R.drawable.ic_fast_forward),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .indication(nextInteraction, ripple(bounded = true))
                    .clickable(
                        interactionSource = nextInteraction,
                        indication = null,
                        enabled = hasNext
                    ) { onNext() },
                colorFilter = ColorFilter.tint(
                    if (hasNext) Color.White else Color.White.copy(alpha = 0.3f)
                )
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (hasDuration) {
                Slider(
                    value = currentPosition.toFloat(),
                    onValueChange = { onSeek(it.toLong()) },
                    valueRange = 0f..durationMs.toFloat(),
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color(0xFFC70025),
                        inactiveTrackColor = Color.White.copy(alpha = 0.4f)
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFC70025))
                        )
                    },
                    track = { sliderState ->
                        SliderDefaults.Track(
                            sliderState = sliderState,
                            modifier = Modifier.height(3.dp),
                            colors = SliderDefaults.colors(
                                activeTrackColor = Color(0xFFC70025),
                                inactiveTrackColor = Color.White.copy(alpha = 0.4f)
                            ),
                            thumbTrackGapSize = 0.dp,
                            trackInsideCornerSize = 0.dp
                        )
                    }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentPosition),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                    Text(
                        text = formatTime(durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}