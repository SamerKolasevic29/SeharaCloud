package com.devfamily.sehara.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay

@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    contentAlignment: Alignment = Alignment.CenterStart,
    gap: Float = 600f
) {
    var textWidth by remember { mutableStateOf(0) }
    var containerWidth by remember { mutableStateOf(0) }
    val offset = remember { Animatable(0f) }

    val shouldScroll = textWidth > containerWidth

    val horizontalAlignment = remember(contentAlignment) {
        when (contentAlignment) {
            Alignment.Center, Alignment.CenterHorizontally, Alignment.BottomCenter, Alignment.TopCenter -> Alignment.CenterHorizontally
            Alignment.CenterEnd, Alignment.BottomEnd, Alignment.TopEnd -> Alignment.End
            else -> Alignment.Start
        }
    }

    LaunchedEffect(text, textWidth, containerWidth) {
        if (!shouldScroll) {
            offset.snapTo(0f)
            return@LaunchedEffect
        }

        offset.snapTo(0f)
        delay(1000L)

        while (true) {
            offset.animateTo(
                targetValue = -(textWidth + gap),
                animationSpec = tween(
                    durationMillis = ((textWidth + gap) * 12).toInt(),
                    easing = LinearEasing
                )
            )
            offset.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .onGloballyPositioned { containerWidth = it.size.width },
        contentAlignment = contentAlignment
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(
                    align = if (shouldScroll) Alignment.Start else horizontalAlignment,
                    unbounded = true
                )
                .offset { IntOffset(offset.value.toInt(), 0) }
        ) {
            Text(
                text = text,
                style = style,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.onGloballyPositioned {
                    textWidth = it.size.width
                }
            )

            if (shouldScroll) {
                Text(
                    text = text,
                    style = style,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .offset { IntOffset((textWidth + gap).toInt(), 0) }
                )
            }
        }
    }
}