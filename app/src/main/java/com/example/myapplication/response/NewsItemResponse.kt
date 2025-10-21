package com.example.myapplication.response

import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@androidx.room.Entity(tableName = "news_articles")
data class NewsArticle (
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val headline:String,
    val description:String,
    val article: String,
    val imageUrl: String,
    val date: String,
    val category: String,
    val centerBias: Float,
    val leftBias: Float,
    val rightBias: Float,
    val source: String
)
@androidx.room.Entity(tableName = "biased_articles")
@Serializable
data class BiasedArticles(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val headline:String,
    val article: String,
    val description: String,
    val imageUrl: String,
    val date: String,
    val category: String,
    val centerBias: Float,
    val leftBias: Float,
    val rightBias: Float,
    val source: String,
    val articleId: Int
)