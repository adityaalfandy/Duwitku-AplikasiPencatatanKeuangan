package com.pemmob.duwitku.ui.feature.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pemmob.duwitku.R
import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.TransactionType
import com.pemmob.duwitku.domain.usecase.AmountError
import com.pemmob.duwitku.domain.usecase.CategoryError
import com.pemmob.duwitku.domain.usecase.DateError
import com.pemmob.duwitku.domain.usecase.NoteError
import com.pemmob.duwitku.ui.components.AmountTextField
import com.pemmob.duwitku.ui.components.CategoryPicker
import com.pemmob.duwitku.ui.components.DatePickerField
import com.pemmob.duwitku.ui.components.DuwitkuTopBar
import com.pemmob.duwitku.ui.components.ErrorView
import com.pemmob.duwitku.ui.components.LoadingView
import com.pemmob.duwitku.ui.components.TypeToggle
import com.pemmob.duwitku.ui.theme.DuwitkuTheme
import com.pemmob.duwitku.ui.theme.DuwitkuThemeHelper
import java.time.LocalDate

@Composable
fun TransactionFormScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    val saveErrorMessage = stringResource(R.string.error_generic)
    LaunchedEffect(uiState.saveError) {
        if (uiState.saveError) {
            snackbarHostState.showSnackbar(saveErrorMessage)
            viewModel.onSaveErrorShown()
        }
    }

    TransactionFormContent(
        uiState = uiState,
        onTypeChange = viewModel::onTypeChange,
        onAmountChange = viewModel::onAmountChange,
        onCategorySelect = viewModel::onCategorySelect,
        onDateSelect = viewModel::onDateSelect,
        onNoteChange = viewModel::onNoteChange,
        onSaveClick = viewModel::onSaveClick,
        onBackClick = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@Composable
fun TransactionFormContent(
    uiState: TransactionFormUiState,
    onTypeChange: (TransactionType) -> Unit,
    onAmountChange: (String) -> Unit,
    onCategorySelect: (Category) -> Unit,
    onDateSelect: (LocalDate) -> Unit,
    onNoteChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val titleRes = if (uiState.isEditMode) R.string.title_edit_transaction else R.string.title_add_transaction

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            DuwitkuTopBar(
                title = stringResource(titleRes),
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingView(modifier = Modifier.padding(paddingValues))
            return@Scaffold
        }

        if (uiState.loadError) {
            ErrorView(
                message = stringResource(R.string.error_generic),
                onRetry = onBackClick,
                modifier = Modifier.padding(paddingValues)
            )
            return@Scaffold
        }

        val spacing = DuwitkuThemeHelper.spacing
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .padding(spacing.md)
        ) {
            TypeToggle(
                selected = uiState.type,
                onSelected = onTypeChange
            )

            Spacer(modifier = Modifier.height(spacing.md))

            AmountTextField(
                value = uiState.amountInput,
                onValueChange = onAmountChange,
                label = stringResource(R.string.form_amount_label),
                errorMessage = uiState.amountError?.let { amountErrorToString(it) }
            )

            Spacer(modifier = Modifier.height(spacing.md))

            val availableCategories = Category.entries.filter { it.type == uiState.type }
            CategoryPicker(
                categories = availableCategories,
                selected = uiState.category,
                onSelected = onCategorySelect,
                errorMessage = uiState.categoryError?.let { categoryErrorToString(it) }
            )

            Spacer(modifier = Modifier.height(spacing.md))

            DatePickerField(
                date = uiState.date,
                onDateSelected = onDateSelect,
                errorMessage = uiState.dateError?.let { dateErrorToString(it) }
            )

            Spacer(modifier = Modifier.height(spacing.md))

            OutlinedTextField(
                value = uiState.note,
                onValueChange = onNoteChange,
                label = { Text(stringResource(R.string.form_note_label)) },
                supportingText = {
                    val errorText = uiState.noteError?.let { noteErrorToString(it) }
                    if (errorText != null) {
                        Text(errorText)
                    } else {
                        Text("${uiState.note.length}/100")
                    }
                },
                isError = uiState.noteError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(spacing.lg))

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.form_save))
                }
            }
        }
    }
}

@Composable
private fun amountErrorToString(error: AmountError): String {
    return when (error) {
        AmountError.Empty, AmountError.NotPositive -> stringResource(R.string.validation_amount_required)
        AmountError.TooLarge -> stringResource(R.string.validation_amount_max)
    }
}

@Composable
private fun categoryErrorToString(error: CategoryError): String {
    return when (error) {
        CategoryError.Missing, CategoryError.WrongType -> stringResource(R.string.validation_category_required)
    }
}

@Composable
private fun dateErrorToString(error: DateError): String {
    return when (error) {
        DateError.InFuture -> stringResource(R.string.validation_date_future)
    }
}

@Composable
private fun noteErrorToString(error: NoteError): String {
    return when (error) {
        NoteError.TooLong -> stringResource(R.string.validation_note_max)
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionFormContentPreview_Empty() {
    DuwitkuTheme {
        TransactionFormContent(
            uiState = TransactionFormUiState(date = LocalDate.now()),
            onTypeChange = {},
            onAmountChange = {},
            onCategorySelect = {},
            onDateSelect = {},
            onNoteChange = {},
            onSaveClick = {},
            onBackClick = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionFormContentPreview_Filled() {
    DuwitkuTheme {
        TransactionFormContent(
            uiState = TransactionFormUiState(
                type = TransactionType.INCOME,
                amountInput = "1500000",
                category = Category.SALARY,
                date = LocalDate.now(),
                note = "Gaji bulan ini"
            ),
            onTypeChange = {},
            onAmountChange = {},
            onCategorySelect = {},
            onDateSelect = {},
            onNoteChange = {},
            onSaveClick = {},
            onBackClick = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionFormContentPreview_Error() {
    DuwitkuTheme {
        TransactionFormContent(
            uiState = TransactionFormUiState(
                amountError = AmountError.Empty,
                categoryError = CategoryError.Missing,
                dateError = DateError.InFuture,
                noteError = NoteError.TooLong,
                date = LocalDate.now().plusDays(1),
                note = "a".repeat(101)
            ),
            onTypeChange = {},
            onAmountChange = {},
            onCategorySelect = {},
            onDateSelect = {},
            onNoteChange = {},
            onSaveClick = {},
            onBackClick = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}
