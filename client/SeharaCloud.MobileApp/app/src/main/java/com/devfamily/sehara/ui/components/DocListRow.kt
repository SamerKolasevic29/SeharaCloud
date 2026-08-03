package com.devfamily.sehara.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devfamily.sehara.R
import com.devfamily.sehara.data.DocumentItem
import androidx.compose.material3.ripple

private val DOC_ROW_HEIGHT = 70.dp
private const val DOC_MAX_VISIBLE_ITEMS = 5
private val DOC_ROW_SHAPE = RoundedCornerShape(16.dp)

@Composable
fun DocListRow(item: DocumentItem, onClick: () -> Unit = {}) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(DOC_ROW_HEIGHT)
            .clip(DOC_ROW_SHAPE)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color(0xFFC70025)),
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_docs),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            colorFilter = ColorFilter.tint(Color(0xFFC70025))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title ?: "Untitled",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFFC70025),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!item.author.isNullOrBlank()) {
                Text(
                    text = item.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF8E8D8D),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DocListScrollable(
    items: List<DocumentItem>,
    onItemClick: (DocumentItem) -> Unit
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
            .heightIn(max = DOC_ROW_HEIGHT * DOC_MAX_VISIBLE_ITEMS)
            .nestedScroll(nestedScrollConnection)
    ) {
        items(items, key = { it.id }) { item ->
            DocListRow(item = item, onClick = { onItemClick(item) })
        }
    }
}