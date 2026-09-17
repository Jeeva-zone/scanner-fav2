package com.example.statusping.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.statusping.util.HttpReasons

/**
 * Display component that shows the returned HTTP status code and a descriptive message
 * (e.g., '200 OK', '404 Not Found') after an HTTP check.
 */
@Composable
fun HttpStatusDisplay(
    code: Int,
    reason: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val statusColor = HttpReasons.getStatusColor(code, isDarkTheme)
    val statusText = HttpReasons.getFormattedStatus(code, reason)
    val categoryLabel = HttpReasons.getCategoryLabel(code)
    val explanation = description ?: HttpReasons.getDescription(code)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("http_status_display"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Prominent status pill badge showing "200 OK", "404 Not Found"
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = statusColor.copy(alpha = if (isDarkTheme) 0.25f else 0.12f),
            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
            modifier = Modifier.testTag("status_badge")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status dot indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(statusColor, shape = CircleShape)
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier.testTag("status_badge_text")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large Status Code number (48-56sp)
        Text(
            text = "$code",
            style = MaterialTheme.typography.displayLarge,
            color = statusColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("status_code_text")
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Reason Phrase directly beneath (e.g., "OK", "Not Found")
        Text(
            text = reason,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("reason_text")
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Category Tag (e.g. "2xx Success", "4xx Client Error")
        Text(
            text = categoryLabel,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = statusColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("category_label_text")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Explanatory descriptive message
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("status_description_container")
        ) {
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("status_description_text")
            )
        }
    }
}
