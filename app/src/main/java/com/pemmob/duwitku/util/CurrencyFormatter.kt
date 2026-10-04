package com.pemmob.duwitku.util

import kotlin.math.abs

object CurrencyFormatter {
    
    /**
     * Memformat angka menjadi format rupiah. Contoh: 1250000 -> "Rp 1.250.000"
     */
    fun formatRp(amount: Long): String {
        val isNegative = amount < 0
        val absAmount = abs(amount)
        
        // Menggunakan String.format dengan Locale.US untuk memastikan ribuan memakai koma, lalu ubah ke titik.
        val formattedNumber = java.lang.String.format(java.util.Locale.US, "%,d", absAmount).replace(',', '.')
        
        val sign = if (isNegative) "-" else ""
        return "${sign}Rp $formattedNumber"
    }

    /**
     * Memformat angka dengan penambahan tanda +/- untuk UI (contoh riwayat transaksi).
     */
    fun formatRpWithSign(amount: Long, isIncome: Boolean): String {
        val absAmount = abs(amount)
        val formattedRp = formatRp(absAmount)
        return if (amount == 0L) {
            formattedRp
        } else if (isIncome) {
            "+ $formattedRp"
        } else {
            "- $formattedRp"
        }
    }
}
