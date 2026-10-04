package com.pemmob.duwitku.domain.model

import java.time.LocalDate

data class DateGroup(
    val date: LocalDate,
    val items: List<Transaction>,
    val dayTotal: Long
)
