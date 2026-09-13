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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.devfamily.sehara.R
import com.devfamily.sehara.data.ArtistItem
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
            delay(1000)
            backEnabled = true
        }
    }

    LaunchedEffect(Unit) {
        if (showInitialSplash) {
            delay(800)
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
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
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
                        .background(Color(0xFF201F1F))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFF161616),
                                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
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
                                .clickable(enabled = backEnabled) { onBackClick() },
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_artist),
                                contentDescription = null,
                                modifier = Modifier.size(45.dp),
                                colorFilter = ColorFilter.tint(Color(0xFFFF3054))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Artists",
                                style = MaterialTheme.typography.displayMedium.copy(letterSpacing = 0.sp),
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.size(28.dp))
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = if (hasActiveSong) 100.dp else 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

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
                                        searchResults.forEach { artist ->
                                            Text(
                                                text = artist.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White,
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    artists.chunked(3).forEach { rowArtists ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                                        ) {
                                            rowArtists.forEach { artist ->
                                                ArtistGridItem(
                                                    artist = artist,
                                                    onClick = { onArtistClick(artist.id, artist.name) }
                                                )
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

@Composable
fun ArtistGridItem(
    artist: ArtistItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val imageModel = remember(artist.thumbnailUrl, artist.id) {
        if (!artist.thumbnailUrl.isNullOrEmpty()) artist.thumbnailUrl else null
    }

    Column(
        modifier = Modifier
            .width(100.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, color = Color(0xFFC70025)),
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = imageModel,
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_unknown_artist),
            placeholder = painterResource(id = R.drawable.ic_unknown_artist),
            fallback = painterResource(id = R.drawable.ic_unknown_artist)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = artist.name,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}