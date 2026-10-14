package com.devfamily.sehara.ui.screens.music

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devfamily.sehara.R
import com.devfamily.sehara.ui.components.AllTracksSection
import com.devfamily.sehara.ui.components.MusicPlayerOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun MusicListScreen(
    filter: MusicFilter,
    onBackClick: () -> Unit = {},
    onArtistClick: () -> Unit = {},
    onGenreClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    viewModel: MusicListViewModel = viewModel(),
    playerViewModel: MusicPlayerViewModel
) {
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val hasActiveSong = currentSong.id.isNotEmpty()
    var playerExpanded by remember { mutableStateOf(false) }
    var backEnabled by remember { mutableStateOf(true) }
    var isMenuOpen by remember { mutableStateOf(false) }

    val blurRadius by animateDpAsState(
        targetValue = if (isMenuOpen) 16.dp else 0.dp,
        label = "menu_blur"
    )

    val artistName = when (filter) {
        is MusicFilter.ByGenre -> filter.name
        is MusicFilter.ByArtist -> filter.name
    }

    val cameFromArtist = filter is MusicFilter.ByArtist

    var sortAscending by rememberSaveable { mutableStateOf(true) }
    var sortByArtist by rememberSaveable { mutableStateOf(false) }
    val tracksListState = rememberLazyListState()
    val uiScope = rememberCoroutineScope()
    val sortedSongs = remember(songs, sortAscending, sortByArtist) {
        val collator = java.text.Collator.getInstance(java.util.Locale("bs")).apply {
            strength = java.text.Collator.TERTIARY
        }
        val textComparator = Comparator<String> { a, b -> collator.compare(a, b) }
        val comparator = if (sortByArtist) {
            compareBy<com.devfamily.sehara.data.SongItem, String>(textComparator) { it.artist ?: "" }
                .thenBy(textComparator) { it.title ?: it.filename }
        } else {
            compareBy<com.devfamily.sehara.data.SongItem, String>(textComparator) { it.title ?: it.filename }
        }
        songs.sortedWith(if (sortAscending) comparator else comparator.reversed())
    }
    LaunchedEffect(filter) {
        viewModel.load(filter)
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .blur(blurRadius)
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
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { isMenuOpen = true },
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
                        items = sortedSongs,
                        onItemClick = { song ->
                            val isFirstSong = !hasActiveSong
                            playerViewModel.loadSongFromQueue(song, sortedSongs)
                            if (isFirstSong) playerExpanded = true
                        },
                        ascending = sortAscending,
                        sortByArtist = sortByArtist,
                        onToggleOrder = {
                            sortAscending = !sortAscending
                            uiScope.launch { tracksListState.scrollToItem(0) }
                        },
                        onToggleSortBy = if (cameFromArtist) null else {
                            {
                                sortByArtist = !sortByArtist
                                uiScope.launch { tracksListState.scrollToItem(0) }
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize(),
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

        if (isMenuOpen) {
            Dialog(
                onDismissRequest = { isMenuOpen = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { isMenuOpen = false }
                        .statusBarsPadding()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 300.dp, height = 265.dp)
                            .background(
                                color = Color.Black,
                                shape = RoundedCornerShape(30.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { }
                            .padding(20.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Browse music by:",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.ic_close),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { isMenuOpen = false },
                                    colorFilter = ColorFilter.tint(Color(0xFFB0B0B0))
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        isMenuOpen = false
                                        onArtistClick()
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_artist),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Artists",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            HorizontalDivider(
                                color = Color(0xFF767676),
                                thickness = 1.dp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        isMenuOpen = false
                                        onGenreClick()
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_genre),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Genres",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            HorizontalDivider(
                                color = Color(0xFF767676),
                                thickness = 1.dp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        isMenuOpen = false
                                        onFavoriteClick()
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_fav_filled),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = ColorFilter.tint(Color(0xFFFF3054))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Favorites",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        if (hasActiveSong) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .blur(blurRadius)
            ) {
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