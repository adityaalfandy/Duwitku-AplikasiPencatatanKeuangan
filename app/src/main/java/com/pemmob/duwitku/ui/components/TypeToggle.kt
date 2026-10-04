package com.pemmob.duwitku.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pemmob.duwitku.R
import com.pemmob.duwitku.domain.model.TransactionType
import com.pemmob.duwitku.ui.theme.DuwitkuThemeHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeToggle(
    selected: TransactionType,
    onSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = DuwitkuThemeHelper.extendedColors
    
    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        SegmentedButton(
            selected = selected == TransactionType.INCOME,
            onClick = { onSelected(TransactionType.INCOME) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            colors = SegmentedButtonDefaults.colors(
                activeContainerColor = extendedColors.income
            )
        ) {
            Text(text = stringResource(R.string.type_income))
        }
        
        SegmentedButton(
            selected = selected == TransactionType.EXPENSE,
            onClick = { onSelected(TransactionType.EXPENSE) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            colors = SegmentedButtonDefaults.colors(
                activeContainerColor = extendedColors.expense
            )
        ) {
            Text(text = stringResource(R.string.type_expense))
        }
    }
}
