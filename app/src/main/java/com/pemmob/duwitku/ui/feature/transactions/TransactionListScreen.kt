package com.pemmob.duwitku.ui.feature.transactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.pemmob.duwitku.domain.model.TransactionType
import com.pemmob.duwitku.ui.components.DuwitkuTopBar
import com.pemmob.duwitku.ui.components.EmptyView
import com.pemmob.duwitku.ui.components.ErrorView
import com.pemmob.duwitku.ui.components.LoadingView
import com.pemmob.duwitku.ui.components.TransactionItem
import com.pemmob.duwitku.ui.theme.DuwitkuThemeHelper
import com.pemmob.duwitku.util.CurrencyFormatter
import com.pemmob.duwitku.util.DateFormatter

@Composable
fun TransactionListScreen(
    onNavigateToForm: (transactionId: Long?) -> Unit,
    onNavigateToDetail: (transactionId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TransactionListContent(
        uiState = uiState,
        onTypeFilterChange = viewModel::onTypeFilterChange,
        onQueryChange = viewModel::onQueryChange,
        onRetry = viewModel::onRetry,
        onTransactionClick = { onNavigateToDetail(it) },
        onAddClick = { onNavigateToForm(null) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionListContent(
    uiState: TransactionListUiState,
    onTypeFilterChange: (TransactionType?) -> Unit,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = DuwitkuThemeHelper.spacing
    val extendedColors = DuwitkuThemeHelper.extendedColors

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            DuwitkuTopBar(
                title = stringResource(R.string.nav_transactions),
                onBackClick = null
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.title_add_transaction)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is TransactionListUiState.Loading -> {
                    LoadingView()
                }
                is TransactionListUiState.Error -> {
                    ErrorView(
                        message = stringResource(R.string.error_generic),
                        onRetry = onRetry
                    )
                }
                is TransactionListUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search
                        OutlinedTextField(
                            value = uiState.query,
                            onValueChange = onQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.md, vertical = spacing.sm),
                            placeholder = { Text(stringResource(R.string.search_hint)) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                if (uiState.query.isNotEmpty()) {
                                    IconButton(onClick = { onQueryChange("") }) {
                                        Icon(Icons.Rounded.Clear, contentDescription = null)
                                    }
                                }
                            },
                            singleLine = true
                        )

                        // Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.md, vertical = spacing.sm),
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                        ) {
                            FilterChip(
                                selected = uiState.selectedType == null,
                                onClick = { onTypeFilterChange(null) },
                                label = { Text(stringResource(R.string.filter_all)) }
                            )
                            FilterChip(
                                selected = uiState.selectedType == TransactionType.INCOME,
                                onClick = { onTypeFilterChange(TransactionType.INCOME) },
                                label = { Text(stringResource(R.string.type_income)) }
                            )
                            FilterChip(
                                selected = uiState.selectedType == TransactionType.EXPENSE,
                                onClick = { onTypeFilterChange(TransactionType.EXPENSE) },
                                label = { Text(stringResource(R.string.type_expense)) }
                            )
                        }

                        // List Content
                        if (uiState.groups.isEmpty()) {
                            if (!uiState.hasAnyTransaction) {
                                EmptyView(
                                    message = stringResource(R.string.empty_no_transactions),
                                    actionLabel = stringResource(R.string.title_add_transaction),
                                    onAction = onAddClick
                                )
                            } else {
                                EmptyView(
                                    message = stringResource(R.string.empty_no_filter_results),
                                    actionLabel = "Reset filter", // Hardcode sesuai arahan atau pakai res. Nggak disuruh buat string baru jadi pakai hardcode saja
                                    onAction = { 
                                        onQueryChange("")
                                        onTypeFilterChange(null) 
                                    }
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 80.dp) // FAB spacing
                            ) {
                                uiState.groups.forEach { group ->
                                    val headerKey = "h_${group.date.toEpochDay()}"
                                    stickyHeader(
                                        key = headerKey,
                                        contentType = "header"
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.background)
                                                .padding(horizontal = spacing.md, vertical = spacing.sm),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = DateFormatter.formatShort(group.date),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            val dayTotalStr = CurrencyFormatter.formatRp(group.dayTotal)
                                            val dayTotalColor = when {
                                                group.dayTotal > 0 -> extendedColors.income
                                                group.dayTotal < 0 -> extendedColors.expense
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                            val sign = if (group.dayTotal > 0) "+" else ""
                                            Text(
                                                text = "$sign$dayTotalStr",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = dayTotalColor
                                            )
                                        }
                                    }

                                    items(
                                        items = group.items,
                                        key = { "t_${it.id}" },
                                        contentType = { "item" }
                                    ) { transaction ->
                                        TransactionItem(
                                            transaction = transaction,
                                            onClick = { onTransactionClick(transaction.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
