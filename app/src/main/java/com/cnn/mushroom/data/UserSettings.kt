package com.cnn.mushroom.data

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserSettings(
    val displayTimestamp: Boolean,
    val nameDisplayFormat: NameDisplayFormat,
    val timeDisplayFormat: TimeDisplayFormat
) {
    companion object {
        val DEFAULT = UserSettings(
            displayTimestamp = true,
            nameDisplayFormat = NameDisplayFormat.BOTH,
            timeDisplayFormat = TimeDisplayFormat.MONTH_YEAR
        )
    }
}

enum class NameDisplayFormat {
    SCIENTIFIC,
    NON_SCIENTIFIC,
    BOTH
}

enum class TimeDisplayFormat {
    MONTH_YEAR,
    DAY_MONTH_YEAR,
    TIME_DAY_MONTH_YEAR
}

fun formatTimestamp(
    timestamp: Long,
    format: TimeDisplayFormat,
    context: Context // Przekazujemy kontekst dla Locale
): String {
    val pattern = when (format) {
        TimeDisplayFormat.MONTH_YEAR -> "MMMM, yyyy"
        TimeDisplayFormat.DAY_MONTH_YEAR -> "dd/MM/yyyy"
        TimeDisplayFormat.TIME_DAY_MONTH_YEAR -> "HH:mm:ss dd/MM/yyyy"
    }

    val locale = context.resources.configuration.locales[0] ?: Locale.getDefault()
    val sdf = SimpleDateFormat(pattern, locale)
    return sdf.format(Date(timestamp))
}

fun formatMushroomName(
    scientificName: String,
    commonName: String,
    format: NameDisplayFormat
): String {
    return when (format) {
        NameDisplayFormat.SCIENTIFIC -> scientificName
        NameDisplayFormat.NON_SCIENTIFIC -> commonName
        NameDisplayFormat.BOTH -> "$commonName ($scientificName)"
    }
}