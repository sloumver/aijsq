package com.example.myapplication.network

import android.util.Log
import com.example.myapplication.data.Message
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

object SiliconFlowClient {
    private const val TAG = "SiliconFlowClient"
    private const val BASE_URL = "https://api.siliconflow.cn/v1/chat/completions"
    private const val API_KEY = "sk-gforaiawdstqsauxbnpreuxtilwpxkmqvonuwhkaexxjrdxe"
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val gson = Gson()

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d(TAG, message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    fun sendChatRequest(messages: List<Message>): String {
        val requestBody = mapOf(
            "model" to "Qwen/Qwen2.5-VL-72B-Instruct",
            "messages" to messages,
            "max_tokens" to 1000
        )

        val jsonBody = gson.toJson(requestBody)
        Log.d(TAG, "Request body: $jsonBody")

        val request = Request.Builder()
            .url(BASE_URL)
            .addHeader("Authorization", "Bearer $API_KEY")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody(JSON))
            .build()

        return try {
            Log.d(TAG, "Sending request to: $BASE_URL")
            Log.d(TAG, "Request headers: ${request.headers}")
            
            val response = client.newCall(request).execute()
            Log.d(TAG, "Response code: ${response.code}")
            Log.d(TAG, "Response headers: ${response.headers}")
            
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                Log.e(TAG, "Error response: $errorBody")
                throw IOException("API请求失败: ${response.code} - $errorBody")
            }
            
            val responseBody = response.body?.string()
            Log.d(TAG, "Response body: $responseBody")
            
            if (responseBody.isNullOrEmpty()) {
                throw IOException("响应体为空")
            }
            
            responseBody
        } catch (e: Exception) {
            Log.e(TAG, "API请求异常", e)
            throw e
        }
    }
} 