package com.sparkgym.core.design

import androidx.compose.ui.graphics.Color

/**
 * The palette is built around the "system window" look: a near-black void,
 * a cyan hologram, and a violet cast for anything the System considers rare.
 */
object SparkColors {
    val Void = Color(0xFF05060E)
    val VoidElevated = Color(0xFF0B0E1A)
    val Panel = Color(0xFF101427)
    val PanelHigh = Color(0xFF161B33)
    val Divider = Color(0xFF243055)

    val Cyan = Color(0xFF37E8FF)
    val CyanDim = Color(0xFF1B7F94)
    val CyanGlow = Color(0x5537E8FF)

    val Violet = Color(0xFF9B6BFF)
    val VioletGlow = Color(0x559B6BFF)

    val Amber = Color(0xFFFFC24B)
    val Danger = Color(0xFFFF5C6E)
    val Success = Color(0xFF4BE38C)

    val TextPrimary = Color(0xFFE8F1FF)
    val TextSecondary = Color(0xFF9AA9C7)
    val TextMuted = Color(0xFF5F6E8E)

    /** Heat-map ramp: cold (untrained) → hot (heavily trained this week). */
    val HeatCold = Color(0xFF1B2340)
    val HeatLow = Color(0xFF2E6FA8)
    val HeatMid = Color(0xFF39C6C0)
    val HeatHigh = Color(0xFFF2B33D)
    val HeatMax = Color(0xFFFF4D4D)

    /** Macro colours reused across nutrition charts and rings. */
    val Protein = Color(0xFF4BE38C)
    val Carbs = Color(0xFF37E8FF)
    val Fat = Color(0xFFFFC24B)
}

/** Rank colours, E through S, plus the National-level flourish. */
enum class RankColor(val color: Color) {
    E(Color(0xFF8A93A8)),
    D(Color(0xFF6FC46F)),
    C(Color(0xFF37E8FF)),
    B(Color(0xFF5C8BFF)),
    A(Color(0xFF9B6BFF)),
    S(Color(0xFFFFC24B))
}
