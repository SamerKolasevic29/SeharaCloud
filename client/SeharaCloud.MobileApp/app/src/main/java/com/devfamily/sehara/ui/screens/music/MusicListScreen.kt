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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devfamily.sehara.R
import com.devfamily.sehara.ui.components.AllTracksSection
import com.devfamily.sehara.ui.components.MusicPlayerOverlay
import kotlinx.coroutines.delay

@Composable
fun MusicListScreen(
    filter: MusicFilter,
    onBackClick: () -> Unit = {},
    viewModel: MusicListViewModel = viewModel(),
    playerViewModel: MusicPlayerViewModel
) {
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentSong by playerViewModel.currentSong.collectAsState()
    val hasActiveSong = currentSong.id.isNotEmpty()
    var playerExpanded by remember { mutableStateOf(false) }
    var backEnabled by remember { mutableStateOf(true) }

    val artistName = when (filter) {
        is MusicFilter.ByGenre -> filter.name
        is MusicFilter.ByArtist -> filter.name
    }

    LaunchedEffect(filter) {
        viewModel.load(filter)
    }

    LaunchedEffect(playerExpanded) {
        if (!playerExpanded) {
            backEnabled = false
            delay(2000)
            backEnabled = true
        }
    }

    BackHandler(enabled = playerExpanded) {
        playerExpanded = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        painter = painterResource(id = R.drawable.ic_music),
                        contentDescription = null,
                        modifier = Modifier.size(45.dp),
                        colorFilter = ColorFilter.tint(Color(0xFFFF3054))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Music",
                        style = MaterialTheme.typography.displayMedium.copy(letterSpacing = 0.sp),
                        color = Color.White
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.ic_meni),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
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
                songs.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No songs found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8E8D8D)
                        )
                    }
                }
                else -> {
                    AllTracksSection(
                        title = "$artistName's tracks:",
                        items = songs,
                        onItemClick = { song ->
                            val isFirstSong = !hasActiveSong
                            playerViewModel.loadSongFromQueue(song, songs)
                            if (isFirstSong) playerExpanded = true
                        },
                        modifier = Modifier
                            .fillMaxSize(),
                        bottomContentPadding = if (hasActiveSong) 100.dp else 0.dp
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