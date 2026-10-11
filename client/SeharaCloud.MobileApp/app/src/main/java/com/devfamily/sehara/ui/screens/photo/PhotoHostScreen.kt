package com.devfamily.sehara.ui.screens.photo

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devfamily.sehara.R

@Composable
private fun PhotoHeader(onBackClick: () -> Unit) {
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
                .clickable { onBackClick() },
            colorFilter = ColorFilter.tint(Color.White)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_photo),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                colorFilter = ColorFilter.tint(Color(0xFFFF3054))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Photos",
                style = MaterialTheme.typography.displayMedium.copy(letterSpacing = 0.sp),
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.size(28.dp))
    }
}

@Composable
fun PhotoHostScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    viewModel: PhotoViewModel = viewModel()
) {
    var selectedIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var isImmersive by rememberSaveable { mutableStateOf(false) }

    val closeViewer = {
        selectedIndex = null
        isImmersive = false
    }

    BackHandler(enabled = selectedIndex != null) {
        closeViewer()
    }

    val viewerOpen = selectedIndex != null
    val hideHeader = viewerOpen && isImmersive

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (hideHeader) Color.Black else Color(0xFF201F1F))
            .navigationBarsPadding()
            .then(if (hideHeader) Modifier.statusBarsPadding() else Modifier)
    ) {
        AnimatedVisibility(
            visible = !hideHeader,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            PhotoHeader(
                onBackClick = {
                    if (viewerOpen) closeViewer() else onBackClick()
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            PhotoScreen(
                modifier = Modifier.fillMaxSize(),
                onPhotoClick = { index -> selectedIndex = index },
                viewModel = viewModel
            )

            selectedIndex?.let { index ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) { detectTapGestures { } }
                ) {
                    PhotoViewerScreen(
                        modifier = Modifier.fillMaxSize(),
                        viewModel = viewModel,
                        initialIndex = index,
                        isImmersive = isImmersive,
                        onImmersiveChange = { isImmersive = it },
                        onBackClick = { closeViewer() }
                    )
                }
            }
        }
    }
}