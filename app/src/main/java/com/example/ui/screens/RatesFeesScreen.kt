package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.viewmodel.AdminUiState
import com.example.viewmodel.AdminViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun RatesFeesScreen(
    viewModel: AdminViewModel,
    uiState: AdminUiState,
    paddingValues: PaddingValues
) {
    val settings = uiState.settings

    var depositFeeText by remember { mutableStateOf(settings.depositFeePercentage.toString()) }
    var withdrawFeeText by remember { mutableStateOf(settings.withdrawFeePercentage.toString()) }
    var usdTzsRateText by remember { mutableStateOf(settings.usdTzsRate.toInt().toString()) }

    // Sync state when loaded
    LaunchedEffect(settings) {
        depositFeeText = settings.depositFeePercentage.toString()
        withdrawFeeText = settings.withdrawFeePercentage.toString()
        usdTzsRateText = settings.usdTzsRate.toInt().toString()
    }

    // Simulator test amount
    var testUsdAmount by remember { mutableStateOf("100") }

    val depositFeeVal = depositFeeText.toDoubleOrNull() ?: 2.5
    val withdrawFeeVal = withdrawFeeText.toDoubleOrNull() ?: 2.0
    val rateVal = usdTzsRateText.toDoubleOrNull() ?: 2580.0
    val simUsdVal = testUsdAmount.toDoubleOrNull() ?: 100.0

    val tzsFormatter = NumberFormat.getNumberInstance(Locale.US)

    // Deposit Math: Client enters base amount USD. Fee % is added. Total paid = amount * (1 + fee%/100)
    val depFeeAmount = simUsdVal * (depositFeeVal / 100.0)
    val depTotalUsd = simUsdVal + depFeeAmount
    val depTotalTzs = depTotalUsd * rateVal

    // Withdrawal Math: Client requests USD payout. Fee % is deducted. Net amount = amount * (1 - fee%/100)
    val withFeeAmount = simUsdVal * (withdrawFeeVal / 100.0)
    val withNetUsd = simUsdVal - withFeeAmount
    val withNetTzs = withNetUsd * rateVal

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
                    text = "Rates & Fees Management",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Live exchange rates and commission margins synced with Client App",
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
            // Live Rates Control Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyExchange,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Platform Parameters",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Updates ClickPesa & Client App calculations immediately",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Deposit Fee %
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Client Deposit Fee %",
                                color = TextLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Added to Mobile Money bill",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = depositFeeText,
                            onValueChange = { depositFeeText = it },
                            leadingIcon = {
                                Icon(Icons.Default.Percent, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            },
                            trailingIcon = { Text("%", color = TextMuted, modifier = Modifier.padding(end = 12.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_deposit_fee"),
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
                    }

                    // Withdrawal Fee %
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Client Withdrawal Fee %",
                                color = TextLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Deducted from client payout",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = withdrawFeeText,
                            onValueChange = { withdrawFeeText = it },
                            leadingIcon = {
                                Icon(Icons.Default.Percent, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(16.dp))
                            },
                            trailingIcon = { Text("%", color = TextMuted, modifier = Modifier.padding(end = 12.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_withdraw_fee"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextLight,
                                focusedBorderColor = PurpleAccent,
                                unfocusedBorderColor = CardBorderDark,
                                focusedContainerColor = Navy800,
                                unfocusedContainerColor = Navy800
                            )
                        )
                    }

                    // USD/TZS Rate
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "USD / TZS Exchange Rate",
                                color = TextLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "TZS per 1 USD",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = usdTzsRateText,
                            onValueChange = { usdTzsRateText = it },
                            trailingIcon = { Text("TZS", color = TextMuted, modifier = Modifier.padding(end = 12.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_usd_tzs_rate"),
                            shape = RoundedCornerShape(10.dp),
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

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            viewModel.updateFeeSettings(depositFeeVal, withdrawFeeVal, rateVal)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_save_settings"),
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = Navy900
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Navy900, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Parameters to Backend", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Interactive Live Math Preview Calculator
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AmberWarning.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = AmberWarning,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Live Settlement Simulator",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Simulate how clients and admin settle trades",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Test USD input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Test USD Amount:", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = testUsdAmount,
                            onValueChange = { testUsdAmount = it },
                            prefix = { Text("$", color = TextWhite) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(8.dp),
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

                    HorizontalDivider(color = CardBorderDark)

                    // Deposit Simulator Result
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
                            Text("CLIENT DEPOSIT (Fiat -> Crypto)", color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("+$depositFeeVal% Fee", color = AmberWarning, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Base Amount to Deliver:", color = TextMuted, fontSize = 11.sp)
                            Text("$$simUsdVal USDT", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Deposit Fee Paid by Client:", color = TextMuted, fontSize = 11.sp)
                            Text("+$${String.format(Locale.US, "%.2f", depFeeAmount)} USD", color = AmberWarning, fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Client Pays via USSD Push:", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("TZS ${tzsFormatter.format(depTotalTzs.toLong())}", color = EmeraldGreen, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    // Withdrawal Simulator Result
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
                            Text("CLIENT WITHDRAWAL (Crypto -> Fiat)", color = PurpleAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("-$withdrawFeeVal% Fee", color = AmberWarning, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Client Sends to Admin Wallet:", color = TextMuted, fontSize = 11.sp)
                            Text("$$simUsdVal USDT", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Withdrawal Fee Retained:", color = TextMuted, fontSize = 11.sp)
                            Text("-$${String.format(Locale.US, "%.2f", withFeeAmount)} USD", color = PurpleAccent, fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Admin Sends Payout to Phone:", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("TZS ${tzsFormatter.format(withNetTzs.toLong())}", color = EmeraldGreen, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
