package com.pemmob.duwitku.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pemmob.duwitku.R
import com.pemmob.duwitku.ui.theme.DuwitkuTheme
import com.pemmob.duwitku.ui.theme.DuwitkuThemeHelper
import com.pemmob.duwitku.util.CurrencyFormatter

@Composable
fun BalanceCard(
    balance: Long,
    monthIncome: Long,
    monthExpense: Long,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.home_balance_label),
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = CurrencyFormatter.formatRp(balance),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryChip(
                    label = stringResource(R.string.home_income_this_month),
                    amountText = CurrencyFormatter.formatRpWithSign(monthIncome, isIncome = true),
                    amountColor = DuwitkuThemeHelper.extendedColors.income,
                    modifier = Modifier.weight(1f)
                )
                
                SummaryChip(
                    label = stringResource(R.string.home_expense_this_month),
                    amountText = CurrencyFormatter.formatRpWithSign(monthExpense, isIncome = false),
                    amountColor = DuwitkuThemeHelper.extendedColors.expense,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Positive Balance")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun BalanceCardPositivePreview() {
    DuwitkuTheme {
        BalanceCard(
            balance = 12500000L,
            monthIncome = 15000000L,
            monthExpense = 2500000L
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Zero Balance")
@Composable
private fun BalanceCardZeroPreview() {
    DuwitkuTheme {
        BalanceCard(
            balance = 0L,
            monthIncome = 0L,
            monthExpense = 0L
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Negative Balance")
@Composable
private fun BalanceCardNegativePreview() {
    DuwitkuTheme {
        BalanceCard(
            balance = -500000L,
            monthIncome = 1000000L,
            monthExpense = 1500000L
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Huge Amount")
@Composable
private fun BalanceCardHugePreview() {
    DuwitkuTheme {
        BalanceCard(
            balance = 999999999999L,
            monthIncome = 500000000000L,
            monthExpense = 499999999999L
        )
    }
}
