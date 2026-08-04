package com.devfamily.sehara.ui.screens.docs

import android.widget.FrameLayout
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.devfamily.sehara.R
import com.devfamily.sehara.data.RetrofitClient
import com.github.barteksc.pdfviewer.PDFView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL

@Composable
fun DocumentViewerScreen(
    modifier: Modifier = Modifier,
    documentId: String,
    documentName: String,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var pdfBytes by remember { mutableStateOf<ByteArray?>(null) }

    val streamUrl = "${RetrofitClient.BASE_URL}api/stream/$documentId"

    LaunchedEffect(documentId) {
        isLoading = true
        hasError = false
        try {
            val bytes = withContext(Dispatchers.IO) {
                URL(streamUrl).openStream().use { it.readBytes() }
            }
            pdfBytes = bytes
        } catch (e: IOException) {
            hasError = true
        } finally {
            isLoading = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
                .navigationBarsPadding()
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
                        .clickable { onBackClick() }
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Docs",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color(0xFF999999)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_docs),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        colorFilter = ColorFilter.tint(Color(0xFF9F001E))
                    )
                }
                Spacer(modifier = Modifier.size(36.dp))
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    hasError -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Unable to load document",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF8E8D8D)
                            )
                        }
                    }
                    pdfBytes != null -> {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                FrameLayout(ctx).apply {
                                    val pdfView = PDFView(ctx, null)
                                    addView(
                                        pdfView,
                                        FrameLayout.LayoutParams(
                                            FrameLayout.LayoutParams.MATCH_PARENT,
                                            FrameLayout.LayoutParams.MATCH_PARENT
                                        )
                                    )
                                    pdfView.fromBytes(pdfBytes)
                                        .enableSwipe(true)
                                        .swipeHorizontal(false)
                                        .enableDoubletap(true)
                                        .defaultPage(0)
                                        .load()
                                }
                            }
                        )
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFC70025))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_docs),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color(0xFFC70025))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = documentName,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF333333),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}