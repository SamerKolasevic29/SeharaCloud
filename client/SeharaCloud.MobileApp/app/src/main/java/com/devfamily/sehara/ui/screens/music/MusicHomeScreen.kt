package com.devfamily.sehara.ui.screens.music

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devfamily.sehara.R
import com.devfamily.sehara.ui.components.CategoryCard
import com.devfamily.sehara.ui.components.MusicListRow
import com.devfamily.sehara.ui.components.MusicPlayerOverlay
import com.devfamily.sehara.ui.components.SearchBar
import kotlinx.coroutines.delay
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun MusicHomeScreen(
    modifier: Modifier = Modifier,
    showInitialSplash: Boolean = true,
    onBackClick: () -> Unit = {},
    onArtistClick: () -> Unit = {},
    onGenreClick: () -> Unit = {},
    viewModel: MusicViewModel = viewModel(),
    playerViewModel: MusicPlayerViewModel
) {
    var showSplash by rememberSaveable { mutableStateOf(showInitialSplash) }
    val musicList by viewModel.musicList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var playerExpanded by remember { mutableStateOf(false) }

    val currentSong by playerViewModel.currentSong.collectAsState()
    val hasActiveSong = currentSong.id.isNotEmpty()

    val recentList = musicList.take(5)
    var backEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(playerExpanded) {
        if (!playerExpanded) {
            backEnabled = false
            delay(2000)
            backEnabled = true
        }
    }

    LaunchedEffect(Unit) {
        if (showInitialSplash) {
            delay(500)
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
            viewModel.searchMusic(searchQuery)
        }
    }


    AnimatedContent(
        targetState = showSplash,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "splash_transition"
    ) { isSplash ->
        if (isSplash) {
            Box(modifier = modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.music_splash_bg),
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
                        painter = painterResource(id = R.drawable.ic_music),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Music",
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White
                    )
                }
            }
        } else {
            BackHandler(enabled = playerExpanded) {
                playerExpanded = false
            }
            Box(modifier = modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(start = 32.dp, top = 32.dp, end = 32.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable(enabled = backEnabled) { onBackClick() }
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Music",
                                style = MaterialTheme.typography.displayMedium,
                                color = Color(0xFF999999)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Image(
                                painter = painterResource(id = R.drawable.ic_music),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp),
                                colorFilter = ColorFilter.tint(Color(0xFF9F001E))
                            )
                        }
                        Spacer(modifier = Modifier.size(36.dp))
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState(), enabled = searchQuery.isBlank())
                            .padding(bottom = if (hasActiveSong) 120.dp else 24.dp),
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
                                            MusicListRow(item = item, onClick = {
                                                val isFirstSong = !hasActiveSong
                                                playerViewModel.loadSong(
                                                    item.id,
                                                    item.title ?: item.filename,
                                                    item.artist ?: "Unknown Artist",
                                                    (item.durationSec ?: 0) * 1000L
                                                )
                                                if (isFirstSong) playerExpanded = true
                                            })
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Box(modifier = Modifier.width(332.dp)) {
                            Text(
                                text = "Recent",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFB30021)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

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
                            recentList.isEmpty() -> {
                                Box(
                                    modifier = Modifier
                                        .width(332.dp)
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No recent music",
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
                                    recentList.forEach { item ->
                                        MusicListRow(item = item, onClick = {
                                            val isFirstSong = !hasActiveSong
                                            playerViewModel.loadSongFromQueue(item, recentList)
                                            if (isFirstSong) playerExpanded = true
                                        })
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Box(modifier = Modifier.width(332.dp)) {
                            Text(
                                text = "All",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFB30021)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

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
                            musicList.isEmpty() -> {
                                Box(
                                    modifier = Modifier
                                        .width(332.dp)
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No music found",
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
                                    musicList.forEach { item ->
                                        MusicListRow(item = item, onClick = {
                                            val isFirstSong = !hasActiveSong
                                            playerViewModel.loadSongFromQueue(item, musicList)
                                            if (isFirstSong) playerExpanded = true
                                        })
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Box(modifier = Modifier.width(332.dp)) {
                            Text(
                                text = "More Categories",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFB30021)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CategoryCard(
                                modifier = Modifier.weight(1f),
                                backgroundImage = R.drawable.music_artist_card,
                                label = "Artist",
                                onClick = onArtistClick
                            )
                            CategoryCard(
                                modifier = Modifier.weight(1f),
                                backgroundImage = R.drawable.music_genre_card,
                                label = "Genre",
                                onClick = onGenreClick
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                if (hasActiveSong) {
                    Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                        MusicPlayerOverlay(
                            viewModel = playerViewModel,
                            isExpanded = playerExpanded,
                            onExpandChange = { playerExpanded = it },
                            onClose = {
                                playerViewModel.closePlayer()
                                playerExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}