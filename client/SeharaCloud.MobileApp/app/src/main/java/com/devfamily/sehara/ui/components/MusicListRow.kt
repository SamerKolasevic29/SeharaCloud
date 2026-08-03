package com.devfamily.sehara.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.devfamily.sehara.R
import com.devfamily.sehara.data.SongItem

private val ROW_HEIGHT = 70.dp
private const val MAX_VISIBLE_ITEMS = 5

@Composable
fun MusicListRow(item: SongItem, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ROW_HEIGHT)
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
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
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFFC70025),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = listOfNotNull(item.artist, item.genre).joinToString(" / "),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF8E8D8D),
                maxLines = 1
            )
        }
    }
}

@Composable
fun MusicListScrollable(
    items: List<SongItem>,
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
            MusicListRow(item = item, onClick = { onItemClick(item) })
        }
    }
}