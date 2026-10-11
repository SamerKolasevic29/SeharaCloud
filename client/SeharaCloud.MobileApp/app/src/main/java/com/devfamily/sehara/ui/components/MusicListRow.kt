package com.devfamily.sehara.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.devfamily.sehara.R
import com.devfamily.sehara.data.SongItem
import androidx.compose.foundation.indication

private val ROW_HEIGHT = 70.dp
private const val MAX_VISIBLE_ITEMS = 5
private val MUSIC_ROW_SHAPE = RoundedCornerShape(16.dp)

@Composable
fun RecentlyAddedSection(
    items: List<SongItem>,
    onItemClick: (SongItem) -> Unit,
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
                RecentlyAddedCard(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
fun RecentlyAddedCard(
    item: SongItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val imageModel = remember(item.thumbnailUrl, item.id) {
        if (!item.thumbnailUrl.isNullOrEmpty()) item.thumbnailUrl else null
    }

    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        AsyncImage(
            model = imageModel,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .indication(interactionSource, ripple(color = Color(0xFFC70025))),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_unknown_artist),
            placeholder = painterResource(id = R.drawable.ic_unknown_artist),
            fallback = painterResource(id = R.drawable.ic_unknown_artist)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.title ?: item.filename,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = item.artist ?: "Unknown Artist",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = Color(0xFF8E8D8D),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MusicListRow(
    item: SongItem,
    isCurrent: Boolean = false,
    isPlaying: Boolean = false,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ROW_HEIGHT)
            .clip(MUSIC_ROW_SHAPE)
            .background(if (isCurrent) Color(0x33C70025) else Color.Transparent)
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
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_unknown_artist),
            placeholder = painterResource(id = R.drawable.ic_unknown_artist),
            fallback = painterResource(id = R.drawable.ic_unknown_artist)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title ?: item.filename,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isCurrent) Color(0xFFFF3054) else Color(0xFFC70025),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = listOfNotNull(item.artist, item.genre).joinToString(" / "),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color(0xFF8E8D8D),
                maxLines = 1
            )
        }

        if (isCurrent) {
            Spacer(modifier = Modifier.width(12.dp))
            EqualizerBars(isPlaying = isPlaying)
        }
    }
}

@Composable
fun EqualizerBars(isPlaying: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "equalizer")
    val h1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(620), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(530), RepeatMode.Reverse),
        label = "h3"
    )

    Row(
        modifier = modifier.height(16.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        listOf(h1, h2, h3).forEach { h ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(if (isPlaying) h else 0.35f)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Color(0xFFFF3054))
            )
        }
    }
}

@Composable
fun MusicListScrollable(
    items: List<SongItem>,
    currentSongId: String = "",
    isPlaying: Boolean = false,
    onItemClick: (SongItem) -> Unit
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
            .heightIn(max = ROW_HEIGHT * MAX_VISIBLE_ITEMS)
            .nestedScroll(nestedScrollConnection)
    ) {
        items(items, key = { it.id }) { item ->
            MusicListRow(
                item = item,
                isCurrent = item.id == currentSongId,
                isPlaying = isPlaying,
                onClick = { onItemClick(item) }
            )
        }
    }
}