package com.devfamily.sehara.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.devfamily.sehara.R
import com.devfamily.sehara.data.SongItem
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter

@Composable
fun AllTracksSection(
    items: List<SongItem>,
    onItemClick: (SongItem) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "All tracks",
    bottomContentPadding: Dp = 0.dp,
    currentSongId: String = "",
    isPlaying: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    onHeaderSwipeDown: (() -> Unit)? = null,
    ascending: Boolean = true,
    sortByArtist: Boolean = false,
    onToggleOrder: (() -> Unit)? = null,
    onToggleSortBy: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(onHeaderSwipeDown) {
                    var total = 0f
                    detectVerticalDragGestures(
                        onDragStart = { total = 0f },
                        onDragEnd = {
                            if (total > 40.dp.toPx()) onHeaderSwipeDown?.invoke()
                            total = 0f
                        },
                        onDragCancel = { total = 0f },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            total += dragAmount
                        }
                    )
                }
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                color = Color.White
            )
            if (onToggleOrder != null && onToggleSortBy != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${items.size})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = Color(0xFF8E8D8D)
                )
                Spacer(modifier = Modifier.weight(1f))
                Image(
                    painter = painterResource(
                        id = if (ascending) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onToggleOrder() },
                    colorFilter = ColorFilter.tint(Color.White)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Image(
                    painter = painterResource(
                        id = if (sortByArtist) R.drawable.ic_artist else R.drawable.ic_music
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onToggleSortBy() },
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = Color.Black,
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                )
                .padding(top = 12.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = bottomContentPadding)
            ) {
                itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                    AllTracksRow(
                        item = item,
                        showDivider = index < items.lastIndex,
                        isCurrent = item.id == currentSongId,
                        isPlaying = isPlaying,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun AllTracksRow(
    item: SongItem,
    showDivider: Boolean,
    isCurrent: Boolean = false,
    isPlaying: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val imageModel = remember(item.thumbnailUrl, item.id) {
        if (!item.thumbnailUrl.isNullOrEmpty()) item.thumbnailUrl else null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isCurrent) Color(0x33C70025) else Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color(0xFFC70025)),
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            AsyncImage(
                model = imageModel,
                contentDescription = null,
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_unknown_artist),
                placeholder = painterResource(id = R.drawable.ic_unknown_artist),
                fallback = painterResource(id = R.drawable.ic_unknown_artist)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.title ?: item.filename,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isCurrent) Color(0xFFFF3054) else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.artist ?: "Unknown Artist",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color(0xFF8E8D8D),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    val totalSeconds = item.durationSec ?: 0
                    if (totalSeconds > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "%d:%02d".format(totalSeconds / 60, totalSeconds % 60),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = Color(0xFF8E8D8D),
                            maxLines = 1
                        )
                    }
                }
            }

            if (isCurrent) {
                Spacer(modifier = Modifier.width(12.dp))
                EqualizerBars(isPlaying = isPlaying)
            }
        }

        if (showDivider) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 24.dp)
            ) {
                Spacer(modifier = Modifier.width(24.dp + 45.dp + 14.dp))
                HorizontalDivider(
                    color = Color(0xFF767676),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}