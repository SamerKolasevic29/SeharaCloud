package com.devfamily.sehara.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.devfamily.sehara.R
import com.devfamily.sehara.data.FileItem

@Composable
fun MusicListRow(item: FileItem, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        AsyncImage(
            model = if (item.thumbnailUrl != null) "http://192.168.100.79:5000/api/thumbnails/${item.id}" else null,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_unknown),
            placeholder = painterResource(id = R.drawable.ic_unknown),
            fallback = painterResource(id = R.drawable.ic_unknown)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
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