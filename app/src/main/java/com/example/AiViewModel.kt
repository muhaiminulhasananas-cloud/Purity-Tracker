package com.example

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import java.net.SocketTimeoutException
import java.util.UUID
import java.util.concurrent.TimeUnit

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val role: String? = null,
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
    val candidates: List<Candidate> = emptyList()
)

@Serializable
data class Candidate(
    val content: Content
)

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST("v1beta/models/{model}:streamGenerateContent")
    @Streaming
    suspend fun generateContentStream(
        @Path("model") model: String,
        @Query("alt") alt: String = "sse",
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GenerateContentRequest
    ): ResponseBody
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
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
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isLiked: Boolean? = null,
    val isFavorite: Boolean = false,
    val isStreaming: Boolean = false,
    val isError: Boolean = false
)

/**
 * Parses Gemini SSE (`data: {...}`) or JSON chunk payloads and extracts all visible model text parts
 * while skipping internal `thought: true` parts.
 */
object GeminiStreamParser {
    fun extractVisibleTextFromChunkJson(jsonChunk: String): String {
        val trimmed = jsonChunk.trim()
        if (trimmed.isEmpty() || trimmed == "[DONE]") return ""
        return try {
            val element = Json.parseToJsonElement(trimmed).jsonObject
            val candidates = element["candidates"]?.jsonArray ?: return ""
            val parts = candidates.firstOrNull()?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("parts")?.jsonArray ?: return ""
            val sb = StringBuilder()
            for (partEl in parts) {
                val obj = partEl.jsonObject
                val isThought = obj["thought"]?.jsonPrimitive?.booleanOrNull == true
                if (!isThought) {
                    val partText = obj["text"]?.jsonPrimitive?.content
                    if (!partText.isNullOrEmpty()) {
                        sb.append(partText)
                    }
                }
            }
            sb.toString()
        } catch (_: Exception) {
            ""
        }
    }
}

class AiViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "AiViewModel"
        private const val PRIMARY_CHAT_MODEL = "gemini-3.5-flash-lite"
        private const val FALLBACK_CHAT_MODEL = "gemini-3.5-flash"

        internal val SYSTEM_INSTRUCTION_TEXT: String = """
            You are 'Noor AI' (নূর AI) — the built-in Islamic counselor, habit recovery assistant, and spiritual companion inside the Purity Tracker app.

            PURITY TRACKER COMPLETE FEATURE GUIDE (KNOW THIS THOROUGHLY):
            1. Streak & Badges (স্ট্রিক আর ব্যাজ):
               - Daily recovery streak tracker that counts clean days continuously.
               - 10 milestone badges to unlock as your streak grows (0, 1, 3, 7, 15, 30, 45, 60, 120, and 365 days — celebrating key recovery checkpoints like 3 days, 7 days, 14/15 days, 21 days, 30 days, 45 days, 60 days, 120 days, and 365 days).
            2. Salah / Prayer Tracking (নামাজ/সালাহ ট্র্যাকিং):
               - Daily 5-waqt Salah checklist (Fajr, Dhuhr, Asr, Maghrib, Isha) and past 7-day Salah record so users stay consistent in prayer.
            3. Content Blocker & Accountability Protection (কনটেন্ট ব্লকার):
               - Blocks adult and harmful content on the phone.
               - To turn off or disable the blocker, Accountability Partner (অভিভাবক) email approval is strictly required.
               - The App PIN is set ONLY by the accountability partner — the user can never set or create the PIN himself.
               - The PIN can NEVER be used to turn off the content blocker.
            4. Trigger & Mood Log + Protection Chart (ট্রিগার আর মুড লগ, সুরক্ষার চার্ট):
               - Lets users log their triggers and moods to discover what puts them at risk.
               - Shows a protection chart so users can see how protected they are and track their safety patterns.
            5. Noor AI Counseling (নূর AI কাউন্সেলিং):
               - Built right inside Purity Tracker for direct Islamic & recovery guidance (limited free daily messages, and unlimited in Premium).
            6. Premium Membership (প্রিমিয়াম):
               - Monthly (মাসিক): ৳১৪৯ (149 BDT)
               - Yearly (বার্ষিক): ৳৭৯৯ (799 BDT)
               - Family / Lifetime (ফ্যামিলি লাইফটাইম): ৳১,৯৯৯ (1,999 BDT)
               - Payment: Manual payment via bKash (বিকাশ), Nagad (নগদ), or Rocket (রকেট).

            CRITICAL INSTRUCTIONS:
            1. RECOMMEND PURITY TRACKER FEATURES FIRST: Whenever the user asks about self-purification (আত্মশুদ্ধি / নিজেকে কীভাবে শুদ্ধ করবো), protection from haram or relapse (সুরক্ষা / কীভাবে বাঁচবো), or any function of the app, FIRST explain the matching Purity Tracker feature step by step and recommend it explicitly by name — for example: "Purity Tracker-এর স্ট্রিক দিয়ে শুরু করো..." or "Start with Purity Tracker's Streak...".
            2. ANSWER DIRECTLY: Answer EXACTLY what the user asks without unnecessary preamble, long greetings, repetitive apologies, or unsolicited disclaimers. Get straight to the core answer in your very first sentence.
            3. NO EXTRA FLUFF (কোনো বাড়তি অপ্রয়োজনীয় কথা নয়): Provide sharp, clear, structured, and practical answers using bullet points or short steps.
            4. AUTHENTIC ISLAMIC & SCIENTIFIC GUIDANCE: Combine authentic Islamic principles (Quran & Sunnah) with proven behavioral psychology steps alongside Purity Tracker's features.
            5. STRICT LANGUAGE MATCH: Always answer in the EXACT SAME language the user asked in. If the user writes in Bengali (বাংলা), respond in clean, natural Bengali. If in English, respond in English.
            6. STRICT CONFIDENTIALITY ABOUT TECH: Never reveal or mention what technology, AI provider, models, keys, cloud services, or developer tools power you or Purity Tracker. Never mention "API key", "Gemini", "AI Studio", or "Secrets" to the user under any circumstance.
            7. TONE: Respectful, compassionate, encouraging, and focused on solving the user's question.
        """.trimIndent()

        internal fun resolveApiKey(rawKey: String?): String {
            return rawKey
                ?.trim()
                ?.removeSurrounding("\"")
                ?.removeSurrounding("'")
                ?.trim()
                .orEmpty()
        }

        internal fun isMissingApiKey(apiKey: String?): Boolean {
            val normalized = resolveApiKey(apiKey)
            return normalized.isEmpty() ||
                normalized.equals("MY_GEMINI_API_KEY", ignoreCase = true) ||
                normalized.equals("<redacted>", ignoreCase = true)
        }
    }

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

    private val _speakingMessageId = MutableStateFlow<String?>(null)
    val speakingMessageId = _speakingMessageId.asStateFlow()

    private val _loadingAudioMessageId = MutableStateFlow<String?>(null)
    val loadingAudioMessageId = _loadingAudioMessageId.asStateFlow()

    private val _ttsError = MutableStateFlow<String?>(null)
    val ttsError = _ttsError.asStateFlow()

    private var speakJob: Job? = null

    fun clearTtsError() {
        _ttsError.value = null
    }

    fun speakMessage(messageId: String, text: String) {
        if (_speakingMessageId.value == messageId || _loadingAudioMessageId.value == messageId) {
            stopSpeaking()
            return
        }

        stopSpeaking()
        _ttsError.value = null

        val apiKey = resolveApiKey(BuildConfig.GEMINI_API_KEY)
        if (isMissingApiKey(apiKey)) {
            Log.e(TAG, "Noor voice unavailable: GEMINI_API_KEY is missing or placeholder in BuildConfig.")
            _ttsError.value = GeminiErrorFormatter.politeUnavailableMessage()
            return
        }

        val context = getApplication<Application>().applicationContext
        speakJob = viewModelScope.launch {
            _loadingAudioMessageId.value = messageId
            try {
                val audio = NoorVoicePlayer.synthesizePegasusAudio(
                    messageId = messageId,
                    text = text,
                    apiKey = apiKey
                )
                _loadingAudioMessageId.value = null
                _speakingMessageId.value = messageId
                NoorVoicePlayer.playAudio(context, audio)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Noor Pegasus TTS failed: ${GeminiErrorFormatter.formatForLog(e, apiKey)}", e)
                _ttsError.value = GeminiErrorFormatter.politeUnavailableMessage()
            } finally {
                if (_loadingAudioMessageId.value == messageId) {
                    _loadingAudioMessageId.value = null
                }
                if (_speakingMessageId.value == messageId) {
                    _speakingMessageId.value = null
                }
            }
        }
    }

    fun stopSpeaking() {
        speakJob?.cancel()
        speakJob = null
        NoorVoicePlayer.stopPlayback()
        _loadingAudioMessageId.value = null
        _speakingMessageId.value = null
    }

    override fun onCleared() {
        stopSpeaking()
        super.onCleared()
    }

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
                val newLiked = if (msg.isLiked == isLike) null else isLike
                msg.copy(isLiked = newLiked)
            } else {
                msg
            }
        }
    }

    fun sendMessage(prompt: String) {
        if (prompt.isBlank()) return

        val previousHistory = _messages.value
            .drop(1) // Skip static welcome message
            .filter { !it.isError && it.text.isNotBlank() && it.text != "limit_reached" }
            .takeLast(6)

        val userMsg = ChatMessage(text = prompt, isUser = true)
        _messages.value = _messages.value + userMsg
        _isLoading.value = true

        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
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

            val apiKey = resolveApiKey(BuildConfig.GEMINI_API_KEY)
            if (isMissingApiKey(apiKey)) {
                Log.e(TAG, "Noor chat unavailable: GEMINI_API_KEY is missing or placeholder in BuildConfig.")
                _isLoading.value = false
                _messages.value = _messages.value + ChatMessage(
                    text = GeminiErrorFormatter.politeUnavailableMessage(),
                    isUser = false,
                    isError = true
                )
                return@launch
            }

            val requestContents = buildList {
                for (msg in previousHistory) {
                    add(
                        Content(
                            role = if (msg.isUser) "user" else "model",
                            parts = listOf(Part(text = msg.text))
                        )
                    )
                }
                add(
                    Content(
                        role = "user",
                        parts = listOf(Part(text = prompt))
                    )
                )
            }

            val request = GenerateContentRequest(
                contents = requestContents,
                systemInstruction = Content(
                    parts = listOf(Part(text = SYSTEM_INSTRUCTION_TEXT))
                )
            )

            var streamingMessageId: String? = null
            var countedUsage = false

            try {
                val responseBody = openStreamWithRetryAndFallback(apiKey, request)

                _isLoading.value = false

                if (!isPremium) {
                    PremiumManager.incrementDailyMsgCount(context)
                    countedUsage = true
                }

                val aiMessageId = UUID.randomUUID().toString()
                streamingMessageId = aiMessageId
                var accumulatedText = ""
                val aiMsg = ChatMessage(id = aiMessageId, text = "", isUser = false, isStreaming = true)
                _messages.value = _messages.value + aiMsg

                withContext(Dispatchers.IO) {
                    responseBody.use { body ->
                        body.byteStream().bufferedReader(Charsets.UTF_8).use { reader ->
                            var line: String?
                            val eventDataBuffer = StringBuilder()

                            while (reader.readLine().also { line = it } != null) {
                                val currentLine = line ?: continue
                                when {
                                    currentLine.startsWith("data:") -> {
                                        val payload = currentLine.removePrefix("data:").trim()
                                        if (payload.isNotEmpty() && payload != "[DONE]") {
                                            eventDataBuffer.append(payload)
                                        }
                                    }
                                    currentLine.isBlank() -> {
                                        if (eventDataBuffer.isNotEmpty()) {
                                            val chunkText = GeminiStreamParser.extractVisibleTextFromChunkJson(
                                                eventDataBuffer.toString()
                                            )
                                            eventDataBuffer.clear()
                                            if (chunkText.isNotEmpty()) {
                                                accumulatedText += chunkText
                                                val snapshot = accumulatedText
                                                withContext(Dispatchers.Main) {
                                                    updateStreamingMessage(aiMessageId, snapshot, isStreaming = true)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (eventDataBuffer.isNotEmpty()) {
                                val chunkText = GeminiStreamParser.extractVisibleTextFromChunkJson(
                                    eventDataBuffer.toString()
                                )
                                if (chunkText.isNotEmpty()) {
                                    accumulatedText += chunkText
                                    val snapshot = accumulatedText
                                    withContext(Dispatchers.Main) {
                                        updateStreamingMessage(aiMessageId, snapshot, isStreaming = true)
                                    }
                                }
                            }
                        }
                    }
                }

                if (accumulatedText.isBlank()) {
                    Log.w(TAG, "Noor SSE stream returned blank text; attempting non-streaming fallback.")
                    val fallbackText = fetchNonStreamingFallback(apiKey, request)
                    if (!fallbackText.isNullOrBlank()) {
                        accumulatedText = fallbackText
                    }
                }

                withContext(Dispatchers.Main) {
                    if (accumulatedText.isBlank()) {
                        Log.e(TAG, "Noor chat returned an empty response.")
                        updateStreamingMessage(
                            aiMessageId,
                            GeminiErrorFormatter.politeUnavailableMessage(),
                            isStreaming = false,
                            isError = true
                        )
                    } else {
                        updateStreamingMessage(aiMessageId, accumulatedText, isStreaming = false)
                    }
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // If SSE streaming failed for a non-auth/non-quota reason, try non-streaming fallback once.
                val httpCode = (e as? HttpException)?.code()
                val nonStreamReply = if (httpCode !in setOf(400, 401, 403, 429)) {
                    Log.w(TAG, "Noor SSE streaming failed; attempting non-streaming fallback.")
                    fetchNonStreamingFallback(apiKey, request)
                } else {
                    null
                }

                _isLoading.value = false
                val existingId = streamingMessageId
                if (!nonStreamReply.isNullOrBlank()) {
                    if (!isPremium && !countedUsage) {
                        PremiumManager.incrementDailyMsgCount(context)
                    }
                    if (existingId != null) {
                        updateStreamingMessage(existingId, nonStreamReply, isStreaming = false, isError = false)
                    } else {
                        _messages.value = _messages.value + ChatMessage(
                            text = nonStreamReply,
                            isUser = false,
                            isStreaming = false
                        )
                    }
                } else {
                    Log.e(TAG, "Noor AI chat request failed: ${GeminiErrorFormatter.formatForLog(e, apiKey)}", e)
                    val politeError = GeminiErrorFormatter.politeUnavailableMessage()
                    if (existingId != null) {
                        updateStreamingMessage(existingId, politeError, isStreaming = false, isError = true)
                    } else {
                        _messages.value = _messages.value + ChatMessage(
                            text = politeError,
                            isUser = false,
                            isError = true
                        )
                    }
                }
            }
        }
    }

    /**
     * Calls Gemini SSE streaming on `gemini-3.5-flash-lite` first (fast and reliable),
     * retrying once on temporary HTTP 503 high demand or timeout, and falling back to
     * `gemini-3.5-flash` only for transient 503/404/timeout errors.
     * Auth/key errors (400/401/403) and quota errors (429) fail immediately with their exact message.
     */
    private suspend fun openStreamWithRetryAndFallback(
        apiKey: String,
        request: GenerateContentRequest
    ): ResponseBody {
        val attempts = listOf(
            PRIMARY_CHAT_MODEL to 0L,
            PRIMARY_CHAT_MODEL to 1200L,
            FALLBACK_CHAT_MODEL to 500L
        )
        var lastException: Exception? = null

        for ((model, backoffMs) in attempts) {
            if (backoffMs > 0L) delay(backoffMs)
            try {
                return withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContentStream(
                        model = model,
                        alt = "sse",
                        apiKey = apiKey,
                        request = request
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                val code = e.code()
                Log.w(TAG, "Gemini chat HTTP $code on model=$model")
                // Never retry invalid/revoked key or quota exhausted — surface immediately
                if (code in setOf(400, 401, 403, 429)) {
                    throw e
                }
                lastException = e
            } catch (e: SocketTimeoutException) {
                Log.w(TAG, "Gemini chat timed out on model=$model")
                lastException = e
            } catch (e: Exception) {
                // Real network / DNS / SSL error — fail immediately without hanging
                throw e
            }
        }

        throw lastException ?: IllegalStateException("Unable to connect to Noor AI.")
    }

    private suspend fun fetchNonStreamingFallback(
        apiKey: String,
        request: GenerateContentRequest
    ): String? {
        for (model in listOf(PRIMARY_CHAT_MODEL, FALLBACK_CHAT_MODEL)) {
            try {
                val resp = withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContent(
                        model = model,
                        apiKey = apiKey,
                        request = request
                    )
                }
                val text = resp.candidates.firstOrNull()?.content?.parts
                    ?.mapNotNull { it.text }
                    ?.joinToString("")
                    ?.trim()
                if (!text.isNullOrBlank()) {
                    return text
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                Log.w(TAG, "Gemini non-streaming fallback HTTP ${e.code()} on model=$model")
                if (e.code() in setOf(400, 401, 403, 429)) {
                    return null
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini non-streaming fallback failed on model=$model: ${e.javaClass.simpleName}")
            }
        }
        return null
    }

    private fun updateStreamingMessage(
        messageId: String,
        newText: String,
        isStreaming: Boolean,
        isError: Boolean = false
    ) {
        _messages.value = _messages.value.map { msg ->
            if (msg.id == messageId) {
                msg.copy(text = newText, isStreaming = isStreaming, isError = isError)
            } else {
                msg
            }
        }
    }
}
