package com.example.statusping.util

import androidx.compose.ui.graphics.Color

object HttpReasons {
    private val REASON_MAP = mapOf(
        100 to "Continue",
        101 to "Switching Protocols",
        102 to "Processing",
        103 to "Early Hints",

        200 to "OK",
        201 to "Created",
        202 to "Accepted",
        203 to "Non-Authoritative Information",
        204 to "No Content",
        205 to "Reset Content",
        206 to "Partial Content",
        207 to "Multi-Status",
        208 to "Already Reported",
        226 to "IM Used",

        300 to "Multiple Choices",
        301 to "Moved Permanently",
        302 to "Found",
        303 to "See Other",
        304 to "Not Modified",
        305 to "Use Proxy",
        307 to "Temporary Redirect",
        308 to "Permanent Redirect",

        400 to "Bad Request",
        401 to "Unauthorized",
        402 to "Payment Required",
        403 to "Forbidden",
        404 to "Not Found",
        405 to "Method Not Allowed",
        406 to "Not Acceptable",
        407 to "Proxy Authentication Required",
        408 to "Request Timeout",
        409 to "Conflict",
        410 to "Gone",
        411 to "Length Required",
        412 to "Precondition Failed",
        413 to "Payload Too Large",
        414 to "URI Too Long",
        415 to "Unsupported Media Type",
        416 to "Range Not Satisfiable",
        417 to "Expectation Failed",
        418 to "I'm a teapot",
        421 to "Misdirected Request",
        422 to "Unprocessable Entity",
        423 to "Locked",
        424 to "Failed Dependency",
        425 to "Too Early",
        426 to "Upgrade Required",
        428 to "Precondition Required",
        429 to "Too Many Requests",
        431 to "Request Header Fields Too Large",
        451 to "Unavailable For Legal Reasons",

        500 to "Internal Server Error",
        501 to "Not Implemented",
        502 to "Bad Gateway",
        503 to "Service Unavailable",
        504 to "Gateway Timeout",
        505 to "HTTP Version Not Supported",
        506 to "Variant Also Negotiates",
        507 to "Insufficient Storage",
        508 to "Loop Detected",
        510 to "Not Extended",
        511 to "Network Authentication Required",
        520 to "Web Server Returned an Unknown Error",
        521 to "Web Server Is Down",
        522 to "Connection Timed Out",
        523 to "Origin Is Unreachable",
        524 to "A Timeout Occurred",
        525 to "SSL Handshake Failed",
        526 to "Invalid SSL Certificate",
        527 to "Railgun Error"
    )

    fun getReasonPhrase(code: Int, responseMessage: String?): String {
        val known = REASON_MAP[code]
        if (known != null) return known
        if (!responseMessage.isNullOrBlank()) return responseMessage
        return when (code / 100) {
            1 -> "Informational"
            2 -> "Success"
            3 -> "Redirection"
            4 -> "Client Error"
            5 -> "Server Error"
            else -> "HTTP Status $code"
        }
    }

    /**
     * Status codes that count as a "good" answer: the host replied and the result is
     * one of the expected outcomes. Everything else that produced a response is orange.
     */
    private val GOOD_CODES = setOf(200, 301, 400, 403)

    private fun isGoodCode(code: Int): Boolean = code in GOOD_CODES

    /**
     * Colour policy:
     *  - 200, 301, 400, 403 -> green  (host reachable, expected outcome)
     *  - any other HTTP code -> orange (responded, but not what we were hoping for)
     *  - no response at all (timeout / unreachable / dead) -> red, via getUnreachableColor(),
     *    because those never reach this helper - they surface as CheckState.Error instead.
     * Colours are tuned for strong contrast in both light and dark themes.
     */
    fun getStatusColor(code: Int, isDarkTheme: Boolean): Color {
        return if (isGoodCode(code)) {
            if (isDarkTheme) Color(0xFF4ADE80) else Color(0xFF15803D) // Green
        } else {
            if (isDarkTheme) Color(0xFFFB923C) else Color(0xFFC2410C) // Orange
        }
    }

    fun getStatusContainerColor(code: Int, isDarkTheme: Boolean): Color {
        return if (isGoodCode(code)) {
            if (isDarkTheme) Color(0xFF064E3B).copy(alpha = 0.4f) else Color(0xFFDCFCE7)
        } else {
            if (isDarkTheme) Color(0xFF7C2D12).copy(alpha = 0.4f) else Color(0xFFFFEDD5)
        }
    }

    /**
     * Red used for targets that never answered: timed out, unreachable or dead.
     */
    fun getUnreachableColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) Color(0xFFF87171) else Color(0xFFB91C1C)
    }

    fun getFormattedStatus(code: Int, reason: String): String {
        return "$code $reason"
    }

    fun getCategoryLabel(code: Int): String {
        return when (code / 100) {
            1 -> "1xx Informational"
            2 -> "2xx Success"
            3 -> "3xx Redirection"
            4 -> "4xx Client Error"
            5 -> "5xx Server Error"
            else -> "HTTP $code"
        }
    }

    fun getDescription(code: Int): String {
        return when (code) {
            200 -> "The request succeeded and the server returned the requested resource."
            201 -> "The request succeeded and a new resource has been created."
            202 -> "The request has been accepted for processing, but processing is not complete."
            204 -> "The server successfully processed the request, but is returning no content."
            301 -> "The requested resource has been assigned a new permanent URI."
            302 -> "The resource resides temporarily under a different URI."
            304 -> "Resource has not been modified; cached version can be used."
            307 -> "Temporary redirect; reissue request with the same HTTP method."
            308 -> "Permanent redirect; reissue request with the same HTTP method."
            400 -> "The server cannot process the request due to malformed syntax."
            401 -> "Authentication is required to access the requested resource."
            403 -> "Forbidden. The server understood the request but refuses authorization."
            404 -> "The requested resource could not be found on this server."
            405 -> "The HTTP method is not allowed for the requested resource."
            408 -> "The server timed out waiting for the client request."
            418 -> "The server refuses to brew coffee because it is a teapot."
            429 -> "Rate limited. Too many requests sent in a given amount of time."
            451 -> "Resource is unavailable due to legal or regulatory reasons."
            500 -> "The server encountered an internal error and was unable to complete the request."
            502 -> "Bad Gateway. The server received an invalid response from an upstream server."
            503 -> "Service Unavailable. The server is currently unable to handle the request."
            504 -> "Gateway Timeout. The upstream server failed to send a timely response."
            else -> when (code / 100) {
                1 -> "Informational response indicating that the request was received."
                2 -> "Success code indicating that the client request was successfully received and accepted."
                3 -> "Redirection code indicating further action is needed to fulfill the request."
                4 -> "Client error indicating an issue with the syntax or permissions of the request."
                5 -> "Server error indicating the host failed to fulfill an apparently valid request."
                else -> "HTTP status response received from host."
            }
        }
    }
}
