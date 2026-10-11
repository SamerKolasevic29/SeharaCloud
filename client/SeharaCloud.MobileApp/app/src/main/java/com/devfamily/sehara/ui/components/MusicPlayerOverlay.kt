package com.devfamily.sehara.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp as lerpDp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.devfamily.sehara.R
import com.devfamily.sehara.ui.screens.music.MusicPlayerViewModel
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val progress = remember { Animatable(if (isExpanded) 1f else 0f) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 300f
    val navBarDp = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val thumbDrag = remember { Animatable(0f) }
    val stepPx = with(density) { 352.dp.toPx() }

    LaunchedEffect(isExpanded) {
        progress.animateTo(if (isExpanded) 1f else 0f, tween(350))
    }

    var settledSongId by remember { mutableStateOf(song.id) }

    LaunchedEffect(song.id) {
        thumbDrag.snapTo(0f)
        settledSongId = song.id
    }
    val glowTransition = rememberInfiniteTransition(label = "glow")
    val glowX by glowTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowX"
    )
    val glowY by glowTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowY"
    )
    val glowRadius by glowTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowRadius"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val maxH = maxHeight
        val miniTotal = navBarDp + 116.dp
        val travelPx = with(density) { (maxH - miniTotal).toPx() }.coerceAtLeast(1f)

        val p = progress.value
        val containerHeight = lerpDp(miniTotal, maxH, p)
        val horizontalPad = lerpDp(12.dp, 0.dp, p)
        val topPad = lerpDp(8.dp, 0.dp, p)
        val bottomPad = lerpDp(navBarDp + 8.dp, 0.dp, p)
        val corner = lerpDp(30.dp, 0.dp, p)
        val cardColor = lerpColor(Color(0xFF420E0E), Color.Black, p)
        val miniAlpha = (1f - p * 3f).coerceIn(0f, 1f)
        val expandedAlpha = ((p - 0.3f) / 0.7f).coerceIn(0f, 1f)
        val shape = RoundedCornerShape(corner)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(containerHeight)
                .align(Alignment.BottomCenter)
                .offset { IntOffset(dragOffsetX.roundToInt(), 0) }
                .pointerInput(travelPx, isExpanded) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                val target = if (isExpanded) progress.value > 0.75f else progress.value > 0.25f
                                progress.animateTo(if (target) 1f else 0f, tween(250))
                                if (target != isExpanded) onExpandChange(target)
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                progress.animateTo(if (isExpanded) 1f else 0f, tween(250))
                            }
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                progress.snapTo((progress.value - dragAmount / travelPx).coerceIn(0f, 1f))
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (progress.value == 0f && kotlin.math.abs(dragOffsetX) > swipeThreshold) onClose()
                            dragOffsetX = 0f
                        },
                        onDragCancel = { dragOffsetX = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            if (progress.value == 0f) dragOffsetX += dragAmount
                        }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = horizontalPad, end = horizontalPad, top = topPad, bottom = bottomPad)
                    .clip(shape)
                    .background(cardColor, shape)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            if (progress.value < 0.5f) onExpandChange(true)
                        }
                    }
            ) {
                if (p > 0.02f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(Alignment.Top, unbounded = true)
                            .height(maxH)
                            .graphicsLayer { alpha = expandedAlpha }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black)
                                .drawBehind {
                                    val radius = size.width * glowRadius
                                    val center = Offset(size.width * glowX, size.height * glowY)
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF8A0F1F),
                                                Color(0xFF4A0A12),
                                                Color.Transparent
                                            ),
                                            center = center,
                                            radius = radius
                                        ),
                                        radius = radius,
                                        center = center
                                    )
                                }
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
                                if (!song.genre.isNullOrBlank()) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_genre),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clickable {
                                                if (progress.value == 1f) viewModel.openCurrentGenre()
                                            },
                                        colorFilter = ColorFilter.tint(Color.White)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.width(32.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(48.dp))

                            Box(
                                modifier = Modifier
                                    .size(320.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        if (progress.value == 1f) viewModel.openCurrentArtist()
                                    }
                                    .pointerInput(Unit) {
                                        val tracker = VelocityTracker()
                                        val minDistance = 40.dp.toPx()
                                        val flickDistance = 16.dp.toPx()
                                        val flickVelocity = 700f
                                        detectHorizontalDragGestures(
                                            onDragStart = { tracker.resetTracking() },
                                            onDragEnd = {
                                                val vx = tracker.calculateVelocity().x
                                                scope.launch {
                                                    val d = thumbDrag.value
                                                    val goNext = viewModel.hasNext &&
                                                            (d < -minDistance || (d < -flickDistance && vx < -flickVelocity))
                                                    val goPrev = viewModel.hasPrevious &&
                                                            (d > minDistance || (d > flickDistance && vx > flickVelocity))
                                                    when {
                                                        goNext -> {
                                                            thumbDrag.animateTo(-stepPx, tween(200))
                                                            viewModel.skipNext()
                                                            delay(700)
                                                            if (thumbDrag.value != 0f) thumbDrag.animateTo(0f, tween(200))
                                                        }
                                                        goPrev -> {
                                                            thumbDrag.animateTo(stepPx, tween(200))
                                                            viewModel.skipPrevious()
                                                            delay(700)
                                                            if (thumbDrag.value != 0f) thumbDrag.animateTo(0f, tween(200))
                                                        }
                                                        else -> thumbDrag.animateTo(0f, tween(250))
                                                    }
                                                }
                                            },
                                            onDragCancel = {
                                                scope.launch { thumbDrag.animateTo(0f, tween(250)) }
                                            },
                                            onHorizontalDrag = { change, dragAmount ->
                                                tracker.addPosition(change.uptimeMillis, change.position)
                                                if (progress.value == 1f) {
                                                    val raw = thumbDrag.value + dragAmount
                                                    val blocked = (raw < 0f && !viewModel.hasNext) || (raw > 0f && !viewModel.hasPrevious)
                                                    val delta = if (blocked) dragAmount * 0.3f else dragAmount
                                                    scope.launch { thumbDrag.snapTo(thumbDrag.value + delta) }
                                                }
                                            }
                                        )
                                    }
                            ) {
                                listOf(-1, 0, 1).forEach { rel ->
                                    val exists = when (rel) {
                                        -1 -> viewModel.hasPrevious
                                        1 -> viewModel.hasNext
                                        else -> true
                                    }
                                    if (exists) {
                                        AsyncImage(
                                            model = if (rel == 0) song.thumbnailUrl else viewModel.thumbnailAt(rel),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .offset {
                                                    val drag = if (settledSongId == song.id) thumbDrag.value else 0f
                                                    IntOffset((drag + rel * stepPx).roundToInt(), 0)
                                                }
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(Color(0xFF151515)),
                                            contentScale = ContentScale.Crop,
                                            error = painterResource(id = R.drawable.ic_unknown_song_thumb),
                                            placeholder = painterResource(id = R.drawable.music_splash_bg),
                                            fallback = painterResource(id = R.drawable.ic_unknown_song_thumb)
                                        )
                                    }
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

                            Spacer(modifier = Modifier.height(54.dp))

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

                            Spacer(modifier = Modifier.height(64.dp))

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
                }

                if (p < 0.34f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .graphicsLayer { alpha = miniAlpha }
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

                                Column(modifier = Modifier.weight(1f)) {
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
}