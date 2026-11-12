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

import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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

    @POST("v1beta/models/gemini-3.5-flash:streamGenerateContent")
    @Streaming
    suspend fun generateContentStream(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): ResponseBody
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

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isLiked: Boolean? = null,
    val isFavorite: Boolean = false,
    val isStreaming: Boolean = false
)

class AiViewModel(application: android.app.Application) : androidx.lifecycle.AndroidViewModel(application) {
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "আসসালামু আলাইকুম! আমি 'নূর AI' — আপনার আত্মশুদ্ধি, দ্বীনি জিজ্ঞাসা ও আসক্তি নিরাময় সহচর। যেকোনো প্রশ্ন বা সমস্যা নির্দ্বিধায় লিখুন, সরাসরি সমাধান পাবেন ইনশাআল্লাহ।",
                isUser = false
            )
        )
    )
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun toggleFavorite(targetMsg: ChatMessage) {
        _messages.value = _messages.value.map { msg ->
            if (msg.id == targetMsg.id) {
                msg.copy(isFavorite = !msg.isFavorite)
            } else {
                msg
            }
        }
    }

    fun toggleLike(messageId: String, isLike: Boolean) {
        _messages.value = _messages.value.map { msg ->
            if (msg.id == messageId) {
                // If clicked again, set to null (neutralize), otherwise set to isLike
                val newLiked = if (msg.isLiked == isLike) null else isLike
                msg.copy(isLiked = newLiked)
            } else {
                msg
            }
        }
    }

    fun sendMessage(prompt: String) {
        if (prompt.isBlank()) return
        
        val userMsg = ChatMessage(text = prompt, isUser = true)
        _messages.value = _messages.value + userMsg
        _isLoading.value = true

        viewModelScope.launch {
            val context = getApplication<android.app.Application>().applicationContext
            val isPremium = PremiumManager.isPremium.value
            val count = PremiumManager.getDailyMsgCount(context)

            if (!isPremium && count >= 3) {
                _isLoading.value = false
                _messages.value = _messages.value + ChatMessage(
                    text = "limit_reached",
                    isUser = false
                )
                return@launch
            }

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
                _isLoading.value = false
                _messages.value = _messages.value + ChatMessage(
                    text = LanguageStore.get("ai_missing_key"),
                    isUser = false
                )
                return@launch
            }

            val systemInstructionText = """
                You are 'Noor AI' (নূর AI) — a wise, empathetic, direct, and concise Islamic counselor, habit recovery assistant, and spiritual companion.

                CRITICAL INSTRUCTIONS:
                1. ANSWER DIRECTLY: Answer EXACTLY what the user asks without unnecessary preamble, long greetings, repetitive apologies, or unsolicited disclaimers. Get straight to the core answer in your very first sentence.
                2. NO EXTRA FLUFF (কোনো বাড়তি অপ্রয়োজনীয় কথা নয়): Do not ramble or add repetitive filler sentences. Provide sharp, precise, and practical answers.
                3. CLEAR & STRUCTURED: Use bullet points, bold key concepts, or brief paragraphs so the user gets actionable insights instantly.
                4. AUTHENTIC ISLAMIC & SCIENTIFIC GUIDANCE: When asked about overcoming bad habits (e.g. pornography, lustful thoughts, masturbation, screen addiction), salah, repentance, duas, or mental peace, provide authentic Islamic principles (Quran & Sunnah) combined with proven behavioral psychology steps concisely.
                5. STRICT LANGUAGE MATCH: Always answer in the EXACT SAME language the user asked in. If the user asks in Bengali (বাংলা), respond in clean, natural, and standard Bengali. If in English, respond in English.
                6. TONE: Respectful, compassionate, encouraging, and razor-sharp focused on solving the user's question.
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(Content(
                    parts = listOf(Part(text = prompt))
                )),
                systemInstruction = Content(
                    parts = listOf(Part(text = systemInstructionText))
                )
            )

            try {
                // Fetch direct streaming response body from service
                val responseBody = withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContentStream(apiKey, request)
                }

                _isLoading.value = false

                // Increment message count if not premium
                if (!isPremium) {
                    PremiumManager.incrementDailyMsgCount(context)
                }

                // Add an empty AI message that we will stream text additions into
                val aiMessageId = java.util.UUID.randomUUID().toString()
                var accumulatedText = ""
                val aiMsg = ChatMessage(id = aiMessageId, text = "", isUser = false, isStreaming = true)
                _messages.value = _messages.value + aiMsg

                withContext(Dispatchers.IO) {
                    responseBody.byteStream().bufferedReader().use { reader ->
                        val buffer = StringBuilder()
                        var braceCount = 0
                        var inString = false
                        var escape = false

                        var charCode: Int
                        while (reader.read().also { charCode = it } != -1) {
                            val c = charCode.toChar()
                            if (braceCount > 0) {
                                buffer.append(c)
                            }
                            if (escape) {
                                escape = false
                                continue
                            }
                            if (c == '\\') {
                                escape = true
                                continue
                            }
                            if (c == '"') {
                                inString = !inString
                                continue
                            }
                            if (!inString) {
                                if (c == '{') {
                                    if (braceCount == 0) {
                                        buffer.append(c)
                                    }
                                    braceCount++
                                } else if (c == '}') {
                                    braceCount--
                                    if (braceCount == 0) {
                                        val jsonStr = buffer.toString()
                                        buffer.clear()
                                        try {
                                            val element = Json.parseToJsonElement(jsonStr).jsonObject
                                            val text = element["candidates"]?.jsonArray
                                                ?.getOrNull(0)?.jsonObject
                                                ?.get("content")?.jsonObject
                                                ?.get("parts")?.jsonArray
                                                ?.getOrNull(0)?.jsonObject
                                                ?.get("text")?.jsonPrimitive?.content
                                            if (text != null) {
                                                accumulatedText += text
                                                val currentText = accumulatedText
                                                withContext(Dispatchers.Main) {
                                                    _messages.value = _messages.value.map { msg ->
                                                        if (msg.id == aiMessageId) {
                                                            msg.copy(text = currentText)
                                                        } else {
                                                            msg
                                                        }
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Finish streaming
                withContext(Dispatchers.Main) {
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == aiMessageId) {
                            msg.copy(isStreaming = false)
                        } else {
                            msg
                        }
                    }
                }

            } catch (e: Exception) {
                _isLoading.value = false
                _messages.value = _messages.value + ChatMessage(
                    text = LanguageStore.get("ai_connection_error"),
                    isUser = false
                )
            }
        }
    }
}
