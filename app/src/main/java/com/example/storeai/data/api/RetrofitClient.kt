package com.example.storeai.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// Update RetrofitClient.kt
object RetrofitClient {
    private const val BASE_URL = "https://ai-powered-website-backend.onrender.com/api/"
    private const val SIMILARITY_BASE_URL = "https://ai-powered-similarities-search.onrender.com/api/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val similarityInstance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(SIMILARITY_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}