package com.devfamily.sehara.ui.screens.docs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devfamily.sehara.R
import com.devfamily.sehara.data.DocumentItem
import com.devfamily.sehara.ui.components.SearchBar
import kotlinx.coroutines.delay

@Composable
private fun DocRow(
    item: DocumentItem,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF201F1F)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_docs),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    colorFilter = ColorFilter.tint(Color(0xFFFF3054))
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = item.title?.takeIf { it.isNotBlank() } ?: "Untitled",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = Color(0xFF767676),
                thickness = 0.5.dp
            )
        }
    }
}

@Composable
fun DocumentsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onDocumentClick: (DocumentItem) -> Unit = {},
    viewModel: DocumentsViewModel = viewModel()
) {
    val docsList by viewModel.docsList.collectAsState()
    val recentList by viewModel.recentList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            viewModel.clearSearch()
        } else {
            delay(300)
            viewModel.searchDocuments(searchQuery)
        }
    }

    val currentList = if (selectedTab == 0) recentList else docsList

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF201F1F))
                .navigationBarsPadding()
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
                        .clickable { onBackClick() },
                    colorFilter = ColorFilter.tint(Color.White)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_docs),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        colorFilter = ColorFilter.tint(Color(0xFFFF3054))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Docs",
                        style = MaterialTheme.typography.displayMedium.copy(letterSpacing = 0.sp),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.size(28.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it }
                    )
                }

                item {
                    AnimatedVisibility(
                        visible = searchQuery.isNotBlank(),
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
                                    searchResults.take(8).forEachIndexed { index, doc ->
                                        DocRow(
                                            item = doc,
                                            showDivider = index < minOf(searchResults.size, 8) - 1,
                                            onClick = {
                                                keyboardController?.hide()
                                                onDocumentClick(doc)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.width(332.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFC70025),
                                    shape = RoundedCornerShape(50.dp)
                                )
                                .background(
                                    color = if (selectedTab == 0) Color(0xFFC70025) else Color(0xFF201F1F),
                                    shape = RoundedCornerShape(50.dp)
                                )
                                .clip(RoundedCornerShape(50.dp))
                                .clickable { selectedTab = 0 }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Recent",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (selectedTab == 0) Color.White else Color(0xFFC70025)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFC70025),
                                    shape = RoundedCornerShape(50.dp)
                                )
                                .background(
                                    color = if (selectedTab == 1) Color(0xFFC70025) else Color(0xFF201F1F),
                                    shape = RoundedCornerShape(50.dp)
                                )
                                .clip(RoundedCornerShape(50.dp))
                                .clickable { selectedTab = 1 }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "All",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (selectedTab == 1) Color.White else Color(0xFFC70025)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                when {
                    isLoading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .width(332.dp)
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFFC70025))
                            }
                        }
                    }
                    error != null -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .width(332.dp)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Error: $error",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF8E8D8D)
                                )
                            }
                        }
                    }
                    currentList.isEmpty() -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .width(332.dp)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (selectedTab == 0) "No recent documents" else "No documents found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF8E8D8D)
                                )
                            }
                        }
                    }
                    else -> {
                        itemsIndexed(
                            items = currentList,
                            key = { index, doc -> "${selectedTab}_${doc.id}_$index" }
                        ) { index, doc ->
                            val first = index == 0
                            val last = index == currentList.lastIndex
                            Box(
                                modifier = Modifier
                                    .width(332.dp)
                                    .background(
                                        color = Color.Black,
                                        shape = RoundedCornerShape(
                                            topStart = if (first) 24.dp else 0.dp,
                                            topEnd = if (first) 24.dp else 0.dp,
                                            bottomStart = if (last) 24.dp else 0.dp,
                                            bottomEnd = if (last) 24.dp else 0.dp
                                        )
                                    )
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = if (first) 24.dp else 0.dp,
                                            topEnd = if (first) 24.dp else 0.dp,
                                            bottomStart = if (last) 24.dp else 0.dp,
                                            bottomEnd = if (last) 24.dp else 0.dp
                                        )
                                    )
                            ) {
                                DocRow(
                                    item = doc,
                                    showDivider = !last,
                                    onClick = { onDocumentClick(doc) }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}