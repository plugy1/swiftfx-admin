package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SavedWallet
import com.example.data.model.Transaction
import com.example.ui.theme.AmberGlow
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
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionCard(
    transaction: Transaction,
    savedWallets: List<SavedWallet>,
    onSendWalletClick: (Transaction) -> Unit,
    onApproveDepositClick: (Transaction) -> Unit,
    onApproveWithdrawalClick: (Transaction) -> Unit,
    onViewDetailsClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDeposit = transaction.isDeposit
    val tzsFormatter = NumberFormat.getNumberInstance(Locale.US)
    val formattedTzs = tzsFormatter.format(transaction.amountTzs.toLong())

    val cardBorderColor = when {
        transaction.status.equals("fiat_received", ignoreCase = true) -> AmberWarning
        transaction.status.equals("awaiting_admin_wallet", ignoreCase = true) -> PurpleAccent
        transaction.status.equals("crypto_received", ignoreCase = true) -> EmeraldGreen
        else -> CardBorderDark
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (transaction.isActionRequired) 1.5.dp else 1.dp,
                color = cardBorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("transaction_card_${transaction.id}"),
        color = CardDark,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Type chip + Network chip + Status Badge + Info icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Type Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDeposit) EmeraldGreen.copy(alpha = 0.15f) else ElectricCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDeposit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (isDeposit) EmeraldGreen else ElectricCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isDeposit) "DEPOSIT" else "WITHDRAWAL",
                                color = if (isDeposit) EmeraldGreen else ElectricCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Crypto Currency & Network Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Navy800)
                            .border(1.dp, CardBorderDark, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${transaction.cryptoCurrency} • ${transaction.cryptoNetwork}",
                            color = TextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = transaction.status)
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { onViewDetailsClick(transaction) },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Details",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Financial Summary Block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Navy800)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isDeposit) "Base Deposit" else "Withdrawal Amount",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "$${transaction.amount} USD",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Fee: ${transaction.feePercentage}% ($${transaction.feeAmount})",
                        color = if (isDeposit) AmberGlow else ElectricCyan,
                        fontSize = 11.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isDeposit) "Total Paid (TZS)" else "Net Payout (TZS)",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "TZS $formattedTzs",
                        color = EmeraldGreen,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (isDeposit) "Total: $${transaction.totalAmount} USD" else "Payout: $${transaction.totalAmount} USD",
                        color = TextLight,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Client & Mobile Money Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Navy900.copy(alpha = 0.6f))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Client row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${transaction.mobileNetwork.ifBlank { "Mobile Money" }} • ${transaction.phoneNumber.ifBlank { "No Phone" }}",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (transaction.phoneNumber.isNotBlank()) {
                        CopyChip(
                            text = transaction.phoneNumber,
                            label = "Phone",
                            toastMessage = "Phone number copied!"
                        )
                    }
                }

                // Temporary Client ID
                if (transaction.userId.isNotBlank()) {
                    Text(
                        text = "Client ID: ${transaction.userId}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Crypto Wallet address row
                if (transaction.cryptoAddress.isNotBlank()) {
                    HorizontalDivider(color = CardBorderDark.copy(alpha = 0.5f), thickness = 0.5.dp)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isDeposit) "Client Destination Wallet:" else "Admin Receiving Wallet:",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            CopyChip(
                                text = transaction.cryptoAddress,
                                label = "Address",
                                toastMessage = "Wallet address copied!"
                            )
                        }

                        Text(
                            text = transaction.cryptoAddress,
                            color = TextLight,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // ACTION PANELS (Based on status)
            when (transaction.status.lowercase()) {
                "fiat_received" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AmberWarning.copy(alpha = 0.12f))
                            .border(1.dp, AmberWarning, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Payment,
                                    contentDescription = null,
                                    tint = AmberWarning,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ACTION REQUIRED: Send Crypto & Approve",
                                    color = AmberWarning,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Client paid TZS $formattedTzs via ClickPesa Mobile Money. Manually transfer $${transaction.amount} ${transaction.cryptoCurrency} to the client's address above, then tap Approve.",
                                color = TextWhite,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (transaction.cryptoAddress.isNotBlank()) {
                                    CopyChip(
                                        text = transaction.cryptoAddress,
                                        label = "Copy Wallet",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Button(
                                    onClick = { onApproveDepositClick(transaction) },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(36.dp)
                                        .testTag("btn_approve_deposit_${transaction.id}"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldGreen,
                                        contentColor = Navy900
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Approve Deposit", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                "awaiting_admin_wallet" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(PurpleAccent.copy(alpha = 0.12f))
                            .border(1.dp, PurpleAccent, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = PurpleAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ACTION REQUIRED: Send Admin Receiving Wallet",
                                    color = PurpleAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Client wants to withdraw $${transaction.amount} USD. Select or paste your ${transaction.cryptoCurrency} (${transaction.cryptoNetwork}) receiving wallet to send to the client.",
                                color = TextWhite,
                                fontSize = 12.sp
                            )

                            // Quick preset chips if matching wallets exist
                            val matchingWallets = savedWallets.filter {
                                it.network.equals(transaction.cryptoNetwork, ignoreCase = true) ||
                                it.currency.equals(transaction.cryptoCurrency, ignoreCase = true)
                            }
                            if (matchingWallets.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Quick Presets:",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    matchingWallets.forEach { w ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Navy800)
                                                .border(1.dp, CardBorderDark, RoundedCornerShape(6.dp))
                                                .clickable { onSendWalletClick(transaction) }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${w.label} (${w.address.take(6)}...)",
                                                color = ElectricCyan,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onSendWalletClick(transaction) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .testTag("btn_send_wallet_${transaction.id}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PurpleAccent,
                                    contentColor = Navy900
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send Wallet Address to Client", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                "crypto_received" -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldGreen.copy(alpha = 0.12f))
                            .border(1.dp, EmeraldGreen, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Payment,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ACTION REQUIRED: Send Mobile Money Payout",
                                    color = EmeraldGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Client deposited crypto! Send net TZS $formattedTzs to ${transaction.phoneNumber} (${transaction.mobileNetwork}), then tap Approve.",
                                color = TextWhite,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CopyChip(
                                    text = formattedTzs.replace(",", ""),
                                    label = "TZS Amount",
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = { onApproveWithdrawalClick(transaction) },
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .height(36.dp)
                                        .testTag("btn_approve_withdrawal_${transaction.id}"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldGreen,
                                        contentColor = Navy900
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Approve Payout", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                "completed" -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldGreen.copy(alpha = 0.08f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (transaction.txHash.isNullOrBlank()) "Completed successfully" else "Tx: ${transaction.txHash.take(16)}...",
                                color = EmeraldGreen,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (!transaction.txHash.isNullOrBlank()) {
                            CopyChip(
                                text = transaction.txHash,
                                label = "Hash",
                                toastMessage = "Transaction hash copied!"
                            )
                        }
                    }
                }
            }
        }
    }
}
