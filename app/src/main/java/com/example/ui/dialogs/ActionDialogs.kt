package com.example.ui.dialogs

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SavedWallet
import com.example.data.model.Transaction
import com.example.ui.components.CopyChip
import com.example.ui.components.StatusBadge
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
fun SendWalletDialog(
    transaction: Transaction,
    savedWallets: List<SavedWallet>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (cryptoAddress: String, customNote: String?) -> Unit
) {
    // Initial auto-fill from matching saved wallet if found
    val matchingWallet = savedWallets.firstOrNull {
        it.network.equals(transaction.cryptoNetwork, ignoreCase = true) ||
        it.currency.equals(transaction.cryptoCurrency, ignoreCase = true)
    }

    var addressInput by remember { mutableStateOf(matchingWallet?.address ?: transaction.cryptoAddress) }
    var noteInput by remember { mutableStateOf("Please deposit exact amount to this wallet address.") }

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, PurpleAccent, RoundedCornerShape(20.dp))
                .testTag("send_wallet_dialog"),
            color = Navy900
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PurpleAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = PurpleAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Send Receiving Wallet",
                                color = TextWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Transaction #${transaction.id.take(8)}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, enabled = !isSubmitting) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Withdrawal Details Summary
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Navy800)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Requested Amount:", color = TextMuted, fontSize = 12.sp)
                        Text("$${transaction.amount} USD", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Currency & Network:", color = TextMuted, fontSize = 12.sp)
                        Text("${transaction.cryptoCurrency} (${transaction.cryptoNetwork})", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Client Payout Phone:", color = TextMuted, fontSize = 12.sp)
                        Text("${transaction.mobileNetwork} ${transaction.phoneNumber}", color = TextLight, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Preset Chips
                if (savedWallets.isNotEmpty()) {
                    Text(
                        text = "SELECT SAVED ADMIN WALLET:",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        savedWallets.forEach { w ->
                            val isSelected = addressInput == w.address
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PurpleAccent.copy(alpha = 0.3f) else Navy800)
                                    .border(
                                        1.dp,
                                        if (isSelected) PurpleAccent else CardBorderDark,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { addressInput = w.address }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = w.label,
                                    color = if (isSelected) PurpleAccent else TextLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Address Input
                Text(
                    text = "RECEIVING CRYPTO ADDRESS:",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    placeholder = { Text("e.g. TYDzsYUEWvYpxqFm7K4vT8N5tZ7wY4hJ9x", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_wallet_address"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextLight,
                        focusedBorderColor = PurpleAccent,
                        unfocusedBorderColor = CardBorderDark,
                        focusedContainerColor = Navy800,
                        unfocusedContainerColor = Navy800
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Note to Client
                Text(
                    text = "NOTE TO CLIENT (OPTIONAL):",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    placeholder = { Text("Instructions for client", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextLight,
                        focusedBorderColor = PurpleAccent,
                        unfocusedBorderColor = CardBorderDark,
                        focusedContainerColor = Navy800,
                        unfocusedContainerColor = Navy800
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting
                    ) {
                        Text("Cancel", color = TextMuted)
                    }

                    Button(
                        onClick = { onConfirm(addressInput, noteInput.ifBlank { null }) },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(44.dp)
                            .testTag("submit_send_wallet"),
                        enabled = addressInput.isNotBlank() && !isSubmitting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurpleAccent,
                            contentColor = Navy900
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Navy900, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send to Client", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApproveDepositDialog(
    transaction: Transaction,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (txHash: String?, adminMessage: String?) -> Unit
) {
    var txHashInput by remember { mutableStateOf("") }
    var messageInput by remember {
        mutableStateOf("Deposit confirmed! Sent $${transaction.amount} ${transaction.cryptoCurrency} (${transaction.cryptoNetwork}) to your wallet.")
    }

    val tzsFormatter = NumberFormat.getNumberInstance(Locale.US)
    val formattedTzs = tzsFormatter.format(transaction.amountTzs.toLong())

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, EmeraldGreen, RoundedCornerShape(20.dp))
                .testTag("approve_deposit_dialog"),
            color = Navy900
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Approve Client Deposit",
                                color = TextWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Marks status as COMPLETED",
                                color = EmeraldGreen,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, enabled = !isSubmitting) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Deposit Verification Checklist
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Navy800)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Paid via Mobile Money:", color = TextMuted, fontSize = 12.sp)
                        Text("TZS $formattedTzs", color = AmberWarning, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Crypto Amount to Client:", color = TextMuted, fontSize = 12.sp)
                        Text("$${transaction.amount} ${transaction.cryptoCurrency}", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Blockchain Network:", color = TextMuted, fontSize = 12.sp)
                        Text(transaction.cryptoNetwork, color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Client Destination Wallet:", color = TextMuted, fontSize = 12.sp)
                        CopyChip(text = transaction.cryptoAddress, label = "Copy")
                    }
                    Text(
                        text = transaction.cryptoAddress,
                        color = TextLight,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tx Hash Input
                Text(
                    text = "BLOCKCHAIN TRANSACTION HASH (OPTIONAL):",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = txHashInput,
                    onValueChange = { txHashInput = it },
                    placeholder = { Text("e.g. 0x8a92... or TronScan hash", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_tx_hash"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextLight,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = CardBorderDark,
                        focusedContainerColor = Navy800,
                        unfocusedContainerColor = Navy800
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Admin Message Input
                Text(
                    text = "CONFIRMATION MESSAGE TO CLIENT:",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextLight,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = CardBorderDark,
                        focusedContainerColor = Navy800,
                        unfocusedContainerColor = Navy800
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting
                    ) {
                        Text("Cancel", color = TextMuted)
                    }

                    Button(
                        onClick = { onConfirm(txHashInput.ifBlank { null }, messageInput.ifBlank { null }) },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(44.dp)
                            .testTag("submit_approve_deposit"),
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            contentColor = Navy900
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Navy900, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Confirm & Approve", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApproveWithdrawalDialog(
    transaction: Transaction,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (txHash: String?, adminMessage: String?) -> Unit
) {
    val tzsFormatter = NumberFormat.getNumberInstance(Locale.US)
    val formattedTzs = tzsFormatter.format(transaction.amountTzs.toLong())

    var refInput by remember { mutableStateOf("") }
    var messageInput by remember {
        mutableStateOf("Withdrawal of $${transaction.amount} USD processed! Net payout of TZS $formattedTzs sent to ${transaction.phoneNumber} (${transaction.feePercentage}% fee deducted).")
    }

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, EmeraldGreen, RoundedCornerShape(20.dp))
                .testTag("approve_withdrawal_dialog"),
            color = Navy900
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Approve Withdrawal Payout",
                                color = TextWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sends payout confirmation to client",
                                color = EmeraldGreen,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, enabled = !isSubmitting) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payout Summary Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Navy800)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Net Mobile Money Payout:", color = TextMuted, fontSize = 12.sp)
                        Text("TZS $formattedTzs", color = EmeraldGreen, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Client Phone Number:", color = TextMuted, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(transaction.phoneNumber, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            CopyChip(text = transaction.phoneNumber, label = "Phone")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Network Operator:", color = TextMuted, fontSize = 12.sp)
                        Text(transaction.mobileNetwork, color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Withdrawal Base / Fee:", color = TextMuted, fontSize = 12.sp)
                        Text("$${transaction.amount} USD (-${transaction.feePercentage}%)", color = TextLight, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mobile Money Reference / Tx Hash Input
                Text(
                    text = "MOBILE MONEY RECEIPT / REF (OPTIONAL):",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = refInput,
                    onValueChange = { refInput = it },
                    placeholder = { Text("e.g. M-Pesa 9JA4893KL or ClickPesa ref", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_withdrawal_ref"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextLight,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = CardBorderDark,
                        focusedContainerColor = Navy800,
                        unfocusedContainerColor = Navy800
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Message to Client
                Text(
                    text = "MESSAGE TO CLIENT:",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextLight,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = CardBorderDark,
                        focusedContainerColor = Navy800,
                        unfocusedContainerColor = Navy800
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting
                    ) {
                        Text("Cancel", color = TextMuted)
                    }

                    Button(
                        onClick = { onConfirm(refInput.ifBlank { null }, messageInput.ifBlank { null }) },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(44.dp)
                            .testTag("submit_approve_withdrawal"),
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            contentColor = Navy900
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Navy900, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Confirm & Approve", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionDetailDialog(
    transaction: Transaction,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, CardBorderDark, RoundedCornerShape(20.dp)),
            color = Navy900
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaction Audit",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                StatusBadge(status = transaction.status)

                DetailRow("Transaction ID", transaction.id, copyable = true)
                DetailRow("Temporary Client ID", transaction.userId.ifBlank { "N/A" }, copyable = transaction.userId.isNotBlank())
                DetailRow("Type", transaction.type.uppercase())
                DetailRow("Crypto Currency", "${transaction.cryptoCurrency} (${transaction.cryptoNetwork})")
                DetailRow("Base USD Amount", "$${transaction.amount}")
                DetailRow("Fee Percentage", "${transaction.feePercentage}%")
                DetailRow("Fee USD Amount", "$${transaction.feeAmount}")
                DetailRow("Total USD Amount", "$${transaction.totalAmount}")
                DetailRow("TZS Amount", "${transaction.amountTzs.toLong()} TZS")
                DetailRow("Mobile Network", transaction.mobileNetwork.ifBlank { "N/A" })
                DetailRow("Phone Number", transaction.phoneNumber.ifBlank { "N/A" }, copyable = transaction.phoneNumber.isNotBlank())
                DetailRow("Wallet Address", transaction.cryptoAddress.ifBlank { "N/A" }, copyable = transaction.cryptoAddress.isNotBlank())
                DetailRow("ClickPesa Reference", transaction.clickpesaReference ?: "None", copyable = transaction.clickpesaReference != null)
                DetailRow("Transaction Hash", transaction.txHash ?: "None", copyable = transaction.txHash != null)
                DetailRow("Admin Note / Message", transaction.adminMessage ?: "None")
                DetailRow("Created At", transaction.createdAt.ifBlank { "N/A" })
                DetailRow("Updated At", transaction.updatedAt.ifBlank { "N/A" })

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy700, contentColor = TextWhite)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    copyable: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Navy800)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            if (copyable) {
                CopyChip(text = value, label = label)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = TextLight,
            fontSize = 12.sp,
            fontFamily = if (copyable) FontFamily.Monospace else FontFamily.Default
        )
    }
}
