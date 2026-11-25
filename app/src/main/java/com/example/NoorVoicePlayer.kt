package com.example

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException

@Serializable
data class GeminiTtsRequest(
    val contents: List<Content>,
    val generationConfig: TtsGenerationConfig
)

@Serializable
data class TtsGenerationConfig(
    val responseModalities: List<String> = listOf("AUDIO"),
    val speechConfig: SpeechConfig
)

@Serializable
data class SpeechConfig(
    val voiceConfig: VoiceConfig
)

@Serializable
data class VoiceConfig(
    val prebuiltVoiceConfig: PrebuiltVoiceConfig
)

@Serializable
data class PrebuiltVoiceConfig(
    val voiceName: String = NoorVoicePlayer.VOICE_NAME_PEGASUS
)

data class SynthesizedAudio(
    val audioBytes: ByteArray,
    val mimeType: String,
    val sampleRateHz: Int,
    val channelCount: Int
)

class GeminiApiException(
    val httpCode: Int,
    val apiStatus: String?,
    val apiMessage: String?
) : Exception(apiMessage ?: "Gemini API error (HTTP $httpCode)")

/**
 * Shared error formatter for both Noor AI chat and Noor Pegasus voice.
 * Full redacted diagnostics are logged strictly to Logcat via [formatForLog];
 * user-facing UI always receives a polite, non-technical apology in the user's selected language.
 */
object GeminiErrorFormatter {
    private const val LOG_TAG = "NoorAI"

    fun politeUnavailableMessage(): String = LanguageStore.get("ai_missing_key")

    fun parseGoogleErrorBody(rawBody: String?): Pair<String?, String?> {
        if (rawBody.isNullOrBlank()) return null to null
        return try {
            val root = Json.parseToJsonElement(rawBody).jsonObject
            val errObj = root["error"]?.jsonObject ?: return null to null
            val status = errObj["status"]?.jsonPrimitive?.content
            val msg = errObj["message"]?.jsonPrimitive?.content
            status to msg
        } catch (_: Exception) {
            null to rawBody.take(180).trim()
        }
    }

    /**
     * Logs the redacted technical diagnostic to Logcat and returns a polite, non-technical message
     * for display to the user.
     */
    fun format(throwable: Throwable, apiKeyToRedact: String = ""): String {
        val diagnostic = formatForLog(throwable, apiKeyToRedact)
        try {
            Log.e(LOG_TAG, diagnostic, throwable)
        } catch (_: Throwable) {
            // Safe fallback in pure JVM unit tests where android.util.Log is stubbed.
        }
        return politeUnavailableMessage()
    }

    /**
     * Formats a detailed, key-redacted diagnostic message intended strictly for Logcat debugging.
     */
    fun formatForLog(throwable: Throwable, apiKeyToRedact: String = ""): String {
        val rawCode: Int
        val apiStatus: String?
        val apiMsg: String?

        when (throwable) {
            is GeminiApiException -> {
                rawCode = throwable.httpCode
                apiStatus = throwable.apiStatus
                apiMsg = throwable.apiMessage
            }
            is HttpException -> {
                rawCode = throwable.code()
                val errBody = try {
                    throwable.response()?.errorBody()?.string()
                } catch (_: Exception) {
                    null
                }
                val parsed = parseGoogleErrorBody(errBody)
                apiStatus = parsed.first
                apiMsg = parsed.second
            }
            is SocketTimeoutException -> {
                return "Request to Noor AI timed out."
            }
            is UnknownHostException, is ConnectException, is NoRouteToHostException -> {
                return "Unable to reach Gemini servers (DNS/connect error)."
            }
            is SSLException -> {
                return "SSL handshake failed while contacting Gemini."
            }
            is IOException -> {
                val msg = sanitize(throwable.localizedMessage, apiKeyToRedact)
                return if (msg.isNotBlank()) {
                    "Network IO error while contacting Noor AI: $msg"
                } else {
                    "Network IO error while contacting Noor AI."
                }
            }
            else -> {
                val msg = sanitize(throwable.localizedMessage, apiKeyToRedact)
                return if (msg.isNotBlank()) {
                    "Noor AI unexpected error: $msg"
                } else {
                    "Noor AI unexpected error."
                }
            }
        }

        val cleanMsg = sanitize(apiMsg, apiKeyToRedact)
        val combined = "${apiStatus.orEmpty()} $cleanMsg".lowercase()

        return when {
            rawCode == 400 && (
                combined.contains("api key") ||
                combined.contains("api_key_invalid") ||
                combined.contains("key expired") ||
                combined.contains("key invalid")
            ) -> {
                "Gemini API key is invalid or expired (HTTP 400): $cleanMsg"
            }
            rawCode == 401 || rawCode == 403 -> {
                if (combined.contains("quota") || combined.contains("billing")) {
                    "Gemini API quota or billing issue (HTTP $rawCode): ${cleanMsg.ifBlank { "Check billing/quota." }}"
                } else {
                    "Gemini API key was rejected or revoked (HTTP $rawCode): ${cleanMsg.ifBlank { "Check key permissions." }}"
                }
            }
            rawCode == 429 || combined.contains("resource_exhausted") || combined.contains("quota") || combined.contains("rate limit") -> {
                "Gemini API quota or rate limit reached (HTTP $rawCode): $cleanMsg"
            }
            rawCode == 404 -> {
                "Gemini model endpoint not found (HTTP 404): ${cleanMsg.ifBlank { "Model unavailable." }}"
            }
            rawCode == 500 || rawCode == 502 || rawCode == 503 || rawCode == 504 -> {
                "Gemini servers temporarily busy (HTTP $rawCode): $cleanMsg"
            }
            rawCode == 400 -> {
                "Gemini API rejected request (HTTP 400): ${cleanMsg.ifBlank { "Invalid request parameters." }}"
            }
            else -> {
                "Gemini API error (HTTP $rawCode): ${cleanMsg.ifBlank { "Unexpected server response." }}"
            }
        }
    }

    private fun sanitize(text: String?, apiKey: String): String {
        if (text.isNullOrBlank()) return ""
        var out = text.trim()
        if (apiKey.isNotBlank()) {
            out = out.replace(apiKey, "[REDACTED]")
        }
        return out.replace(Regex("key=[^&\\s]+"), "key=[REDACTED]")
    }
}

/**
 * Generates and plays Noor AI's spoken replies exclusively via Gemini TTS using the "Pegasus" voice.
 * Never uses Android's built-in system TextToSpeech engine.
 */
object NoorVoicePlayer {
    private const val TAG = "NoorVoicePlayer"
    const val VOICE_NAME_PEGASUS = "Pegasus"

    // gemini-3.1-flash-tts-preview natively supports voice_name "Pegasus" (Engaged, deeper voice).
    // gemini-2.5-flash-preview-tts is kept only as a fallback if 3.1 returns 404 on an older tier.
    const val PRIMARY_TTS_MODEL = "gemini-3.1-flash-tts-preview"
    const val FALLBACK_TTS_MODEL = "gemini-2.5-flash-preview-tts"

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    private const val MAX_TTS_CHARS = 3500
    private const val MAX_CACHE_ENTRIES = 16

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    // In-memory LRU cache so replaying a message plays immediately without extra API calls
    private val audioCache = object : LinkedHashMap<String, SynthesizedAudio>(MAX_CACHE_ENTRIES, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, SynthesizedAudio>?): Boolean {
            return size > MAX_CACHE_ENTRIES
        }
    }

    @Volatile
    private var activeAudioTrack: AudioTrack? = null

    @Volatile
    private var activeMediaPlayer: MediaPlayer? = null

    fun buildTtsRequestJson(text: String, voiceName: String = VOICE_NAME_PEGASUS): String {
        val cleanText = cleanTextForSpeech(text)
        val request = GeminiTtsRequest(
            contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(Part(text = cleanText))
                )
            ),
            generationConfig = TtsGenerationConfig(
                responseModalities = listOf("AUDIO"),
                speechConfig = SpeechConfig(
                    voiceConfig = VoiceConfig(
                        prebuiltVoiceConfig = PrebuiltVoiceConfig(voiceName = voiceName)
                    )
                )
            )
        )
        return json.encodeToString(request)
    }

    /**
     * Strips Markdown syntax so Noor's Pegasus voice speaks smoothly without reading asterisks or formatting symbols.
     */
    fun cleanTextForSpeech(raw: String): String {
        val cleaned = raw
            .replace(Regex("```[\\s\\S]*?```"), " ")
            .replace(Regex("`([^`]*)`"), "$1")
            .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")
            .replace(Regex("__([^_]+)__"), "$1")
            .replace(Regex("(?m)^\\s{0,3}#{1,6}\\s+"), "")
            .replace(Regex("(?m)^\\s*[-*•]\\s+"), "")
            .replace("*", "")
            .replace(Regex("\\s{2,}"), " ")
            .trim()
        return if (cleaned.length > MAX_TTS_CHARS) {
            cleaned.take(MAX_TTS_CHARS)
        } else {
            cleaned
        }
    }

    /**
     * Parses PCM sample rate and channel count from a Gemini TTS mimeType like
     * "audio/l16; rate=24000; channels=1" or "audio/L16;codec=pcm;rate=24000".
     */
    fun parseAudioMimeParams(mimeType: String): Pair<Int, Int> {
        val rate = Regex("rate=(\\d+)", RegexOption.IGNORE_CASE)
            .find(mimeType)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 24000
        val channels = Regex("channels=(\\d+)", RegexOption.IGNORE_CASE)
            .find(mimeType)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1
        return rate to channels
    }

    suspend fun synthesizePegasusAudio(
        messageId: String,
        text: String,
        apiKey: String
    ): SynthesizedAudio = withContext(Dispatchers.IO) {
        synchronized(audioCache) {
            audioCache[messageId]?.let { return@withContext it }
        }

        val cleanText = cleanTextForSpeech(text)
        if (cleanText.isBlank()) {
            throw IllegalArgumentException("There is no readable text in this message.")
        }

        val requestJson = buildTtsRequestJson(cleanText, VOICE_NAME_PEGASUS)

        // Primary: gemini-3.1-flash-tts-preview with voiceName = "Pegasus" (retries once on transient 503)
        var lastError: Throwable? = null
        val attempts = listOf(
            PRIMARY_TTS_MODEL to 0L,
            PRIMARY_TTS_MODEL to 1500L
        )

        for ((model, backoffMs) in attempts) {
            if (backoffMs > 0L) delay(backoffMs)
            try {
                val audio = requestTtsFromModel(model, requestJson, apiKey)
                synchronized(audioCache) {
                    audioCache[messageId] = audio
                }
                return@withContext audio
            } catch (e: CancellationException) {
                throw e
            } catch (e: GeminiApiException) {
                lastError = e
                Log.e(TAG, "Gemini TTS failed on $model (HTTP ${e.httpCode}): ${e.apiMessage}")
                // Do not retry auth/key/quota errors
                if (e.httpCode in setOf(400, 401, 403, 429)) {
                    throw e
                }
                // If primary model is 404, try fallback model with Pegasus
                if (e.httpCode == 404) {
                    break
                }
            } catch (e: Exception) {
                lastError = e
                Log.e(TAG, "Gemini TTS network/io error on $model", e)
                if (e !is SocketTimeoutException) {
                    throw e
                }
            }
        }

        // Secondary fallback only if primary returned 404
        if ((lastError as? GeminiApiException)?.httpCode == 404) {
            val audio = requestTtsFromModel(FALLBACK_TTS_MODEL, requestJson, apiKey)
            synchronized(audioCache) {
                audioCache[messageId] = audio
            }
            return@withContext audio
        }

        throw lastError ?: IllegalStateException("Failed to generate Pegasus voice audio.")
    }

    private fun requestTtsFromModel(
        model: String,
        requestJson: String,
        apiKey: String
    ): SynthesizedAudio {
        val url = "$BASE_URL$model:generateContent?key=$apiKey"
        val body = requestJson.toRequestBody("application/json; charset=utf-8".toMediaType())
        val httpRequest = Request.Builder()
            .url(url)
            .post(body)
            .build()

        httpClient.newCall(httpRequest).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val (status, msg) = GeminiErrorFormatter.parseGoogleErrorBody(responseBody)
                throw GeminiApiException(response.code, status, msg)
            }

            val root: JsonObject = Json.parseToJsonElement(responseBody).jsonObject
            val parts = root["candidates"]?.jsonArray
                ?.firstOrNull()?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("parts")?.jsonArray
                ?: throw IllegalStateException("Gemini TTS returned no audio candidates.")

            var base64Audio: String? = null
            var mimeType = "audio/l16; rate=24000; channels=1"
            for (partEl in parts) {
                val inlineData = partEl.jsonObject["inlineData"]?.jsonObject
                    ?: partEl.jsonObject["inline_data"]?.jsonObject
                if (inlineData != null) {
                    val dataStr = inlineData["data"]?.jsonPrimitive?.content
                    if (!dataStr.isNullOrBlank()) {
                        base64Audio = dataStr
                        mimeType = inlineData["mimeType"]?.jsonPrimitive?.content
                            ?: inlineData["mime_type"]?.jsonPrimitive?.content
                            ?: mimeType
                        break
                    }
                }
            }

            if (base64Audio.isNullOrBlank()) {
                throw IllegalStateException("Gemini TTS response did not include audio data.")
            }

            val rawBytes = Base64.decode(base64Audio, Base64.DEFAULT)
            if (rawBytes.isEmpty()) {
                throw IllegalStateException("Gemini TTS returned empty audio bytes.")
            }

            val (sampleRate, channels) = parseAudioMimeParams(mimeType)
            return SynthesizedAudio(
                audioBytes = rawBytes,
                mimeType = mimeType,
                sampleRateHz = sampleRate,
                channelCount = channels
            )
        }
    }

    /**
     * Plays synthesized Gemini Pegasus audio synchronously on Dispatchers.IO until completion or cancellation.
     */
    suspend fun playAudio(context: Context, audio: SynthesizedAudio) = withContext(Dispatchers.IO) {
        stopPlayback()
        val mimeLower = audio.mimeType.lowercase()
        val isRawPcm = mimeLower.contains("l16") || mimeLower.contains("pcm")

        if (isRawPcm) {
            playRawPcm(audio.audioBytes, audio.sampleRateHz, audio.channelCount)
        } else {
            playEncodedAudio(context, audio.audioBytes, mimeLower)
        }
    }

    private suspend fun playRawPcm(pcmBytes: ByteArray, sampleRateHz: Int, channelCount: Int) {
        val channelConfig = if (channelCount >= 2) {
            AudioFormat.CHANNEL_OUT_STEREO
        } else {
            AudioFormat.CHANNEL_OUT_MONO
        }
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRateHz,
            channelConfig,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        val audioFormat = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRateHz)
            .setChannelMask(channelConfig)
            .build()

        val track = AudioTrack.Builder()
            .setAudioAttributes(audioAttributes)
            .setAudioFormat(audioFormat)
            .setBufferSizeInBytes(minBuf * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        activeAudioTrack = track
        try {
            track.play()
            val chunkSize = 4096
            var offset = 0
            while (offset < pcmBytes.size && currentCoroutineContext().isActive) {
                val toWrite = minOf(chunkSize, pcmBytes.size - offset)
                val written = track.write(pcmBytes, offset, toWrite)
                if (written < 0) break
                offset += written
            }
            if (currentCoroutineContext().isActive) {
                // Allow hardware audio buffer to finish draining cleanly
                val bytesPerFrame = 2 * channelCount
                val drainMs = ((minBuf.toDouble() / (sampleRateHz * bytesPerFrame)) * 1000.0).toLong().coerceIn(80L, 350L)
                delay(drainMs)
            }
        } finally {
            try {
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.pause()
                    track.flush()
                    track.stop()
                }
            } catch (_: Exception) {}
            try {
                track.release()
            } catch (_: Exception) {}
            if (activeAudioTrack === track) {
                activeAudioTrack = null
            }
        }
    }

    private suspend fun playEncodedAudio(context: Context, bytes: ByteArray, mimeLower: String) {
        val ext = when {
            mimeLower.contains("wav") -> "wav"
            mimeLower.contains("ogg") -> "ogg"
            else -> "mp3"
        }
        val tempFile = File(context.cacheDir, "noor_pegasus_${System.currentTimeMillis()}.$ext")
        tempFile.writeBytes(bytes)

        val player = MediaPlayer()
        activeMediaPlayer = player
        try {
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            player.setDataSource(tempFile.absolutePath)
            player.prepare()
            player.start()
            while (player.isPlaying && currentCoroutineContext().isActive) {
                delay(80L)
            }
        } finally {
            try {
                if (player.isPlaying) player.stop()
            } catch (_: Exception) {}
            try {
                player.release()
            } catch (_: Exception) {}
            if (activeMediaPlayer === player) {
                activeMediaPlayer = null
            }
            try {
                tempFile.delete()
            } catch (_: Exception) {}
        }
    }

    fun stopPlayback() {
        val track = activeAudioTrack
        activeAudioTrack = null
        if (track != null) {
            try {
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.pause()
                    track.flush()
                    track.stop()
                }
            } catch (_: Exception) {}
            try {
                track.release()
            } catch (_: Exception) {}
        }

        val player = activeMediaPlayer
        activeMediaPlayer = null
        if (player != null) {
            try {
                if (player.isPlaying) player.stop()
            } catch (_: Exception) {}
            try {
                player.release()
            } catch (_: Exception) {}
        }
    }
}
