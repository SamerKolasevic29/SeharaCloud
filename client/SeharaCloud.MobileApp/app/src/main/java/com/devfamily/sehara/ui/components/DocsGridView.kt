package com.devfamily.sehara.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devfamily.sehara.R
import com.devfamily.sehara.data.DocumentItem

private val DOC_GRID_SHAPE = RoundedCornerShape(16.dp)

@Composable
fun DocsGridView(
    items: List<DocumentItem>,
    onItemClick: (DocumentItem) -> Unit = {}
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .width(332.dp)
            .height(500.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items, key = { it.id }) { doc ->
            val interactionSource = remember { MutableInteractionSource() }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(DOC_GRID_SHAPE)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFC70025),
                        shape = DOC_GRID_SHAPE
                    )
                    .background(
                        color = Color.White,
                        shape = DOC_GRID_SHAPE
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(color = Color(0xFFC70025)),
                        onClick = { onItemClick(doc) }
                    )
                    .padding(vertical = 12.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_docs),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    colorFilter = ColorFilter.tint(Color(0xFFC70025))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = doc.title ?: "Untitled",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF333333),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}