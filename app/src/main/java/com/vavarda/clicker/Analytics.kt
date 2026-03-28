package com.vavarda.clicker

import android.util.Log

interface AnalyticsTracker {
    fun track(name: String, params: Map<String, Any?> = emptyMap())
}

class LocalAnalyticsTracker : AnalyticsTracker {
    override fun track(name: String, params: Map<String, Any?>) {
        val payload = if (params.isEmpty()) {
            ""
        } else {
            params.entries.joinToString(prefix = " | ") { (key, value) -> "$key=$value" }
        }
        Log.d(TAG, "event=$name$payload")
    }

    private companion object {
        const val TAG = "VavardaAnalytics"
    }
}
