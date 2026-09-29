package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.dialogs.ApproveDepositDialog
import com.example.ui.dialogs.ApproveWithdrawalDialog
import com.example.ui.dialogs.SendWalletDialog
import com.example.ui.dialogs.TransactionDetailDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.RatesFeesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardDark
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SwiftFXAdminTheme
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.viewmodel.ActionDialogState
import com.example.viewmodel.AdminTab
import com.example.viewmodel.AdminViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SwiftFXAdminTheme(darkTheme = true) {
                AdminApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AdminApp(viewModel: AdminViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Feedback messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedbackMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { err ->
            snackbarHostState.showSnackbar("Error: $err")
            viewModel.clearFeedbackMessages()
        }
    }

    // Back handler: dismiss dialog or return to dashboard
    BackHandler(enabled = uiState.dialogState !is ActionDialogState.None || uiState.selectedTab != AdminTab.DASHBOARD) {
        if (uiState.dialogState !is ActionDialogState.None) {
            viewModel.closeDialog()
        } else if (uiState.selectedTab != AdminTab.DASHBOARD) {
            viewModel.selectTab(AdminTab.DASHBOARD)
        }
    }

    val actionRequiredCount = uiState.transactions.count { it.isActionRequired }
    val unreadCount = uiState.unreadNotificationsCount

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = Navy800,
                contentColor = TextLight,
                windowInsets = WindowInsets.navigationBars,
                tonalElevation = 8.dp
            ) {
                // Tab 1: Operations / Dashboard
                NavigationBarItem(
                    selected = uiState.selectedTab == AdminTab.DASHBOARD,
                    onClick = { viewModel.selectTab(AdminTab.DASHBOARD) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (actionRequiredCount > 0) {
                                    Badge(containerColor = AmberWarning, contentColor = Navy900) {
                                        Text("$actionRequiredCount", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (uiState.selectedTab == AdminTab.DASHBOARD) Icons.Default.SwapHoriz else Icons.Outlined.SwapHoriz,
                                contentDescription = "Transactions"
                            )
                        }
                    },
                    label = { Text("Transactions", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Navy900,
                        selectedTextColor = ElectricCyan,
                        indicatorColor = ElectricCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_transactions")
                )

                // Tab 2: Alerts / Notifications
                NavigationBarItem(
                    selected = uiState.selectedTab == AdminTab.NOTIFICATIONS,
                    onClick = { viewModel.selectTab(AdminTab.NOTIFICATIONS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(containerColor = AmberWarning, contentColor = Navy900) {
                                        Text("$unreadCount", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (uiState.selectedTab == AdminTab.NOTIFICATIONS) Icons.Default.NotificationsActive else Icons.Outlined.Notifications,
                                contentDescription = "Alerts"
                            )
                        }
                    },
                    label = { Text("Live Alerts", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Navy900,
                        selectedTextColor = AmberWarning,
                        indicatorColor = AmberWarning,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_alerts")
                )

                // Tab 3: Rates & Fees
                NavigationBarItem(
                    selected = uiState.selectedTab == AdminTab.RATES_FEES,
                    onClick = { viewModel.selectTab(AdminTab.RATES_FEES) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.selectedTab == AdminTab.RATES_FEES) Icons.Default.CurrencyExchange else Icons.Outlined.CurrencyExchange,
                            contentDescription = "Fees"
                        )
                    },
                    label = { Text("Fees & Rates", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Navy900,
                        selectedTextColor = EmeraldGreen,
                        indicatorColor = EmeraldGreen,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_rates")
                )

                // Tab 4: Settings
                NavigationBarItem(
                    selected = uiState.selectedTab == AdminTab.SETTINGS,
                    onClick = { viewModel.selectTab(AdminTab.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.selectedTab == AdminTab.SETTINGS) Icons.Default.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Navy900,
                        selectedTextColor = ElectricCyan,
                        indicatorColor = ElectricCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = uiState.selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "tab_animation"
        ) { targetTab ->
            when (targetTab) {
                AdminTab.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    paddingValues = padding
                )
                AdminTab.NOTIFICATIONS -> NotificationsScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    paddingValues = padding
                )
                AdminTab.RATES_FEES -> RatesFeesScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    paddingValues = padding
                )
                AdminTab.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    paddingValues = padding
                )
            }
        }

        // Render Action Dialogs
        when (val dialog = uiState.dialogState) {
            is ActionDialogState.SendWallet -> {
                SendWalletDialog(
                    transaction = dialog.transaction,
                    savedWallets = uiState.savedWallets,
                    isSubmitting = uiState.isSubmittingAction,
                    onDismiss = { viewModel.closeDialog() },
                    onConfirm = { address, note ->
                        viewModel.sendWalletAddress(dialog.transaction.id, address, note)
                    }
                )
            }
            is ActionDialogState.ApproveDeposit -> {
                ApproveDepositDialog(
                    transaction = dialog.transaction,
                    isSubmitting = uiState.isSubmittingAction,
                    onDismiss = { viewModel.closeDialog() },
                    onConfirm = { txHash, msg ->
                        viewModel.approveTransaction(dialog.transaction.id, txHash, msg)
                    }
                )
            }
            is ActionDialogState.ApproveWithdrawal -> {
                ApproveWithdrawalDialog(
                    transaction = dialog.transaction,
                    isSubmitting = uiState.isSubmittingAction,
                    onDismiss = { viewModel.closeDialog() },
                    onConfirm = { txHash, msg ->
                        viewModel.approveTransaction(dialog.transaction.id, txHash, msg)
                    }
                )
            }
            is ActionDialogState.ViewDetails -> {
                TransactionDetailDialog(
                    transaction = dialog.transaction,
                    onDismiss = { viewModel.closeDialog() }
                )
            }
            ActionDialogState.None -> {}
        }
    }
}
