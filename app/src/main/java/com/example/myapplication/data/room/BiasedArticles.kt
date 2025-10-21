package com.example.myapplication.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "biased_articles")
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
