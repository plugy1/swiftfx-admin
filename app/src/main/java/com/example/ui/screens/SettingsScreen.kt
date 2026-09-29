package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SavedWallet
import com.example.data.storage.AdminPreferences
import com.example.ui.components.CopyChip
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CoralRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.viewmodel.AdminUiState
import com.example.viewmodel.AdminViewModel
import java.util.UUID

@Composable
fun SettingsScreen(
    viewModel: AdminViewModel,
    uiState: AdminUiState,
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val prefs = viewModel.preferences

    var apiKeyInput by remember { mutableStateOf(prefs.adminApiKey) }
    var baseUrlInput by remember { mutableStateOf(prefs.baseUrl) }
    var showApiKey by remember { mutableStateOf(false) }

    var soundEnabled by remember { mutableStateOf(prefs.soundEnabled) }
    var vibrationEnabled by remember { mutableStateOf(prefs.vibrationEnabled) }
    var pollInterval by remember { mutableStateOf(prefs.pollIntervalSeconds) }

    var showAddWalletDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy900)
            .padding(paddingValues)
    ) {
        // Header
        Surface(color = Navy900, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "Admin Configuration",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Backend auth, alert preferences, and saved receiving wallets",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // BACKEND AUTH & CONNECTION CARD
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp)),
                color = CardDark
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Backend & Admin API Key", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Render live backend with Supabase and ClickPesa", color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    // Base URL Input
                    Column {
                        Text("Backend Base URL", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = baseUrlInput,
                            onValueChange = { baseUrlInput = it },
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_base_url"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextLight,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = CardBorderDark,
                                focusedContainerColor = Navy800,
                                unfocusedContainerColor = Navy800
                            ),
                            singleLine = true
                        )
                    }

                    // Admin API Key Input
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("x-admin-api-key Header", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Default Key Loaded",
                                color = EmeraldGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showApiKey = !showApiKey }) {
                                    Icon(
                                        imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle visibility",
                                        tint = TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_admin_api_key"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextLight,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = CardBorderDark,
                                focusedContainerColor = Navy800,
                                unfocusedContainerColor = Navy800
                            ),
                            singleLine = true
                        )
                    }

                    // Save / Reset Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                apiKeyInput = AdminPreferences.DEFAULT_ADMIN_KEY
                                baseUrlInput = AdminPreferences.DEFAULT_BASE_URL
                                viewModel.updateAdminApiKey(AdminPreferences.DEFAULT_ADMIN_KEY)
                                viewModel.updateBaseUrl(AdminPreferences.DEFAULT_BASE_URL)
                                Toast.makeText(context, "Reset to default credentials", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Reset Default", fontSize = 12.sp, color = TextLight)
                        }

                        Button(
                            onClick = {
                                viewModel.updateAdminApiKey(apiKeyInput.trim())
                                viewModel.updateBaseUrl(baseUrlInput.trim())
                                Toast.makeText(context, "Saved & refreshed backend", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Navy900),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save & Connect", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ALERTS & REAL-TIME POLLING CARD
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp)),
                color = CardDark
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AmberWarning.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Alerts & Notification Chimes", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Synthesized audio alert chimes and haptic vibrations", color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    // Sound Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Audible Alert Chime", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Plays dual-tone chime on new event", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = {
                                soundEnabled = it
                                viewModel.toggleSound(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ElectricCyan,
                                checkedTrackColor = Navy700
                            )
                        )
                    }

                    HorizontalDivider(color = CardBorderDark.copy(alpha = 0.5f))

                    // Vibration Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Device Vibration Alert", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Haptic motor feedback on arrival", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = {
                                vibrationEnabled = it
                                viewModel.toggleVibration(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PurpleAccent,
                                checkedTrackColor = Navy700
                            )
                        )
                    }

                    HorizontalDivider(color = CardBorderDark.copy(alpha = 0.5f))

                    // Polling Interval
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Background Polling Interval", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text("${pollInterval}s", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(2, 4, 8, 15).forEach { sec ->
                                val isSelected = pollInterval == sec
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) EmeraldGreen.copy(alpha = 0.2f) else Navy800)
                                        .border(1.dp, if (isSelected) EmeraldGreen else CardBorderDark, RoundedCornerShape(8.dp))
                                        .clickable {
                                            pollInterval = sec
                                            viewModel.setPollInterval(sec)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${sec}s" + if (sec == 4) " ★" else "",
                                        color = if (isSelected) EmeraldGreen else TextLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Test Chime & Vibration Button
                    Button(
                        onClick = { viewModel.testChimeSound() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("btn_test_audio_vibration"),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy700, contentColor = ElectricCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Chime & Haptics Now", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // SAVED PRESET RECEIVING WALLETS
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp)),
                color = CardDark
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                                    .background(PurpleAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Saved Receiving Wallets", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Quick-fill into client withdrawal requests", color = TextMuted, fontSize = 11.sp)
                            }
                        }

                        IconButton(
                            onClick = { showAddWalletDialog = true },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Navy700)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Wallet", tint = ElectricCyan, modifier = Modifier.size(18.dp))
                        }
                    }

                    if (uiState.savedWallets.isEmpty()) {
                        Text("No saved preset wallets. Tap '+' to add one.", color = TextMuted, fontSize = 12.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.savedWallets.forEach { wallet ->
                                SavedWalletItem(
                                    wallet = wallet,
                                    onDelete = { viewModel.deletePresetWallet(wallet.id) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAddWalletDialog) {
        AddWalletDialog(
            onDismiss = { showAddWalletDialog = false },
            onSave = { wallet ->
                viewModel.savePresetWallet(wallet)
                showAddWalletDialog = false
            }
        )
    }
}

@Composable
private fun SavedWalletItem(
    wallet: SavedWallet,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Navy800)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PurpleAccent.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(wallet.network, color = PurpleAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(wallet.label, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                CopyChip(text = wallet.address, label = "Copy")
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = wallet.address,
            color = TextMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun AddWalletDialog(
    onDismiss: () -> Unit,
    onSave: (SavedWallet) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("USDT") }
    var network by remember { mutableStateOf("TRC20") }
    var address by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp)),
            color = Navy900
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Add Receiving Wallet", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (e.g. USDT Binance TRC20)", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextLight,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = CardBorderDark,
                        focusedContainerColor = Navy800,
                        unfocusedContainerColor = Navy800
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text("Currency (USDT, BTC)", color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextLight,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = CardBorderDark,
                            focusedContainerColor = Navy800,
                            unfocusedContainerColor = Navy800
                        )
                    )

                    OutlinedTextField(
                        value = network,
                        onValueChange = { network = it },
                        label = { Text("Network (TRC20, BEP20)", color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextLight,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = CardBorderDark,
                            focusedContainerColor = Navy800,
                            unfocusedContainerColor = Navy800
                        )
                    )
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Wallet Address", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextLight,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = CardBorderDark,
                        focusedContainerColor = Navy800,
                        unfocusedContainerColor = Navy800
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel", color = TextLight)
                    }

                    Button(
                        onClick = {
                            if (address.isNotBlank() && label.isNotBlank()) {
                                onSave(
                                    SavedWallet(
                                        id = "w_${UUID.randomUUID()}",
                                        label = label.trim(),
                                        currency = currency.trim().uppercase(),
                                        network = network.trim().uppercase(),
                                        address = address.trim()
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = address.isNotBlank() && label.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Navy900)
                    ) {
                        Text("Save Wallet", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
