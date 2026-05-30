package com.luisfagundes.impl.presentation.tools

import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

fun YearMonth.getFormattedMonthName(): String = this.month.getDisplayName(
    TextStyle.FULL,
    Locale.getDefault()
)