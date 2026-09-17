package com.example.statusping.cloud

sealed interface ScanStatus {
    data object Loading : ScanStatus
    data class Success(val code: Int, val reason: String, val latencyMs: Long) : ScanStatus
    data class Error(val detail: String) : ScanStatus
}

data class CloudScanItem(
    val ip: String,
    val httpResult: ScanStatus? = null,
    val faviconResult: ScanStatus? = null,
    val isScanning: Boolean = false
)

sealed interface CloudSearchState {
    data object Idle : CloudSearchState
    data object UpdatingSource : CloudSearchState
    data class Scanning(
        val items: List<CloudScanItem>,
        val completedCount: Int,
        val totalCount: Int
    ) : CloudSearchState
    data class Completed(
        val items: List<CloudScanItem>,
        val totalCount: Int,
        val isStopped: Boolean = false
    ) : CloudSearchState
    data class Error(val message: String) : CloudSearchState
}
