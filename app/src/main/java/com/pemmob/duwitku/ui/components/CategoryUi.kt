package com.pemmob.duwitku.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.pemmob.duwitku.R
import com.pemmob.duwitku.domain.model.Category

val Category.labelResId: Int
    @StringRes get() = when (this) {
        Category.FOOD -> R.string.category_food
        Category.TRANSPORT -> R.string.category_transport
        Category.SHOPPING -> R.string.category_shopping
        Category.BILLS -> R.string.category_bills
        Category.ENTERTAINMENT -> R.string.category_entertainment
        Category.HEALTH -> R.string.category_health
        Category.EDUCATION -> R.string.category_education
        Category.OTHER_EXPENSE -> R.string.category_other_expense
        Category.ALLOWANCE -> R.string.category_allowance
        Category.SALARY -> R.string.category_salary
        Category.BONUS -> R.string.category_bonus
        Category.OTHER_INCOME -> R.string.category_other_income
    }

val Category.icon: ImageVector
    get() = when (this) {
        Category.FOOD -> Icons.Rounded.Restaurant
        Category.TRANSPORT -> Icons.Rounded.DirectionsCar
        Category.SHOPPING -> Icons.Rounded.ShoppingCart
        Category.BILLS -> Icons.Rounded.Receipt
        Category.ENTERTAINMENT -> Icons.Rounded.Movie
        Category.HEALTH -> Icons.Rounded.MedicalServices
        Category.EDUCATION -> Icons.Rounded.School
        Category.OTHER_EXPENSE -> Icons.Rounded.MoreHoriz
        Category.ALLOWANCE -> Icons.Rounded.AccountBalanceWallet
        Category.SALARY -> Icons.Rounded.AttachMoney
        Category.BONUS -> Icons.Rounded.CardGiftcard
        Category.OTHER_INCOME -> Icons.Rounded.MoreHoriz
    }
