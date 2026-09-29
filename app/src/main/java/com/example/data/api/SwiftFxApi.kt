package com.example.data.api

import com.example.data.model.AdminNotification
import com.example.data.model.ApproveRequest
import com.example.data.model.AppSettings
import com.example.data.model.NotificationsResponse
import com.example.data.model.SendWalletRequest
import com.example.data.model.SettingsResponse
import com.example.data.model.Transaction
import com.example.data.model.TransactionActionResponse
import com.example.data.model.TransactionsResponse
import com.example.data.model.UpdateSettingsRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface SwiftFxApi {

    @GET("api/admin/transactions")
    suspend fun getTransactions(
        @Header("x-admin-api-key") apiKey: String
    ): Response<ResponseBody>

    @GET("api/admin/notifications")
    suspend fun getNotifications(
        @Header("x-admin-api-key") apiKey: String
    ): Response<ResponseBody>

    @POST("api/admin/transactions/{id}/send-wallet")
    suspend fun sendWalletAddress(
        @Path("id") transactionId: String,
        @Header("x-admin-api-key") apiKey: String,
        @Body request: SendWalletRequest
    ): Response<ResponseBody>

    @POST("api/admin/transactions/{id}/approve")
    suspend fun approveTransaction(
        @Path("id") transactionId: String,
        @Header("x-admin-api-key") apiKey: String,
        @Body request: ApproveRequest
    ): Response<ResponseBody>

    @GET("api/settings")
    suspend fun getSettings(): Response<ResponseBody>

    @PUT("api/settings")
    suspend fun updateSettings(
        @Header("x-admin-api-key") apiKey: String,
        @Body request: UpdateSettingsRequest
    ): Response<ResponseBody>
}
