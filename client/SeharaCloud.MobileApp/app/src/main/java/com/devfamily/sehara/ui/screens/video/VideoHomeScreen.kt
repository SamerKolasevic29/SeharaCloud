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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devfamily.sehara.R
import com.devfamily.sehara.ui.components.CategoryCard
import com.devfamily.sehara.ui.components.SearchBar
import com.devfamily.sehara.ui.components.VideoListScrollable
import com.devfamily.sehara.ui.components.VideoPlayerOverlay
import kotlinx.coroutines.delay

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
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val keyboardController = LocalSoftwareKeyboardController.current



    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            viewModel.clearSearch()
        } else {
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF201F1F))
                        .navigationBarsPadding()
                ) {
                    if (playerMode == VideoPlayerMode.PORTRAIT) {
                        Box(modifier = Modifier.statusBarsPadding()) {
                            VideoPlayerOverlay(
                                viewModel = playerViewModel,
                                modifier = Modifier.fillMaxWidth(),
                                recentList = recentList
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFF161616),
                                shape = RoundedCornerShape(bottomStart = 25.dp, bottomEnd = 25.dp)
                            )
                            .then(
                                if (playerMode != VideoPlayerMode.PORTRAIT) Modifier.statusBarsPadding() else Modifier
                            )
                            .padding(
                                start = 24.dp,
                                top = if (playerMode == VideoPlayerMode.PORTRAIT) 16.dp else 24.dp,
                                end = 24.dp,
                                bottom = 16.dp
                            ),
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
                            .verticalScroll(rememberScrollState()),
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

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.width(332.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFC70025),
                                        shape = RoundedCornerShape(50.dp)
                                    )
                                    .background(
                                        color = if (selectedTab == 0) Color(0xFFC70025) else Color(0xFF201F1F),
                                        shape = RoundedCornerShape(50.dp)
                                    )
                                    .clip(RoundedCornerShape(50.dp))
                                    .clickable { selectedTab = 0 }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Recent",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (selectedTab == 0) Color.White else Color(0xFFC70025)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFC70025),
                                        shape = RoundedCornerShape(50.dp)
                                    )
                                    .background(
                                        color = if (selectedTab == 1) Color(0xFFC70025) else Color(0xFF201F1F),
                                        shape = RoundedCornerShape(50.dp)
                                    )
                                    .clip(RoundedCornerShape(50.dp))
                                    .clickable { selectedTab = 1 }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "All",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (selectedTab == 1) Color.White else Color(0xFFC70025)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

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
                            selectedTab == 0 -> {
                                if (recentList.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .width(332.dp)
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No recent videos",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF8E8D8D)
                                        )
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier
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
                                    ) {
                                        VideoListScrollable(items = recentList) { item ->
                                            playerViewModel.loadVideo(item, recentList)
                                        }
                                    }
                                }
                            }
                            selectedTab == 1 -> {
                                if (videoList.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .width(332.dp)
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No videos found",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF8E8D8D)
                                        )
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier
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
                                    ) {
                                        VideoListScrollable(items = videoList) { item ->
                                            playerViewModel.loadVideo(item, videoList)
                                        }
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