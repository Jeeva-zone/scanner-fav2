package com.example.statusping

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.statusping.cloud.CloudScanItem
import com.example.statusping.cloud.CloudSearchState
import com.example.statusping.cloud.CloudSourceManager
import com.example.statusping.cloud.ScanStatus
import com.example.statusping.cloud.UpdateResult
import com.example.statusping.network.CheckResult
import com.example.statusping.network.CheckState
import com.example.statusping.network.ErrorCode
import com.example.statusping.network.FaviconCheckResult
import com.example.statusping.network.StatusChecker
import com.example.statusping.util.deriveFaviconUrl
import com.example.statusping.util.normalizeTarget
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.atomic.AtomicInteger

class StatusViewModel(application: Application) : AndroidViewModel(application) {
    private val checker = StatusChecker(application.applicationContext)
    private val cloudSourceManager = CloudSourceManager(application.applicationContext)

    // Single Target Check State
    private val _uiState = MutableStateFlow<CheckState>(CheckState.Idle)
    val uiState: StateFlow<CheckState> = _uiState.asStateFlow()

    // Cloud Search States
    private val _cloudSearchState = MutableStateFlow<CloudSearchState>(CloudSearchState.Idle)
    val cloudSearchState: StateFlow<CloudSearchState> = _cloudSearchState.asStateFlow()

    private val _sourceStatus = MutableStateFlow<String?>("Source loaded")
    val sourceStatus: StateFlow<String?> = _sourceStatus.asStateFlow()

    private val _cachedIpCount = MutableStateFlow(0)
    val cachedIpCount: StateFlow<Int> = _cachedIpCount.asStateFlow()

    private val _isUpdatingSource = MutableStateFlow(false)
    val isUpdatingSource: StateFlow<Boolean> = _isUpdatingSource.asStateFlow()

    val isHttpScanSelected = MutableStateFlow(true)
    val isFaviconScanSelected = MutableStateFlow(true)

    private var currentJob: Job? = null
    private var cloudJob: Job? = null

    init {
        val cached = cloudSourceManager.getCachedIps()
        if (cached.isNotEmpty()) {
            _cachedIpCount.value = cached.size
            _sourceStatus.value = "Source loaded"
        }
        // Check for update on remote link in background, replacing local copy if updated
        viewModelScope.launch(Dispatchers.IO) {
            val res = cloudSourceManager.updateSource(force = false)
            if (res is UpdateResult.Success) {
                _cachedIpCount.value = res.ips.size
                _sourceStatus.value = res.message
            }
        }
    }

    fun setHttpScan(selected: Boolean) {
        isHttpScanSelected.value = selected
    }

    fun setFaviconScan(selected: Boolean) {
        isFaviconScanSelected.value = selected
    }

    fun manualUpdateSource() {
        if (_isUpdatingSource.value) return
        _isUpdatingSource.value = true
        viewModelScope.launch {
            try {
                val res = cloudSourceManager.updateSource(force = true)
                when (res) {
                    is UpdateResult.Success -> {
                        _cachedIpCount.value = res.ips.size
                        _sourceStatus.value = res.message
                    }
                    is UpdateResult.Error -> {
                        _sourceStatus.value = "Source loaded"
                    }
                }
            } finally {
                _isUpdatingSource.value = false
            }
        }
    }

    fun runCloudSearch() {
        if (!isHttpScanSelected.value && !isFaviconScanSelected.value) {
            _cloudSearchState.value = CloudSearchState.Error(
                getApplication<Application>().getString(R.string.select_at_least_one)
            )
            return
        }

        cloudJob?.cancel()
        cloudJob = viewModelScope.launch {
            var ips = cloudSourceManager.getCachedIps()
            if (ips.isEmpty()) {
                _cloudSearchState.value = CloudSearchState.UpdatingSource
                val updateRes = cloudSourceManager.updateSource()
                if (updateRes is UpdateResult.Success) {
                    ips = updateRes.ips
                    _cachedIpCount.value = ips.size
                    _sourceStatus.value = updateRes.message
                } else if (updateRes is UpdateResult.Error) {
                    _cloudSearchState.value = CloudSearchState.Error(updateRes.error)
                    return@launch
                }
            }

            if (ips.isEmpty()) {
                _cloudSearchState.value = CloudSearchState.Error("No IPs found to scan")
                return@launch
            }

            val scanHttp = isHttpScanSelected.value
            val scanFav = isFaviconScanSelected.value

            val items = ArrayList(ips.map { CloudScanItem(ip = it) })
            _cloudSearchState.value = CloudSearchState.Scanning(
                items = items.toList(),
                completedCount = 0,
                totalCount = items.size
            )

            val semaphore = Semaphore(4)
            val completedCounter = AtomicInteger(0)

            try {
                coroutineScope {
                    items.forEachIndexed { index, item ->
                        launch {
                            semaphore.withPermit {
                                synchronized(items) {
                                    items[index] = items[index].copy(isScanning = true)
                                }

                                val targetIp = item.ip.trim()
                                val formattedHost = if (targetIp.contains(":") && !targetIp.startsWith("[")) {
                                    "[$targetIp]"
                                } else {
                                    targetIp
                                }

                                var httpRes: ScanStatus? = null
                                if (scanHttp) {
                                    val url = "http://$formattedHost:80/"
                                    val res = checker.checkSingleUrl(url)
                                    httpRes = when (res) {
                                        is CheckResult.Success -> ScanStatus.Success(res.code, res.reason, res.latencyMs)
                                        is CheckResult.Error -> ScanStatus.Error(res.detail)
                                    }
                                }

                                var favRes: ScanStatus? = null
                                if (scanFav) {
                                    val url = "http://$formattedHost:80/favicon.ico"
                                    val res = checker.checkSingleUrl(url)
                                    favRes = when (res) {
                                        is CheckResult.Success -> ScanStatus.Success(res.code, res.reason, res.latencyMs)
                                        is CheckResult.Error -> ScanStatus.Error(res.detail)
                                    }
                                }

                                synchronized(items) {
                                    items[index] = CloudScanItem(
                                        ip = item.ip,
                                        httpResult = httpRes,
                                        faviconResult = favRes,
                                        isScanning = false
                                    )
                                }

                                val completed = completedCounter.incrementAndGet()
                                _cloudSearchState.value = CloudSearchState.Scanning(
                                    items = items.toList(),
                                    completedCount = completed,
                                    totalCount = items.size
                                )
                            }
                        }
                    }
                }

                _cloudSearchState.value = CloudSearchState.Completed(
                    items = items.toList(),
                    totalCount = items.size,
                    isStopped = false
                )
            } catch (_: CancellationException) {
                _cloudSearchState.value = CloudSearchState.Completed(
                    items = items.toList(),
                    totalCount = items.size,
                    isStopped = true
                )
            }
        }
    }

    fun stopCloudSearch() {
        cloudJob?.cancel()
        val current = _cloudSearchState.value
        if (current is CloudSearchState.Scanning) {
            _cloudSearchState.value = CloudSearchState.Completed(
                items = current.items,
                totalCount = current.totalCount,
                isStopped = true
            )
        }
    }

    fun checkStatus(rawInput: String) {
        val trimmed = rawInput.trim()
        val normalized = normalizeTarget(trimmed)

        if (normalized == null) {
            currentJob?.cancel()
            checker.cancelInFlight()
            _uiState.value = CheckState.Error(
                errorCode = ErrorCode.INVALID_INPUT,
                detail = getApplication<Application>().getString(R.string.error_invalid_input)
            )
            return
        }

        currentJob?.cancel()
        checker.cancelInFlight()

        _uiState.value = CheckState.Loading

        currentJob = viewModelScope.launch {
            try {
                // 1. Primary URL test
                val primaryResult = checker.checkSingleUrl(normalized)

                when (primaryResult) {
                    is CheckResult.Success -> {
                        val faviconUrl = deriveFaviconUrl(normalized)
                        if (faviconUrl != null) {
                            _uiState.value = CheckState.Success(
                                code = primaryResult.code,
                                reason = primaryResult.reason,
                                finalUrl = primaryResult.finalUrl,
                                latencyMs = primaryResult.latencyMs,
                                redirectCount = primaryResult.redirectCount,
                                faviconResult = FaviconCheckResult.Loading
                            )

                            // 2. Favicon test
                            val favResult = checker.checkSingleUrl(faviconUrl)
                            val favState = when (favResult) {
                                is CheckResult.Success -> FaviconCheckResult.Success(
                                    code = favResult.code,
                                    reason = favResult.reason,
                                    finalUrl = favResult.finalUrl,
                                    latencyMs = favResult.latencyMs,
                                    redirectCount = favResult.redirectCount
                                )
                                is CheckResult.Error -> FaviconCheckResult.Error(
                                    errorCode = favResult.errorCode,
                                    detail = favResult.detail,
                                    targetUrl = faviconUrl
                                )
                            }

                            _uiState.value = CheckState.Success(
                                code = primaryResult.code,
                                reason = primaryResult.reason,
                                finalUrl = primaryResult.finalUrl,
                                latencyMs = primaryResult.latencyMs,
                                redirectCount = primaryResult.redirectCount,
                                faviconResult = favState
                            )
                        } else {
                            _uiState.value = CheckState.Success(
                                code = primaryResult.code,
                                reason = primaryResult.reason,
                                finalUrl = primaryResult.finalUrl,
                                latencyMs = primaryResult.latencyMs,
                                redirectCount = primaryResult.redirectCount,
                                faviconResult = null
                            )
                        }
                    }
                    is CheckResult.Error -> {
                        _uiState.value = CheckState.Error(
                            errorCode = primaryResult.errorCode,
                            detail = primaryResult.detail
                        )
                    }
                }
            } catch (_: CancellationException) {
                // Request was cancelled intentionally
            } catch (e: Throwable) {
                _uiState.value = CheckState.Error(
                    errorCode = ErrorCode.UNKNOWN,
                    detail = getApplication<Application>().getString(
                        R.string.error_unknown,
                        e.message?.take(60) ?: "Unknown failure"
                    )
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
        cloudJob?.cancel()
        checker.cancelInFlight()
    }
}

