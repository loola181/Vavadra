package com.vavarda.clicker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private object ProfileImageCache {
    private val bitmaps = object : LruCache<String, Bitmap>(18 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.allocationByteCount / 1024
        }
    }

    fun get(uriString: String): Bitmap? = bitmaps.get(uriString)

    fun put(uriString: String, bitmap: Bitmap) {
        bitmaps.put(uriString, bitmap)
    }

    fun remove(uriString: String) {
        bitmaps.remove(uriString)
    }
}

@Composable
internal fun rememberProfileImageBitmap(
    uriString: String
): ImageBitmap? {
    val context = LocalContext.current
    val resolvedUri = remember(uriString) { uriString.trim() }
    val cachedBitmap = remember(resolvedUri) { ProfileImageCache.get(resolvedUri) }
    val bitmap by produceState<Bitmap?>(initialValue = cachedBitmap, key1 = resolvedUri) {
        if (resolvedUri.isBlank() || value != null) return@produceState
        value = withContext(Dispatchers.IO) {
            decodeProfileBitmap(context, resolvedUri)
        }?.also { decodedBitmap ->
            ProfileImageCache.put(resolvedUri, decodedBitmap)
        }
    }
    return bitmap?.asImageBitmap()
}

internal fun clearProfileImageCache(uriString: String) {
    if (uriString.isNotBlank()) {
        ProfileImageCache.remove(uriString)
    }
}

private fun decodeProfileBitmap(
    context: Context,
    uriString: String
): Bitmap? {
    return runCatching {
        val uri = Uri.parse(uriString)
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, boundsOptions)
        }

        val displayMetrics = context.resources.displayMetrics
        val targetEdge = min(displayMetrics.widthPixels, displayMetrics.heightPixels).coerceAtLeast(320)
        val decodeOptions = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inSampleSize = calculateProfileInSampleSize(
                srcWidth = boundsOptions.outWidth,
                srcHeight = boundsOptions.outHeight,
                reqWidth = targetEdge,
                reqHeight = targetEdge
            )
        }

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, decodeOptions)
        }
    }.getOrNull()
}

private fun calculateProfileInSampleSize(
    srcWidth: Int,
    srcHeight: Int,
    reqWidth: Int,
    reqHeight: Int
): Int {
    if (srcWidth <= 0 || srcHeight <= 0 || reqWidth <= 0 || reqHeight <= 0) {
        return 1
    }

    var inSampleSize = 1
    var halfWidth = srcWidth / 2
    var halfHeight = srcHeight / 2

    while (halfWidth / inSampleSize >= reqWidth && halfHeight / inSampleSize >= reqHeight) {
        inSampleSize *= 2
    }

    return inSampleSize.coerceAtLeast(1)
}
