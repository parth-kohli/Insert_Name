package com.example.myapplication.data.room

import androidx.room.PrimaryKey



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
