package com.example.data.repository

import android.util.Log
import com.example.data.api.SwiftFxApi
import com.example.data.model.AdminNotification
import com.example.data.model.AppSettings
import com.example.data.model.ApproveRequest
import com.example.data.model.SendWalletRequest
import com.example.data.model.Transaction
import com.example.data.model.UpdateSettingsRequest
import com.example.data.storage.AdminPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val statusCode: Int? = null) : Resource<Nothing>()
}

class SwiftFxRepository(
    private val preferences: AdminPreferences
) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private var currentBaseUrl: String = ""
    private var api: SwiftFxApi? = null

    private fun getApi(): SwiftFxApi {
        val configuredUrl = preferences.baseUrl.trimEnd('/') + "/"
        if (api == null || currentBaseUrl != configuredUrl) {
            currentBaseUrl = configuredUrl
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .client(okHttpClient)
                .build()

            api = retrofit.create(SwiftFxApi::class.java)
        }
        return api!!
    }

    suspend fun fetchTransactions(): Resource<List<Transaction>> = withContext(Dispatchers.IO) {
        try {
            val response = getApi().getTransactions(preferences.adminApiKey)
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string() ?: ""
                val list = parseTransactions(bodyStr)
                Resource.Success(list)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Resource.Error(errorMsg ?: "Failed to fetch transactions (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            Log.e("SwiftFxRepo", "Fetch transactions exception", e)
            Resource.Error(e.localizedMessage ?: "Network error connecting to backend")
        }
    }

    suspend fun fetchNotifications(): Resource<List<AdminNotification>> = withContext(Dispatchers.IO) {
        try {
            val response = getApi().getNotifications(preferences.adminApiKey)
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string() ?: ""
                val list = parseNotifications(bodyStr)
                Resource.Success(list)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Resource.Error(errorMsg ?: "Failed to fetch notifications (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            Log.e("SwiftFxRepo", "Fetch notifications exception", e)
            Resource.Error(e.localizedMessage ?: "Network error connecting to backend")
        }
    }

    suspend fun sendWalletAddress(
        transactionId: String,
        cryptoAddress: String,
        customNote: String?
    ): Resource<Transaction> = withContext(Dispatchers.IO) {
        try {
            val req = SendWalletRequest(cryptoAddress, customNote)
            val response = getApi().sendWalletAddress(transactionId, preferences.adminApiKey, req)
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string() ?: ""
                val tx = parseSingleTransaction(bodyStr)
                if (tx != null) {
                    Resource.Success(tx)
                } else {
                    Resource.Error("Updated transaction data was empty")
                }
            } else {
                val err = parseErrorMessage(response.errorBody()?.string())
                Resource.Error(err ?: "Failed to send wallet address (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            Log.e("SwiftFxRepo", "Send wallet address exception", e)
            Resource.Error(e.localizedMessage ?: "Failed to send wallet address")
        }
    }

    suspend fun approveTransaction(
        transactionId: String,
        txHash: String?,
        adminMessage: String?
    ): Resource<Transaction> = withContext(Dispatchers.IO) {
        try {
            val req = ApproveRequest(
                txHash = if (txHash.isNullOrBlank()) null else txHash.trim(),
                adminMessage = if (adminMessage.isNullOrBlank()) null else adminMessage.trim()
            )
            val response = getApi().approveTransaction(transactionId, preferences.adminApiKey, req)
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string() ?: ""
                val tx = parseSingleTransaction(bodyStr)
                if (tx != null) {
                    Resource.Success(tx)
                } else {
                    // Fallback create completed transaction placeholder
                    Resource.Success(
                        Transaction(
                            id = transactionId,
                            status = "completed",
                            txHash = txHash,
                            adminMessage = adminMessage
                        )
                    )
                }
            } else {
                val err = parseErrorMessage(response.errorBody()?.string())
                Resource.Error(err ?: "Approval failed (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            Log.e("SwiftFxRepo", "Approve transaction exception", e)
            Resource.Error(e.localizedMessage ?: "Failed to approve transaction")
        }
    }

    suspend fun fetchSettings(): Resource<AppSettings> = withContext(Dispatchers.IO) {
        try {
            val response = getApi().getSettings()
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string() ?: ""
                val settings = parseSettings(bodyStr)
                Resource.Success(settings)
            } else {
                val err = parseErrorMessage(response.errorBody()?.string())
                Resource.Error(err ?: "Failed to fetch settings (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            Log.e("SwiftFxRepo", "Fetch settings exception", e)
            Resource.Error(e.localizedMessage ?: "Failed to fetch settings")
        }
    }

    suspend fun updateSettings(
        depositFee: Double,
        withdrawFee: Double,
        usdTzsRate: Double
    ): Resource<AppSettings> = withContext(Dispatchers.IO) {
        try {
            val req = UpdateSettingsRequest(depositFee, withdrawFee, usdTzsRate)
            val response = getApi().updateSettings(preferences.adminApiKey, req)
            if (response.isSuccessful) {
                val bodyStr = response.body()?.string() ?: ""
                val settings = parseSettings(bodyStr)
                Resource.Success(settings)
            } else {
                val err = parseErrorMessage(response.errorBody()?.string())
                Resource.Error(err ?: "Failed to update settings (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            Log.e("SwiftFxRepo", "Update settings exception", e)
            Resource.Error(e.localizedMessage ?: "Failed to update settings")
        }
    }

    // --- Resilient Parsers ---

    private fun parseTransactions(jsonStr: String): List<Transaction> {
        if (jsonStr.isBlank()) return emptyList()
        return try {
            val txAdapter = moshi.adapter(Transaction::class.java)
            val listAdapter = moshi.adapter<List<Transaction>>(
                Types.newParameterizedType(List::class.java, Transaction::class.java)
            )

            if (jsonStr.trim().startsWith("[")) {
                return listAdapter.fromJson(jsonStr) ?: emptyList()
            }

            val jsonObj = JSONObject(jsonStr)
            val array = when {
                jsonObj.has("transactions") -> jsonObj.getJSONArray("transactions")
                jsonObj.has("data") -> jsonObj.getJSONArray("data")
                else -> null
            }

            if (array != null) {
                val list = mutableListOf<Transaction>()
                for (i in 0 until array.length()) {
                    val itemStr = array.getJSONObject(i).toString()
                    txAdapter.fromJson(itemStr)?.let { list.add(it) }
                }
                list
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("SwiftFxRepo", "Failed to parse transactions JSON", e)
            emptyList()
        }
    }

    private fun parseNotifications(jsonStr: String): List<AdminNotification> {
        if (jsonStr.isBlank()) return emptyList()
        return try {
            val adapter = moshi.adapter(AdminNotification::class.java)
            val listAdapter = moshi.adapter<List<AdminNotification>>(
                Types.newParameterizedType(List::class.java, AdminNotification::class.java)
            )

            if (jsonStr.trim().startsWith("[")) {
                return listAdapter.fromJson(jsonStr) ?: emptyList()
            }

            val jsonObj = JSONObject(jsonStr)
            val array = when {
                jsonObj.has("notifications") -> jsonObj.getJSONArray("notifications")
                jsonObj.has("data") -> jsonObj.getJSONArray("data")
                else -> null
            }

            if (array != null) {
                val list = mutableListOf<AdminNotification>()
                for (i in 0 until array.length()) {
                    val itemStr = array.getJSONObject(i).toString()
                    adapter.fromJson(itemStr)?.let { list.add(it) }
                }
                list
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("SwiftFxRepo", "Failed to parse notifications JSON", e)
            emptyList()
        }
    }

    private fun parseSingleTransaction(jsonStr: String): Transaction? {
        if (jsonStr.isBlank()) return null
        return try {
            val adapter = moshi.adapter(Transaction::class.java)
            val jsonObj = JSONObject(jsonStr)
            val target = when {
                jsonObj.has("transaction") -> jsonObj.getJSONObject("transaction").toString()
                jsonObj.has("data") -> jsonObj.getJSONObject("data").toString()
                else -> jsonStr
            }
            adapter.fromJson(target)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseSettings(jsonStr: String): AppSettings {
        return try {
            val adapter = moshi.adapter(AppSettings::class.java)
            val jsonObj = JSONObject(jsonStr)
            val target = when {
                jsonObj.has("settings") -> jsonObj.getJSONObject("settings").toString()
                jsonObj.has("data") -> jsonObj.getJSONObject("data").toString()
                else -> jsonStr
            }
            adapter.fromJson(target) ?: AppSettings()
        } catch (e: Exception) {
            AppSettings()
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val obj = JSONObject(errorBody)
            when {
                obj.has("message") -> obj.getString("message")
                obj.has("error") -> obj.getString("error")
                else -> errorBody
            }
        } catch (e: Exception) {
            errorBody
        }
    }
}
