package com.pemmob.duwitku.ui.feature.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pemmob.duwitku.R
import com.pemmob.duwitku.ui.components.CategoryTotalItem
import com.pemmob.duwitku.ui.components.DuwitkuTopBar
import com.pemmob.duwitku.ui.components.EmptyView
import com.pemmob.duwitku.ui.components.ErrorView
import com.pemmob.duwitku.ui.components.LoadingView
import com.pemmob.duwitku.ui.components.MonthSelector
import com.pemmob.duwitku.ui.theme.DuwitkuThemeHelper
import com.pemmob.duwitku.util.CurrencyFormatter

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    StatsContent(
        uiState = uiState,
        onPreviousMonth = viewModel::onPreviousMonth,
        onNextMonth = viewModel::onNextMonth,
        onRetry = viewModel::onRetry,
        modifier = modifier
    )
}

@Composable
fun StatsContent(
    uiState: StatsUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = DuwitkuThemeHelper.spacing
    
    Scaffold(
        topBar = {
            DuwitkuTopBar(
                title = stringResource(R.string.title_stats)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = spacing.lg)
        ) {
            when (uiState) {
                is StatsUiState.Loading -> {
                    // Just show a default MonthSelector so user isn't stuck if it loads forever?
                    // But we don't have the month... let's just show LoadingView
                    item(key = "loading", contentType = "loading") {
                        LoadingView(modifier = Modifier.fillParentMaxSize())
                    }
                }
                is StatsUiState.Error -> {
                    item(key = "error", contentType = "error") {
                        ErrorView(
                            message = stringResource(R.string.error_generic),
                            onRetry = onRetry,
                            modifier = Modifier.fillParentMaxSize()
                        )
                    }
                }
                is StatsUiState.Success -> {
                    item(key = "month_selector", contentType = "selector") {
                        MonthSelector(
                            month = uiState.month,
                            canGoNext = uiState.canGoNext,
                            onPrevious = onPreviousMonth,
                            onNext = onNextMonth,
                            modifier = Modifier.padding(
                                horizontal = spacing.md,
                                vertical = spacing.sm
                            )
                        )
                    }
                    
                    item(key = "summary", contentType = "summary") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.md, vertical = spacing.sm),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(spacing.lg),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.stats_total_expense),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = CurrencyFormatter.formatRp(uiState.totalExpense),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = DuwitkuThemeHelper.extendedColors.expense
                                )
                            }
                        }
                    }
                    
                    if (uiState.items.isEmpty()) {
                        item(key = "empty", contentType = "empty") {
                            EmptyView(
                                message = stringResource(R.string.empty_no_expenses_this_month),
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .height(300.dp) // Approximate height for empty state
                            )
                        }
                    } else {
                        item(key = "title", contentType = "title") {
                            Text(
                                text = stringResource(R.string.expense_by_category),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(
                                    start = spacing.md,
                                    end = spacing.md,
                                    top = spacing.lg,
                                    bottom = spacing.sm
                                )
                            )
                        }
                        
                        items(
                            items = uiState.items,
                            key = { "c_${it.category.name}" },
                            contentType = { "category_item" }
                        ) { item ->
                            CategoryTotalItem(
                                category = item.category,
                                total = item.total,
                                percent = item.percent,
                                modifier = Modifier.padding(horizontal = spacing.md)
                            )
                        }
                    }
                }
            }
        }
    }
}
