package com.devfamily.sehara.ui.screens.photo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devfamily.sehara.R
import com.devfamily.sehara.ui.components.PhotoGridView

@Composable
fun PhotoScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onPhotoClick: (Int) -> Unit = {},
    viewModel: PhotoViewModel
) {
    val photoList by viewModel.photoList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF201F1F))
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF161616),
                        shape = RoundedCornerShape(bottomStart = 25.dp, bottomEnd = 25.dp)
                    )
                    .statusBarsPadding()
                    .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onBackClick() },
                    colorFilter = ColorFilter.tint(Color.White)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_photo),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        colorFilter = ColorFilter.tint(Color(0xFFFF3054))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Photos",
                        style = MaterialTheme.typography.displayMedium.copy(letterSpacing = 0.sp),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.size(28.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFC70025))
                        }
                    }
                    error != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Error: $error",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF8E8D8D)
                            )
                        }
                    }
                    photoList.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No photos found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF8E8D8D)
                            )
                        }
                    }
                    else -> {
                        PhotoGridView(
                            items = photoList,
                            modifier = Modifier.fillMaxSize()
                        ) { photo ->
                            val index = photoList.indexOfFirst { it.id == photo.id }
                            if (index >= 0) onPhotoClick(index)
                        }
                    }
                }
            }
        }
    }
}