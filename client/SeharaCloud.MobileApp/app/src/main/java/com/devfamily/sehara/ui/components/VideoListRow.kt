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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.devfamily.sehara.R
import com.devfamily.sehara.data.VideoItem

private val VIDEO_ROW_HEIGHT = 70.dp
private const val VIDEO_MAX_VISIBLE_ITEMS = 5
private val VIDEO_ROW_SHAPE = RoundedCornerShape(16.dp)

@Composable
fun VideoListRow(item: VideoItem, onClick: () -> Unit = {}) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(VIDEO_ROW_HEIGHT)
            .clip(VIDEO_ROW_SHAPE)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color(0xFFC70025)),
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        val imageModel = remember(item.thumbnailUrl, item.id) {
            if (!item.thumbnailUrl.isNullOrEmpty()) item.thumbnailUrl else null
        }

        AsyncImage(
            model = imageModel,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_unknown_video),
            placeholder = painterResource(id = R.drawable.ic_unknown_video),
            fallback = painterResource(id = R.drawable.ic_unknown_video)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun VideoListScrollable(
    items: List<VideoItem>,
    onItemClick: (VideoItem) -> Unit
) {
    val listState = rememberLazyListState()

    val nestedScrollConnection = remember(listState) {
        object : NestedScrollConnection {

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return available
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .heightIn(max = VIDEO_ROW_HEIGHT * VIDEO_MAX_VISIBLE_ITEMS)
            .nestedScroll(nestedScrollConnection)
    ) {
        items(items, key = { it.id }) { item ->
            VideoListRow(item = item, onClick = { onItemClick(item) })
        }
    }
}

private fun formatVideoDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, secs)
    } else {
        "%d:%02d".format(minutes, secs)
    }
}

@Composable
fun VideoRecentRow(item: VideoItem, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val imageModel = remember(item.thumbnailUrl, item.id) {
        if (!item.thumbnailUrl.isNullOrEmpty()) item.thumbnailUrl else null
    }
    val duration = item.durationSec ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(VIDEO_ROW_SHAPE)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color(0xFFC70025)),
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageModel,
            contentDescription = null,
            modifier = Modifier
                .size(width = 96.dp, height = 56.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_unknown_video),
            placeholder = painterResource(id = R.drawable.ic_unknown_video),
            fallback = painterResource(id = R.drawable.ic_unknown_video)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = Color(0xFFC70025),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (duration > 0) {
                Text(
                    text = formatVideoDuration(duration),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color(0xFF8E8D8D),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun VideoRecentSection(
    items: List<VideoItem>,
    onItemClick: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.Black,
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
            )
            .padding(top = 8.dp, bottom = 24.dp)
    ) {
        Text(
            text = "Recent videos",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        )

        items.forEachIndexed { index, item ->
            VideoRecentRow(item = item, onClick = { onItemClick(item) })
            if (index < items.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = Color(0xFF767676),
                    thickness = 0.5.dp
                )
            }
        }
    }
}

@Composable
fun VideoRecentlyAddedSection(
    items: List<VideoItem>,
    onItemClick: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Recently added",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items, key = { it.id }) { item ->
                VideoRecentCard(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
fun VideoRecentCard(
    item: VideoItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val imageModel = remember(item.thumbnailUrl, item.id) {
        if (!item.thumbnailUrl.isNullOrEmpty()) item.thumbnailUrl else null
    }
    val duration = item.durationSec ?: 0

    Column(
        modifier = Modifier
            .width(180.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color(0xFFC70025)),
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = imageModel,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_unknown_video),
                placeholder = painterResource(id = R.drawable.ic_unknown_video),
                fallback = painterResource(id = R.drawable.ic_unknown_video)
            )

            if (duration > 0) {
                Text(
                    text = formatVideoDuration(duration),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .background(Color(0xB3000000), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AllVideosSection(
    items: List<VideoItem>,
    onItemClick: (VideoItem) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "All videos",
    bottomContentPadding: Dp = 0.dp,
    gridState: LazyGridState = rememberLazyGridState()
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = Color.Black,
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                )
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = 24.dp,
                    bottom = bottomContentPadding
                ),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                gridItems(items, key = { it.id }) { item ->
                    VideoGridItem(item = item, onClick = { onItemClick(item) })
                }
            }
        }
    }
}

@Composable
fun VideoGridItem(
    item: VideoItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val imageModel = remember(item.thumbnailUrl, item.id) {
        if (!item.thumbnailUrl.isNullOrEmpty()) item.thumbnailUrl else null
    }
    val duration = item.durationSec ?: 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color(0xFFC70025)),
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(14.dp))
        ) {
            AsyncImage(
                model = imageModel,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_unknown_video),
                placeholder = painterResource(id = R.drawable.ic_unknown_video),
                fallback = painterResource(id = R.drawable.ic_unknown_video)
            )

            if (duration > 0) {
                Text(
                    text = formatVideoDuration(duration),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .background(Color(0xB3000000), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 17.sp),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}