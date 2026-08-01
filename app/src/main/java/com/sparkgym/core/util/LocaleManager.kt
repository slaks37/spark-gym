package com.sparkgym.core.util

import androidx.compose.runtime.compositionLocalOf

/**
 * In-app language selection, stored in DataStore rather than following the
 * device locale. This keeps the toggle instantaneous and predictable.
 */
enum class AppLanguage(val code: String, val flag: String, val label: String) {
    EN("en", "🇬🇧", "English"),
    ID("id", "🇮🇩", "Bahasa Indonesia")
}

/** Provided at the root of the Compose tree via [CompositionLocalProvider]. */
val LocalAppLanguage = compositionLocalOf { AppLanguage.EN }
