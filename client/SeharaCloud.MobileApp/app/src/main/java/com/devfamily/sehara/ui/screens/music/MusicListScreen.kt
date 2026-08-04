package com.devfamily.sehara.ui.screens.music

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devfamily.sehara.R
import com.devfamily.sehara.ui.components.MusicListRow
import com.devfamily.sehara.ui.components.MusicPlayerOverlay
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding

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

    val title = when (filter) {
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
                .background(Color.White)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF999999)
                )
                Spacer(modifier = Modifier.size(48.dp))
            }

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFC70025))
                    }
                }
                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Error: $error", color = Color.Red)
                    }
                }
                songs.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No songs found", color = Color(0xFF8E8D8D))
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .width(332.dp)
                            .padding(bottom = if (hasActiveSong) 120.dp else 24.dp)
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
                        items(songs) { song ->
                            MusicListRow(item = song, onClick = {
                                val isFirstSong = !hasActiveSong
                                playerViewModel.loadSongFromQueue(song, songs)
                                if (isFirstSong) playerExpanded = true
                            })
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