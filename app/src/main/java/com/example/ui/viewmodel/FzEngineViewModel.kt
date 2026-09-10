package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FzDatabase
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.LicenseEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.SellerEntity
import com.example.data.local.entity.SessionEntity
import com.example.data.model.LicenseStatus
import com.example.data.model.NavigationTab
import com.example.data.model.RankingPeriod
import com.example.data.model.ServerHealthStatus
import com.example.data.model.UserRole
import com.example.data.repository.AppUpdateInfo
import com.example.data.repository.FzRepository
import com.example.data.repository.LicenseActivationResult
import com.example.data.repository.ServerErrorCode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LicenseDialogUiState(
    val isOpen: Boolean = false,
    val keyInput: String = "",
    val isKeyMasked: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class FzEngineViewModel(application: Application) : AndroidViewModel(application) {
    private val database = FzDatabase.getDatabase(application)
    private val repository = FzRepository(database.fzEngineDao(), application)

    val currentTab = MutableStateFlow(NavigationTab.HOME)
    val serverStatus: StateFlow<ServerHealthStatus> = repository.serverStatus
    val currentUserRole: StateFlow<UserRole> = repository.currentUserRole
    val updateInfo: StateFlow<AppUpdateInfo> = repository.updateInfo

    val activeLicense: StateFlow<LicenseEntity?> = repository.activeLicense.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val sellers: StateFlow<List<SellerEntity>> = repository.sellers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeSessions: StateFlow<List<SessionEntity>> = repository.activeSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.auditLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notifications: StateFlow<List<NotificationEntity>> = repository.notifications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val unreadCount: StateFlow<Int> = repository.unreadCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // License Dialog UI State
    private val _licenseDialogState = MutableStateFlow(LicenseDialogUiState())
    val licenseDialogState: StateFlow<LicenseDialogUiState> = _licenseDialogState.asStateFlow()

    // Sheet / Dialog visibility
    val isNotificationsSheetOpen = MutableStateFlow(false)
    val isAdminSheetOpen = MutableStateFlow(false)
    val isDiagnosticsDialogOpen = MutableStateFlow(false)
    val isGameDetailsDialogOpen = MutableStateFlow(false)
    val isUpdateModalOpen = MutableStateFlow(false)
    val isPrivacyTermsModalOpen = MutableStateFlow(false)
    val privacyTermsMode = MutableStateFlow("PRIVACY") // "PRIVACY" or "TERMS"

    // Sellers Screen State
    val sellersSubTab = MutableStateFlow(0) // 0 = RANKING, 1 = FIND SELLERS
    val rankingPeriod = MutableStateFlow(RankingPeriod.ALL_TIME)
    val rankingScope = MutableStateFlow("GLOBAL") // "GLOBAL" or "COUNTRY"
    val sellersSearchQuery = MutableStateFlow("")
    val selectedCountryFilter = MutableStateFlow("All")

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.initializeSeedData()
        }
    }

    fun selectTab(tab: NavigationTab) {
        currentTab.value = tab
    }

    // License Dialog Methods
    fun openLicenseDialog(prefillKey: String = "") {
        _licenseDialogState.value = LicenseDialogUiState(
            isOpen = true,
            keyInput = prefillKey,
            isKeyMasked = true,
            isLoading = false,
            errorMessage = null,
            successMessage = null
        )
    }

    fun dismissLicenseDialog() {
        _licenseDialogState.value = _licenseDialogState.value.copy(isOpen = false)
    }

    fun updateKeyInput(input: String) {
        _licenseDialogState.value = _licenseDialogState.value.copy(
            keyInput = input,
            errorMessage = null,
            successMessage = null
        )
    }

    fun toggleKeyMask() {
        _licenseDialogState.value = _licenseDialogState.value.copy(
            isKeyMasked = !_licenseDialogState.value.isKeyMasked
        )
    }

    fun submitLicenseKey() {
        val currentKey = _licenseDialogState.value.keyInput.trim()
        if (currentKey.isEmpty()) {
            _licenseDialogState.value = _licenseDialogState.value.copy(
                errorMessage = "Please enter a valid license key"
            )
            return
        }

        _licenseDialogState.value = _licenseDialogState.value.copy(
            isLoading = true,
            errorMessage = null,
            successMessage = null
        )

        viewModelScope.launch {
            val result = repository.validateAndActivateLicense(currentKey)
            when (result) {
                is LicenseActivationResult.Success -> {
                    _licenseDialogState.value = _licenseDialogState.value.copy(
                        isLoading = false,
                        successMessage = result.message
                    )
                    _snackbarEvent.emit("License Activated: ${result.license.plan}")
                }
                is LicenseActivationResult.Failure -> {
                    _licenseDialogState.value = _licenseDialogState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                    _snackbarEvent.emit("Activation Failed: ${result.code.name}")
                }
            }
        }
    }

    fun deactivateLicense() {
        viewModelScope.launch {
            repository.revokeCurrentLicense("User manual deactivation")
            _snackbarEvent.emit("License has been deactivated from this device.")
        }
    }

    // Session Management
    fun terminateOtherSessions() {
        viewModelScope.launch {
            repository.revokeOtherSessions()
            _snackbarEvent.emit("All remote sessions have been terminated.")
        }
    }

    fun signOutCurrentSession() {
        viewModelScope.launch {
            repository.signOutCurrentSession()
            _snackbarEvent.emit("Signed out of current session.")
        }
    }

    // Server Health Controls
    fun setServerStatus(status: ServerHealthStatus) {
        repository.setServerStatus(status)
        viewModelScope.launch {
            _snackbarEvent.emit("Server status changed to: ${status.displayName}")
        }
    }

    // Role Switcher for Admin / Testing
    fun setUserRole(role: UserRole) {
        repository.setUserRole(role)
        viewModelScope.launch {
            _snackbarEvent.emit("Switched active perspective to: ${role.displayName}")
        }
    }

    // Notifications
    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
            _snackbarEvent.emit("All notifications marked as read.")
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
            _snackbarEvent.emit("Notifications cleared.")
        }
    }

    // Diagnostics
    fun clearLogs() {
        viewModelScope.launch {
            repository.clearDiagnosticLogs()
            _snackbarEvent.emit("Diagnostic audit logs cleared.")
        }
    }

    // Updates
    fun triggerCheckForUpdates() {
        viewModelScope.launch {
            _snackbarEvent.emit("Checking FZ ENGINE distribution network for updates...")
            kotlinx.coroutines.delay(800)
            isUpdateModalOpen.value = true
        }
    }

    fun simulateUpdateAvailability(hasUpdate: Boolean, isRequired: Boolean) {
        repository.setUpdateStatus(hasUpdate, isRequired)
        isUpdateModalOpen.value = true
    }

    // Game Launch for Carrom Disc Pool
    fun launchCarromGame() {
        val context = getApplication<Application>()
        val packageName = "com.miniclip.carrom"
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)

        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            // Legitimate launcher behavior: notify user or offer Google Play Store link
            try {
                val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(storeIntent)
            } catch (e: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            }
            viewModelScope.launch {
                _snackbarEvent.emit("Carrom Disc Pool is not installed locally. Redirecting to Google Play Store.")
            }
        }
    }
}
