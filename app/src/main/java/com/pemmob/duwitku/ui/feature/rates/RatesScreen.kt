package com.pemmob.duwitku.ui.feature.rates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pemmob.duwitku.R
import com.pemmob.duwitku.ui.components.AmountTextField
import com.pemmob.duwitku.ui.components.ErrorView
import com.pemmob.duwitku.ui.components.LoadingView
import com.pemmob.duwitku.ui.theme.DuwitkuTheme
import com.pemmob.duwitku.ui.theme.DuwitkuThemeHelper

@Composable
fun RatesScreen(
    viewModel: RatesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    RatesContent(
        uiState = uiState,
        onAmountChange = viewModel::onAmountChange,
        onRefresh = viewModel::loadRates
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatesContent(
    uiState: RatesUiState,
    onAmountChange: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.title_rates)) },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.rates_refresh)
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        BoxOrColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (uiState) {
                is RatesUiState.Loading -> {
                    LoadingView()
                }
                is RatesUiState.Error -> {
                    ErrorView(
                        message = stringResource(uiState.messageResId),
                        onRetry = onRefresh
                    )
                }
                is RatesUiState.Success -> {
                    RatesSuccessContent(
                        state = uiState,
                        onAmountChange = onAmountChange
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxOrColumn(modifier: Modifier, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        content()
    }
}

@Composable
private fun RatesSuccessContent(
    state: RatesUiState.Success,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = DuwitkuThemeHelper.spacing
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.md)
    ) {
        AmountTextField(
            value = state.amountInput,
            onValueChange = onAmountChange,
            label = stringResource(R.string.rates_input_label)
        )
        
        Spacer(modifier = Modifier.height(spacing.sm))
        
        Text(
            text = stringResource(R.string.rates_last_updated, state.dateString),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(spacing.md))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
            contentPadding = PaddingValues(bottom = spacing.xl)
        ) {
            items(
                items = state.items,
                key = { it.currencyCode }
            ) { item ->
                RateCard(item = item)
            }
        }
    }
}

@Composable
private fun RateCard(
    item: RateItem,
    modifier: Modifier = Modifier
) {
    val spacing = DuwitkuThemeHelper.spacing
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.currencyCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(item.nameResId),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                Text(
                    text = item.rateText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = item.convertedAmount,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RatesContentLoadingPreview() {
    DuwitkuTheme {
        RatesContent(
            uiState = RatesUiState.Loading,
            onAmountChange = {},
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RatesContentErrorPreview() {
    DuwitkuTheme {
        RatesContent(
            uiState = RatesUiState.Error(R.string.error_no_internet),
            onAmountChange = {},
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RatesContentSuccessPreview() {
    DuwitkuTheme {
        RatesContent(
            uiState = RatesUiState.Success(
                dateString = "Senin, 5 Oktober 2026",
                amountInput = "150000",
                items = listOf(
                    RateItem("USD", R.string.currency_usd, "1 USD = Rp 15.000", "10.00"),
                    RateItem("AUD", R.string.currency_aud, "1 AUD = Rp 10.000", "15.00")
                )
            ),
            onAmountChange = {},
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RatesContentSuccessDarkPreview() {
    DuwitkuTheme(darkTheme = true) {
        RatesContent(
            uiState = RatesUiState.Success(
                dateString = "Senin, 5 Oktober 2026",
                amountInput = "150000",
                items = listOf(
                    RateItem("USD", R.string.currency_usd, "1 USD = Rp 15.000", "10.00")
                )
            ),
            onAmountChange = {},
            onRefresh = {}
        )
    }
}
