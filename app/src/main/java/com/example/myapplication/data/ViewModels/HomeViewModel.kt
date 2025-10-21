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

class HomeViewModel(private val repository: CacheServerRepo): ViewModel() {
    val articles = MutableLiveData<List<NewsArticle>>()
    @RequiresApi(Build.VERSION_CODES.O)
    fun getTodayNews(){
        articles.value=emptyList()
        val date = LocalDate.now().toString()
        viewModelScope.launch {
            articles.value=repository.fetchTodaysNews()
        }
    }
}