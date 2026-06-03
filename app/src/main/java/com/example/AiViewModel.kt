package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null
)

@Serializable
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>
)

@Serializable
data class Candidate(
    val content: Content
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

data class ChatMessage(val text: String, val isUser: Boolean)

class AiViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("আসসালামু আলাইকুম ওয়া রাহমাতুল্লাহ! আমি আপনার ইসলামিক কাউনসিলর এবং আসক্তি নিরাময় সঙ্গী। আমি আপনাকে খারাপ অভ্যাস, পর্নোগ্রাফি এবং নেতিবাচক চিন্তার ফাঁদ থেকে মুক্ত হয়ে একটি পবিত্র, সচেতন এবং আধ্যাত্মিক জীবন গড়ে তুলতে সাহায্য করার জন্য ও আপনার রুকইয়াহ্ হিসেবে নির্দেশনা দিতে নিয়োজিত রয়েছি ইনশাআল্লাহ। আপনার যেকোনো মানসিক বা আত্মিক অনুভূতি কোনো দ্বিধা ছাড়াই সরাসরি লিখে শেয়ার করতে পারেন, যা সম্পূর্ণভাবে গোপন থাকবে।", false)
        )
    )
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun sendMessage(prompt: String) {
        if (prompt.isBlank()) return
        
        val userMsg = ChatMessage(prompt, true)
        _messages.value = _messages.value + userMsg
        _isLoading.value = true

        viewModelScope.launch {
            val response = generateContent(prompt)
            _messages.value = _messages.value + ChatMessage(response, false)
            _isLoading.value = false
        }
    }

    private suspend fun generateContent(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
            return@withContext "দয়া করে এআই স্টুডিও এর 'Secrets' প্যানেলে গিয়ে 'GEMINI_API_KEY' নামে একটি ভেরিয়েবল তৈরি করে আপনার জেমিনি এপিআই-কী (API Key) সেট করুন।"
        }
        
        val request = GenerateContentRequest(
            contents = listOf(Content(
                parts = listOf(Part(text = prompt))
            )),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are an Islamic counselor and addiction recovery companion. Your goal is to help users overcome bad habits, pornography, and negative thoughts, and guide them towards a pure, conscious, and spiritual life. You should answer in the SAME language the user uses to ask the question (e.g. if they ask in Bengali, reply in Bengali). Keep responses compassionate, encouraging, and based on Islamic principles."))
            )
        )
        
        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response text"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
