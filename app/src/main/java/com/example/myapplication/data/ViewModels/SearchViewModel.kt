package com.example.myapplication.data.ViewModels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CacheServerRepo
import com.example.myapplication.response.NewsArticle
import kotlinx.coroutines.launch
import java.time.LocalDate

class SearchViewModel(private val repository: CacheServerRepo): ViewModel() {
    val articles = MutableLiveData<List<NewsArticle>>()
    @RequiresApi(Build.VERSION_CODES.O)
    fun search(query: String, page: Int){

        articles.value=emptyList()
        viewModelScope.launch {
            articles.value=repository.searchNewsArticles(query.lowercase(), page)
        }
    }
    fun category(query: String){
        articles.value=emptyList()
        viewModelScope.launch {
            articles.value=repository.fetchByCategory(query.lowercase())
        }
    }
}