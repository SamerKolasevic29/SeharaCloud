package com.devfamily.sehara.ui.screens.video

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devfamily.sehara.R
import com.devfamily.sehara.ui.components.AllVideosSection
import com.devfamily.sehara.ui.components.CategoryCard
import com.devfamily.sehara.ui.components.SearchBar
import com.devfamily.sehara.ui.components.VideoListScrollable
import com.devfamily.sehara.ui.components.VideoPlayerOverlay
import com.devfamily.sehara.ui.components.VideoRecentlyAddedSection
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun VideoHomeScreen(
    modifier: Modifier = Modifier,
    showInitialSplash: Boolean = true,
    onBackClick: () -> Unit = {},
    viewModel: VideoViewModel = viewModel(),
    playerViewModel: VideoPlayerViewModel = viewModel()
) {
    var showSplash by rememberSaveable { mutableStateOf(false) }
    val videoList by viewModel.videoList.collectAsState()
    val recentList by viewModel.recentList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val playerMode by playerViewModel.playerMode.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val gridState = rememberLazyGridState()

    var collapseOffset by remember { mutableFloatStateOf(0f) }
    var topHeightPx by remember { mutableIntStateOf(0) }

    val collapseConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (searchQuery.isNotBlank() || available.y >= 0f) return Offset.Zero
                val newOffset = (collapseOffset + available.y).coerceIn(-topHeightPx.toFloat(), 0f)
                val consumedY = newOffset - collapseOffset
                collapseOffset = newOffset
                return Offset(0f, consumedY)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (searchQuery.isNotBlank() || available.y <= 0f) return Offset.Zero
                val newOffset = (collapseOffset + available.y).coerceIn(-topHeightPx.toFloat(), 0f)
                val consumedY = newOffset - collapseOffset
                collapseOffset = newOffset
                return Offset(0f, consumedY)
            }
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            viewModel.clearSearch()
        } else {
            collapseOffset = 0f
            delay(300)
            viewModel.searchVideos(searchQuery)
        }
    }

    val context = LocalContext.current
    LaunchedEffect(playerMode) {
        val activity = context as? Activity
        if (playerMode == VideoPlayerMode.FULLSCREEN) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val activity = context as? Activity
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    AnimatedContent(
        targetState = showSplash,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "splash_transition"
    ) { isSplash ->
        if (isSplash) {
            Box(modifier = modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Image(
                    painter = painterResource(id = R.drawable.video_splash_bg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_video),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Video",
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White
                    )
                }
            }
        } else {
            Box(modifier = modifier.fillMaxSize()) {
                if (playerMode != VideoPlayerMode.PORTRAIT) {
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
                                    painter = painterResource(id = R.drawable.ic_video),
                                    contentDescription = null,
                                    modifier = Modifier.size(45.dp),
                                    colorFilter = ColorFilter.tint(Color(0xFFFF3054))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Video",
                                    style = MaterialTheme.typography.displayMedium.copy(letterSpacing = 0.sp),
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.size(28.dp))
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(collapseConnection),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clipToBounds()
                                    .layout { measurable, constraints ->
                                        val placeable = measurable.measure(
                                            constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
                                        )
                                        val offset = collapseOffset.coerceIn(-placeable.height.toFloat(), 0f)
                                        val visibleHeight = (placeable.height + offset).roundToInt().coerceAtLeast(0)
                                        layout(placeable.width, visibleHeight) {
                                            placeable.place(0, offset.roundToInt())
                                        }
                                    }
                                    .graphicsLayer {
                                        val progress = if (topHeightPx > 0) {
                                            (-collapseOffset / topHeightPx).coerceIn(0f, 1f)
                                        } else 0f
                                        alpha = (1f - progress * 1.4f).coerceIn(0f, 1f)
                                        val s = 1f - 0.12f * progress
                                        scaleX = s
                                        scaleY = s
                                        transformOrigin = TransformOrigin(0.5f, 0f)
                                    }
                                    .onSizeChanged { topHeightPx = it.height },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))

                                SearchBar(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it }
                                )

                                AnimatedVisibility(
                                    visible = searchQuery.isNotBlank(),
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(top = 12.dp)
                                            .width(332.dp)
                                            .border(
                                                width = 1.dp,
                                                color = Color(0xFFC70025),
                                                shape = RoundedCornerShape(24.dp)
                                            )
                                            .background(
                                                color = Color(0xFF201F1F),
                                                shape = RoundedCornerShape(24.dp)
                                            )
                                            .padding(vertical = 8.dp)
                                    ) {
                                        when {
                                            isSearching -> {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(80.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator(color = Color(0xFFC70025))
                                                }
                                            }
                                            searchResults.isEmpty() -> {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(80.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "No results found",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color(0xFF8E8D8D)
                                                    )
                                                }
                                            }
                                            else -> {
                                                VideoListScrollable(items = searchResults) { item ->
                                                    keyboardController?.hide()
                                                    playerViewModel.loadVideo(item, searchResults)
                                                }
                                            }
                                        }
                                    }
                                }

                                if (recentList.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(20.dp))
                                    VideoRecentlyAddedSection(
                                        items = recentList,
                                        onItemClick = { item ->
                                            playerViewModel.loadVideo(item, recentList)
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                            }

                            when {
                                isLoading -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = Color(0xFFC70025))
                                    }
                                }
                                error != null -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Error: $error",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF8E8D8D)
                                        )
                                    }
                                }
                                videoList.isEmpty() -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No videos found",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF8E8D8D)
                                        )
                                    }
                                }
                                else -> {
                                    AllVideosSection(
                                        items = videoList,
                                        onItemClick = { item ->
                                            playerViewModel.loadVideo(item, videoList)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        bottomContentPadding = 40.dp,
                                        gridState = gridState
                                    )
                                }
                            }
                        }
                    }
                }

                if (playerMode == VideoPlayerMode.PORTRAIT) {
                    VideoPlayerOverlay(
                        viewModel = playerViewModel,
                        modifier = Modifier.fillMaxSize(),
                        recentList = recentList
                    )
                }

                if (playerMode == VideoPlayerMode.FLOATING) {
                    VideoPlayerOverlay(
                        viewModel = playerViewModel
                    )
                }

                if (playerMode == VideoPlayerMode.FULLSCREEN) {
                    VideoPlayerOverlay(
                        viewModel = playerViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}