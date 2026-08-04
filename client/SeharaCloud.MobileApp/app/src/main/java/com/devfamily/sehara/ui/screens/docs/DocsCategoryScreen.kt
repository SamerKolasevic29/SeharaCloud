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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devfamily.sehara.R
import com.devfamily.sehara.data.DocumentItem
import com.devfamily.sehara.ui.components.DocListScrollable
import com.devfamily.sehara.ui.components.DocsGridView
import com.devfamily.sehara.ui.components.SearchBar
import kotlinx.coroutines.delay

enum class DocsCategory { BOOKS, DOCUMENTS, OTHERS }

private enum class DocsCategoryViewMode { LIST, GRID }

@Composable
fun DocsCategoryScreen(
    modifier: Modifier = Modifier,
    category: DocsCategory,
    onBackClick: () -> Unit = {},
    onDocumentClick: (DocumentItem) -> Unit = {},
    viewModel: DocumentsViewModel = viewModel()
) {
    val docsList by viewModel.docsList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var viewMode by rememberSaveable { mutableStateOf(DocsCategoryViewMode.LIST) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val title = when (category) {
        DocsCategory.BOOKS -> "Books"
        DocsCategory.DOCUMENTS -> "Documents"
        DocsCategory.OTHERS -> "Others"
    }

    LaunchedEffect(category) {
        when (category) {
            DocsCategory.BOOKS -> viewModel.fetchBooks()
            DocsCategory.DOCUMENTS -> viewModel.fetchStandardDocuments()
            DocsCategory.OTHERS -> viewModel.fetchOtherDocuments()
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            viewModel.clearSearch()
        } else {
            delay(300)
            when (category) {
                DocsCategory.BOOKS -> viewModel.searchBooks(searchQuery)
                DocsCategory.DOCUMENTS -> viewModel.searchStandardDocuments(searchQuery)
                DocsCategory.OTHERS -> {}
            }
        }
    }

    val displayedSearchResults = if (category == DocsCategory.OTHERS) {
        docsList.filter { it.title?.contains(searchQuery, ignoreCase = true) == true }
    } else {
        searchResults
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
                        text = title,
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )

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
                                color = Color.White,
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
                            displayedSearchResults.isEmpty() -> {
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
                                DocListScrollable(items = displayedSearchResults) { onDocumentClick(it) }
                            }
                        }
                    }
                }

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
                                color = if (viewMode == DocsCategoryViewMode.LIST) Color(0xFFC70025) else Color.White,
                                shape = RoundedCornerShape(50.dp)
                            )
                            .clip(RoundedCornerShape(50.dp))
                            .clickable { viewMode = DocsCategoryViewMode.LIST }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "List",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (viewMode == DocsCategoryViewMode.LIST) Color.White else Color(0xFFC70025)
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
                                color = if (viewMode == DocsCategoryViewMode.GRID) Color(0xFFC70025) else Color.White,
                                shape = RoundedCornerShape(50.dp)
                            )
                            .clip(RoundedCornerShape(50.dp))
                            .clickable { viewMode = DocsCategoryViewMode.GRID }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Grid",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (viewMode == DocsCategoryViewMode.GRID) Color.White else Color(0xFFC70025)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .width(332.dp)
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFC70025))
                        }
                    }
                    error != null -> {
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
                    docsList.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .width(332.dp)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No documents found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF8E8D8D)
                            )
                        }
                    }
                    viewMode == DocsCategoryViewMode.LIST -> {
                        Column(
                            modifier = Modifier
                                .width(332.dp)
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
                            DocListScrollable(items = docsList) { onDocumentClick(it) }
                        }
                    }
                    else -> {
                        DocsGridView(items = docsList) { onDocumentClick(it) }
                    }
                }
            }
        }
    }
}