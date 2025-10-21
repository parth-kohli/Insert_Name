package com.example.myapplication.data.ViewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CacheServerRepo
import com.example.myapplication.response.NewsArticle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SavedViewModel(private val repository: CacheServerRepo): ViewModel() {
    val _articles = MutableStateFlow<List<NewsArticle>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    val articles: StateFlow<List<NewsArticle>> = _articles

    fun getSaved(){
        viewModelScope.launch {
            _isLoading.value=true
            _articles.value=emptyList()
            try {
                _articles.value = repository.fetchSaved()
            }
            catch (e:Exception){
                Log.e("SavedViewModel", "Failed to fetch news: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}