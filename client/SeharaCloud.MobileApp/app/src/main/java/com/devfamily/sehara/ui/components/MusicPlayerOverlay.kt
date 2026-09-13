package com.devfamily.sehara.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.devfamily.sehara.R
import com.devfamily.sehara.ui.screens.music.MusicPlayerViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerOverlay(
    viewModel: MusicPlayerViewModel,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    val song by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val hasDuration = song.durationMs > 0L
    val hasPrevious by viewModel.hasPreviousFlow.collectAsState()
    val hasNext by viewModel.hasNextFlow.collectAsState()

    val accentColor = Color(0xFFFF3054)

    if (song.id.isEmpty()) return

    AnimatedContent(
        targetState = isExpanded,
        transitionSpec = {
            if (targetState) {
                (fadeIn(tween(400)) + expandVertically(
                    animationSpec = tween(400),
                    expandFrom = Alignment.Top
                )) togetherWith shrinkVertically(
                    animationSpec = tween(400),
                    shrinkTowards = Alignment.Top
                )
            } else {
                (fadeIn(tween(400)) + expandVertically(
                    animationSpec = tween(400),
                    expandFrom = Alignment.Top
                )) togetherWith shrinkVertically(
                    animationSpec = tween(400),
                    shrinkTowards = Alignment.Top
                )
            }
        },
        label = "player_expand"
    ) { expanded ->
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .pointerInput(Unit) { detectTapGestures { } }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.player_bg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp)
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_arrow_down),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { onExpandChange(false) },
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                        Spacer(modifier = Modifier.width(32.dp))
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Box(
                        modifier = Modifier
                            .size(320.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF151515))
                    ) {
                        AsyncImage(
                            model = song.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.ic_unknown_song_thumb),
                            placeholder = painterResource(id = R.drawable.music_splash_bg)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MarqueeText(
                            text = song.title,
                            contentAlignment = Alignment.Center,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color(0xFFB3B3B3),
                                textAlign = TextAlign.Center
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (hasDuration) {
                        Slider(
                            value = currentPosition.toFloat(),
                            onValueChange = { viewModel.seekTo(it.toLong()) },
                            valueRange = 0f..song.durationMs.toFloat(),
                            colors = SliderDefaults.colors(
                                activeTrackColor = accentColor,
                                inactiveTrackColor = Color(0x4DFFFFFF)
                            ),
                            thumb = {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(accentColor)
                                )
                            },
                            track = { sliderState ->
                                SliderDefaults.Track(
                                    sliderState = sliderState,
                                    modifier = Modifier.height(4.dp),
                                    colors = SliderDefaults.colors(
                                        activeTrackColor = accentColor,
                                        inactiveTrackColor = Color(0x4DFFFFFF)
                                    ),
                                    thumbTrackGapSize = 0.dp,
                                    trackInsideCornerSize = 0.dp
                                )
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0x4DFFFFFF))
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        val currentTimeFormatted = viewModel.formatTime(currentPosition)
                        val totalTimeFormatted = if (hasDuration) viewModel.formatTime(song.durationMs) else "--:--"

                        Text(
                            text = "$currentTimeFormatted / $totalTimeFormatted",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFB3B3B3)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val rewindInteractionExpanded = remember { MutableInteractionSource() }
                        Image(
                            painter = painterResource(id = R.drawable.ic_fast_rewind),
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .indication(rewindInteractionExpanded, ripple(bounded = true))
                                .clickable(
                                    interactionSource = rewindInteractionExpanded,
                                    indication = null,
                                    enabled = hasPrevious
                                ) { viewModel.skipPrevious() }
                                .padding(8.dp),
                            colorFilter = ColorFilter.tint(
                                if (hasPrevious) Color.White else Color(0x66FFFFFF)
                            )
                        )

                        Spacer(modifier = Modifier.width(32.dp))

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clickable { viewModel.togglePlayPause() },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                                contentDescription = null,
                                modifier = Modifier.size(if (isPlaying) 40.dp else 56.dp),
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        val forwardInteractionExpanded = remember { MutableInteractionSource() }
                        Image(
                            painter = painterResource(id = R.drawable.ic_fast_forward),
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .indication(forwardInteractionExpanded, ripple(bounded = true))
                                .clickable(
                                    interactionSource = forwardInteractionExpanded,
                                    indication = null,
                                    enabled = hasNext
                                ) { viewModel.skipNext() }
                                .padding(8.dp),
                            colorFilter = ColorFilter.tint(
                                if (hasNext) Color.White else Color(0x66FFFFFF)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        } else {
            var dragOffsetX by remember { mutableFloatStateOf(0f) }
            val swipeThreshold = 300f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .offset { IntOffset(dragOffsetX.roundToInt(), 0) }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (kotlin.math.abs(dragOffsetX) > swipeThreshold) {
                                    onClose()
                                }
                                dragOffsetX = 0f
                            },
                            onDragCancel = {
                                dragOffsetX = 0f
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                dragOffsetX += dragAmount
                            }
                        )
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            color = Color(0xFF420E0E),
                            shape = RoundedCornerShape(30.dp)
                        )
                        .clip(RoundedCornerShape(30.dp))
                        .clickable { onExpandChange(true) }
                        .padding(start = 16.dp, end = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 52.dp, height = 56.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF151515))
                            ) {
                                AsyncImage(
                                    model = song.thumbnailUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.ic_unknown_song_thumb),
                                    placeholder = painterResource(id = R.drawable.music_splash_bg)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                MarqueeText(
                                    text = song.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFFB3B3B3)
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val rewindInteractionCollapsed = remember { MutableInteractionSource() }
                            Image(
                                painter = painterResource(id = R.drawable.ic_fast_rewind),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .indication(rewindInteractionCollapsed, ripple(bounded = false))
                                    .clickable(
                                        interactionSource = rewindInteractionCollapsed,
                                        indication = null,
                                        enabled = hasPrevious
                                    ) { viewModel.skipPrevious() },
                                colorFilter = ColorFilter.tint(
                                    if (hasPrevious) Color.White else Color(0x66FFFFFF)
                                )
                            )

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { viewModel.togglePlayPause() },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                                    contentDescription = null,
                                    modifier = Modifier.size(if (isPlaying) 22.dp else 30.dp),
                                    colorFilter = ColorFilter.tint(Color.White)
                                )
                            }

                            val forwardInteractionCollapsed = remember { MutableInteractionSource() }
                            Image(
                                painter = painterResource(id = R.drawable.ic_fast_forward),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .indication(forwardInteractionCollapsed, ripple(bounded = false))
                                    .clickable(
                                        interactionSource = forwardInteractionCollapsed,
                                        indication = null,
                                        enabled = hasNext
                                    ) { viewModel.skipNext() },
                                colorFilter = ColorFilter.tint(
                                    if (hasNext) Color.White else Color(0x66FFFFFF)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}