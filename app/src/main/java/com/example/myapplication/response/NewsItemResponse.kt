package com.example.myapplication.response

import kotlinx.serialization.Serializable

@Serializable
data class NewsArticle(
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
@Serializable
data class BiasedArticles(
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