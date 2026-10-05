package com.pemmob.duwitku.ui.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pemmob.duwitku.R
import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import com.pemmob.duwitku.ui.components.*
import com.pemmob.duwitku.ui.theme.DuwitkuTheme
import com.pemmob.duwitku.ui.theme.DuwitkuThemeHelper
import com.pemmob.duwitku.util.CurrencyFormatter
import com.pemmob.duwitku.util.DateFormatter
import java.time.LocalDate

@Composable
fun TransactionDetailScreen(
    onNavigateBack: () -> Unit,
    onEditClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }

    val deleteErrorMsg = stringResource(R.string.msg_delete_failed)
    LaunchedEffect(uiState.deleteError) {
        if (uiState.deleteError) {
            snackbarHostState.showSnackbar(deleteErrorMsg)
            viewModel.onDeleteErrorShown()
        }
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = stringResource(R.string.dialog_delete_title),
            message = stringResource(R.string.dialog_delete_message),
            confirmText = stringResource(R.string.dialog_delete_confirm),
            cancelText = stringResource(R.string.dialog_cancel),
            onConfirm = {
                showDeleteDialog = false
                viewModel.onDeleteConfirmed()
            },
            onDismissRequest = {
                showDeleteDialog = false
            }
        )
    }

    TransactionDetailContent(
        uiState = uiState,
        onBackClick = onNavigateBack,
        onEditClick = {
            val state = uiState.detailState
            if (state is TransactionDetailState.Success) {
                onEditClick(state.transaction.id)
            }
        },
        onDeleteClick = { showDeleteDialog = true },
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@Composable
fun TransactionDetailContent(
    uiState: TransactionDetailUiState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val spacing = DuwitkuThemeHelper.spacing

    Scaffold(
        modifier = modifier,
        topBar = {
            DuwitkuTopBar(
                title = stringResource(R.string.title_transaction_detail),
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val detailState = uiState.detailState) {
                is TransactionDetailState.Loading -> {
                    LoadingView(modifier = Modifier.fillMaxSize())
                }
                is TransactionDetailState.Error -> {
                    ErrorView(
                        message = stringResource(R.string.error_generic),
                        onRetry = onBackClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is TransactionDetailState.NotFound -> {
                    EmptyView(
                        message = stringResource(R.string.msg_transaction_not_found),
                        title = null,
                        actionLabel = stringResource(R.string.navigate_back),
                        onAction = onBackClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is TransactionDetailState.Success -> {
                    val tx = detailState.transaction
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(spacing.md)
                    ) {
                        TransactionCard(transaction = tx)
                        
                        Spacer(modifier = Modifier.height(spacing.xl))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = onEditClick,
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isDeleting
                            ) {
                                Text(stringResource(R.string.detail_edit))
                            }
                            
                            Spacer(modifier = Modifier.width(spacing.md))
                            
                            Button(
                                onClick = onDeleteClick,
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isDeleting,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ) {
                                Text(stringResource(R.string.detail_delete))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    val spacing = DuwitkuThemeHelper.spacing
    val extendedColors = DuwitkuThemeHelper.extendedColors
    
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) extendedColors.income else extendedColors.expense
    val sign = if (isIncome) "+" else "-"
    val amountStr = "$sign${CurrencyFormatter.formatRp(transaction.amount)}"
    val typeStrRes = if (isIncome) R.string.type_income else R.string.type_expense
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = transaction.category.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(spacing.md))
            
            Text(
                text = amountStr,
                style = MaterialTheme.typography.headlineLarge,
                color = amountColor
            )
            
            Spacer(modifier = Modifier.height(spacing.sm))
            
            Text(
                text = stringResource(transaction.category.labelResId),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = stringResource(typeStrRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(spacing.lg))
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            
            Spacer(modifier = Modifier.height(spacing.md))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.form_date_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = DateFormatter.formatFull(transaction.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(spacing.sm))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.form_note_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = if (transaction.note.isNotBlank()) transaction.note else stringResource(R.string.detail_no_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f, fill = false),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

// Previews
@Preview(showBackground = true)
@Composable
private fun TransactionDetailContentIncomePreview() {
    DuwitkuTheme {
        TransactionDetailContent(
            uiState = TransactionDetailUiState(
                detailState = TransactionDetailState.Success(
                    Transaction(
                        id = 1L,
                        type = TransactionType.INCOME,
                        amount = 5000000L,
                        category = Category.SALARY,
                        date = LocalDate.now(),
                        note = "Gaji bulan ini"
                    )
                )
            ),
            onBackClick = {},
            onEditClick = {},
            onDeleteClick = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionDetailContentExpensePreview() {
    DuwitkuTheme {
        TransactionDetailContent(
            uiState = TransactionDetailUiState(
                detailState = TransactionDetailState.Success(
                    Transaction(
                        id = 2L,
                        type = TransactionType.EXPENSE,
                        amount = 150000L,
                        category = Category.FOOD,
                        date = LocalDate.now(),
                        note = "Makan malam panjang banget teksnya biar kelihatan wrap atau ngga"
                    )
                )
            ),
            onBackClick = {},
            onEditClick = {},
            onDeleteClick = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionDetailContentNoNotePreview() {
    DuwitkuTheme {
        TransactionDetailContent(
            uiState = TransactionDetailUiState(
                detailState = TransactionDetailState.Success(
                    Transaction(
                        id = 3L,
                        type = TransactionType.EXPENSE,
                        amount = 50000L,
                        category = Category.TRANSPORT,
                        date = LocalDate.now(),
                        note = ""
                    )
                )
            ),
            onBackClick = {},
            onEditClick = {},
            onDeleteClick = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}

@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TransactionDetailContentDarkPreview() {
    DuwitkuTheme {
        TransactionDetailContent(
            uiState = TransactionDetailUiState(
                detailState = TransactionDetailState.Success(
                    Transaction(
                        id = 4L,
                        type = TransactionType.INCOME,
                        amount = 200000L,
                        category = Category.BONUS,
                        date = LocalDate.now(),
                        note = "Tip"
                    )
                )
            ),
            onBackClick = {},
            onEditClick = {},
            onDeleteClick = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}
