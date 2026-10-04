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
import com.pemmob.duwitku.R
import com.pemmob.duwitku.domain.repository.TransactionRepository
import com.pemmob.duwitku.ui.feature.form.TransactionFormScreen
import com.pemmob.duwitku.ui.feature.rates.RatesScreen
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
            }
        )
        transactionsScreen(
            onNavigateToForm = {
                navController.navigate(TransactionFormRoute())
            },
            onNavigateToDetail = { transactionId ->
                navController.navigate(TransactionDetailRoute(transactionId))
            },
            onNavigateToEdit = { transactionId ->
                navController.navigate(TransactionFormRoute(transactionId = transactionId))
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

// === Placeholder layar — akan diganti di milestone berikutnya ===

private fun NavGraphBuilder.homeScreen(
    onNavigateToForm: (String) -> Unit
) {
    composable<HomeRoute> {
        // TEMP M3c — ganti di M4/M5
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Text("Home Placeholder")
            Button(onClick = { onNavigateToForm("INCOME") }) { Text("+ Pemasukan") }
            Button(onClick = { onNavigateToForm("EXPENSE") }) { Text("+ Pengeluaran") }
        }
    }
}

@HiltViewModel
class TempTransactionsViewModel @Inject constructor(
    repository: TransactionRepository
) : ViewModel() {
    val transactions = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

private fun NavGraphBuilder.transactionsScreen(
    onNavigateToForm: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    composable<TransactionsRoute> {
        // TEMP M3c — ganti di M4/M5
        val viewModel: TempTransactionsViewModel = hiltViewModel()
        val list by viewModel.transactions.collectAsStateWithLifecycle()
        
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Transactions Placeholder")
            Button(onClick = onNavigateToForm) { Text("Form tanpa arg") }
            LazyColumn {
                items(list, key = { it.id }) { tx ->
                    val amountStr = CurrencyFormatter.formatRp(tx.amount)
                    val dateStr = DateFormatter.formatShort(tx.date)
                    Text(
                        text = "$amountStr - ${tx.category.name} - $dateStr",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToEdit(tx.id) } // Edit langsung utk testing form
                            .padding(8.dp)
                    )
                }
            }
        }
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
