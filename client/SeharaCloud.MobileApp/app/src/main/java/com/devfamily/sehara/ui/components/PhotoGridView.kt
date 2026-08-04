package com.devfamily.sehara.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.devfamily.sehara.R
import com.devfamily.sehara.data.PhotoItem
import java.time.OffsetDateTime
import java.util.Locale

private val PHOTO_TILE_SHAPE = RoundedCornerShape(16.dp)

private data class PhotoGroupKey(val year: Int, val month: Int)

private fun PhotoItem.parsedDate(): OffsetDateTime? {
    return try {
        dateTaken?.let { OffsetDateTime.parse(it) }
    } catch (e: Exception) {
        null
    }
}

@Composable
fun PhotoGridView(
    items: List<PhotoItem>,
    onItemClick: (PhotoItem) -> Unit = {}
) {
    val grouped = remember(items) {
        items
            .mapNotNull { photo -> photo.parsedDate()?.let { photo to it } }
            .sortedByDescending { it.second }
            .groupBy { PhotoGroupKey(it.second.year, it.second.monthValue) }
            .toSortedMap(compareByDescending<PhotoGroupKey> { it.year }.thenByDescending { it.month })
    }

    Column(
        modifier = Modifier.width(332.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        grouped.forEach { (key, entries) ->
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = monthYearLabel(key),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFB30021)
                )

                val rows = entries.map { it.first }.chunked(3)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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

private fun monthYearLabel(key: PhotoGroupKey): String {
    val monthName = java.time.Month.of(key.month)
        .getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
    return "$monthName ${key.year}"
}