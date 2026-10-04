package com.pemmob.duwitku.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.ui.theme.DuwitkuThemeHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryPicker(
    categories: List<Category>,
    selected: Category?,
    onSelected: (Category) -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null
) {
    val spacing = DuwitkuThemeHelper.spacing
    
    Column(modifier = modifier.fillMaxWidth()) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = category == selected,
                    onClick = { onSelected(category) },
                    label = { Text(text = stringResource(category.labelResId)) },
                    leadingIcon = {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null
                        )
                    }
                )
            }
        }
        
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = spacing.xs, start = spacing.md)
            )
        }
    }
}
