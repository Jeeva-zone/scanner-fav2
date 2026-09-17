package com.example.statusping.util

import java.net.URI
import java.net.URL

/**
 * Normalizes user input into a fully qualified HTTP/HTTPS URL per specification.
 * Returns null if the input is invalid or uses an unsupported scheme.
 */
fun normalizeTarget(raw: String): String? {
    val t = raw.trim()
    if (t.isEmpty()) return null
    if (t.contains(" ")) return null

    // If an explicit scheme is provided (e.g. ftp://, file://, ws://), reject non-http/https schemes
    if (t.contains("://")) {
        if (!t.startsWith("http://", ignoreCase = true) && !t.startsWith("https://", ignoreCase = true)) {
            return null
        }
    }

    val candidate = if (t.startsWith("http://", ignoreCase = true) || t.startsWith("https://", ignoreCase = true)) {
        t
    } else {
        "http://$t"
    }

    return try {
        val uri = URI(candidate)
        val scheme = uri.scheme?.lowercase() ?: return null
        if (scheme != "http" && scheme != "https") return null

        val host = uri.host
        if (host.isNullOrBlank()) return null

        val port = uri.port
        val effectivePort = if (port != -1) {
            port
        } else {
            if (scheme == "https") 443 else 80
        }

        val formattedHost = if (host.contains(":") && !host.startsWith("[")) {
            "[$host]"
        } else {
            host
        }

        val rawPath = uri.rawPath
        val path = if (rawPath.isNullOrEmpty()) "/" else rawPath
        val query = if (uri.rawQuery != null) "?${uri.rawQuery}" else ""
        val fragment = if (uri.rawFragment != null) "#${uri.rawFragment}" else ""

        val normalized = "$scheme://$formattedHost:$effectivePort$path$query$fragment"
        URL(normalized)
        normalized
    } catch (_: Throwable) {
        null
    }
}

/**
 * Derives the /favicon.ico URL corresponding to the given normalized URL (http://IP:port/favicon.ico).
 */
fun deriveFaviconUrl(normalizedUrl: String): String? {
    return try {
        val uri = URI(normalizedUrl)
        val scheme = uri.scheme?.lowercase() ?: "http"
        val host = uri.host ?: return null
        val port = if (uri.port != -1) uri.port else if (scheme == "https") 443 else 80
        val formattedHost = if (host.contains(":") && !host.startsWith("[")) "[$host]" else host
        "$scheme://$formattedHost:$port/favicon.ico"
    } catch (_: Throwable) {
        null
    }
}

