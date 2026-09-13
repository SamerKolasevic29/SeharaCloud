package com.devfamily.sehara.ui.screens.photo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.devfamily.sehara.R
import com.devfamily.sehara.data.RetrofitClient
import com.devfamily.sehara.ui.components.PhotoDetailRow
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun formatFileSize(bytes: Long?): String {
    if (bytes == null) return "Unknown size"
    val mb = bytes / 1024.0 / 1024.0
    return if (mb >= 1.0) {
        "%.2f MB".format(mb)
    } else {
        val kb = bytes / 1024.0
        "%.0f KB".format(kb)
    }
}

private fun formatDate(dateTaken: String?): String {
    if (dateTaken == null) return "Unknown date"
    return try {
        val parsed = OffsetDateTime.parse(dateTaken)
        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm", Locale.ENGLISH)
        parsed.format(formatter)
    } catch (e: Exception) {
        dateTaken
    }
}

@Composable
fun PhotoViewerScreen(
    modifier: Modifier = Modifier,
    viewModel: PhotoViewModel,
    initialIndex: Int,
    onBackClick: () -> Unit = {}
) {
    val photos by viewModel.photoList.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { photos.size }
    )
    val context = LocalContext.current

    var isDetailsExpanded by remember { mutableStateOf(false) }
    val dragOffset = remember { mutableFloatStateOf(0f) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, top = 32.dp, end = 32.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onBackClick() }
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Photos",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color(0xFFCCCCCC)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_photo),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        colorFilter = ColorFilter.tint(Color(0xFF9F001E))
                    )
                }
                Spacer(modifier = Modifier.size(36.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            dragOffset.floatValue += delta
                        },
                        onDragStopped = {
                            if (dragOffset.floatValue < -60f) {
                                isDetailsExpanded = true
                            } else if (dragOffset.floatValue > 60f) {
                                isDetailsExpanded = false
                            }
                            dragOffset.floatValue = 0f
                        }
                    )
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = !isDetailsExpanded
                ) { page ->
                    val photo = photos.getOrNull(page)
                    val streamUrl = photo?.let { "${RetrofitClient.BASE_URL}api/stream/${it.id}" }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        var isImageLoading by remember(streamUrl) { mutableStateOf(true) }
                        var hasImageError by remember(streamUrl) { mutableStateOf(false) }

                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(streamUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = photo?.fileName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(),
                            onState = { state ->
                                isImageLoading = state is AsyncImagePainter.State.Loading
                                hasImageError = state is AsyncImagePainter.State.Error
                            }
                        )

                        if (isImageLoading) {
                            CircularProgressIndicator(color = Color(0xFFC70025))
                        }

                        if (hasImageError) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_photo),
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    colorFilter = ColorFilter.tint(Color(0xFF8E8D8D))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Unable to load photo",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF8E8D8D)
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color(0xFF555555), shape = RoundedCornerShape(2.dp))
                )
            }

            val currentPhoto = remember(pagerState.currentPage, photos) {
                photos.getOrNull(pagerState.currentPage)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_photo),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color(0xFFC70025))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = currentPhoto?.fileName ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            AnimatedVisibility(
                visible = isDetailsExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFF1A1A1A),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PhotoDetailRow(label = "Date taken", value = formatDate(currentPhoto?.dateTaken))
                        PhotoDetailRow(label = "Camera", value = currentPhoto?.camera ?: "Unknown")
                        PhotoDetailRow(
                            label = "Resolution",
                            value = if (currentPhoto?.width != null && currentPhoto.height != null) {
                                "${currentPhoto.width} x ${currentPhoto.height}"
                            } else {
                                "Unknown"
                            }
                        )
                        PhotoDetailRow(label = "File size", value = formatFileSize(currentPhoto?.sizeBytes))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}