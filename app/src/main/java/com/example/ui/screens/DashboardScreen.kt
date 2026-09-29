package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.ui.components.LiveNotificationBanner
import com.example.ui.components.MetricStatCard
import com.example.ui.components.TransactionCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.viewmodel.ActionDialogState
import com.example.viewmodel.AdminUiState
import com.example.viewmodel.AdminViewModel
import com.example.viewmodel.TransactionFilter
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: AdminViewModel,
    uiState: AdminUiState,
    paddingValues: PaddingValues
) {
    val transactions = uiState.transactions
    val actionRequiredCount = transactions.count { it.isActionRequired }
    val totalVolumeUsd = transactions.sumOf { it.amount }
    val completedCount = transactions.count { it.status.equals("completed", ignoreCase = true) }

    // Filter transactions
    val filteredTransactions = transactions.filter { tx ->
        val matchesFilter = when (uiState.selectedFilter) {
            TransactionFilter.ALL -> true
            TransactionFilter.ACTION_REQUIRED -> tx.isActionRequired
            TransactionFilter.DEPOSITS -> tx.isDeposit
            TransactionFilter.WITHDRAWALS -> tx.isWithdrawal
            TransactionFilter.COMPLETED -> tx.status.equals("completed", ignoreCase = true)
        }

        val query = uiState.searchQuery.trim().lowercase()
        val matchesSearch = if (query.isBlank()) {
            true
        } else {
            tx.id.lowercase().contains(query) ||
            tx.userId.lowercase().contains(query) ||
            tx.phoneNumber.lowercase().contains(query) ||
            tx.cryptoAddress.lowercase().contains(query) ||
            tx.cryptoCurrency.lowercase().contains(query) ||
            tx.mobileNetwork.lowercase().contains(query)
        }

        matchesFilter && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy900)
            .padding(paddingValues)
    ) {
        // Top Brand & Live Status Bar
        Surface(
            color = Navy900,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SWIFT",
                                color = TextWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "FX",
                                color = ElectricCyan,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PurpleAccent.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ADMIN",
                                    color = PurpleAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        Text(
                            text = "Render Backend • ClickPesa & Supabase",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Live Status Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (uiState.isConnected) EmeraldGreen.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.2f))
                                .border(
                                    1.dp,
                                    if (uiState.isConnected) EmeraldGreen.copy(alpha = 0.4f) else Color.Red,
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.isConnected) EmeraldGreen else Color.Red)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (uiState.isConnected) "Live 4s" else "Offline",
                                    color = if (uiState.isConnected) EmeraldGreen else Color.Red,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Test Chime Button
                        IconButton(
                            onClick = { viewModel.testChimeSound() },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Test Audio Chime",
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Refresh Button
                        IconButton(
                            onClick = { viewModel.manualRefresh() },
                            modifier = Modifier.size(34.dp),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    color = ElectricCyan,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = TextLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Real-Time Notification Banner (when event happens)
        LiveNotificationBanner(
            notification = uiState.activeBannerAlert,
            onDismiss = { viewModel.dismissBanner() },
            onViewTransaction = { txId ->
                viewModel.dismissBanner()
                val targetTx = viewModel.findTransactionById(txId)
                if (targetTx != null) {
                    viewModel.openDialog(ActionDialogState.ViewDetails(targetTx))
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Metrics Stat Grid
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "Action Required",
                        value = "$actionRequiredCount",
                        subtext = if (actionRequiredCount > 0) "Immediate attention" else "All up to date",
                        icon = Icons.Default.NotificationImportant,
                        accentColor = if (actionRequiredCount > 0) AmberWarning else EmeraldGreen,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_action_required"),
                        onClick = { viewModel.selectFilter(TransactionFilter.ACTION_REQUIRED) }
                    )

                    MetricStatCard(
                        title = "Total Volume",
                        value = "$${totalVolumeUsd.toInt()}",
                        subtext = "${transactions.size} total operations",
                        icon = Icons.Default.AttachMoney,
                        accentColor = ElectricCyan,
                        modifier = Modifier.weight(1f)
                    )

                    MetricStatCard(
                        title = "Completed",
                        value = "$completedCount",
                        subtext = "Successful settlements",
                        icon = Icons.Default.CheckCircle,
                        accentColor = EmeraldGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.selectFilter(TransactionFilter.COMPLETED) }
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "Search ID, phone, client, wallet...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("search_transactions_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextLight,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = CardBorderDark,
                        focusedContainerColor = CardDark,
                        unfocusedContainerColor = CardDark
                    ),
                    singleLine = true
                )
            }

            // Filter Chips Row
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChipItem(
                        label = "Action Required",
                        count = actionRequiredCount,
                        isSelected = uiState.selectedFilter == TransactionFilter.ACTION_REQUIRED,
                        badgeColor = if (actionRequiredCount > 0) AmberWarning else EmeraldGreen,
                        onClick = { viewModel.selectFilter(TransactionFilter.ACTION_REQUIRED) }
                    )
                    FilterChipItem(
                        label = "All",
                        count = transactions.size,
                        isSelected = uiState.selectedFilter == TransactionFilter.ALL,
                        badgeColor = ElectricCyan,
                        onClick = { viewModel.selectFilter(TransactionFilter.ALL) }
                    )
                    FilterChipItem(
                        label = "Deposits",
                        count = transactions.count { it.isDeposit },
                        isSelected = uiState.selectedFilter == TransactionFilter.DEPOSITS,
                        badgeColor = EmeraldGreen,
                        onClick = { viewModel.selectFilter(TransactionFilter.DEPOSITS) }
                    )
                    FilterChipItem(
                        label = "Withdrawals",
                        count = transactions.count { it.isWithdrawal },
                        isSelected = uiState.selectedFilter == TransactionFilter.WITHDRAWALS,
                        badgeColor = ElectricCyan,
                        onClick = { viewModel.selectFilter(TransactionFilter.WITHDRAWALS) }
                    )
                    FilterChipItem(
                        label = "Completed",
                        count = completedCount,
                        isSelected = uiState.selectedFilter == TransactionFilter.COMPLETED,
                        badgeColor = EmeraldGreen,
                        onClick = { viewModel.selectFilter(TransactionFilter.COMPLETED) }
                    )
                }
            }

            // Transaction Cards List
            if (filteredTransactions.isEmpty()) {
                item {
                    EmptyTransactionsView(
                        filter = uiState.selectedFilter,
                        hasSearch = uiState.searchQuery.isNotBlank(),
                        onClearSearch = { viewModel.updateSearchQuery("") },
                        onShowAll = { viewModel.selectFilter(TransactionFilter.ALL) }
                    )
                }
            } else {
                items(
                    items = filteredTransactions,
                    key = { it.id }
                ) { tx ->
                    TransactionCard(
                        transaction = tx,
                        savedWallets = uiState.savedWallets,
                        onSendWalletClick = { viewModel.openDialog(ActionDialogState.SendWallet(it)) },
                        onApproveDepositClick = { viewModel.openDialog(ActionDialogState.ApproveDeposit(it)) },
                        onApproveWithdrawalClick = { viewModel.openDialog(ActionDialogState.ApproveWithdrawal(it)) },
                        onViewDetailsClick = { viewModel.openDialog(ActionDialogState.ViewDetails(it)) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    count: Int,
    isSelected: Boolean,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else CardDark)
            .border(
                1.dp,
                if (isSelected) ElectricCyan else CardBorderDark,
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
            .testTag("filter_chip_${label.lowercase().replace(" ", "_")}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                color = if (isSelected) ElectricCyan else TextLight,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) ElectricCyan else badgeColor.copy(alpha = 0.25f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$count",
                    color = if (isSelected) Navy900 else badgeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyTransactionsView(
    filter: TransactionFilter,
    hasSearch: Boolean,
    onClearSearch: () -> Unit,
    onShowAll: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp)),
        color = CardDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Navy800),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (hasSearch) "No Matching Transactions" else when (filter) {
                    TransactionFilter.ACTION_REQUIRED -> "No Action Required Right Now!"
                    TransactionFilter.DEPOSITS -> "No Deposits Found"
                    TransactionFilter.WITHDRAWALS -> "No Withdrawals Found"
                    TransactionFilter.COMPLETED -> "No Completed Transactions"
                    TransactionFilter.ALL -> "No Transactions Yet"
                },
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (hasSearch) "Try adjusting your search keywords." else when (filter) {
                    TransactionFilter.ACTION_REQUIRED -> "All deposits and withdrawals have been processed. New client actions will trigger audio chimes."
                    else -> "Live polling is active. New events from ClickPesa will appear automatically."
                },
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (hasSearch || filter != TransactionFilter.ALL) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (hasSearch) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Navy700)
                                .clickable { onClearSearch() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Clear Search", color = ElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (filter != TransactionFilter.ALL) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Navy700)
                                .clickable { onShowAll() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("View All Transactions", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
