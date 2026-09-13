package com.devfamily.sehara.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import coil.compose.AsyncImage
import com.devfamily.sehara.R
import com.devfamily.sehara.ui.screens.music.MusicPlayerViewModel
import kotlin.math.roundToInt
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding

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
                    .background(Color.White)
                    .pointerInput(Unit) { detectTapGestures { } }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp)
                        .statusBarsPadding()
                        .navigationBarsPadding()
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
                            colorFilter = ColorFilter.tint(Color(0xFF8E8D8D))
                        )
                        Spacer(modifier = Modifier.width(32.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFF5F5F5))
                    ) {
                        val unknownThumbUrl = "http://192.168.100.79:5000/api/thumbnail/b875c19d-70c8-44cf-998a-bf9d06fe9533"
                        val isUnknownThumb = song.thumbnailUrl == null || song.thumbnailUrl == unknownThumbUrl

                        if (isUnknownThumb) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_unknown_song_thumb),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            AsyncImage(
                                model = song.thumbnailUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                error = painterResource(id = R.drawable.ic_unknown_song_thumb),
                                placeholder = painterResource(id = R.drawable.music_splash_bg)
                            )
                        }
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
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color(0xFF999999),
                                textAlign = TextAlign.Center
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    if (hasDuration) {
                        Slider(
                            value = currentPosition.toFloat(),
                            onValueChange = { viewModel.seekTo(it.toLong()) },
                            valueRange = 0f..song.durationMs.toFloat(),
                            colors = SliderDefaults.colors(
                                activeTrackColor = Color(0xFFC70025),
                                inactiveTrackColor = Color(0xFFEEEEEE)
                            ),
                            thumb = {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFC70025))
                                )
                            },
                            track = { sliderState ->
                                SliderDefaults.Track(
                                    sliderState = sliderState,
                                    modifier = Modifier.height(4.dp),
                                    colors = SliderDefaults.colors(
                                        activeTrackColor = Color(0xFFC70025),
                                        inactiveTrackColor = Color(0xFFEEEEEE)
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
                                .background(Color(0xFFEEEEEE))
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = viewModel.formatTime(currentPosition),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF999999)
                        )
                        Text(
                            text = if (hasDuration) viewModel.formatTime(song.durationMs) else "--:--",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF999999)
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

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
                                if (hasPrevious) Color(0xFFC70025) else Color(0xFFCCCCCC)
                            )
                        )

                        Spacer(modifier = Modifier.width(24.dp))

                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(
                                    width = 2.dp,
                                    color = Color(0xFFC70025),
                                    shape = CircleShape
                                )
                                .clickable { viewModel.togglePlayPause() },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                colorFilter = ColorFilter.tint(Color(0xFFC70025))
                            )
                        }

                        Spacer(modifier = Modifier.width(24.dp))

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
                                if (hasNext) Color(0xFFC70025) else Color(0xFFCCCCCC)
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
                        .offset(y = (-6).dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(Color(0xFFC70025))
                        .height(20.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .clickable { onExpandChange(true) }
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 24.dp)
                            ) {
                                MarqueeText(
                                    text = song.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                )
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = Color(0xFF999999)
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val rewindInteractionCollapsed = remember { MutableInteractionSource() }
                                Image(
                                    painter = painterResource(id = R.drawable.ic_fast_rewind),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .indication(rewindInteractionCollapsed, ripple(bounded = true))
                                        .clickable(
                                            interactionSource = rewindInteractionCollapsed,
                                            indication = null,
                                            enabled = hasPrevious
                                        ) { viewModel.skipPrevious() },
                                    colorFilter = ColorFilter.tint(
                                        if (hasPrevious) Color(0xFFC70025) else Color(0xFFCCCCCC)
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(
                                            width = 2.dp,
                                            color = Color(0xFFC70025),
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.togglePlayPause() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        colorFilter = ColorFilter.tint(Color(0xFFC70025))
                                    )
                                }
                                val forwardInteractionCollapsed = remember { MutableInteractionSource() }
                                Image(
                                    painter = painterResource(id = R.drawable.ic_fast_forward),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .indication(forwardInteractionCollapsed, ripple(bounded = true))
                                        .clickable(
                                            interactionSource = forwardInteractionCollapsed,
                                            indication = null,
                                            enabled = hasNext
                                        ) { viewModel.skipNext() },
                                    colorFilter = ColorFilter.tint(
                                        if (hasNext) Color(0xFFC70025) else Color(0xFFCCCCCC)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (hasDuration) {
                            Slider(
                                value = currentPosition.toFloat(),
                                onValueChange = { viewModel.seekTo(it.toLong()) },
                                valueRange = 0f..song.durationMs.toFloat(),
                                colors = SliderDefaults.colors(
                                    activeTrackColor = Color(0xFFC70025),
                                    inactiveTrackColor = Color(0xFFEEEEEE)
                                ),
                                thumb = {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFC70025))
                                    )
                                },
                                track = { sliderState ->
                                    SliderDefaults.Track(
                                        sliderState = sliderState,
                                        modifier = Modifier.height(4.dp),
                                        colors = SliderDefaults.colors(
                                            activeTrackColor = Color(0xFFC70025),
                                            inactiveTrackColor = Color(0xFFEEEEEE)
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
                                    text = viewModel.formatTime(currentPosition),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF999999)
                                )
                                Text(
                                    text = viewModel.formatTime(song.durationMs),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF999999)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFFEEEEEE))
                            )
                        }
                    }
                }
            }
        }
    }
}