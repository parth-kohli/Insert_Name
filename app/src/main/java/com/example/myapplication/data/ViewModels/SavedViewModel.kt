package com.example.myapplication.data.ViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CacheServerRepo
import com.example.myapplication.response.NewsArticle
import kotlinx.coroutines.launch

class SavedViewModel(private val repository: CacheServerRepo): ViewModel() {
    val articles = MutableLiveData<List<NewsArticle>>()

    fun getSaved(){
        viewModelScope.launch {
            articles.value=repository.fetchSaved()
        }
    }
}