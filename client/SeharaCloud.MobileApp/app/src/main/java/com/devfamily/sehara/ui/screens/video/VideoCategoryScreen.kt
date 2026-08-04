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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devfamily.sehara.R
import com.devfamily.sehara.ui.components.SearchBar
import com.devfamily.sehara.ui.components.VideoListRow
import com.devfamily.sehara.ui.components.VideoPlayerOverlay
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding

private data class VideoCategoryUi(
    val title: String,
    val splashBg: Int,
    val emptyMessage: String
)

private fun VideoCategory.toUi(): VideoCategoryUi = when (this) {
    VideoCategory.MOVIE -> VideoCategoryUi(
        title = "Movie",
        splashBg = R.drawable.video_movie_splash_bg,
        emptyMessage = "No movies found"
    )
    VideoCategory.DOCUMENTARY -> VideoCategoryUi(
        title = "Documentary",
        splashBg = R.drawable.video_movie_splash_bg,
        emptyMessage = "No documentaries found"
    )
    VideoCategory.OTHER -> VideoCategoryUi(
        title = "Other",
        splashBg = R.drawable.video_movie_splash_bg,
        emptyMessage = "No videos found"
    )
}

@Composable
fun VideoCategoryScreen(
    modifier: Modifier = Modifier,
    category: VideoCategory,
    showInitialSplash: Boolean = true,
    onBackClick: () -> Unit = {},
    viewModel: VideoCategoryViewModel = viewModel(),
    playerViewModel: VideoPlayerViewModel = viewModel()
) {
    var showSplash by rememberSaveable { mutableStateOf(showInitialSplash) }
    val videoList by viewModel.videoList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val playerMode by playerViewModel.playerMode.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val ui = category.toUi()

    LaunchedEffect(category) {
        viewModel.loadCategory(category)
    }

    LaunchedEffect(Unit) {
        if (showInitialSplash) {
            delay(2000)
            showSplash = false
        } else {
            showSplash = false
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            viewModel.clearSearch()
        } else {
            delay(300)
            viewModel.search(searchQuery)
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
        label = "video_category_splash_transition"
    ) { isSplash ->
        if (isSplash) {
            Box(modifier = modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Image(
                    painter = painterResource(id = ui.splashBg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = ui.title,
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp)
                )
            }
        } else {
            Box(modifier = modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    if (playerMode == VideoPlayerMode.PORTRAIT) {
                        VideoPlayerOverlay(
                            viewModel = playerViewModel,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(
                                start = 32.dp,
                                top = if (playerMode == VideoPlayerMode.PORTRAIT) 16.dp else 32.dp,
                                end = 32.dp,
                                bottom = 12.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { onBackClick() }
                        )
                        Text(
                            text = ui.title,
                            style = MaterialTheme.typography.displayMedium,
                            color = Color(0xFF999999)
                        )
                        Spacer(modifier = Modifier.size(36.dp))
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState(), enabled = searchQuery.isBlank())
                            .padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

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
                                        color = Color.White,
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
                                        searchResults.forEach { item ->
                                            VideoListRow(
                                                item = item,
                                                onClick = {
                                                    keyboardController?.hide()
                                                    playerViewModel.loadVideo(item, searchResults)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        when {
                            isLoading -> {
                                Box(
                                    modifier = Modifier
                                        .width(332.dp)
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color(0xFFC70025))
                                }
                            }
                            error != null -> {
                                Box(
                                    modifier = Modifier
                                        .width(332.dp)
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
                                        .width(332.dp)
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ui.emptyMessage,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF8E8D8D)
                                    )
                                }
                            }
                            else -> {
                                Column(
                                    modifier = Modifier
                                        .width(332.dp)
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFC70025),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        .background(
                                            color = Color.White,
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                ) {
                                    videoList.forEach { item ->
                                        VideoListRow(
                                            item = item,
                                            onClick = {
                                                playerViewModel.loadVideo(item, videoList)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
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