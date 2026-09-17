package com.example.statusping.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.statusping.StatusViewModel
import com.example.statusping.cloud.CloudScanItem
import com.example.statusping.cloud.CloudSearchState
import com.example.statusping.cloud.ScanStatus
import com.example.statusping.util.HttpReasons

private enum class ScanFilter {
    ALL, ONLINE, FAILED
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CloudSearchSection(
    viewModel: StatusViewModel,
    modifier: Modifier = Modifier
) {
    val cloudSearchState by viewModel.cloudSearchState.collectAsState()
    val sourceStatus by viewModel.sourceStatus.collectAsState()
    val cachedIpCount by viewModel.cachedIpCount.collectAsState()
    val isUpdatingSource by viewModel.isUpdatingSource.collectAsState()
    val isHttpScan by viewModel.isHttpScanSelected.collectAsState()
    val isFaviconScan by viewModel.isFaviconScanSelected.collectAsState()

    val isDarkTheme = isSystemInDarkTheme()
    val isScanning = cloudSearchState is CloudSearchState.Scanning

    val greenColor = if (isDarkTheme) Color(0xFF4ADE80) else Color(0xFF16A34A)
    var activeFilter by remember { mutableStateOf(ScanFilter.ALL) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Checkboxes to choose HTTP and Favicon scan
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // HTTP scan checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("checkbox_http_scan_row")
                ) {
                    Checkbox(
                        checked = isHttpScan,
                        onCheckedChange = { viewModel.setHttpScan(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("checkbox_http_scan")
                    )
                    Text(
                        text = stringResource(R.string.http_scan_label),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Favicon scan checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("checkbox_favicon_scan_row")
                ) {
                    Checkbox(
                        checked = isFaviconScan,
                        onCheckedChange = { viewModel.setFaviconScan(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("checkbox_favicon_scan")
                    )
                    Text(
                        text = stringResource(R.string.favicon_scan_label),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Action row: Cloud Search button + Manual Update button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Cloud search" button
            Button(
                onClick = {
                    if (isScanning) {
                        viewModel.stopCloudSearch()
                    } else {
                        viewModel.runCloudSearch()
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier
                    .weight(1.3f)
                    .height(52.dp)
                    .testTag("cloud_search_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isScanning) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = stringResource(R.string.stop_scan),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.stop_scan),
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.cloud_search_button),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // "Manual update" button
            OutlinedButton(
                onClick = { viewModel.manualUpdateSource() },
                enabled = !isUpdatingSource && !isScanning,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("manual_update_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isUpdatingSource) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.updating_source),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.manual_update_button),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.manual_update_button),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Green status indicator: "Source loaded" (never reveals github source link)
        if (sourceStatus != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(greenColor, shape = CircleShape)
                    )
                    Text(
                        text = stringResource(R.string.source_loaded),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = greenColor,
                        modifier = Modifier.testTag("source_loaded_text")
                    )
                }

                if (cachedIpCount > 0) {
                    Text(
                        text = "$cachedIpCount IPs ready",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Cloud search error message
        if (cloudSearchState is CloudSearchState.Error) {
            val error = (cloudSearchState as CloudSearchState.Error).message
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Scanning progress bar
        if (cloudSearchState is CloudSearchState.Scanning) {
            val scanning = cloudSearchState as CloudSearchState.Scanning
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Scanning IPs: ${scanning.completedCount} of ${scanning.totalCount}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${if (scanning.totalCount > 0) (scanning.completedCount * 100 / scanning.totalCount) else 0}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    LinearProgressIndicator(
                        progress = {
                            if (scanning.totalCount > 0) scanning.completedCount.toFloat() / scanning.totalCount else 0f
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Results view (Scanning or Completed)
        val items = when (cloudSearchState) {
            is CloudSearchState.Scanning -> (cloudSearchState as CloudSearchState.Scanning).items
            is CloudSearchState.Completed -> (cloudSearchState as CloudSearchState.Completed).items
            else -> emptyList()
        }

        if (items.isNotEmpty()) {
            val completedItems = items.filter { !it.isScanning && (it.httpResult != null || it.faviconResult != null) }
            val onlineItems = completedItems.filter {
                (it.httpResult is ScanStatus.Success && it.httpResult.code == 200) ||
                (it.faviconResult is ScanStatus.Success && it.faviconResult.code == 200)
            }
            val failedItems = completedItems.filter {
                it.httpResult is ScanStatus.Error || (it.httpResult is ScanStatus.Success && it.httpResult.code != 200)
            }

            // Filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = activeFilter == ScanFilter.ALL,
                    onClick = { activeFilter = ScanFilter.ALL },
                    label = { Text("All (${items.size})") }
                )
                FilterChip(
                    selected = activeFilter == ScanFilter.ONLINE,
                    onClick = { activeFilter = ScanFilter.ONLINE },
                    label = { Text("Online 200 (${onlineItems.size})") }
                )
                FilterChip(
                    selected = activeFilter == ScanFilter.FAILED,
                    onClick = { activeFilter = ScanFilter.FAILED },
                    label = { Text("Other (${items.size - onlineItems.size})") }
                )
            }

            val displayedItems = when (activeFilter) {
                ScanFilter.ALL -> items
                ScanFilter.ONLINE -> onlineItems
                ScanFilter.FAILED -> failedItems
            }

            // List of IP cards
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                displayedItems.forEach { item ->
                    CloudScanItemCard(
                        item = item,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}

@Composable
private fun CloudScanItemCard(
    item: CloudScanItem,
    isDarkTheme: Boolean
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // IP Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.ip,
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // HTTP result badge
            if (item.httpResult != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HTTP :80",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    StatusBadge(status = item.httpResult, isDarkTheme = isDarkTheme)
                }
            }

            // Favicon result badge
            if (item.faviconResult != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Favicon /favicon.ico",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    StatusBadge(status = item.faviconResult, isDarkTheme = isDarkTheme)
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    status: ScanStatus,
    isDarkTheme: Boolean
) {
    when (status) {
        is ScanStatus.Loading -> {
            Text(
                text = "…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        is ScanStatus.Success -> {
            val statusColor = HttpReasons.getStatusColor(status.code, isDarkTheme)
            val text = "${status.code} ${status.reason} • ${status.latencyMs}ms"
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = statusColor.copy(alpha = if (isDarkTheme) 0.25f else 0.12f)
                ),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
        is ScanStatus.Error -> {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "— ${status.detail.take(24)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
