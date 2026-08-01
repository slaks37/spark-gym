package com.sparkgym.core.design

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val SparkScheme = lightColorScheme(
    primary = SparkColors.Cyan,
    onPrimary = Color.White,
    primaryContainer = SparkColors.CyanDim,
    onPrimaryContainer = SparkColors.TextPrimary,
    secondary = SparkColors.Violet,
    onSecondary = Color.White,
    tertiary = SparkColors.Amber,
    onTertiary = Color.White,
    background = SparkColors.Void,
    onBackground = SparkColors.TextPrimary,
    surface = SparkColors.Panel,
    onSurface = SparkColors.TextPrimary,
    surfaceVariant = SparkColors.PanelHigh,
    onSurfaceVariant = SparkColors.TextSecondary,
    outline = SparkColors.Divider,
    error = SparkColors.Danger,
    onError = Color.White
)

/**
 * Everything monospaced. The System does not use a humanist typeface.
 */
private val SparkTypography = Typography().let { base ->
    val mono = FontFamily.Monospace
    Typography(
        displayLarge = base.displayLarge.copy(fontFamily = mono, fontWeight = FontWeight.Bold),
        displayMedium = base.displayMedium.copy(fontFamily = mono, fontWeight = FontWeight.Bold),
        displaySmall = base.displaySmall.copy(fontFamily = mono, fontWeight = FontWeight.Bold),
        headlineLarge = base.headlineLarge.copy(fontFamily = mono, fontWeight = FontWeight.Bold),
        headlineMedium = base.headlineMedium.copy(fontFamily = mono, fontWeight = FontWeight.Bold),
        headlineSmall = base.headlineSmall.copy(fontFamily = mono, fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontFamily = mono, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
        titleMedium = base.titleMedium.copy(fontFamily = mono, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp),
        titleSmall = base.titleSmall.copy(fontFamily = mono, fontWeight = FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.copy(fontFamily = mono),
        bodyMedium = base.bodyMedium.copy(fontFamily = mono),
        bodySmall = base.bodySmall.copy(fontFamily = mono),
        labelLarge = base.labelLarge.copy(fontFamily = mono, letterSpacing = 1.2.sp),
        labelMedium = base.labelMedium.copy(fontFamily = mono, letterSpacing = 1.sp),
        labelSmall = base.labelSmall.copy(fontFamily = mono, letterSpacing = 1.sp)
    )
}

/** Uppercase, wide-tracked label used for System headings. */
val SystemLabel = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Bold,
    fontSize = 11.sp,
    letterSpacing = 2.5.sp,
    color = SparkColors.TextMuted
)

/**
 * Spark Gym is light-only by design — a gym floor is a bright place and the
 * heat map needs a white backdrop to read. There is deliberately no dark
 * scheme and no [isSystemInDarkTheme] switch: an unused `darkTheme` parameter
 * here is exactly how an app drifts back to being dark by accident.
 */
@Composable
fun SparkGymTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val context = LocalContext.current
        SideEffect {
            (context as? Activity)?.window?.let { window ->
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    // Light mode: dark icons on light background
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = SparkScheme,
        typography = SparkTypography,
        content = content
    )
}
