package com.devfamily.sehara.ui.screens.music

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Constraints
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devfamily.sehara.R
import com.devfamily.sehara.ui.components.AllTracksSection
import com.devfamily.sehara.ui.components.MusicListScrollable
import com.devfamily.sehara.ui.components.MusicPlayerOverlay
import com.devfamily.sehara.ui.components.RecentlyAddedSection
import com.devfamily.sehara.ui.components.SearchBar
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.animate
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
@Composable
fun MusicHomeScreen(
    modifier: Modifier = Modifier,
    showInitialSplash: Boolean = true,
    onBackClick: () -> Unit = {},
    onArtistClick: () -> Unit = {},
    onGenreClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    viewModel: MusicViewModel = viewModel(),
    playerViewModel: MusicPlayerViewModel
) {
    var showSplash by rememberSaveable { mutableStateOf(false) }
    val musicList by viewModel.musicList.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var playerExpanded by remember { mutableStateOf(false) }
    var isMenuOpen by remember { mutableStateOf(false) }

    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val hasActiveSong = currentSong.id.isNotEmpty()

    val blurRadius by animateDpAsState(
        targetValue = if (isMenuOpen) 16.dp else 0.dp,
        label = "menu_blur"
    )

    val recentList by viewModel.recentList.collectAsState()
    var backEnabled by remember { mutableStateOf(true) }
    val keyboardController = LocalSoftwareKeyboardController.current

    var collapseOffset by remember { mutableFloatStateOf(0f) }
    var topHeightPx by remember { mutableIntStateOf(0) }
    val tracksListState = rememberLazyListState()
    val uiScope = rememberCoroutineScope()
    val orientationContext = LocalContext.current

    DisposableEffect(Unit) {
        val activity = orientationContext as? Activity
        val previous = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            activity?.requestedOrientation = previous
        }
    }
    var sortAscending by rememberSaveable { mutableStateOf(true) }
    var sortByArtist by rememberSaveable { mutableStateOf(false) }
    val sortedMusicList = remember(musicList, sortAscending, sortByArtist) {
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
        musicList.sortedWith(if (sortAscending) comparator else comparator.reversed())
    }
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

    LaunchedEffect(playerExpanded) {
        if (!playerExpanded && hasActiveSong) {
            backEnabled = false
            delay(400)
            backEnabled = true
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            viewModel.clearSearch()
        } else {
            collapseOffset = 0f
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
            Box(modifier = modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
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
            BackHandler {
                if (playerExpanded) {
                    playerExpanded = false
                } else if (backEnabled) {
                    onBackClick()
                }
            }
            Box(modifier = modifier.fillMaxSize()) {
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
                                .clickable(enabled = backEnabled) {
                                    onBackClick()
                                },
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
                                visible = searchQuery.trim().length >= 2,
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
                                            MusicListScrollable(
                                                items = searchResults
                                            ) { item ->
                                                val isFirstSong = !hasActiveSong
                                                playerViewModel.loadSongFromSearch(item)
                                                keyboardController?.hide()
                                                if (isFirstSong) playerExpanded = true
                                            }
                                        }
                                    }
                                }
                            }

                            if (isLoading && recentList.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color(0xFFFF3054))
                                }
                            } else if (recentList.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(20.dp))
                                RecentlyAddedSection(
                                    items = recentList,
                                    onItemClick = { item ->
                                        val isFirstSong = !hasActiveSong
                                        playerViewModel.loadSongFromQueue(item, recentList)
                                        if (isFirstSong) playerExpanded = true
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (isLoading && musicList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFFFF3054))
                            }
                        } else if (musicList.isNotEmpty()) {
                            AllTracksSection(
                                items = sortedMusicList,
                                onItemClick = { item ->
                                    val isFirstSong = !hasActiveSong
                                    playerViewModel.loadSongFromQueue(item, sortedMusicList)
                                    if (isFirstSong) playerExpanded = true
                                },
                                ascending = sortAscending,
                                sortByArtist = sortByArtist,
                                onToggleOrder = {
                                    sortAscending = !sortAscending
                                    uiScope.launch { tracksListState.scrollToItem(0) }
                                },
                                onToggleSortBy = {
                                    sortByArtist = !sortByArtist
                                    uiScope.launch { tracksListState.scrollToItem(0) }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                bottomContentPadding = if (hasActiveSong) {
                                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 132.dp
                                } else 40.dp,
                                currentSongId = currentSong.id,
                                isPlaying = isPlaying,
                                listState = tracksListState,
                                onHeaderSwipeDown = {
                                    uiScope.launch {
                                        animate(
                                            initialValue = collapseOffset,
                                            targetValue = 0f
                                        ) { value, _ -> collapseOffset = value }
                                    }
                                }
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
                                            modifier = Modifier.size(24.dp),
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
                                            modifier = Modifier.size(24.dp),
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
                    Box(modifier = Modifier.align(Alignment.BottomCenter).blur(blurRadius)) {
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