package com.devfamily.sehara.ui.screens.music

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.devfamily.sehara.R
import com.devfamily.sehara.data.GenreItem
import com.devfamily.sehara.ui.components.MusicPlayerOverlay
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars

@Composable
fun GenreScreen(
    modifier: Modifier = Modifier,
    showInitialSplash: Boolean = true,
    onBackClick: () -> Unit = {},
    onGenreClick: (id: String, name: String) -> Unit = { _, _ -> },
    viewModel: GenreViewModel = viewModel(),
    playerViewModel: MusicPlayerViewModel
) {
    val genres by viewModel.genres.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val currentSong by playerViewModel.currentSong.collectAsState()
    val hasActiveSong = currentSong.id.isNotEmpty()
    var playerExpanded by remember { mutableStateOf(false) }
    var backEnabled by remember { mutableStateOf(true) }

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

    run {
        run {
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
                                painter = painterResource(id = R.drawable.ic_genre),
                                contentDescription = null,
                                modifier = Modifier.size(45.dp),
                                colorFilter = ColorFilter.tint(Color(0xFFFF3054))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Genres",
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
                            .padding(
                                bottom = if (hasActiveSong) {
                                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 132.dp
                                } else 40.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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
                                        .padding(horizontal = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(24.dp)
                                ) {
                                    genres.chunked(2).forEach { rowGenres ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = if (rowGenres.size == 1) {
                                                Arrangement.Center
                                            } else {
                                                Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
                                            }
                                        ) {
                                            rowGenres.forEach { genre ->
                                                GenreGridItem(
                                                    genre = genre,
                                                    onClick = { onGenreClick(genre.id, genre.name) }
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
fun GenreGridItem(
    genre: GenreItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val imageModel = remember(genre.thumbnailUrl, genre.id) {
        if (!genre.thumbnailUrl.isNullOrEmpty()) genre.thumbnailUrl else null
    }

    Column(
        modifier = Modifier
            .width(115.dp)
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
                .size(115.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.music_artist_splash_bg),
            placeholder = painterResource(id = R.drawable.music_artist_splash_bg),
            fallback = painterResource(id = R.drawable.music_artist_splash_bg)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = genre.name,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}