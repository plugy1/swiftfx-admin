package com.example

import com.example.data.model.Transaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SwiftFxModelTest {

    @Test
    fun testTransactionDepositFlagsAndActionRequired() {
        val depositTx = Transaction(
            id = "tx_123",
            userId = "usr_99",
            type = "deposit",
            cryptoCurrency = "USDT",
            cryptoNetwork = "TRC20",
            amount = 100.0,
            feePercentage = 2.5,
            feeAmount = 2.5,
            totalAmount = 102.5,
            amountTzs = 264450.0,
            status = "fiat_received"
        )

        assertTrue(depositTx.isDeposit)
        assertTrue(depositTx.isActionRequired)
        assertEquals(100.0, depositTx.amount, 0.001)
        assertEquals(102.5, depositTx.totalAmount, 0.001)
    }

    @Test
    fun testTransactionWithdrawalActionRequired() {
        val withdrawalTx = Transaction(
            id = "tx_456",
            userId = "usr_88",
            type = "withdrawal",
            cryptoCurrency = "USDT",
            cryptoNetwork = "BEP20",
            amount = 200.0,
            feePercentage = 2.0,
            feeAmount = 4.0,
            totalAmount = 196.0,
            amountTzs = 505680.0,
            status = "awaiting_admin_wallet"
        )

        assertTrue(withdrawalTx.isWithdrawal)
        assertTrue(withdrawalTx.isActionRequired)
    }
}
