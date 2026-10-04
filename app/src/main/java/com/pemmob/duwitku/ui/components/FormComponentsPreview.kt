package com.pemmob.duwitku.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.TransactionType
import com.pemmob.duwitku.ui.theme.DuwitkuTheme
import com.pemmob.duwitku.ui.theme.DuwitkuThemeHelper
import java.time.LocalDate

@Composable
private fun FormComponentsGallery() {
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amount by remember { mutableStateOf("150000") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Scaffold(
        topBar = {
            DuwitkuTopBar(title = "Preview Form", onBackClick = {})
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(DuwitkuThemeHelper.spacing.md)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(DuwitkuThemeHelper.spacing.md)
        ) {
            TypeToggle(
                selected = selectedType,
                onSelected = { selectedType = it }
            )
            
            AmountTextField(
                value = amount,
                onValueChange = { amount = it },
                label = "Nominal",
                errorMessage = if (amount.isEmpty()) "Nominal harus lebih dari 0" else null
            )
            
            CategoryPicker(
                categories = Category.entries.filter { it.type == selectedType },
                selected = selectedCategory,
                onSelected = { selectedCategory = it },
                errorMessage = if (selectedCategory == null) "Pilih kategori dulu ya" else null
            )
            
            DatePickerField(
                date = selectedDate,
                onDateSelected = { selectedDate = it }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FormComponentsPreviewLight() {
    DuwitkuTheme(darkTheme = false) {
        FormComponentsGallery()
    }
}

@Preview(showBackground = true)
@Composable
fun FormComponentsPreviewDark() {
    DuwitkuTheme(darkTheme = true) {
        FormComponentsGallery()
    }
}
