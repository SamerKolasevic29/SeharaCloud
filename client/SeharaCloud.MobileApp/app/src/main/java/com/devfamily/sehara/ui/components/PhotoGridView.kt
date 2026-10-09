package com.devfamily.sehara.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import coil.request.CachePolicy
import coil.request.ImageRequest
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.devfamily.sehara.R
import com.devfamily.sehara.data.PhotoItem
import com.devfamily.sehara.data.resolvedDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PHOTO_TILE_SHAPE = RoundedCornerShape(16.dp)

@Composable
fun PhotoGridView(
    items: List<PhotoItem>,
    modifier: Modifier = Modifier,
    onItemClick: (PhotoItem) -> Unit = {}
) {
    val context = LocalContext.current
    val formatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH) }
    val grouped = remember(items) {
        items.groupBy { photo -> photo.resolvedDate()?.let { YearMonth.from(it) } }
    }

    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        grouped.forEach { (month, photos) ->
            item(key = "header_${month ?: "unknown"}") {
                Text(
                    text = month?.format(formatter) ?: "Unknown date",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFB30021),
                    modifier = Modifier
                        .width(332.dp)
                        .padding(top = 12.dp, bottom = 10.dp)
                )
            }

            val rows = photos.chunked(3)
            rows.forEachIndexed { rowIndex, rowItems ->
                item(key = "row_${month ?: "unknown"}_$rowIndex") {
                    Row(
                        modifier = Modifier
                            .width(332.dp)
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (i in 0 until 3) {
                            if (i < rowItems.size) {
                                val photo = rowItems[i]
                                val interactionSource = remember { MutableInteractionSource() }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(PHOTO_TILE_SHAPE)
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFC70025),
                                            shape = PHOTO_TILE_SHAPE
                                        )
                                        .background(
                                            color = Color(0xFFC70025),
                                            shape = PHOTO_TILE_SHAPE
                                        )
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = ripple(color = Color.White),
                                            onClick = { onItemClick(photo) }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_photo),
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        colorFilter = ColorFilter.tint(Color.White)
                                    )
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