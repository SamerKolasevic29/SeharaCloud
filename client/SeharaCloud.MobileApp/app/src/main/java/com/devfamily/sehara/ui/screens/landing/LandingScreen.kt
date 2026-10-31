package com.devfamily.sehara.ui.screens.landing

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devfamily.sehara.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LandingScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val activity = context as? Activity
        val previous = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            activity?.requestedOrientation = previous
        }
    }

    val titleAlpha = remember { Animatable(0f) }
    val titleTranslationY = remember { Animatable(40f) }

    val platformAlpha = remember { Animatable(0f) }
    val platformTranslationY = remember { Animatable(40f) }

    val footerAlpha = remember { Animatable(0f) }
    val footerTranslationY = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        launch {
            titleAlpha.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
        }
        launch {
            titleTranslationY.animateTo(0f, tween(800, easing = FastOutSlowInEasing))
        }

        delay(300)

        launch {
            platformAlpha.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
        }
        launch {
            platformTranslationY.animateTo(0f, tween(800, easing = FastOutSlowInEasing))
        }

        delay(400)

        launch {
            footerAlpha.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
        }
        launch {
            footerTranslationY.animateTo(0f, tween(800, easing = FastOutSlowInEasing))
        }

        delay(1200)
        onTimeout()
    }

    val gradientBrush = Brush.linearGradient(
        colorStops = arrayOf(
            0.30f to Color(0xFF3A1012),
            0.69f to Color(0xFFC70025),
            1.00f to Color(0xFFFF607C)
        ),
        start = Offset(0f, 0f),
        end = Offset(0f, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 200.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Sehara",
                color = Color.White,
                fontSize = 78.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha.value
                    translationY = titleTranslationY.value
                }
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.graphicsLayer {
                    alpha = platformAlpha.value
                    translationY = platformTranslationY.value
                }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier.size(68.dp),
                    colorFilter = ColorFilter.tint(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "platform",
                    color = Color(0xFFFF607C),
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = "Everything you need",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 200.dp)
                .graphicsLayer {
                    alpha = footerAlpha.value
                    translationY = footerTranslationY.value
                }
        )
    }
}