package com.devfamily.sehara.ui.screens.music

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devfamily.sehara.R
import com.devfamily.sehara.ui.components.CategoryCard
import com.devfamily.sehara.ui.components.MusicPlayerOverlay
import com.devfamily.sehara.ui.components.SearchBar
import kotlinx.coroutines.delay

@Composable
fun ArtistScreen(
    modifier: Modifier = Modifier,
    showInitialSplash: Boolean = true,
    onBackClick: () -> Unit = {},
    onArtistClick: (id: String, name: String) -> Unit = { _, _ -> },
    viewModel: ArtistViewModel = viewModel(),
    playerViewModel: MusicPlayerViewModel
) {
    var showSplash by rememberSaveable { mutableStateOf(showInitialSplash) }
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentSong by playerViewModel.currentSong.collectAsState()
    val hasActiveSong = currentSong.id.isNotEmpty()
    var playerExpanded by remember { mutableStateOf(false) }
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
            delay(1000)
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
            viewModel.searchArtists(searchQuery)
        }
    }

    BackHandler(enabled = playerExpanded) {
        playerExpanded = false
    }

    AnimatedContent(
        targetState = showSplash,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "artist_splash_transition"
    ) { isSplash ->
        if (isSplash) {
            Box(modifier = modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.music_artist_card),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = "Artist",
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
                        Text(
                            text = "Artist",
                            style = MaterialTheme.typography.displayMedium,
                            color = Color(0xFF999999)
                        )
                        Spacer(modifier = Modifier.size(36.dp))
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = if (hasActiveSong) 120.dp else 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Search artists"
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
                                        searchResults.forEach { artist ->
                                            Text(
                                                text = artist.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFF333333),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        onArtistClick(artist.id, artist.name)
                                                        searchQuery = ""
                                                        viewModel.clearSearch()
                                                    }
                                                    .padding(horizontal = 16.dp, vertical = 12.dp)
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
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color(0xFFC70025))
                                }
                            }
                            error != null -> {
                                Text(
                                    text = "Error: $error",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Red,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            else -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    artists.chunked(2).forEach { rowArtists ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                                        ) {
                                            rowArtists.forEachIndexed { index, artist ->
                                                val shape = if (index == 0) {
                                                    RoundedCornerShape(
                                                        topStart = 0.dp,
                                                        topEnd = 32.dp,
                                                        bottomEnd = 32.dp,
                                                        bottomStart = 0.dp
                                                    )
                                                } else {
                                                    RoundedCornerShape(
                                                        topStart = 32.dp,
                                                        topEnd = 0.dp,
                                                        bottomEnd = 0.dp,
                                                        bottomStart = 32.dp
                                                    )
                                                }

                                                CategoryCard(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .border(
                                                            width = 1.dp,
                                                            color = Color(0xFFC70025),
                                                            shape = shape
                                                        ),
                                                    shape = shape,
                                                    backgroundImage = R.drawable.music_genre_splash_bg,
                                                    backgroundUrl = artist.thumbnailUrl,
                                                    label = "",
                                                    onClick = { onArtistClick(artist.id, artist.name) },
                                                    bottomStartContent = {
                                                        Row(modifier = Modifier.fillMaxWidth()) {
                                                            Spacer(modifier = Modifier.weight(1f))
                                                            Text(
                                                                text = artist.name,
                                                                color = Color.White,
                                                                style = MaterialTheme.typography.titleLarge
                                                            )
                                                        }
                                                    }
                                                )

                                                if (index == 0 && rowArtists.size == 2) {
                                                    Spacer(modifier = Modifier.width(16.dp))
                                                }
                                            }

                                            if (rowArtists.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
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