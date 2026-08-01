package com.sparkgym.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

object Dates {

    fun today(): Long = LocalDate.now().toEpochDay()

    fun epochDay(millis: Long): Long =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()

    fun startOfDayMillis(epochDay: Long): Long =
        LocalDate.ofEpochDay(epochDay).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun localDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)

    private val apiFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val prettyFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())
    private val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    fun apiDate(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(apiFormat)

    fun pretty(epochDay: Long): String = when (epochDay) {
        today() -> "Today"
        today() - 1 -> "Yesterday"
        today() + 1 -> "Tomorrow"
        else -> LocalDate.ofEpochDay(epochDay).format(prettyFormat)
    }

    fun clockTime(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalTime().format(timeFormat)

    /** "1h 24m" style duration for session cards. */
    fun durationLabel(millis: Long): String {
        val totalMinutes = (millis / 60_000).toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    /** "01:24" style, for the rest timer and the live session clock. */
    fun stopwatch(seconds: Int): String {
        val safe = seconds.coerceAtLeast(0)
        val h = safe / 3600
        val m = (safe % 3600) / 60
        val s = safe % 60
        return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s)
        else String.format(Locale.US, "%02d:%02d", m, s)
    }
}

/** Formatting helpers used all over the UI. */
fun Double.kg(decimals: Int = 1): String =
    if (this % 1.0 == 0.0) "${toInt()} kg" else String.format(Locale.US, "%.${decimals}f kg", this)

fun Double.compactVolume(): String = when {
    this >= 1_000_000 -> String.format(Locale.US, "%.1fM", this / 1_000_000)
    this >= 1_000 -> String.format(Locale.US, "%.1fk", this / 1_000)
    else -> roundToInt().toString()
}

fun Double.oneDecimal(): String = String.format(Locale.US, "%.1f", this)
