package com.devfamily.sehara.ui.screens.photo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.devfamily.sehara.ui.components.PhotoGridView

@Composable
fun PhotoScreen(
    modifier: Modifier = Modifier,
    onPhotoClick: (Int) -> Unit = {},
    viewModel: PhotoViewModel
) {
    val photoList by viewModel.photoList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF201F1F)),
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