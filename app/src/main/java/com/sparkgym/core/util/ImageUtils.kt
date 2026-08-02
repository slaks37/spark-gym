package com.sparkgym.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Copies a picked image into app storage, downscaled.
 *
 * A copy rather than the content:// URI it came from: the URI grant can be
 * revoked and the original can be deleted from the gallery, either of which
 * leaves a picture the app can no longer read.
 */
object ImageUtils {

    /** Where progress photos live, so they can be found and cleaned up. */
    private const val PROGRESS_PREFIX = "progress_"

    /**
     * The profile photo. One fixed name — there is only ever one, and replacing
     * it should replace it rather than accumulate.
     */
    suspend fun saveAvatar(context: Context, uri: Uri): String? =
        save(context, uri, "avatar.jpg", maxDim = 512)

    /**
     * A progress photo, under a name of its own.
     *
     * These used to go through saveAvatar, which writes to a fixed `avatar.jpg`.
     * That meant adding a progress photo overwrote the user's profile picture,
     * every row in the gallery pointed at the same file, and each new photo
     * destroyed the one before it — a history feature that kept no history.
     *
     * Saved larger than the avatar: the whole point is comparing them later.
     */
    suspend fun saveProgressPhoto(context: Context, uri: Uri): String? =
        save(context, uri, "$PROGRESS_PREFIX${System.currentTimeMillis()}.jpg", maxDim = 1440)

    /** Removes a photo's file. Safe to call for one already gone. */
    fun delete(path: String?): Boolean = runCatching {
        path != null && File(path).let { it.exists() && it.delete() }
    }.getOrDefault(false)

    private suspend fun save(
        context: Context,
        uri: Uri,
        fileName: String,
        maxDim: Int
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver

            // Measure first, then decode subsampled: a 12 MP photo decoded at
            // full size is 48 MB and can take the process down on a small phone.
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null

            var sample = 1
            while (bounds.outWidth / (sample * 2) >= maxDim && bounds.outHeight / (sample * 2) >= maxDim) {
                sample *= 2
            }

            val decoded = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply {
                    inSampleSize = sample
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                })
            } ?: return@runCatching null

            val longest = maxOf(decoded.width, decoded.height)
            val scale = maxDim.toFloat() / longest
            val resized = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    decoded,
                    (decoded.width * scale).toInt().coerceAtLeast(1),
                    (decoded.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else {
                decoded
            }

            val file = File(context.filesDir, fileName)
            FileOutputStream(file).use { out ->
                resized.compress(Bitmap.CompressFormat.JPEG, 88, out)
            }
            if (resized !== decoded) resized.recycle()
            decoded.recycle()
            file.absolutePath
        }.getOrNull()
    }
}
