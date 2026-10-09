package com.devfamily.sehara.ui.screens.photo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.devfamily.sehara.R
import com.devfamily.sehara.data.PhotoItem
import com.devfamily.sehara.data.RetrofitClient
import com.devfamily.sehara.data.displayName
import com.devfamily.sehara.data.resolvedDate
import com.devfamily.sehara.ui.components.PhotoDetailRow
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun formatFileSize(bytes: Long?): String {
    if (bytes == null || bytes <= 0L) return "Unknown size"
    val mb = bytes / 1024.0 / 1024.0
    return if (mb >= 1.0) {
        "%.2f MB".format(Locale.ENGLISH, mb)
    } else {
        "%.0f KB".format(Locale.ENGLISH, bytes / 1024.0)
    }
}

private fun formatDate(photo: PhotoItem?): String {
    if (photo == null) return "Unknown date"
    val raw = photo.dateTaken
    if (!raw.isNullOrBlank()) {
        try {
            val parsed = OffsetDateTime.parse(raw)
            return parsed.format(DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm", Locale.ENGLISH))
        } catch (e: Exception) {
        }
    }
    val date = photo.resolvedDate() ?: return "Unknown date"
    return date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH))
}

@Composable
fun PhotoViewerScreen(
    modifier: Modifier = Modifier,
    viewModel: PhotoViewModel,
    initialIndex: Int,
    onBackClick: () -> Unit = {}
) {
    val photos by viewModel.photoList.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceAtLeast(0),
        pageCount = { photos.size }
    )
    val context = LocalContext.current

    var isDetailsExpanded by remember { mutableStateOf(false) }
    var isZoomed by remember { mutableStateOf(false) }
    var isImmersive by remember { mutableStateOf(false) }
    val dragOffset = remember { mutableFloatStateOf(0f) }

    val currentPhoto = remember(pagerState.currentPage, photos) {
        photos.getOrNull(pagerState.currentPage)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isImmersive) Color.Black else Color(0xFF201F1F))
                .navigationBarsPadding()
                .then(if (isImmersive) Modifier.statusBarsPadding() else Modifier)
        ) {
            AnimatedVisibility(
                visible = !isImmersive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF161616),
                        shape = RoundedCornerShape(bottomStart = 25.dp, bottomEnd = 25.dp)
                    )
                    .statusBarsPadding()
                    .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onBackClick() }
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_photo),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        colorFilter = ColorFilter.tint(Color(0xFFFF3054))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Photos",
                        style = MaterialTheme.typography.displayMedium.copy(letterSpacing = 0.sp),
                        color = Color.White
                    )
                }
                    Spacer(modifier = Modifier.size(28.dp))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .draggable(
                        enabled = !isZoomed && !isImmersive,
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            dragOffset.floatValue += delta
                        },
                        onDragStopped = {
                            if (dragOffset.floatValue < -60f) {
                                isDetailsExpanded = true
                            } else if (dragOffset.floatValue > 60f) {
                                isDetailsExpanded = false
                            }
                            dragOffset.floatValue = 0f
                        }
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = !isDetailsExpanded && !isZoomed
                    ) { page ->
                        val photo = photos.getOrNull(page)
                        val streamUrl = photo?.let {
                            "${RetrofitClient.BASE_URL.trimEnd('/')}/api/images/${it.id}/stream"
                        }

                        var scale by remember(streamUrl) { mutableFloatStateOf(1f) }
                        var offset by remember(streamUrl) { mutableStateOf(Offset.Zero) }

                        LaunchedEffect(scale, pagerState.currentPage) {
                            if (page == pagerState.currentPage) {
                                isZoomed = scale > 1f
                                Log.d("PhotoZoom", "page=$page isZoomed=$isZoomed scale=$scale")
                            }
                        }

                        LaunchedEffect(pagerState.currentPage) {
                            if (page != pagerState.currentPage) {
                                scale = 1f
                                offset = Offset.Zero
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clipToBounds()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            isImmersive = !isImmersive
                                            if (isImmersive) isDetailsExpanded = false
                                        },
                                        onDoubleTap = {
                                            if (scale > 1f) {
                                                scale = 1f
                                                offset = Offset.Zero
                                            } else {
                                                scale = 2.5f
                                            }
                                        }
                                    )
                                }
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        awaitFirstDown(requireUnconsumed = false)
                                        do {
                                            val event = awaitPointerEvent()
                                            val pressedCount = event.changes.count { it.pressed }
                                            if (pressedCount >= 2 || scale > 1f) {
                                                val zoom = event.calculateZoom().let { if (it.isNaN() || it <= 0f) 1f else it }
                                                val pan = event.calculatePan().let {
                                                    if (!it.isSpecified || it.x.isNaN() || it.y.isNaN()) Offset.Zero else it
                                                }
                                                val rawCentroid = event.calculateCentroid(useCurrent = false)
                                                val centroid = if (
                                                    rawCentroid.isSpecified &&
                                                    !rawCentroid.x.isNaN() &&
                                                    !rawCentroid.y.isNaN()
                                                ) rawCentroid else Offset(size.width / 2f, size.height / 2f)
                                                val oldScale = scale
                                                val newScale = (oldScale * zoom).coerceIn(1f, 5f)
                                                val center = Offset(size.width / 2f, size.height / 2f)
                                                val c = centroid - center
                                                val raw = c - (c - offset) * (newScale / oldScale) + pan
                                                val maxX = size.width * (newScale - 1f) / 2f
                                                val maxY = size.height * (newScale - 1f) / 2f
                                                val safeRawX = if (raw.x.isNaN() || raw.x.isInfinite()) offset.x else raw.x
                                                val safeRawY = if (raw.y.isNaN() || raw.y.isInfinite()) offset.y else raw.y
                                                scale = newScale
                                                offset = if (newScale <= 1f) {
                                                    Offset.Zero
                                                } else {
                                                    Offset(
                                                        safeRawX.coerceIn(-maxX, maxX),
                                                        safeRawY.coerceIn(-maxY, maxY)
                                                    )
                                                }
                                                Log.d(
                                                    "PhotoZoom",
                                                    "page=$page zoom=$zoom old=$oldScale new=$newScale pan=$pan raw=$raw offset=$offset max=($maxX,$maxY) size=${size.width}x${size.height}"
                                                )
                                                event.changes.forEach {
                                                    if (it.positionChanged()) it.consume()
                                                }
                                            }
                                        } while (event.changes.any { it.pressed })
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            var isImageLoading by remember(streamUrl) { mutableStateOf(true) }
                            var hasImageError by remember(streamUrl) { mutableStateOf(false) }

                            val imageRequest = remember(streamUrl) {
                                ImageRequest.Builder(context)
                                    .data(streamUrl)
                                    .size(2048)
                                    .allowHardware(false)
                                    .placeholderMemoryCacheKey(photo?.thumbnailUrl)
                                    .crossfade(false)
                                    .build()
                            }

                            AsyncImage(
                                model = imageRequest,
                                contentDescription = photo?.displayName(),
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y
                                    ),
                                onState = { state ->
                                    isImageLoading = state is AsyncImagePainter.State.Loading
                                    hasImageError = state is AsyncImagePainter.State.Error
                                    when (state) {
                                        is AsyncImagePainter.State.Loading ->
                                            Log.d("PhotoZoom", "page=$page state=Loading url=$streamUrl")
                                        is AsyncImagePainter.State.Success -> {
                                            val d = state.result.drawable
                                            Log.d(
                                                "PhotoZoom",
                                                "page=$page state=Success drawable=${d.intrinsicWidth}x${d.intrinsicHeight} class=${d.javaClass.simpleName}"
                                            )
                                        }
                                        is AsyncImagePainter.State.Error ->
                                            Log.e("PhotoZoom", "page=$page state=Error", state.result.throwable)
                                        else -> Log.d("PhotoZoom", "page=$page state=Empty")
                                    }
                                }
                            )

                            if (isImageLoading) {
                                CircularProgressIndicator(color = Color(0xFFC70025))
                            }

                            if (hasImageError) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_photo),
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        colorFilter = ColorFilter.tint(Color(0xFF8E8D8D))
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Unable to load photo",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF8E8D8D)
                                    )
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = !isImmersive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(Color(0xFF555555), shape = RoundedCornerShape(2.dp))
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = !isImmersive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_photo),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(Color(0xFFC70025))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = currentPhoto?.displayName() ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            AnimatedVisibility(
                visible = isDetailsExpanded && !isImmersive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = Color(0xFFC70025),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .background(
                                color = Color(0xFF161616),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PhotoDetailRow(label = "Date taken", value = formatDate(currentPhoto))
                        PhotoDetailRow(label = "Camera", value = currentPhoto?.camera ?: "Unknown")
                        PhotoDetailRow(
                            label = "Resolution",
                            value = if (currentPhoto?.width != null && currentPhoto.height != null) {
                                "${currentPhoto.width} x ${currentPhoto.height}"
                            } else {
                                "Unknown"
                            }
                        )
                        PhotoDetailRow(label = "File size", value = formatFileSize(currentPhoto?.sizeBytes))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}