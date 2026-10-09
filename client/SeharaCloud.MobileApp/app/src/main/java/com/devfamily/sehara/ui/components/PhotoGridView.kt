package com.devfamily.sehara.ui.components

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.devfamily.sehara.R
import com.devfamily.sehara.data.PhotoItem
import com.devfamily.sehara.data.resolvedDate
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ln

private const val LEVEL_DAYS = 0
private const val LEVEL_MONTHS = 1
private const val LEVEL_YEARS = 2
private const val ZOOM_THRESHOLD = 1.3f
private val LN_THRESHOLD = ln(ZOOM_THRESHOLD)

@Composable
fun PhotoGridView(
    items: List<PhotoItem>,
    modifier: Modifier = Modifier,
    onItemClick: (PhotoItem) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var level by rememberSaveable { mutableIntStateOf(LEVEL_MONTHS) }
    var pinch by remember { mutableFloatStateOf(1f) }

    val dayFormatter = remember { DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH) }
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH) }

    val columns = when (level) {
        LEVEL_DAYS -> 3
        LEVEL_MONTHS -> 4
        else -> 5
    }

    val grouped = remember(items, level) {
        val today = LocalDate.now()
        items.groupBy { photo ->
            val date = photo.resolvedDate()
            when {
                date == null -> "Unknown date"
                level == LEVEL_DAYS -> when (date) {
                    today -> "Today"
                    today.minusDays(1) -> "Yesterday"
                    else -> date.format(dayFormatter)
                }
                level == LEVEL_MONTHS -> date.format(monthFormatter)
                else -> date.year.toString()
            }
        }
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    var accumulated = 1f
                    var handled = false
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val pressedCount = event.changes.count { it.pressed }
                        if (pressedCount >= 2) {
                            val zoom = event.calculateZoom()
                            if (!zoom.isNaN() && zoom > 0f && !handled) {
                                accumulated *= zoom
                                val canZoomIn = accumulated > 1f && level > LEVEL_DAYS
                                val canZoomOut = accumulated < 1f && level < LEVEL_YEARS
                                if (canZoomIn || canZoomOut) {
                                    pinch = accumulated.coerceIn(1f / ZOOM_THRESHOLD, ZOOM_THRESHOLD)
                                }
                                if (accumulated > ZOOM_THRESHOLD && level > LEVEL_DAYS) {
                                    level -= 1
                                    handled = true
                                    val start = 1f / ZOOM_THRESHOLD
                                    pinch = start
                                    scope.launch {
                                        animate(start, 1f, animationSpec = tween(300)) { v, _ -> pinch = v }
                                    }
                                } else if (accumulated < 1f / ZOOM_THRESHOLD && level < LEVEL_YEARS) {
                                    level += 1
                                    handled = true
                                    val start = ZOOM_THRESHOLD
                                    pinch = start
                                    scope.launch {
                                        animate(start, 1f, animationSpec = tween(300)) { v, _ -> pinch = v }
                                    }
                                }
                            }
                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })

                    if (!handled && pinch != 1f) {
                        val from = pinch
                        scope.launch {
                            animate(from, 1f, animationSpec = tween(250)) { v, _ -> pinch = v }
                        }
                    }
                }
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = pinch
                    scaleY = pinch
                    val p = (abs(ln(pinch)) / LN_THRESHOLD).coerceIn(0f, 1f)
                    alpha = 1f - 0.85f * p
                },
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
        ) {
            grouped.forEach { (label, photos) ->
                item(key = "header_${level}_$label") {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFB30021),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 10.dp)
                    )
                }

                val rows = photos.chunked(columns)
                rows.forEachIndexed { rowIndex, rowItems ->
                    item(key = "row_${level}_${label}_$rowIndex") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            for (i in 0 until columns) {
                                if (i < rowItems.size) {
                                    val photo = rowItems[i]
                                    val thumbUrl = photo.thumbnailUrl?.takeIf { it.isNotEmpty() }
                                    val request = remember(thumbUrl) {
                                        ImageRequest.Builder(context)
                                            .data(thumbUrl)
                                            .size(256)
                                            .memoryCacheKey(thumbUrl)
                                            .diskCacheKey(thumbUrl)
                                            .memoryCachePolicy(CachePolicy.ENABLED)
                                            .diskCachePolicy(CachePolicy.ENABLED)
                                            .crossfade(false)
                                            .build()
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .background(Color(0xFF161616))
                                            .clickable { onItemClick(photo) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_photo),
                                            contentDescription = null,
                                            modifier = Modifier.size(28.dp),
                                            colorFilter = ColorFilter.tint(Color(0xFF555555))
                                        )
                                        AsyncImage(
                                            model = request,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.matchParentSize()
                                        )
                                    }
                                } else {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}