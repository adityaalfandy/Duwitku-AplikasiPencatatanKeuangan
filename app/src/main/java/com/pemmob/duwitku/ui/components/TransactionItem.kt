package com.pemmob.duwitku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pemmob.duwitku.domain.model.Category
import com.pemmob.duwitku.domain.model.Transaction
import com.pemmob.duwitku.domain.model.TransactionType
import com.pemmob.duwitku.ui.theme.DuwitkuTheme
import com.pemmob.duwitku.ui.theme.DuwitkuThemeHelper
import com.pemmob.duwitku.util.CurrencyFormatter
import com.pemmob.duwitku.util.DateFormatter
import java.time.LocalDate

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = DuwitkuThemeHelper.spacing
    val extendedColors = DuwitkuThemeHelper.extendedColors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.md, vertical = spacing.sm)
            .defaultMinSize(minHeight = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = transaction.category.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.width(spacing.md))

        // Name & Note
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(transaction.category.labelResId),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (transaction.note.isNotBlank()) {
                Text(
                    text = transaction.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(spacing.sm))

        // Amount & Date
        Column(horizontalAlignment = Alignment.End) {
            val amountStr = CurrencyFormatter.formatRp(transaction.amount)
            val isIncome = transaction.type == TransactionType.INCOME
            val amountColor = if (isIncome) extendedColors.income else extendedColors.expense
            val sign = if (isIncome) "+" else "-"

            Text(
                text = "$sign$amountStr",
                style = MaterialTheme.typography.titleMedium,
                color = amountColor
            )
            Text(
                text = DateFormatter.formatShort(transaction.date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionItemIncomePreview() {
    DuwitkuTheme {
        TransactionItem(
            transaction = Transaction(
                id = 1L,
                type = TransactionType.INCOME,
                amount = 5000000L,
                category = Category.SALARY,
                date = LocalDate.now(),
                note = "Gaji bulan ini"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionItemExpensePreview() {
    DuwitkuTheme {
        TransactionItem(
            transaction = Transaction(
                id = 2L,
                type = TransactionType.EXPENSE,
                amount = 250000L,
                category = Category.FOOD,
                date = LocalDate.now(),
                note = "Makan malam bersama teman-teman yang panjang teksnya"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionItemNoNotePreview() {
    DuwitkuTheme {
        TransactionItem(
            transaction = Transaction(
                id = 3L,
                type = TransactionType.EXPENSE,
                amount = 50000L,
                category = Category.TRANSPORT,
                date = LocalDate.now(),
                note = ""
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TransactionItemDarkPreview() {
    DuwitkuTheme {
        TransactionItem(
            transaction = Transaction(
                id = 4L,
                type = TransactionType.INCOME,
                amount = 100000L,
                category = Category.BONUS,
                date = LocalDate.now(),
                note = "Bonus tip"
            ),
            onClick = {}
        )
    }
}
