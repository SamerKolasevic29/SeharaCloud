package com.devfamily.sehara.ui.screens.music

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devfamily.sehara.R
import com.devfamily.sehara.ui.components.AllTracksSection
import com.devfamily.sehara.ui.components.MusicPlayerOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FavoriteScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    viewModel: FavoriteViewModel = viewModel(),
    playerViewModel: MusicPlayerViewModel
) {
    val favorites by viewModel.favorites.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val sortAscending by viewModel.sortAscending.collectAsState()
    val sortByArtist by viewModel.sortByArtist.collectAsState()

    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val hasActiveSong = currentSong.id.isNotEmpty()
    var playerExpanded by remember { mutableStateOf(false) }
    var backEnabled by remember { mutableStateOf(true) }

    val tracksListState = rememberLazyListState()
    val uiScope = rememberCoroutineScope()

    LaunchedEffect(playerExpanded) {
        if (!playerExpanded && hasActiveSong) {
            backEnabled = false
            delay(400)
            backEnabled = true
        }
    }

    BackHandler(enabled = playerExpanded) {
        playerExpanded = false
    }

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
                        painter = painterResource(id = R.drawable.ic_fav_filled),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        colorFilter = ColorFilter.tint(Color(0xFFFF3054))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Favorites",
                        style = MaterialTheme.typography.displayMedium.copy(letterSpacing = 0.sp),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.size(28.dp))
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFC70025))
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: $error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Red
                        )
                    }
                }
                favorites.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No favorites yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8E8D8D)
                        )
                    }
                }
                else -> {
                    AllTracksSection(
                        title = "Favorite tracks",
                        items = favorites,
                        onItemClick = { song ->
                            val isFirstSong = !hasActiveSong
                            playerViewModel.loadSongFromQueue(song, favorites)
                            if (isFirstSong) playerExpanded = true
                        },
                        ascending = sortAscending,
                        sortByArtist = sortByArtist,
                        onToggleOrder = {
                            viewModel.toggleOrder()
                            uiScope.launch { tracksListState.scrollToItem(0) }
                        },
                        onToggleSortBy = {
                            viewModel.toggleSortBy()
                            uiScope.launch { tracksListState.scrollToItem(0) }
                        },
                        modifier = Modifier.fillMaxSize(),
                        bottomContentPadding = if (hasActiveSong) {
                            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 132.dp
                        } else 40.dp,
                        currentSongId = currentSong.id,
                        isPlaying = isPlaying,
                        listState = tracksListState
                    )
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