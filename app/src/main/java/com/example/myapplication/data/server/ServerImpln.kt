package com.example.myapplication.data.server

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object server {
    private const val BASE_URL = "https://9b2529b638b6.ngrok-free.app/"

    val api: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApiService::class.java)
    }
}
