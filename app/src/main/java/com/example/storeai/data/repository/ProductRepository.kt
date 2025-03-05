package com.example.storeai.data.repository

import com.example.storeai.data.api.ApiService
import com.example.storeai.data.api.RetrofitClient
import com.example.storeai.data.model.Product
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody


class ProductRepository {
    private val apiService = RetrofitClient.instance

    suspend fun getAllProducts(): List<Product> {
        return apiService.getAllProducts().body() ?: emptyList()
    }

    suspend fun getProductById(id: String): Product {
        return apiService.getProductById(id).body() ?: throw Exception("Product not found")
    }

    suspend fun getSimilarProducts(imageFile: File): List<Product> {
        val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
        return RetrofitClient.similarityInstance.getSimilarProducts(imagePart).body() ?: emptyList()
    }
}