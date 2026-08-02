package com.sparkgym.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    suspend fun saveAvatar(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val bitmap = resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: return@withContext null
            
            // scale down to a reasonable avatar size (max 512x512)
            val maxDim = 512f
            val scale = maxDim / maxOf(bitmap.width, bitmap.height)
            val resized = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt(),
                    (bitmap.height * scale).toInt(),
                    true
                )
            } else {
                bitmap
            }

            val file = File(context.filesDir, "avatar.jpg")
            FileOutputStream(file).use { out ->
                resized.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            if (resized != bitmap) {
                resized.recycle()
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
