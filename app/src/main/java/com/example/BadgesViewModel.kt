package com.example

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class CustomBadge(
    val id: String = "",
    val name: String = "",
    val requiredDays: Int = 0,
    val description: String = "",
    val imageUrl: String = ""
)

val DEFAULT_BADGES = listOf(
    CustomBadge("default_0", "Clown", 0, "Initial stage", "res://badge_0"),
    CustomBadge("default_1", "New Warrior", 1, "1 day clean", "res://badge_1"),
    CustomBadge("default_2", "Novice", 3, "3 days clean", "res://badge_2"),
    CustomBadge("default_3", "Average", 7, "7 days clean", "res://badge_3"),
    CustomBadge("default_4", "Advanced", 15, "15 days clean", "res://badge_4"),
    CustomBadge("default_5", "Sigma", 30, "1 month clean", "res://badge_5"),
    CustomBadge("default_6", "Chad", 45, "45 days clean", "res://badge_6"),
    CustomBadge("default_7", "Absolute Chad", 60, "2 months clean", "res://badge_7"),
    CustomBadge("default_8", "Giga Chad", 120, "4 months clean", "res://badge_8"),
    CustomBadge("default_9", "Absolute Giga Chad", 365, "1 year clean", "res://badge_9")
)

class BadgesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _badges = MutableStateFlow<List<CustomBadge>>(DEFAULT_BADGES)
    val badges = _badges.asStateFlow()

    init {
        fetchBadges()
    }

    private fun fetchBadges() {
        try {
            db.collection("badges")
                .orderBy("requiredDays")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("BadgesViewModel", "Listen failed. Default badges retained.", e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val badgeList = snapshot.documents.mapNotNull {
                            it.toObject(CustomBadge::class.java)?.copy(id = it.id)
                        }
                        if (badgeList.isNotEmpty()) {
                            _badges.value = badgeList
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("BadgesViewModel", "Error fetching badges", e)
        }
    }

    private fun setupDefaultBadges() {
        val defaultBadges = listOf(
            CustomBadge("", "Clown", 0, "Clown"),
            CustomBadge("", "Novice", 3, "Novice"),
            CustomBadge("", "Apprentice", 7, "Apprentice"),
            CustomBadge("", "Warrior", 14, "Warrior"),
            CustomBadge("", "Sigma", 21, "Sigma"),
            CustomBadge("", "Alpha", 30, "Alpha"),
            CustomBadge("", "Chad", 45, "Chad"),
            CustomBadge("", "Absolute Chad", 60, "Absolute Chad"),
            CustomBadge("", "Giga Chad", 120, "Giga Chad"),
            CustomBadge("", "Absolute Giga Chad", 365, "Absolute Giga Chad")
        )
        defaultBadges.forEach { badge ->
            db.collection("badges").add(badge)
        }
    }

    fun addBadge(context: Context, name: String, requiredDays: Int, description: String, imageUri: Uri?) {
        if (imageUri != null) {
            val base64 = resizeAndConvertToBase64(context, imageUri)
            db.collection("badges").add(CustomBadge(name = name, requiredDays = requiredDays, description = description, imageUrl = base64))
        } else {
            db.collection("badges").add(CustomBadge(name = name, requiredDays = requiredDays, description = description))
        }
    }

    fun updateBadge(context: Context, id: String, name: String, requiredDays: Int, description: String, imageUri: Uri?, oldImageUrl: String) {
        if (imageUri != null && !imageUri.toString().startsWith("http") && !imageUri.toString().startsWith("data:")) { // New local Uri
            val base64 = resizeAndConvertToBase64(context, imageUri)
            db.collection("badges").document(id).update(
                mapOf("name" to name, "requiredDays" to requiredDays, "description" to description, "imageUrl" to base64)
            )
        } else {
            db.collection("badges").document(id).update(
                mapOf("name" to name, "requiredDays" to requiredDays, "description" to description, "imageUrl" to oldImageUrl)
            )
        }
    }
    
    private fun resizeAndConvertToBase64(context: Context, uri: Uri): String {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            
            val maxDim = 256
            val scale = Math.min(maxDim.toFloat() / originalBitmap.width, maxDim.toFloat() / originalBitmap.height)
            val width = Math.round(scale * originalBitmap.width)
            val height = Math.round(scale * originalBitmap.height)
            
            val resizedBitmap = android.graphics.Bitmap.createScaledBitmap(originalBitmap, width, height, true)
            
            val outputStream = java.io.ByteArrayOutputStream()
            resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
            val bytes = outputStream.toByteArray()
            
            return "data:image/jpeg;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("BadgesViewModel", "Image conversion failed", e)
            return ""
        }
    }

    fun deleteBadge(id: String) {
        db.collection("badges").document(id).delete()
    }
}
