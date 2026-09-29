package com.example.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.SavedWallet
import org.json.JSONArray
import org.json.JSONObject

class AdminPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("swiftfx_admin_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_BASE_URL = "https://swiftfx-backend.onrender.com"
        const val DEFAULT_ADMIN_KEY = "K7vQ9xP2mL8rT4zN6wY3sA5dF1hJ9cB8"
        private const val KEY_BASE_URL = "backend_base_url"
        private const val KEY_ADMIN_API_KEY = "admin_api_key"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_POLL_INTERVAL_SEC = "poll_interval_sec"
        private const val KEY_SAVED_WALLETS = "saved_wallets_json"
    }

    var baseUrl: String
        get() = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) = prefs.edit().putString(KEY_BASE_URL, value.trim().trimEnd('/')).apply()

    var adminApiKey: String
        get() = prefs.getString(KEY_ADMIN_API_KEY, DEFAULT_ADMIN_KEY) ?: DEFAULT_ADMIN_KEY
        set(value) = prefs.edit().putString(KEY_ADMIN_API_KEY, value.trim()).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, value).apply()

    var pollIntervalSeconds: Int
        get() = prefs.getInt(KEY_POLL_INTERVAL_SEC, 4)
        set(value) = prefs.edit().putInt(KEY_POLL_INTERVAL_SEC, value).apply()

    fun getSavedWallets(): List<SavedWallet> {
        val json = prefs.getString(KEY_SAVED_WALLETS, null)
        if (json.isNullOrBlank()) {
            return getDefaultSavedWallets()
        }
        return try {
            val list = mutableListOf<SavedWallet>()
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SavedWallet(
                        id = obj.getString("id"),
                        label = obj.getString("label"),
                        currency = obj.getString("currency"),
                        network = obj.getString("network"),
                        address = obj.getString("address")
                    )
                )
            }
            if (list.isEmpty()) getDefaultSavedWallets() else list
        } catch (e: Exception) {
            getDefaultSavedWallets()
        }
    }

    fun saveWallets(wallets: List<SavedWallet>) {
        val array = JSONArray()
        wallets.forEach { w ->
            val obj = JSONObject().apply {
                put("id", w.id)
                put("label", w.label)
                put("currency", w.currency)
                put("network", w.network)
                put("address", w.address)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_SAVED_WALLETS, array.toString()).apply()
    }

    private fun getDefaultSavedWallets(): List<SavedWallet> {
        return listOf(
            SavedWallet(
                id = "w_usdt_trc20",
                label = "USDT (TRON / TRC20)",
                currency = "USDT",
                network = "TRC20",
                address = "TYDzsYUEWvYpxqFm7K4vT8N5tZ7wY4hJ9x"
            ),
            SavedWallet(
                id = "w_usdt_bep20",
                label = "USDT (BNB Chain / BEP20)",
                currency = "USDT",
                network = "BEP20",
                address = "0x71c8959d24d081f9a2e7c4f69168fbc923a1a36e"
            ),
            SavedWallet(
                id = "w_btc",
                label = "BTC (Bitcoin Native)",
                currency = "BTC",
                network = "BTC",
                address = "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh"
            ),
            SavedWallet(
                id = "w_usdt_erc20",
                label = "USDT (Ethereum / ERC20)",
                currency = "USDT",
                network = "ERC20",
                address = "0x71c8959d24d081f9a2e7c4f69168fbc923a1a36e"
            )
        )
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }
}
