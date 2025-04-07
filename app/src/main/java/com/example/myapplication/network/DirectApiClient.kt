package com.example.myapplication.network

import com.example.myapplication.data.Message
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

object DirectApiClient {
    private const val BASE_URL = "https://api.siliconflow.cn/v1/chat/completions"
    private const val API_KEY = "sk-gforaiawdstqsauxbnpreuxtilwpxkmqvonuwhkaexxjrdxe"
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun sendChatRequest(messages: List<Message>): String {
        val requestBody = mapOf(
            "model" to "Qwen/Qwen2.5-VL-72B-Instruct",
            "messages" to messages,
            "max_tokens" to 1000
        )

        val request = Request.Builder()
            .url(BASE_URL)
            .addHeader("Authorization", "Bearer $API_KEY")
            .addHeader("Content-Type", "application/json")
            .post(gson.toJson(requestBody).toRequestBody(JSON))
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }
            response.body?.string() ?: throw IOException("Empty response body")
        } catch (e: Exception) {
            throw e
        }
    }
} 