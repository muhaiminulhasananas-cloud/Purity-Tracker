package com.example

import android.os.Handler
import android.os.Looper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * ============================================================================
 * EmailJS Configuration (Free Tier — 200 emails/month, no Blaze plan required)
 * ============================================================================
 * Replace the three placeholder strings below with your own values from
 * https://dashboard.emailjs.com :
 *   1. SERVICE_ID  -> Email Services -> Service ID (e.g. "service_xxxxxxx")
 *   2. TEMPLATE_ID -> Email Templates -> Template ID (e.g. "template_xxxxxxx")
 *   3. PUBLIC_KEY  -> Account -> General -> Public Key (e.g. "xxxxxxxxxxxxxxx")
 *
 * IMPORTANT: NEVER put your EmailJS Private Key in the app. Only the Public Key
 * (`user_id`) belongs here.
 */
object EmailJsConfig {
    const val SERVICE_ID = "service_uxk7xgr"
    const val TEMPLATE_ID = "template_ik11piq"
    const val PUBLIC_KEY = "eWcmrfHKfv-9jKkec"
}

/**
 * Sends Accountability Partner approval emails directly from the app via the
 * EmailJS REST API (`https://api.emailjs.com/api/v1.0/email/send`) using OkHttp.
 */
object EmailJsSender {

    private const val EMAILJS_SEND_URL = "https://api.emailjs.com/api/v1.0/email/send"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Builds the EmailJS JSON payload for unit testing and network dispatch.
     */
    fun buildRequestJson(
        toEmail: String,
        subject: String,
        messageText: String,
        messageHtml: String,
        serviceId: String = EmailJsConfig.SERVICE_ID,
        templateId: String = EmailJsConfig.TEMPLATE_ID,
        publicKey: String = EmailJsConfig.PUBLIC_KEY
    ): JSONObject {
        val templateParams = JSONObject().apply {
            put("to_email", toEmail)
            put("subject", subject)
            put("message_text", messageText)
            put("message_html", messageHtml)
        }
        return JSONObject().apply {
            put("service_id", serviceId)
            put("template_id", templateId)
            put("user_id", publicKey)
            put("template_params", templateParams)
        }
    }

    /**
     * Asynchronously sends an email via EmailJS off the main thread and posts
     * the result back to [mainHandler].
     *
     * Reports delivery honestly:
     * - HTTP 2xx -> `onComplete(true, "Request sent to <toEmail>. Waiting for partner approval.")`
     * - Network error or non-2xx -> `onComplete(false, "<clear error message>")`
     */
    fun sendEmail(
        toEmail: String,
        subject: String,
        messageText: String,
        messageHtml: String,
        serviceId: String = EmailJsConfig.SERVICE_ID,
        templateId: String = EmailJsConfig.TEMPLATE_ID,
        publicKey: String = EmailJsConfig.PUBLIC_KEY,
        endpointUrl: String = EMAILJS_SEND_URL,
        onComplete: (Boolean, String) -> Unit
    ) {
        val payload = try {
            buildRequestJson(
                toEmail = toEmail,
                subject = subject,
                messageText = messageText,
                messageHtml = messageHtml,
                serviceId = serviceId,
                templateId = templateId,
                publicKey = publicKey
            ).toString()
        } catch (e: Exception) {
            mainHandler.post {
                onComplete(false, "Failed to build email request: ${e.localizedMessage ?: "Invalid payload"}")
            }
            return
        }

        val requestBody = payload.toRequestBody(JSON_MEDIA_TYPE)
        val request = try {
            Request.Builder()
                .url(endpointUrl)
                .header("Content-Type", "application/json")
                .header("Origin", "http://localhost")
                .post(requestBody)
                .build()
        } catch (e: Exception) {
            mainHandler.post {
                onComplete(false, "Failed to send email: ${e.localizedMessage ?: "Invalid EmailJS URL"}")
            }
            return
        }

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post {
                    onComplete(
                        false,
                        "Failed to send email to $toEmail: ${e.localizedMessage ?: "Network error"}"
                    )
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    val responseText = try {
                        res.body?.string()?.trim().orEmpty()
                    } catch (e: Exception) {
                        ""
                    }

                    if (res.isSuccessful) {
                        mainHandler.post {
                            onComplete(true, "Request sent to $toEmail. Waiting for partner approval.")
                        }
                    } else {
                        val detail = if (responseText.isNotEmpty()) {
                            responseText
                        } else {
                            "HTTP ${res.code}"
                        }
                        mainHandler.post {
                            onComplete(
                                false,
                                "Failed to send email to $toEmail (EmailJS error: $detail)"
                            )
                        }
                    }
                }
            }
        })
    }
}
