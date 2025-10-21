package com.example.myapplication.data.ViewModels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CacheServerRepo
import com.example.myapplication.data.room.SearchedItems
import com.example.myapplication.response.NewsArticle

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class SearchViewModel(private val repository: CacheServerRepo) : ViewModel() {
    private val _results = MutableStateFlow<List<SearchedItems>>(emptyList())
    val results: StateFlow<List<SearchedItems>> = _results


    private val _articles = MutableStateFlow<List<NewsArticle>>(emptyList())
    private val _isLoading = MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> = _isLoading
    val articles: StateFlow<List<NewsArticle>> = _articles

    fun search(query: String, page: Int) {
        _articles.value = emptyList()

        viewModelScope.launch {
            _isLoading.value = true

            try {
                val searchResults = repository.searchNewsArticles(query.lowercase(), page)
                _articles.value = searchResults
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Failed to search news: ${e.message}")
            } finally {
                _isLoading.value = false
                addRecentSearch(query)
            }
        }
    }

    fun category(query: String) {
        _articles.value = emptyList()

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val categoryResults = repository.fetchByCategory(query.lowercase())
                _articles.value = categoryResults
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Failed to fetch category: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun getRecentSearches() {
        viewModelScope.launch {
            try {
                _results.value = repository.getRecentSearches()
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Failed to get recent searches: ${e.message}")
            }
        }
    }
    private suspend fun addRecentSearch(searchTerm: String) {
        try {
            repository.addRecentSearch(searchTerm)

            _results.value = repository.getRecentSearches()
        } catch (e: Exception) {
            Log.e("SearchViewModel", "Failed to add recent search: ${e.message}")
        }
    }
    fun deleteRecentSearch(item: SearchedItems) {
        viewModelScope.launch {
            try {
                repository.deleteRecentSearch(item)
                _results.value = repository.getRecentSearches()
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Failed to delete recent search: ${e.message}")
            }
        }
    }
}