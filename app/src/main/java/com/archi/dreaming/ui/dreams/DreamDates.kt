package com.archi.dreaming.ui.dreams

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private val utc = TimeZone.getTimeZone("UTC")
private val russian = Locale.forLanguageTag("ru")

fun todayUtcMillis(): Long {
    val local = Calendar.getInstance()
    return Calendar.getInstance(utc).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

fun formatLongDate(utcMillis: Long): String = format(utcMillis, "d MMMM yyyy")

fun formatShortDate(utcMillis: Long): String = format(utcMillis, "d MMM yyyy")

private fun format(utcMillis: Long, pattern: String): String =
    SimpleDateFormat(pattern, russian).apply { timeZone = utc }.format(utcMillis)
