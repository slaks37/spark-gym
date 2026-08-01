package com.sparkgym.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sparkgym.core.design.SparkColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The user's photo, or their initials when they have not set one.
 *
 * Decoding is done by hand rather than pulling in an image-loading library:
 * one avatar, one bitmap, and doing it here means the downsampling is explicit.
 * A 12 MP camera photo decoded at full size is 48 MB, so the loader always
 * subsamples down to roughly the size it will actually be drawn at.
 */
@Composable
fun ProfileAvatar(
    uri: String?,
    name: String,
    size: Dp,
    modifier: Modifier = Modifier,
    accent: Color = SparkColors.Cyan,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val targetPx = with(density) { size.roundToPx() }.coerceAtLeast(64)

    var bitmap by remember(uri, targetPx) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(uri, targetPx) {
        bitmap = uri?.let { withContext(Dispatchers.IO) { decodeScaled(context, it, targetPx) } }
    }

    val shape = CircleShape
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(accent.copy(alpha = 0.12f))
            .border(2.dp, accent.copy(alpha = 0.55f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        val bmp = bitmap
        when {
            bmp != null -> Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(shape)
            )

            name.isNotBlank() -> Text(
                text = name.trim().split(" ").take(2).mapNotNull { it.firstOrNull() }
                    .joinToString("").uppercase().ifBlank { "?" },
                color = accent,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.36f).sp,
                style = MaterialTheme.typography.headlineSmall
            )

            else -> Icon(
                Icons.Filled.AddAPhoto,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(size * 0.4f)
            )
        }

        // A small camera badge, so it reads as "tap me" without a caption.
        if (onClick != null) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.30f)
                    .clip(shape)
                    .background(accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.AddAPhoto,
                    contentDescription = null,
                    tint = SparkColors.Panel,
                    modifier = Modifier.size(size * 0.17f)
                )
            }
        }
    }
}

/**
 * Decodes at the smallest power-of-two subsample that still covers [targetPx].
 * Returns null on anything unreadable — a revoked permission, a deleted file —
 * so the caller silently falls back to initials rather than crashing.
 */
private fun decodeScaled(context: Context, uriString: String, targetPx: Int): Bitmap? = runCatching {
    val uri = Uri.parse(uriString)
    val resolver = context.contentResolver

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sample = 1
    while (bounds.outWidth / (sample * 2) >= targetPx && bounds.outHeight / (sample * 2) >= targetPx) {
        sample *= 2
    }

    val opts = BitmapFactory.Options().apply {
        inSampleSize = sample
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
}.getOrNull()
