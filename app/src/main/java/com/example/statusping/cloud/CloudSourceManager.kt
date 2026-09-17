package com.example.statusping.cloud

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

sealed interface UpdateResult {
    data class Success(val ips: List<String>, val updated: Boolean, val message: String) : UpdateResult
    data class Error(val error: String) : UpdateResult
}

class CloudSourceManager(private val context: Context) {
    companion object {
        // Internal data source URL (never revealed in the UI)
        private const val SOURCE_URL = "https://raw.githubusercontent.com/Durgaa17/Raam-Public-Vless/refs/heads/main/scaniplist.txt"
        private const val PREFS_NAME = "cloud_source_prefs"
        private const val KEY_LAST_ETAG = "last_etag"
        private const val KEY_LAST_HASH = "last_hash"
        private const val FILE_NAME = "scaniplist.txt"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val localFile = File(context.filesDir, FILE_NAME)

    fun hasLocalCopy(): Boolean = localFile.exists() && localFile.length() > 0

    fun getCachedIps(): List<String> {
        if (!localFile.exists()) return emptyList()
        return try {
            localFile.readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    /**
     * Checks the remote link for any updates. If an update is detected, replaces the local copy.
     * Returns the loaded IPs and status.
     */
    suspend fun updateSource(force: Boolean = false): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val reqBuilder = Request.Builder()
                .url(SOURCE_URL)
                .get()
                .header("User-Agent", "StatusPing/1.0")

            val lastEtag = prefs.getString(KEY_LAST_ETAG, null)
            if (!force && lastEtag != null && localFile.exists()) {
                reqBuilder.header("If-None-Match", lastEtag)
            }

            val response = client.newCall(reqBuilder.build()).execute()
            response.use { res ->
                // 304 Not Modified: local copy is already up-to-date
                if (res.code == 304 && localFile.exists()) {
                    val ips = getCachedIps()
                    return@withContext UpdateResult.Success(
                        ips = ips,
                        updated = false,
                        message = "Source loaded"
                    )
                }

                if (!res.isSuccessful) {
                    val cached = getCachedIps()
                    if (cached.isNotEmpty()) {
                        return@withContext UpdateResult.Success(
                            ips = cached,
                            updated = false,
                            message = "Source loaded"
                        )
                    }
                    return@withContext UpdateResult.Error("Could not load source (${res.code})")
                }

                val body = res.body?.string().orEmpty()
                val etag = res.header("ETag")
                val newHash = body.hashCode().toString()
                val previousHash = prefs.getString(KEY_LAST_HASH, null)

                val hasChanged = previousHash != newHash || !localFile.exists()

                if (hasChanged) {
                    localFile.writeText(body)
                    prefs.edit()
                        .putString(KEY_LAST_ETAG, etag)
                        .putString(KEY_LAST_HASH, newHash)
                        .apply()
                }

                val ips = getCachedIps()
                UpdateResult.Success(
                    ips = ips,
                    updated = hasChanged,
                    message = "Source loaded"
                )
            }
        } catch (e: Throwable) {
            val cached = getCachedIps()
            if (cached.isNotEmpty()) {
                UpdateResult.Success(
                    ips = cached,
                    updated = false,
                    message = "Source loaded"
                )
            } else {
                UpdateResult.Error("Failed to load source: ${e.message?.take(50) ?: "network error"}")
            }
        }
    }
}
