package com.example.myapplication.data

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import java.lang.reflect.Type

data class ChatRequest(
    val model: String = "Qwen/Qwen2.5-VL-72B-Instruct",
    val messages: List<Message>,
    val max_tokens: Int = 1000
)

data class ResponseFormat(
    val type: String = "text"
)

data class Tool(
    val type: String = "function",
    val function: Function
)

data class Function(
    val description: String = "",
    val name: String = "",
    val parameters: Map<String, Any> = emptyMap(),
    val strict: Boolean = false
)

data class Message(
    val role: String,
    val content: String
)

data class ContentItem(
    val type: String, // "text" 或 "image_url"
    val text: String? = null,
    val image_url: ImageUrl? = null
)

data class ImageUrl(
    val url: String
)

data class ChatResponse(
    val id: String,
    val model: String,
    val created: Long,
    val choices: List<Choice>,
    val usage: Usage
)

data class Choice(
    val index: Int,
    val message: Message,
    val finish_reason: String
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

// 用于UI的聊天消息模型
data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val isUser: Boolean,
    val text: String? = null,
    val imageUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 自定义适配器，用于处理API返回的content字段可能是字符串或数组的情况
 */
class ContentAdapter : JsonDeserializer<List<ContentItem>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<ContentItem> {
        return when {
            json.isJsonArray -> {
                // 如果是数组，直接解析
                val contentItems = mutableListOf<ContentItem>()
                json.asJsonArray.forEach { element ->
                    contentItems.add(context.deserialize(element, ContentItem::class.java))
                }
                contentItems
            }
            json.isJsonPrimitive -> {
                // 如果是字符串，创建一个只有text属性的ContentItem
                listOf(ContentItem(type = "text", text = json.asString))
            }
            else -> {
                // 未知类型，返回空列表
                emptyList()
            }
        }
    }
} 