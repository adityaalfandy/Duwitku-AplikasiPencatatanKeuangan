package com.pemmob.duwitku.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.pemmob.duwitku.R
import com.pemmob.duwitku.domain.repository.TransactionRepository
import com.pemmob.duwitku.ui.feature.detail.TransactionDetailScreen
import com.pemmob.duwitku.ui.feature.form.TransactionFormScreen
import com.pemmob.duwitku.ui.feature.home.HomeScreen
import com.pemmob.duwitku.ui.feature.rates.RatesScreen
import com.pemmob.duwitku.ui.feature.stats.StatsScreen
import com.pemmob.duwitku.ui.feature.transactions.TransactionListScreen
import com.pemmob.duwitku.ui.theme.LocalSpacing
import com.pemmob.duwitku.util.CurrencyFormatter
import com.pemmob.duwitku.util.DateFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

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
        homeScreen(
            onNavigateToForm = { type ->
                navController.navigate(TransactionFormRoute(type = type))
            },
            onNavigateToTransactions = {
                navController.navigate(TransactionsRoute) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onNavigateToDetail = { transactionId ->
                navController.navigate(TransactionDetailRoute(transactionId))
            }
        )
        transactionsScreen(
            onNavigateToForm = { transactionId ->
                navController.navigate(TransactionFormRoute(transactionId))
            },
            onNavigateToDetail = { transactionId ->
                navController.navigate(TransactionDetailRoute(transactionId))
            }
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

private fun NavGraphBuilder.homeScreen(
    onNavigateToForm: (String) -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    composable<HomeRoute> {
        HomeScreen(
            onAddIncome = { onNavigateToForm("INCOME") },
            onAddExpense = { onNavigateToForm("EXPENSE") },
            onSeeAll = onNavigateToTransactions,
            onTransactionClick = onNavigateToDetail
        )
    }
}

private fun NavGraphBuilder.transactionsScreen(
            onNavigateToForm: (Long?) -> Unit,
            onNavigateToDetail: (Long) -> Unit
        ) {
            composable<TransactionsRoute> {
                TransactionListScreen(
                    onNavigateToForm = onNavigateToForm,
                    onNavigateToDetail = onNavigateToDetail
                )
            }
        }

private fun NavGraphBuilder.statsScreen() {
    composable<StatsRoute> {
        StatsScreen()
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
        TransactionFormScreen(
            onNavigateBack = onNavigateBack
        )
    }
}

private fun NavGraphBuilder.transactionDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
) {
    composable<TransactionDetailRoute> {
        TransactionDetailScreen(
            onNavigateBack = onNavigateBack,
            onEditClick = onNavigateToEdit
        )
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
