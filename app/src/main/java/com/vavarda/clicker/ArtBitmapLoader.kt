package com.vavarda.clicker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private object ArtBitmapCache {
    private val bitmaps = object : LruCache<Int, Bitmap>(24 * 1024) {
        override fun sizeOf(key: Int, value: Bitmap): Int {
            return value.allocationByteCount / 1024
        }
    }

    fun get(resourceId: Int): Bitmap? = bitmaps.get(resourceId)

    fun put(resourceId: Int, bitmap: Bitmap) {
        bitmaps.put(resourceId, bitmap)
    }
}

private val artDrawableIds = mapOf(
    "ic_launcher_foreground" to R.drawable.ic_launcher_foreground,
    "vavarda_arcane_cache" to R.drawable.vavarda_arcane_cache,
    "vavarda_boss_rift_warden" to R.drawable.vavarda_boss_rift_warden,
    "vavarda_dark_legion" to R.drawable.vavarda_dark_legion,
    "vavarda_light_guardians" to R.drawable.vavarda_light_guardians,
    "vavarda_lvl_1" to R.drawable.vavarda_lvl_1,
    "vavarda_lvl_2" to R.drawable.vavarda_lvl_2,
    "vavarda_lvl_3" to R.drawable.vavarda_lvl_3,
    "vavarda_lvl_4" to R.drawable.vavarda_lvl_4,
    "vavarda_lvl_5" to R.drawable.vavarda_lvl_5,
    "vavarda_lvl_6" to R.drawable.vavarda_lvl_6,
    "vavarda_lvl_7" to R.drawable.vavarda_lvl_7,
    "vavarda_lvl_8" to R.drawable.vavarda_lvl_8,
    "vavarda_lvl_9" to R.drawable.vavarda_lvl_9,
    "vavarda_lvl_10" to R.drawable.vavarda_lvl_10,
    "vavarda_lvl_25" to R.drawable.vavarda_lvl_25,
    "vavarda_lvl_50" to R.drawable.vavarda_lvl_50,
    "vavarda_lvl_75" to R.drawable.vavarda_lvl_75,
    "vavarda_lvl_100" to R.drawable.vavarda_lvl_100,
    "vavarda_relic_vault" to R.drawable.vavarda_relic_vault,
    "vavarda_return_streak" to R.drawable.vavarda_return_streak,
    "vavarda_season_altar" to R.drawable.vavarda_season_altar
)

@Composable
internal fun rememberArtBitmap(
    drawableName: String
): Pair<Int, ImageBitmap?> {
    val context = LocalContext.current
    val resourceId = remember(drawableName) {
        artDrawableIds[drawableName] ?: 0
    }
    val cachedBitmap = remember(resourceId) { ArtBitmapCache.get(resourceId) }
    val bitmap by produceState<Bitmap?>(initialValue = cachedBitmap, key1 = resourceId) {
        if (resourceId == 0 || value != null) return@produceState
        value = withContext(Dispatchers.IO) {
            decodeBitmapResource(context, resourceId)
        }?.also { decodedBitmap ->
            ArtBitmapCache.put(resourceId, decodedBitmap)
        }
    }

    return resourceId to bitmap?.asImageBitmap()
}

private fun decodeBitmapResource(
    context: Context,
    resourceId: Int
): Bitmap? {
    return runCatching {
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(context.resources, resourceId, boundsOptions)

        val displayMetrics = context.resources.displayMetrics
        val decodeOptions = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inSampleSize = calculateInSampleSize(
                srcWidth = boundsOptions.outWidth,
                srcHeight = boundsOptions.outHeight,
                reqWidth = displayMetrics.widthPixels,
                reqHeight = displayMetrics.heightPixels
            )
        }
        BitmapFactory.decodeResource(context.resources, resourceId, decodeOptions)
    }.getOrNull()
}

private fun calculateInSampleSize(
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
