package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

object NotificationHelper {
    private var cachedLogoBitmap: Bitmap? = null

    /**
     * Generates a crisp, high-resolution circular/squircle icon Bitmap of the app logo
     * with vibrant brand colors suitable for notification large icons.
     */
    @Synchronized
    fun getAppLogoBitmap(context: Context): Bitmap {
        cachedLogoBitmap?.let {
            if (!it.isRecycled) return it
        }

        val size = 192 // 192x192 px for sharp largeIcon
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw clean rounded light background surface
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        val rect = RectF(4f, 4f, size.toFloat() - 4f, size.toFloat() - 4f)
        val cornerRadius = 38f
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

        // Draw vector logo centered with safe padding
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_app_logo)
        if (drawable != null) {
            val padding = 16
            drawable.setBounds(padding, padding, size - padding, size - padding)
            drawable.draw(canvas)
        }

        cachedLogoBitmap = bitmap
        return bitmap
    }

    /**
     * Applies standard app branding to any notification builder:
     * - Status bar small icon (ic_notification_logo)
     * - Brand tint color (#0284C7)
     * - Colored rich app logo largeIcon
     */
    fun applyAppBranding(builder: NotificationCompat.Builder, context: Context): NotificationCompat.Builder {
        return builder
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setColor(0xFF0284C7.toInt())
            .setLargeIcon(getAppLogoBitmap(context))
    }
}
