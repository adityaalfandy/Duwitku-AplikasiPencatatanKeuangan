package com.pemmob.duwitku.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.ui.graphics.vector.ImageVector
import com.pemmob.duwitku.R

enum class BottomNavItem(
    val route: Any,
    val icon: ImageVector,
    @get:StringRes val labelResId: Int,
) {
    HOME(
        route = HomeRoute,
        icon = Icons.Rounded.Home,
        labelResId = R.string.nav_home,
    ),
    TRANSACTIONS(
        route = TransactionsRoute,
        icon = Icons.AutoMirrored.Rounded.ReceiptLong,
        labelResId = R.string.nav_transactions,
    ),
    STATS(
        route = StatsRoute,
        icon = Icons.Rounded.PieChart,
        labelResId = R.string.nav_stats,
    ),
    RATES(
        route = RatesRoute,
        icon = Icons.Rounded.CurrencyExchange,
        labelResId = R.string.nav_rates,
    ),
}
