package com.example.myapplication.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.speech.RecognizerIntent
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.myapplication.data.ChatMessage
import com.example.myapplication.data.ChatRequest
import com.example.myapplication.data.ChatResponse
import com.example.myapplication.data.ContentItem
import com.example.myapplication.data.ImageUrl
import com.example.myapplication.data.Message
import com.example.myapplication.network.ApiClient
import com.example.myapplication.network.DirectApiClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.myapplication.network.SiliconFlowClient
import android.util.Log
import java.io.IOException
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(listOf())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val gson = Gson()
    
    init {
        // 初始AI欢迎消息
        addMessage(
            ChatMessage(
                isUser = false,
                text = "你好！我是AI助手，可以回答你的问题。你可以通过文字、图片或语音与我交流。",
            )
        )
    }
    
    fun addMessage(message: ChatMessage) {
        _messages.value = _messages.value + message
    }
    
    fun sendTextMessage(text: String) {
        if (text.isBlank()) return
        
        val userMessage = ChatMessage(isUser = true, text = text)
        addMessage(userMessage)
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val messages = listOf(
                    Message(
                        role = "system",
                        content = "你是一个智能数学问答助手，请始终使用中文回复用户，格式如下\n答案：\n（正确答案）\n过程：\n（解题过程仅数学结构）\n解释：\n，并保持清晰的结构和格式。如果涉及数学结构，请使用Latex表达式。"
                    )
                ) + _messages.value
                    .filter { it.text != null }
                    .map {
                        Message(
                            role = if (it.isUser) "user" else "assistant",
                            content = it.text ?: ""
                        )
                    }
                
                try {
                    val responseJson = SiliconFlowClient.sendChatRequest(messages)
                    val responseType = object : TypeToken<ChatResponse>() {}.type
                    val response = gson.fromJson<ChatResponse>(responseJson, responseType)
                    
                    // 安全地从响应中提取文本
                    val botMessage = try {
                        response.choices.firstOrNull()?.message?.content
                            ?: "我没有收到明确的响应"
                    } catch (e: Exception) {
                        Log.e("ChatViewModel", "解析响应失败", e)
                        "数据解析错误: ${e.message}"
                    }
                    
                    addMessage(ChatMessage(isUser = false, text = botMessage))
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "API请求失败", e)
                    val errorMessage = when (e) {
                        is IOException -> {
                            if (e.message?.contains("Failed to connect") == true) {
                                "网络连接失败，请检查网络设置"
                            } else if (e.message?.contains("401") == true) {
                                "API密钥无效或已过期"
                            } else if (e.message?.contains("403") == true) {
                                "没有权限访问API"
                            } else {
                                "网络请求错误: ${e.message}"
                            }
                        }
                        else -> "API请求错误: ${e.message}"
                    }
                    addMessage(ChatMessage(isUser = false, text = errorMessage))
                }
            } catch (e: Exception) {
                addMessage(ChatMessage(isUser = false, text = "发生错误: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun sendImageMessage(context: Context, imageUri: Uri) {
        val userMessage = ChatMessage(isUser = true, imageUri = imageUri.toString())
        addMessage(userMessage)
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // 处理图像文件
                val parcelFileDescriptor = context.contentResolver.openFileDescriptor(imageUri, "r")
                val inputStream = parcelFileDescriptor?.fileDescriptor?.let {
                    java.io.FileInputStream(it)
                }
                
                val file = File(context.cacheDir, "image_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                
                parcelFileDescriptor?.close()
                inputStream?.close()
                outputStream.close()
                
                // 准备图像上传
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                
                // 准备请求
                val messages = listOf(
                    Message(
                        role = "system",
                        content = "你是一个智能数学问答助手，请始终使用中文回复用户，格式如下\n答案：\n（正确答案）\n过程：\n（解题过程仅数学结构）\n解释：\n，并保持清晰的结构和格式。如果涉及数学结构，请使用Latex表达式；如果涉及列表，请使用编号；如果涉及重点文字，请用加粗标记；如果是专业术语，请给出解释。"
                    ),
                    Message(
                        role = "user",
                        content = "请描述这张图片"
                    )
                )
                
                val chatRequest = ChatRequest(
                    messages = messages,
                    max_tokens = 1000
                )
                
                val apiService = ApiClient.apiService
                val response = apiService.chatCompletions(
                    authorization = ApiClient.getAuthHeader(),
                    request = chatRequest
                )
                
                val botMessage = response.choices.firstOrNull()?.message?.content
                
                if (botMessage != null) {
                    addMessage(ChatMessage(isUser = false, text = botMessage))
                } else {
                    addMessage(ChatMessage(isUser = false, text = "抱歉，我无法处理这张图片。"))
                }
                
                // 清理临时文件
                file.delete()
            } catch (e: Exception) {
                addMessage(ChatMessage(isUser = false, text = "处理图片时发生错误: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 模拟语音识别结果
    fun processVoiceInput(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            // 模拟语音识别延迟
            delay(1500)
            // 由于移除了MLKit依赖，这里直接使用模拟的语音识别结果
            val simulatedVoiceText = "这是一条模拟的语音识别消息"
            addMessage(ChatMessage(isUser = true, text = simulatedVoiceText))
            // 模拟AI响应
            delay(1000)
            addMessage(ChatMessage(isUser = false, text = "我收到了你的语音消息：\"$simulatedVoiceText\"。由于当前网络环境限制，语音识别功能使用的是模拟数据。"))
            _isLoading.value = false
        }
    }
}

// Base64编码扩展函数
fun ByteArray.encodeBase64(): String {
    return Base64.encodeToString(this, Base64.DEFAULT)
}

@Composable
fun AIAssistantScreen() {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    setSupportZoom(true)
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { webView ->
            webView.loadUrl("file:///android_asset/chat.html")
        }
    )
}

@Composable
fun ChatHeader() {
    Text(
        text = "AI智能助手",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isUser) 
        MaterialTheme.colorScheme.primary 
    else 
        MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (message.isUser)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSecondaryContainer
    
    Column(
        horizontalAlignment = alignment,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (message.imageUri != null) {
            // 显示图片消息
            Surface(
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp,
                color = bubbleColor,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(max = 280.dp)
            ) {
                AsyncImage(
                    model = message.imageUri,
                    contentDescription = "发送的图片",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(200.dp)
                )
            }
        }
        
        if (message.text != null) {
            // 显示文本消息
            Surface(
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp,
                color = bubbleColor,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.text,
                    color = contentColor,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
            Text(
                text = "正在思考...",
                modifier = Modifier.padding(start = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ChatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onImageClick: () -> Unit,
    onVoiceClick: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图片按钮
        IconButton(
            onClick = { onImageClick() },
            enabled = !isLoading
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "选择图片",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        // 语音按钮
        IconButton(
            onClick = { onVoiceClick() },
            enabled = !isLoading
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardVoice,
                contentDescription = "语音输入",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        // 输入框
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("输入消息...") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            enabled = !isLoading
        )
        
        // 发送按钮
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(enabled = !isLoading && value.isNotEmpty()) { onSendClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "发送",
                tint = Color.White
            )
        }
    }
} 