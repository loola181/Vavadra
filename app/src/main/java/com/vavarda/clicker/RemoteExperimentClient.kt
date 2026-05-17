package com.vavarda.clicker

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal sealed class RemoteExperimentResult {
    object Default : RemoteExperimentResult()
    data class WelcomeUrl(val url: String) : RemoteExperimentResult()
}

internal object RemoteExperimentClient {

    private const val ENDPOINT =
        "https://qajwaizcjtqieltssyrl.supabase.co/functions/v1/client-config/sync"
    private const val PROBE_URL = "https://www.google.com/generate_204"
    private const val PROBE_TIMEOUT_MS = 8_000
    private const val SYNC_CONNECT_TIMEOUT_MS = 8_000
    private const val SYNC_READ_TIMEOUT_MS = 12_000
    private const val OVERALL_BUDGET_MS = 15_000L

    suspend fun resolve(context: Context): RemoteExperimentResult = withContext(Dispatchers.IO) {
        val outcome = withTimeoutOrNull(OVERALL_BUDGET_MS) {
            if (!hasNetwork(context)) return@withTimeoutOrNull RemoteExperimentResult.Default
            if (!reachableProbe()) return@withTimeoutOrNull RemoteExperimentResult.Default
            fetchVariant()
        }
        outcome ?: RemoteExperimentResult.Default
    }

    private fun hasNetwork(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val net = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(net) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            cm.activeNetworkInfo?.isConnected == true
        }
    }

    private fun reachableProbe(): Boolean {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(PROBE_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = PROBE_TIMEOUT_MS
                readTimeout = PROBE_TIMEOUT_MS
                instanceFollowRedirects = true
                requestMethod = "GET"
            }
            val code = conn.responseCode
            code in 200..399
        } catch (_: Throwable) {
            false
        } finally {
            conn?.disconnect()
        }
    }

    private fun fetchVariant(): RemoteExperimentResult {
        val payload = JSONObject().apply {
            put("device_locale", Locale.getDefault().toString())
            put("timezone", TimeZone.getDefault().id)
            put("user_agent", "Android OS ${Build.VERSION.RELEASE}")
            put("client_time", isoTimestamp())
        }.toString()

        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                connectTimeout = SYNC_CONNECT_TIMEOUT_MS
                readTimeout = SYNC_READ_TIMEOUT_MS
                requestMethod = "POST"
                doOutput = true
                doInput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }

            val code = conn.responseCode
            if (code !in 200..299) return RemoteExperimentResult.Default

            val body = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val json = JSONObject(body)
            val variant = json.optInt("variant", 0)
            val url = json.optString("welcome_url", "").trim()
            if (variant == 1 && url.isNotEmpty() && url.startsWith("http")) {
                RemoteExperimentResult.WelcomeUrl(url)
            } else {
                RemoteExperimentResult.Default
            }
        } catch (_: Throwable) {
            RemoteExperimentResult.Default
        } finally {
            conn?.disconnect()
        }
    }

    private fun isoTimestamp(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return fmt.format(Date())
    }
}
