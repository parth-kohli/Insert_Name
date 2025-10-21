package com.example.myapplication.data

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Query
import com.example.myapplication.data.room.BiasedArticleDao
import com.example.myapplication.data.room.NewsArticleDao
import com.example.myapplication.data.room.SavedArticleDao
import com.example.myapplication.data.server.NewsApiService
import com.example.myapplication.response.BiasedArticles
import com.example.myapplication.response.NewsArticle
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate

class CacheServerRepo(
    private val api: NewsApiService,
    private val newsdao: NewsArticleDao,
    private val biaseddao: BiasedArticleDao,
    private val saveddao: SavedArticleDao
) {
    suspend fun fetchBiasedArticle( id: Int){
        val biased = api.getBiasedNews(id)
        biaseddao.insertAll(biased)
    }
    suspend fun returnBiasedArticle( id: Int): List<BiasedArticles> = coroutineScope{
        try {
            val biased = api.getBiasedNews(id)
            biaseddao.insertAll(biased)
            return@coroutineScope biased
        }
        catch (e: Exception){
            val biased = biaseddao.getBiasedArticles(id)
            return@coroutineScope biased
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchTodaysNews(): List<NewsArticle> = coroutineScope {
        try {
            val articles = api.getTodaysNews()
            newsdao.insertAll(articles)
            articles.map { article ->
                async {
                    fetchBiasedArticle(article.id)
                }
            }.awaitAll()
            return@coroutineScope articles
        }
        catch (e: Exception) {
            val articles = newsdao.searchToday(LocalDate.now().minusDays(1).toString())
            return@coroutineScope articles
        }
    }
    suspend fun searchNewsArticles( query: String, page: Int): List<NewsArticle> = coroutineScope{
        try {
            val result = api.searchNews(query, page)
            newsdao.insertAll(result)
            result.map { article ->
                async {
                    fetchBiasedArticle(article.id)
                }
            }.awaitAll()
            return@coroutineScope result
        }
        catch (e: Exception){
            val result=newsdao.searchDatabase(query)
            return@coroutineScope result
        }
    }
    suspend fun fetchByCategory( query: String): List<NewsArticle> = coroutineScope{
        try {
            val result = api.getByCategory(query)
            newsdao.insertAll(result)
            result.map { article ->
                async {
                    fetchBiasedArticle(article.id)
                }
            }.awaitAll()
            return@coroutineScope result
        }
        catch (e: Exception){
            val result=newsdao.searchCategory(query)
            return@coroutineScope result
        }
    }

    suspend fun fetchSaved(): List<NewsArticle>  {
        return saveddao.getAll()
    }

    suspend fun deleteSaved(query: NewsArticle): Boolean {
        try {
            saveddao.delete(query)
            return true
        }
        catch (e: Exception){
            return false
        }
    }

    suspend fun addSaved(query: NewsArticle): Boolean{
        try {
            saveddao.insert(query)
            return true
        }
        catch (e: Exception){
            return false
        }
    }

    suspend fun fetchArticle(id: Int): NewsArticle?{
        return newsdao.fetchArticle(id)
    }

}