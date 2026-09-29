package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AdminNotification
import com.example.data.model.AppSettings
import com.example.data.model.SavedWallet
import com.example.data.model.Transaction
import com.example.data.repository.Resource
import com.example.data.repository.SwiftFxRepository
import com.example.data.storage.AdminPreferences
import com.example.util.AlertManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AdminTab {
    DASHBOARD,
    NOTIFICATIONS,
    RATES_FEES,
    SETTINGS
}

enum class TransactionFilter {
    ALL,
    ACTION_REQUIRED,
    DEPOSITS,
    WITHDRAWALS,
    COMPLETED
}

sealed class ActionDialogState {
    data object None : ActionDialogState()
    data class SendWallet(val transaction: Transaction) : ActionDialogState()
    data class ApproveDeposit(val transaction: Transaction) : ActionDialogState()
    data class ApproveWithdrawal(val transaction: Transaction) : ActionDialogState()
    data class ViewDetails(val transaction: Transaction) : ActionDialogState()
}

data class AdminUiState(
    val transactions: List<Transaction> = emptyList(),
    val notifications: List<AdminNotification> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val savedWallets: List<SavedWallet> = emptyList(),
    val selectedTab: AdminTab = AdminTab.DASHBOARD,
    val selectedFilter: TransactionFilter = TransactionFilter.ACTION_REQUIRED,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isPolling: Boolean = true,
    val lastSyncedTimeMillis: Long = 0L,
    val unreadNotificationsCount: Int = 0,
    val activeBannerAlert: AdminNotification? = null,
    val dialogState: ActionDialogState = ActionDialogState.None,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSubmittingAction: Boolean = false,
    val isConnected: Boolean = true
)

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    val preferences = AdminPreferences(application)
    val alertManager = AlertManager(application, preferences)
    private val repository = SwiftFxRepository(preferences)

    private val _uiState = MutableStateFlow(
        AdminUiState(savedWallets = preferences.getSavedWallets())
    )
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private val knownNotificationIds = mutableSetOf<String>()
    private val knownActionableTxIds = mutableSetOf<String>()
    private var isFirstSync = true

    init {
        loadSettings()
        startPolling()
    }

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            _uiState.update { it.copy(isPolling = true) }
            while (isActive) {
                pollBackend()
                delay((preferences.pollIntervalSeconds.coerceAtLeast(2) * 1000).toLong())
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        _uiState.update { it.copy(isPolling = false) }
    }

    fun manualRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            pollBackend()
            fetchSettingsInternal()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun pollBackend() {
        val txResult = repository.fetchTransactions()
        val notifResult = repository.fetchNotifications()

        var hasNewAlert = false
        var alertNotification: AdminNotification? = null

        when (txResult) {
            is Resource.Success -> {
                val transactions = txResult.data
                val actionableTxs = transactions.filter { it.isActionRequired }

                if (!isFirstSync) {
                    val newlyActionable = actionableTxs.filter { it.id !in knownActionableTxIds }
                    if (newlyActionable.isNotEmpty()) {
                        hasNewAlert = true
                        val first = newlyActionable.first()
                        val alertTitle = when (first.status.lowercase()) {
                            "fiat_received" -> "Deposit Paid: $${first.amount} USD"
                            "awaiting_admin_wallet" -> "Withdrawal Request: $${first.amount} USD"
                            "crypto_received" -> "Crypto Received: TZS ${first.amountTzs.toLong()}"
                            else -> "Action Required"
                        }
                        alertNotification = AdminNotification(
                            id = "tx_${first.id}_${System.currentTimeMillis()}",
                            transactionId = first.id,
                            type = first.status,
                            title = alertTitle,
                            message = "${first.cryptoCurrency} (${first.cryptoNetwork}) • ${first.mobileNetwork} ${first.phoneNumber}",
                            isRead = false,
                            createdAt = "Just now"
                        )
                    }
                }

                knownActionableTxIds.clear()
                actionableTxs.forEach { knownActionableTxIds.add(it.id) }

                _uiState.update { current ->
                    current.copy(
                        transactions = transactions,
                        lastSyncedTimeMillis = System.currentTimeMillis(),
                        isConnected = true
                    )
                }
            }
            is Resource.Error -> {
                _uiState.update { it.copy(isConnected = false) }
            }
        }

        when (notifResult) {
            is Resource.Success -> {
                val notifications = notifResult.data
                val unreadCount = notifications.count { !it.isRead }

                if (!isFirstSync) {
                    val newNotifs = notifications.filter { it.id !in knownNotificationIds }
                    if (newNotifs.isNotEmpty()) {
                        hasNewAlert = true
                        alertNotification = alertNotification ?: newNotifs.first()
                    }
                }

                knownNotificationIds.clear()
                notifications.forEach { knownNotificationIds.add(it.id) }

                _uiState.update { current ->
                    current.copy(
                        notifications = notifications,
                        unreadNotificationsCount = unreadCount,
                        isConnected = true
                    )
                }
            }
            is Resource.Error -> {
                // Connection or auth error handled
            }
        }

        if (hasNewAlert && alertNotification != null) {
            alertManager.triggerAlert(urgent = true)
            _uiState.update { current ->
                current.copy(activeBannerAlert = alertNotification)
            }
        }

        isFirstSync = false
    }

    private fun loadSettings() {
        viewModelScope.launch {
            fetchSettingsInternal()
        }
    }

    private suspend fun fetchSettingsInternal() {
        when (val res = repository.fetchSettings()) {
            is Resource.Success -> {
                _uiState.update { it.copy(settings = res.data) }
            }
            is Resource.Error -> {
                // Keep default settings
            }
        }
    }

    // --- Tab & Filter Navigation ---

    fun selectTab(tab: AdminTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectFilter(filter: TransactionFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun dismissBanner() {
        _uiState.update { it.copy(activeBannerAlert = null) }
    }

    fun openDialog(dialogState: ActionDialogState) {
        _uiState.update { it.copy(dialogState = dialogState) }
    }

    fun closeDialog() {
        _uiState.update { it.copy(dialogState = ActionDialogState.None) }
    }

    fun clearFeedbackMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    // --- Admin Transaction Actions ---

    fun sendWalletAddress(transactionId: String, cryptoAddress: String, customNote: String?) {
        if (cryptoAddress.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Wallet address cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingAction = true, errorMessage = null) }
            when (val res = repository.sendWalletAddress(transactionId, cryptoAddress.trim(), customNote)) {
                is Resource.Success -> {
                    _uiState.update { current ->
                        val updatedList = current.transactions.map {
                            if (it.id == transactionId) {
                                it.copy(
                                    cryptoAddress = cryptoAddress.trim(),
                                    status = "awaiting_crypto_deposit",
                                    adminMessage = customNote
                                )
                            } else it
                        }
                        current.copy(
                            transactions = updatedList,
                            dialogState = ActionDialogState.None,
                            isSubmittingAction = false,
                            successMessage = "Wallet address sent to client successfully!"
                        )
                    }
                    pollBackend()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingAction = false,
                            errorMessage = res.message
                        )
                    }
                }
            }
        }
    }

    fun approveTransaction(transactionId: String, txHash: String?, adminMessage: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingAction = true, errorMessage = null) }
            when (val res = repository.approveTransaction(transactionId, txHash, adminMessage)) {
                is Resource.Success -> {
                    _uiState.update { current ->
                        val updatedList = current.transactions.map {
                            if (it.id == transactionId) {
                                it.copy(
                                    status = "completed",
                                    txHash = txHash,
                                    adminMessage = adminMessage
                                )
                            } else it
                        }
                        current.copy(
                            transactions = updatedList,
                            dialogState = ActionDialogState.None,
                            isSubmittingAction = false,
                            successMessage = "Transaction #$transactionId approved as COMPLETED!"
                        )
                    }
                    pollBackend()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingAction = false,
                            errorMessage = res.message
                        )
                    }
                }
            }
        }
    }

    fun updateFeeSettings(depositFee: Double, withdrawFee: Double, usdTzsRate: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val res = repository.updateSettings(depositFee, withdrawFee, usdTzsRate)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            settings = res.data,
                            isLoading = false,
                            successMessage = "Fee percentages & exchange rate updated successfully!"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = res.message
                        )
                    }
                }
            }
        }
    }

    // --- Saved Preset Receiving Wallets ---

    fun savePresetWallet(wallet: SavedWallet) {
        val current = _uiState.value.savedWallets.toMutableList()
        val index = current.indexOfFirst { it.id == wallet.id }
        if (index >= 0) {
            current[index] = wallet
        } else {
            current.add(wallet)
        }
        preferences.saveWallets(current)
        _uiState.update { it.copy(savedWallets = current, successMessage = "Saved wallet '${wallet.label}'") }
    }

    fun deletePresetWallet(walletId: String) {
        val updated = _uiState.value.savedWallets.filterNot { it.id == walletId }
        preferences.saveWallets(updated)
        _uiState.update { it.copy(savedWallets = updated) }
    }

    // --- Preference Updates ---

    fun updateAdminApiKey(newKey: String) {
        preferences.adminApiKey = newKey
        manualRefresh()
    }

    fun updateBaseUrl(newUrl: String) {
        preferences.baseUrl = newUrl
        manualRefresh()
    }

    fun toggleSound(enabled: Boolean) {
        preferences.soundEnabled = enabled
    }

    fun toggleVibration(enabled: Boolean) {
        preferences.vibrationEnabled = enabled
    }

    fun setPollInterval(seconds: Int) {
        preferences.pollIntervalSeconds = seconds
        startPolling()
    }

    fun testChimeSound() {
        alertManager.triggerAlert(urgent = true)
    }

    fun findTransactionById(transactionId: String): Transaction? {
        return _uiState.value.transactions.find { it.id == transactionId }
    }
}
