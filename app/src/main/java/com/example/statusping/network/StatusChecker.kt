package com.example.statusping.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.SystemClock
import com.example.R
import com.example.statusping.util.HttpReasons
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException

enum class ErrorCode {
    INVALID_INPUT,
    NO_CONNECTION,
    DNS_FAILURE,
    CONNECTION_REFUSED,
    TIMEOUT,
    SSL_ERROR,
    CONNECTION_RESET,
    UNKNOWN
}

sealed interface FaviconCheckResult {
    data object Loading : FaviconCheckResult
    data class Success(
        val code: Int,
        val reason: String,
        val finalUrl: String,
        val latencyMs: Long,
        val redirectCount: Int
    ) : FaviconCheckResult
    data class Error(
        val errorCode: ErrorCode,
        val detail: String,
        val targetUrl: String
    ) : FaviconCheckResult
}

sealed interface CheckResult {
    data class Success(
        val code: Int,
        val reason: String,
        val finalUrl: String,
        val latencyMs: Long,
        val redirectCount: Int
    ) : CheckResult
    data class Error(
        val errorCode: ErrorCode,
        val detail: String
    ) : CheckResult
}

sealed interface CheckState {
    data object Idle : CheckState
    data object Loading : CheckState
    data class Success(
        val code: Int,
        val reason: String,
        val finalUrl: String,
        val latencyMs: Long,
        val redirectCount: Int,
        val faviconResult: FaviconCheckResult? = null
    ) : CheckState
    data class Error(
        val errorCode: ErrorCode,
        val detail: String,
        val faviconResult: FaviconCheckResult? = null
    ) : CheckState
}

class StatusChecker(private val context: Context) {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .callTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val activeCalls = java.util.Collections.synchronizedSet(mutableSetOf<Call>())

    fun cancelInFlight() {
        synchronized(activeCalls) {
            for (call in activeCalls) {
                call.cancel()
            }
            activeCalls.clear()
        }
    }

    suspend fun checkSingleUrl(url: String): CheckResult = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable(context)) {
            return@withContext CheckResult.Error(
                errorCode = ErrorCode.NO_CONNECTION,
                detail = context.getString(R.string.error_no_connection)
            )
        }

        val request = Request.Builder()
            .url(url)
            .get()
            .header("User-Agent", "StatusPing/1.0")
            .build()

        val call = client.newCall(request)
        activeCalls.add(call)

        val startTime = SystemClock.elapsedRealtime()
        try {
            val response = call.execute()
            val latency = SystemClock.elapsedRealtime() - startTime
            response.use { res ->
                var hopCount = 0
                var prior = res.priorResponse
                while (prior != null) {
                    hopCount++
                    prior = prior.priorResponse
                }

                val finalUrl = res.request.url.toString()
                val code = res.code
                val reason = HttpReasons.getReasonPhrase(code, res.message)

                CheckResult.Success(
                    code = code,
                    reason = reason,
                    finalUrl = finalUrl,
                    latencyMs = latency,
                    redirectCount = hopCount
                )
            }
        } catch (e: Throwable) {
            if (e is CancellationException) {
                throw e
            }
            if (call.isCanceled()) {
                throw CancellationException("Request canceled", e)
            }
            val err = mapExceptionToCheckState(e, context)
            CheckResult.Error(err.errorCode, err.detail)
        } finally {
            activeCalls.remove(call)
        }
    }

    suspend fun checkStatus(normalizedUrl: String): CheckState = withContext(Dispatchers.IO) {
        when (val res = checkSingleUrl(normalizedUrl)) {
            is CheckResult.Success -> CheckState.Success(
                code = res.code,
                reason = res.reason,
                finalUrl = res.finalUrl,
                latencyMs = res.latencyMs,
                redirectCount = res.redirectCount
            )
            is CheckResult.Error -> CheckState.Error(
                errorCode = res.errorCode,
                detail = res.detail
            )
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun mapExceptionToCheckState(e: Throwable, context: Context): CheckState.Error {
        val msg = e.message?.lowercase().orEmpty()
        return when (e) {
            is UnknownHostException -> CheckState.Error(
                errorCode = ErrorCode.DNS_FAILURE,
                detail = context.getString(R.string.error_dns_failure)
            )
            is ConnectException -> {
                if (msg.contains("reset")) {
                    CheckState.Error(
                        errorCode = ErrorCode.CONNECTION_RESET,
                        detail = context.getString(R.string.error_connection_reset)
                    )
                } else {
                    CheckState.Error(
                        errorCode = ErrorCode.CONNECTION_REFUSED,
                        detail = context.getString(R.string.error_connection_refused)
                    )
                }
            }
            is SocketTimeoutException -> CheckState.Error(
                errorCode = ErrorCode.TIMEOUT,
                detail = context.getString(R.string.error_timeout)
            )
            is SSLException -> CheckState.Error(
                errorCode = ErrorCode.SSL_ERROR,
                detail = context.getString(R.string.error_ssl)
            )
            is SocketException -> {
                if (msg.contains("reset") || msg.contains("broken pipe")) {
                    CheckState.Error(
                        errorCode = ErrorCode.CONNECTION_RESET,
                        detail = context.getString(R.string.error_connection_reset)
                    )
                } else if (msg.contains("refused")) {
                    CheckState.Error(
                        errorCode = ErrorCode.CONNECTION_REFUSED,
                        detail = context.getString(R.string.error_connection_refused)
                    )
                } else {
                    CheckState.Error(
                        errorCode = ErrorCode.UNKNOWN,
                        detail = context.getString(R.string.error_unknown, "Socket connection error")
                    )
                }
            }
            is IOException -> {
                if (msg.contains("reset")) {
                    CheckState.Error(
                        errorCode = ErrorCode.CONNECTION_RESET,
                        detail = context.getString(R.string.error_connection_reset)
                    )
                } else {
                    val detailMsg = sanitizeErrorMessage(e.message)
                    CheckState.Error(
                        errorCode = ErrorCode.UNKNOWN,
                        detail = context.getString(R.string.error_unknown, detailMsg)
                    )
                }
            }
            else -> {
                val detailMsg = sanitizeErrorMessage(e.message)
                CheckState.Error(
                    errorCode = ErrorCode.UNKNOWN,
                    detail = context.getString(R.string.error_unknown, detailMsg)
                )
            }
        }
    }

    private fun sanitizeErrorMessage(raw: String?): String {
        if (raw.isNullOrBlank()) return "Network request failed"
        // Avoid leaking technical class names or multi-line messages
        val firstLine = raw.lines().firstOrNull()?.trim().orEmpty()
        return if (firstLine.length > 60) firstLine.take(60) + "…" else firstLine
    }
}
