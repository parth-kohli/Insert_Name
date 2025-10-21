package com.example.myapplication.data.room


import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.response.BiasedArticles
import com.example.myapplication.response.NewsArticle

@Database(entities = [NewsArticle::class, BiasedArticles::class, SavedArticles::class, SearchedItems::class], version = 1,
    exportSchema = false)
abstract class AppDataBase : RoomDatabase() {
    abstract fun newsArticleDao(): NewsArticleDao
    abstract fun biasedArticleDao(): BiasedArticleDao
    abstract fun savedArticleDao(): SavedArticleDao
    abstract fun searchedItemsDao(): SearchedItemsDao

}