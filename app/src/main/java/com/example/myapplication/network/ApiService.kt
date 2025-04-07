package com.example.myapplication.network

import com.example.myapplication.data.ChatRequest
import com.example.myapplication.data.ChatResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * API服务接口，用于与Qwen2.5-VL-72B模型通信
 */
interface ApiService {
    @POST("chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): ChatResponse

    @Multipart
    @POST("chat/completions")
    suspend fun chatCompletionsWithImage(
        @Header("Authorization") authorization: String,
        @Part("request") request: ChatRequest,
        @Part image: MultipartBody.Part
    ): ChatResponse
} 