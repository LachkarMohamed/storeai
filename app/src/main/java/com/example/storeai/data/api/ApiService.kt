package com.example.storeai.data.api


import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.Response
import com.example.storeai.data.model.RegisterResponse
import com.example.storeai.data.model.Product
import com.example.storeai.data.model.RegisterRequest
import com.example.storeai.data.model.User
import com.example.storeai.data.model.LoginRequest
import com.example.storeai.data.model.LoginResponse
import com.example.storeai.data.model.Category
import retrofit2.http.Part
import okhttp3.MultipartBody
import retrofit2.http.Multipart


interface ApiService {
    @GET("products")
    suspend fun getAllProducts(): Response<List<Product>>

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: String): Response<Product>

    @POST("users")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("users/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @GET("categories")
    suspend fun getAllCategories(): Response<List<Category>>

    @Multipart @POST("get_similar_images") suspend fun getSimilarProducts(@Part image: MultipartBody.Part): Response<List<Product>>

}