package com.vavarda.clicker

import android.content.Context
import android.util.Log

private const val CONFIG_LOG_TAG = "VavardaConfig"

internal inline fun <T> loadJsonAssetOrDefault(
    context: Context,
    assetName: String,
    defaultValue: () -> T,
    parser: (String) -> T
): T {
    return runCatching {
        val raw = context.assets.open(assetName).bufferedReader().use { it.readText() }
        parser(raw)
    }.getOrElse { error ->
        Log.w(CONFIG_LOG_TAG, "Failed to load $assetName. Falling back to defaults.", error)
        defaultValue()
    }
}
