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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devfamily.sehara.R
import com.devfamily.sehara.ui.screens.music.MusicPlayerViewModel
import coil.compose.AsyncImage

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
                    .clickable {}
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp)
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
                                .clickable { onExpandChange(false) }
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
                        AsyncImage(
                            model = song.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.music_splash_bg),
                            placeholder = painterResource(id = R.drawable.music_splash_bg)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color(0xFF999999)
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
                                thumbColor = Color(0xFFC70025),
                                activeTrackColor = Color(0xFFC70025),
                                inactiveTrackColor = Color(0xFFEEEEEE)
                            )
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
                        Image(
                            painter = painterResource(id = R.drawable.ic_fast_rewind),
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable(enabled = hasPrevious) { viewModel.skipPrevious() }
                                .padding(8.dp),
                            colorFilter = ColorFilter.tint(
                                if (hasPrevious) Color(0xFFC70025) else Color(0xFFCCCCCC)
                            )
                        )

                        Spacer(modifier = Modifier.width(24.dp))

                        Image(
                            painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                            contentDescription = null,
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFC70025))
                                .clickable { viewModel.togglePlayPause() }
                                .padding(20.dp)
                        )

                        Spacer(modifier = Modifier.width(24.dp))

                        Image(
                            painter = painterResource(id = R.drawable.ic_fast_forward),
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable(enabled = hasNext) { viewModel.skipNext() }
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .clickable {}
                    .padding(horizontal = 16.dp, vertical = 30.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_arrow_up),
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onExpandChange(true) }
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 24.dp)
                        ) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.labelSmall.copy(
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
                            Image(
                                painter = painterResource(id = R.drawable.ic_fast_rewind),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable(enabled = hasPrevious) { viewModel.skipPrevious() },
                                colorFilter = ColorFilter.tint(
                                    if (hasPrevious) Color(0xFFC70025) else Color(0xFFCCCCCC)
                                )
                            )
                            Image(
                                painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFC70025))
                                    .clickable { viewModel.togglePlayPause() }
                                    .padding(8.dp)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.ic_fast_forward),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable(enabled = hasNext) { viewModel.skipNext() },
                                colorFilter = ColorFilter.tint(
                                    if (hasNext) Color(0xFFC70025) else Color(0xFFCCCCCC)
                                )
                            )
                            Image(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { onClose() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (hasDuration) {
                        Slider(
                            value = currentPosition.toFloat(),
                            onValueChange = { viewModel.seekTo(it.toLong()) },
                            valueRange = 0f..song.durationMs.toFloat(),
                            modifier = Modifier.height(16.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFC70025),
                                activeTrackColor = Color(0xFFC70025),
                                inactiveTrackColor = Color(0xFFEEEEEE)
                            )
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