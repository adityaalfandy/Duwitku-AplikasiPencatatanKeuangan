package com.pemmob.duwitku.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pemmob.duwitku.ui.feature.rates.RatesScreen
import com.pemmob.duwitku.R
import com.pemmob.duwitku.ui.theme.LocalSpacing

@Composable
fun DuwitkuNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        homeScreen()
        transactionsScreen(
            onNavigateToForm = {
                navController.navigate(TransactionFormRoute())
            },
            onNavigateToDetail = { transactionId ->
                navController.navigate(TransactionDetailRoute(transactionId))
            },
        )
        statsScreen()
        ratesScreen()
        transactionFormScreen(
            onNavigateBack = { navController.popBackStack() },
        )
        transactionDetailScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToEdit = { transactionId ->
                navController.navigate(TransactionFormRoute(transactionId = transactionId))
            },
        )
    }
}

// === Placeholder layar — akan diganti di milestone berikutnya ===

private fun NavGraphBuilder.homeScreen() {
    composable<HomeRoute> {
        PlaceholderScreen(title = stringResource(R.string.nav_home))
    }
}

private fun NavGraphBuilder.transactionsScreen(
    onNavigateToForm: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
) {
    composable<TransactionsRoute> {
        PlaceholderScreen(title = stringResource(R.string.nav_transactions))
    }
}

private fun NavGraphBuilder.statsScreen() {
    composable<StatsRoute> {
        PlaceholderScreen(title = stringResource(R.string.nav_stats))
    }
}

private fun NavGraphBuilder.ratesScreen() {
    composable<RatesRoute> {
        RatesScreen()
    }
}

private fun NavGraphBuilder.transactionFormScreen(
    onNavigateBack: () -> Unit,
) {
    composable<TransactionFormRoute> {
        PlaceholderScreen(title = stringResource(R.string.title_add_transaction))
    }
}

private fun NavGraphBuilder.transactionDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
) {
    composable<TransactionDetailRoute> {
        PlaceholderScreen(title = stringResource(R.string.title_transaction_detail))
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
