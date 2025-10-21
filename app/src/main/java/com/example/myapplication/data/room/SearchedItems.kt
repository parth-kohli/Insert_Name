package com.example.myapplication.data.room
import androidx.room.PrimaryKey

@androidx.room.Entity(tableName = "searched_items")
data class SearchedItems(
    @PrimaryKey
    val id: Long,
    val search: String
)
