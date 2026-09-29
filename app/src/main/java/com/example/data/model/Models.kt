package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Transaction(
    val id: String,
    @Json(name = "user_id") val userId: String = "",
    val type: String = "deposit", // "deposit" | "withdrawal"
    @Json(name = "crypto_currency") val cryptoCurrency: String = "USDT",
    @Json(name = "crypto_network") val cryptoNetwork: String = "TRC20",
    @Json(name = "mobile_network") val mobileNetwork: String = "",
    @Json(name = "phone_number") val phoneNumber: String = "",
    val amount: Double = 0.0,
    @Json(name = "fee_percentage") val feePercentage: Double = 0.0,
    @Json(name = "fee_amount") val feeAmount: Double = 0.0,
    @Json(name = "total_amount") val totalAmount: Double = 0.0,
    @Json(name = "amount_tzs") val amountTzs: Double = 0.0,
    @Json(name = "crypto_address") val cryptoAddress: String = "",
    val status: String = "pending_payment",
    @Json(name = "clickpesa_reference") val clickpesaReference: String? = null,
    @Json(name = "admin_message") val adminMessage: String? = null,
    @Json(name = "tx_hash") val txHash: String? = null,
    @Json(name = "created_at") val createdAt: String = "",
    @Json(name = "updated_at") val updatedAt: String = ""
) {
    val isDeposit: Boolean get() = type.equals("deposit", ignoreCase = true)
    val isWithdrawal: Boolean get() = type.equals("withdrawal", ignoreCase = true)

    val isActionRequired: Boolean
        get() = when (status.lowercase()) {
            "fiat_received", "awaiting_admin_wallet", "crypto_received" -> true
            else -> false
        }
}

@JsonClass(generateAdapter = true)
data class AdminNotification(
    val id: String,
    @Json(name = "transaction_id") val transactionId: String = "",
    val type: String = "", // "deposit_webhook_confirmed" | "withdrawal_requested" | "crypto_deposit_sent"
    val title: String = "",
    val message: String = "",
    @Json(name = "is_read") val isRead: Boolean = false,
    @Json(name = "created_at") val createdAt: String = ""
)

@JsonClass(generateAdapter = true)
data class AppSettings(
    @Json(name = "deposit_fee_percentage") val depositFeePercentage: Double = 2.5,
    @Json(name = "withdraw_fee_percentage") val withdrawFeePercentage: Double = 2.0,
    @Json(name = "usd_tzs_rate") val usdTzsRate: Double = 2580.0
)

@JsonClass(generateAdapter = true)
data class TransactionsResponse(
    val success: Boolean = true,
    val transactions: List<Transaction> = emptyList(),
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class NotificationsResponse(
    val success: Boolean = true,
    val notifications: List<AdminNotification> = emptyList(),
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class SettingsResponse(
    val success: Boolean = true,
    val settings: AppSettings? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class TransactionActionResponse(
    val success: Boolean = true,
    val transaction: Transaction? = null,
    val message: String? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class SendWalletRequest(
    @Json(name = "crypto_address") val cryptoAddress: String,
    @Json(name = "custom_note") val customNote: String? = null
)

@JsonClass(generateAdapter = true)
data class ApproveRequest(
    @Json(name = "tx_hash") val txHash: String? = null,
    @Json(name = "admin_message") val adminMessage: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateSettingsRequest(
    @Json(name = "deposit_fee_percentage") val depositFeePercentage: Double,
    @Json(name = "withdraw_fee_percentage") val withdrawFeePercentage: Double,
    @Json(name = "usd_tzs_rate") val usdTzsRate: Double
)

data class SavedWallet(
    val id: String,
    val label: String,
    val currency: String,
    val network: String,
    val address: String
)
