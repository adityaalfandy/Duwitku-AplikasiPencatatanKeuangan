package com.pemmob.duwitku.ui.feature.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pemmob.duwitku.R
import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import com.pemmob.duwitku.ui.components.BalanceCard
import com.pemmob.duwitku.ui.components.EmptyView
import com.pemmob.duwitku.ui.components.ErrorView
import com.pemmob.duwitku.ui.components.LoadingView
import com.pemmob.duwitku.ui.components.TransactionItem
import com.pemmob.duwitku.ui.theme.DuwitkuTheme
import com.pemmob.duwitku.ui.theme.DuwitkuThemeHelper
import com.pemmob.duwitku.util.DateFormatter
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun HomeScreen(
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onSeeAll: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onAddIncome = onAddIncome,
        onAddExpense = onAddExpense,
        onSeeAll = onSeeAll,
        onTransactionClick = onTransactionClick,
        onRetry = viewModel::onRetry,
        modifier = modifier
    )
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onSeeAll: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is HomeUiState.Loading -> {
            LoadingView(modifier = modifier.fillMaxSize())
        }
        is HomeUiState.Error -> {
            ErrorView(
                message = stringResource(R.string.error_generic),
                onRetry = onRetry,
                modifier = modifier.fillMaxSize()
            )
        }
        is HomeUiState.Success -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    all = DuwitkuThemeHelper.spacing.md
                ),
                verticalArrangement = Arrangement.spacedBy(DuwitkuThemeHelper.spacing.md)
            ) {
                // Header
                item(key = "greeting", contentType = "header") {
                    Column {
                        Text(
                            text = stringResource(R.string.title_home_greeting),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = DateFormatter.formatMonthYear(uiState.month),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // BalanceCard
                item(key = "balance", contentType = "balance") {
                    BalanceCard(
                        balance = uiState.balance,
                        monthIncome = uiState.monthIncome,
                        monthExpense = uiState.monthExpense,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Actions (+ Income, + Expense)
                item(key = "actions", contentType = "actions") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DuwitkuThemeHelper.spacing.md)
                    ) {
                        Button(
                            onClick = onAddIncome,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.home_add_income),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        
                        Button(
                            onClick = onAddExpense,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.home_add_expense),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                // Recent Transactions Header
                item(key = "recent_header", contentType = "recent_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.home_recent_transactions),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        TextButton(onClick = onSeeAll) {
                            Text(
                                text = stringResource(R.string.home_see_all),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                // Recent Transactions List or EmptyView
                if (uiState.recent.isEmpty()) {
                    item(key = "empty_recent", contentType = "empty") {
                        EmptyView(
                            message = stringResource(R.string.empty_no_transactions),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = DuwitkuThemeHelper.spacing.lg)
                        )
                    }
                } else {
                    items(
                        items = uiState.recent,
                        key = { "t_${it.id}" },
                        contentType = { "transaction_item" }
                    ) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onClick = { onTransactionClick(transaction.id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Success - Light")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Success - Dark")
@Composable
private fun HomeContentSuccessPreview() {
    DuwitkuTheme {
        HomeContent(
            uiState = HomeUiState.Success(
                month = YearMonth.of(2026, 10),
                balance = 2450000L,
                monthIncome = 3500000L,
                monthExpense = 1050000L,
                recent = listOf(
                    Transaction(1L, TransactionType.EXPENSE, 25000L, Category.FOOD, LocalDate.now(), "Nasi goreng + es teh"),
                    Transaction(2L, TransactionType.EXPENSE, 18000L, Category.TRANSPORT, LocalDate.now(), "Ojek ke kampus"),
                    Transaction(3L, TransactionType.INCOME, 3000000L, Category.ALLOWANCE, LocalDate.now().minusDays(1), "Kiriman bulanan")
                )
            ),
            onAddIncome = {},
            onAddExpense = {},
            onSeeAll = {},
            onTransactionClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentEmptyPreview() {
    DuwitkuTheme {
        HomeContent(
            uiState = HomeUiState.Success(
                month = YearMonth.of(2026, 10),
                balance = 0L,
                monthIncome = 0L,
                monthExpense = 0L,
                recent = emptyList()
            ),
            onAddIncome = {},
            onAddExpense = {},
            onSeeAll = {},
            onTransactionClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentNegativeBalancePreview() {
    DuwitkuTheme {
        HomeContent(
            uiState = HomeUiState.Success(
                month = YearMonth.of(2026, 10),
                balance = -150000L,
                monthIncome = 100000L,
                monthExpense = 250000L,
                recent = emptyList()
            ),
            onAddIncome = {},
            onAddExpense = {},
            onSeeAll = {},
            onTransactionClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentLoadingPreview() {
    DuwitkuTheme {
        HomeContent(
            uiState = HomeUiState.Loading,
            onAddIncome = {},
            onAddExpense = {},
            onSeeAll = {},
            onTransactionClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentErrorPreview() {
    DuwitkuTheme {
        HomeContent(
            uiState = HomeUiState.Error,
            onAddIncome = {},
            onAddExpense = {},
            onSeeAll = {},
            onTransactionClick = {},
            onRetry = {}
        )
    }
}
