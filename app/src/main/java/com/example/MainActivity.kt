package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.NavigationTab
import com.example.ui.components.AdminManagementSheet
import com.example.ui.components.CarromDetailsDialog
import com.example.ui.components.DiagnosticsDialog
import com.example.ui.components.FzBottomNavigation
import com.example.ui.components.FzTopHeader
import com.example.ui.components.LegalDialog
import com.example.ui.components.LicenseEntryDialog
import com.example.ui.components.NotificationSheet
import com.example.ui.components.UpdateDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PremiumScreen
import com.example.ui.screens.SellersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.FzBackground
import com.example.ui.theme.FzEngineTheme
import com.example.ui.viewmodel.FzEngineViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FzEngineTheme {
                FzEngineApp()
            }
        }
    }
}

@Composable
fun FzEngineApp(viewModel: FzEngineViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsState()
    val activeLicense by viewModel.activeLicense.collectAsState()
    val serverStatus by viewModel.serverStatus.collectAsState()
    val userRole by viewModel.currentUserRole.collectAsState()
    val sellers by viewModel.sellers.collectAsState()
    val activeSessions by viewModel.activeSessions.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val licenseDialogState by viewModel.licenseDialogState.collectAsState()
    val updateInfo by viewModel.updateInfo.collectAsState()

    val isNotificationsSheetOpen by viewModel.isNotificationsSheetOpen.collectAsState()
    val isAdminSheetOpen by viewModel.isAdminSheetOpen.collectAsState()
    val isDiagnosticsOpen by viewModel.isDiagnosticsDialogOpen.collectAsState()
    val isGameDetailsOpen by viewModel.isGameDetailsDialogOpen.collectAsState()
    val isUpdateModalOpen by viewModel.isUpdateModalOpen.collectAsState()
    val isLegalOpen by viewModel.isPrivacyTermsModalOpen.collectAsState()
    val legalMode by viewModel.privacyTermsMode.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = FzBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FzTopHeader(
                title = when (currentTab) {
                    NavigationTab.HOME -> "Games"
                    NavigationTab.PREMIUM -> "Premium"
                    NavigationTab.SELLERS -> "Sellers"
                    NavigationTab.SETTINGS -> "Settings"
                },
                userRole = userRole,
                isLicensed = activeLicense != null,
                unreadNotificationsCount = unreadCount,
                onNotificationsClick = { viewModel.isNotificationsSheetOpen.value = true },
                onAdminConsoleClick = { viewModel.isAdminSheetOpen.value = true }
            )
        },
        bottomBar = {
            FzBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FzBackground)
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetTab ->
                when (targetTab) {
                    NavigationTab.HOME -> {
                        HomeScreen(
                            activeLicense = activeLicense,
                            serverStatus = serverStatus,
                            onUnlockClick = { viewModel.openLicenseDialog() },
                            onOpenGameClick = { viewModel.launchCarromGame() },
                            onViewGameDetails = { viewModel.isGameDetailsDialogOpen.value = true },
                            onViewLicenseInfo = { viewModel.selectTab(NavigationTab.PREMIUM) },
                            onSupportClick = { viewModel.isNotificationsSheetOpen.value = true },
                            onServerStatusClick = { viewModel.isAdminSheetOpen.value = true }
                        )
                    }
                    NavigationTab.PREMIUM -> {
                        PremiumScreen(
                            activeLicense = activeLicense,
                            activeSessions = activeSessions,
                            onEnterKeyClick = { viewModel.openLicenseDialog() },
                            onDeactivateLicense = { viewModel.deactivateLicense() },
                            onTerminateOtherSessions = { viewModel.terminateOtherSessions() },
                            onSignOutCurrent = { viewModel.signOutCurrentSession() }
                        )
                    }
                    NavigationTab.SELLERS -> {
                        SellersScreen(
                            sellers = sellers,
                            onShowSnackbar = { msg ->
                                // Trigger snackbar through viewmodel
                                viewModel.selectTab(NavigationTab.SELLERS)
                            }
                        )
                    }
                    NavigationTab.SETTINGS -> {
                        SettingsScreen(
                            userRole = userRole,
                            serverStatus = serverStatus,
                            onOpenRoleConsole = { viewModel.isAdminSheetOpen.value = true },
                            onCheckForUpdates = { viewModel.triggerCheckForUpdates() },
                            onOpenDiagnostics = { viewModel.isDiagnosticsDialogOpen.value = true },
                            onOpenLegal = { mode ->
                                viewModel.privacyTermsMode.value = mode
                                viewModel.isPrivacyTermsModalOpen.value = true
                            },
                            onSignOutSession = { viewModel.signOutCurrentSession() }
                        )
                    }
                }
            }
        }
    }

    // Modal dialogs & bottom sheets
    LicenseEntryDialog(
        isOpen = licenseDialogState.isOpen,
        keyInput = licenseDialogState.keyInput,
        isKeyMasked = licenseDialogState.isKeyMasked,
        isLoading = licenseDialogState.isLoading,
        errorMessage = licenseDialogState.errorMessage,
        successMessage = licenseDialogState.successMessage,
        onKeyChange = { viewModel.updateKeyInput(it) },
        onToggleMask = { viewModel.toggleKeyMask() },
        onSubmit = { viewModel.submitLicenseKey() },
        onDismiss = { viewModel.dismissLicenseDialog() }
    )

    NotificationSheet(
        isOpen = isNotificationsSheetOpen,
        notifications = notifications,
        onDismiss = { viewModel.isNotificationsSheetOpen.value = false },
        onMarkRead = { viewModel.markNotificationAsRead(it) },
        onMarkAllRead = { viewModel.markAllNotificationsAsRead() },
        onClearAll = { viewModel.clearAllNotifications() }
    )

    AdminManagementSheet(
        isOpen = isAdminSheetOpen,
        currentRole = userRole,
        serverStatus = serverStatus,
        onRoleSelected = { viewModel.setUserRole(it) },
        onServerStatusChanged = { viewModel.setServerStatus(it) },
        onSimulateUpdate = { hasUpdate, isRequired ->
            viewModel.simulateUpdateAvailability(hasUpdate, isRequired)
        },
        onOpenDiagnostics = {
            viewModel.isAdminSheetOpen.value = false
            viewModel.isDiagnosticsDialogOpen.value = true
        },
        onRevokeLicense = {
            viewModel.deactivateLicense()
        },
        onDismiss = { viewModel.isAdminSheetOpen.value = false }
    )

    DiagnosticsDialog(
        isOpen = isDiagnosticsOpen,
        logs = auditLogs,
        onClearLogs = { viewModel.clearLogs() },
        onDismiss = { viewModel.isDiagnosticsDialogOpen.value = false }
    )

    CarromDetailsDialog(
        isOpen = isGameDetailsOpen,
        license = activeLicense,
        onLaunch = { viewModel.launchCarromGame() },
        onUnlockClick = { viewModel.openLicenseDialog() },
        onDismiss = { viewModel.isGameDetailsDialogOpen.value = false }
    )

    UpdateDialog(
        isOpen = isUpdateModalOpen,
        updateInfo = updateInfo,
        onDismiss = { viewModel.isUpdateModalOpen.value = false }
    )

    LegalDialog(
        isOpen = isLegalOpen,
        mode = legalMode,
        onDismiss = { viewModel.isPrivacyTermsModalOpen.value = false }
    )
}
