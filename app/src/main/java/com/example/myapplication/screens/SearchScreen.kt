package com.example.myapplication.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.myapplication.allNewsCategories
import com.example.myapplication.data.*
import com.example.myapplication.response.NewsArticle
import kotlinx.coroutines.delay

/**
 * Defines the possible states of the search UI.
 */
enum class SearchState {
    DEFAULT,
    ACTIVE,
    SEARCHING,
    SEARCHED
}

/**
 * The main composable for the search feature screen.
 * It manages the UI state and orchestrates the different components.
 */
@Composable
fun SearchScreen(innerPadding: PaddingValues, onBackPressed: () -> Unit, onArticleClick: (Int) -> Unit) {

    val query = remember { mutableStateOf("") }
    val searchState = remember { mutableStateOf(SearchState.DEFAULT) }
    val selectedCategory = remember { mutableStateOf<String?>(null) }
    val searchResults = remember { mutableStateOf<List<NewsArticle>>(emptyList()) }
    val recentSearches = remember { mutableStateListOf<String>() }

    // This effect handles the data fetching and state transitions for searching.
    LaunchedEffect(searchState.value) {
        if (searchState.value == SearchState.SEARCHING) {
            val results = if (query.value.isNotBlank()) {
                searchNewsArticles(query.value)
            } else if (selectedCategory.value != null) {
                searchCategories(selectedCategory.value!!)
            } else {
                emptyList()
            }
            // Simulate a network delay for a better user experience
            delay(1000)
            searchResults.value = results
            searchState.value = SearchState.SEARCHED
        } else if (searchState.value == SearchState.DEFAULT) {
            // Clear results and category when returning to the default state
            searchResults.value = emptyList()
            selectedCategory.value = null
            query.value = ""
        } else if (searchState.value == SearchState.ACTIVE) {
            // Refresh the list of recent searches when the bar is activated
            recentSearches.clear()
            recentSearches.addAll(getCachedSearchResults())
        }
    }

    // Custom back handling for the different search states
    BackHandler {
        when {
            searchState.value != SearchState.DEFAULT -> searchState.value = SearchState.DEFAULT
            else -> onBackPressed()
        }
    }

    Column(Modifier.fillMaxSize().padding(innerPadding)) {
        Spacer(Modifier.height(32.dp))

        GlassSearchBar(
            query = query,
            searchState = searchState,
            onSearch = { searchText ->
                addSearchToCache(searchText)
                searchState.value = SearchState.SEARCHING
            }
        )

        // Main content area that changes based on the current search state
        when (searchState.value) {
            SearchState.DEFAULT -> {
                CategorySelectionGrid { category ->
                    selectedCategory.value = category
                    searchState.value = SearchState.SEARCHING
                }
            }
            SearchState.ACTIVE -> {
                RecentSearches(
                    recentSearches = recentSearches,
                    onItemClick = { search ->
                        query.value = search
                        searchState.value = SearchState.SEARCHING
                    },
                    onItemRemove = { search ->
                        removeSearchFromCache(search)
                        recentSearches.remove(search) // Update local state for immediate UI feedback
                    }
                )
            }
            SearchState.SEARCHING -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            SearchState.SEARCHED -> {
                if (searchResults.value.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No results found.", color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp)
                    }
                } else {
                    LazyColumn(state = rememberLazyListState()) {
                        items(searchResults.value) { article ->
                            NewsBlock(newsArticle = article){
                                onArticleClick(it)

                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * The custom search bar with a liquid glass aesthetic and multiple states.
 */
@Composable
fun GlassSearchBar(
    query: MutableState<String>,
    searchState: MutableState<SearchState>,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    // Effect to automatically request focus when the state becomes ACTIVE
    LaunchedEffect(searchState.value) {
        if (searchState.value == SearchState.ACTIVE) {
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(50))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.1f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(50)
            )
            .clickable(
                enabled = searchState.value == SearchState.DEFAULT || searchState.value== SearchState.SEARCHED,
                onClick = { searchState.value = SearchState.ACTIVE; query.value="" }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (searchState.value == SearchState.SEARCHING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White.copy(alpha = 0.8f),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchState.value == SearchState.ACTIVE) {
                    BasicTextField(
                        value = query.value,
                        onValueChange = { query.value = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(Color.White.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (query.value.isNotBlank()) {
                                    onSearch(query.value)
                                    focusManager.clearFocus()
                                }
                            }
                        )
                    )
                }

                if (query.value.isEmpty() && searchState.value != SearchState.DEFAULT) {
                    Text(
                        text = "Search news...",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )
                } else if (searchState.value == SearchState.DEFAULT) {
                    Text(
                        text = "Search or select a category...",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )
                }
                else if (searchState.value==SearchState.SEARCHED || searchState.value== SearchState.SEARCHING){
                    Text(
                        text = query.value,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )
                }
            }

            AnimatedVisibility(visible = searchState.value == SearchState.ACTIVE) {
                Text(
                    text = "Cancel",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    modifier = Modifier.clickable {
                        query.value = ""
                        searchState.value = SearchState.DEFAULT
                        focusManager.clearFocus()
                    }
                )
            }
        }
    }
}

/**
 * A grid that displays all available news categories for filtering.
 */
@Composable
fun CategorySelectionGrid(onCategorySelected: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(allNewsCategories) { category ->
            CategoryGlassChip(category = category) {
                onCategorySelected(category)
            }
        }
    }
}

/**
 * An individual, clickable "chip" for a news category with the glass effect.
 */
@Composable
fun CategoryGlassChip(category: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(8.dp)
            .aspectRatio(2f)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.1f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Text(
            text = category,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

/**
 * A list of recently searched items, displayed within a glass container.
 */
@Composable
fun RecentSearches(
    recentSearches: List<String>,
    onItemClick: (String) -> Unit,
    onItemRemove: (String) -> Unit
) {
    if (recentSearches.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("No recent searches", color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        LazyColumn(contentPadding = PaddingValues(8.dp)) {
            item {
                Text(
                    text = "Recent Searches",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(recentSearches) { searchText ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onItemClick(searchText) }
                        .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Recent Search",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = searchText,
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton({ onItemRemove(searchText) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Search",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}