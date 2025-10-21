package com.example.myapplication.data.ViewModels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CacheServerRepo

import com.example.myapplication.response.BiasedArticles
import com.example.myapplication.response.NewsArticle
import kotlinx.coroutines.launch
import java.time.LocalDate

class ArticlesViewModel(private val repository: CacheServerRepo): ViewModel() {
    val articles = MutableLiveData<List<BiasedArticles>>()
    val article = MutableLiveData<NewsArticle?>()
    val isSaved = MutableLiveData<Boolean>()

    suspend fun getArticle(id: Int){
        article.value=repository.fetchArticle(id)
        val saved= repository.fetchSaved()
        if (article.value==null) article.value=saved.find{it.id==id}
        if (saved.contains(article.value)) isSaved.value=true
    }
    fun getBiased(id: Int){
        articles.value=emptyList()
        viewModelScope.launch {
            articles.value=repository.returnBiasedArticle(id)
        }
    }

    fun addSaved(newsArticle: NewsArticle){
        viewModelScope.launch {
            val result = repository.addSaved(newsArticle)
            isSaved.value=result
        }
    }

    fun removeSaved(newsArticle: NewsArticle){
        viewModelScope.launch {
            val result = repository.deleteSaved(newsArticle)
            isSaved.value=!result
        }
    }
}