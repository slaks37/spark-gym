package com.sparkgym.core.design

import androidx.compose.ui.graphics.Color

/**
 * Light-mode palette.  Clean white surfaces with vibrant accent pops,
 * subtle shadows, and strong contrast for readability in daylight.
 */
object SparkColors {
    // ── Surfaces ─────────────────────────────────────────────────────
    val Void = Color(0xFFF5F7FA)            // page background — warm off-white
    val VoidElevated = Color(0xFFEDF0F5)    // slightly dimmer for nav bars
    val Panel = Color(0xFFFFFFFF)            // card / panel fill — pure white
    val PanelHigh = Color(0xFFF0F2F7)       // slightly tinted for chips / wells
    val Divider = Color(0xFFD6DCE8)         // light border / separator

    // ── Primary accents ──────────────────────────────────────────────
    val Cyan = Color(0xFF0099CC)            // darkened for white-bg contrast
    val CyanDim = Color(0xFF6EC6DF)
    val CyanGlow = Color(0x330099CC)

    val Violet = Color(0xFF7C4DFF)          // deepened purple
    val VioletGlow = Color(0x337C4DFF)

    val Amber = Color(0xFFE8A317)           // warm gold, darker for light bg
    val Danger = Color(0xFFE53945)          // strong red
    val Success = Color(0xFF16A34A)         // rich green

    // ── Text ─────────────────────────────────────────────────────────
    val TextPrimary = Color(0xFF1A1D26)     // near-black for headlines
    val TextSecondary = Color(0xFF4A5568)   // slate for body copy
    val TextMuted = Color(0xFF94A3B8)       // placeholder / captions

    // ── Heat-map ramp ────────────────────────────────────────────────
    val HeatCold = Color(0xFFE2E8F0)
    val HeatLow = Color(0xFF7EC8E3)
    val HeatMid = Color(0xFF38BDF8)
    val HeatHigh = Color(0xFFF59E0B)
    val HeatMax = Color(0xFFEF4444)

    // ── Macro colours ────────────────────────────────────────────────
    val Protein = Color(0xFF16A34A)
    val Carbs = Color(0xFF0099CC)
    val Fat = Color(0xFFE8A317)
}

/** Rank colours, tuned for legibility on white panels. */
enum class RankColor(val color: Color) {
    E(Color(0xFF94A3B8)),
    D(Color(0xFF22C55E)),
    C(Color(0xFF0099CC)),
    B(Color(0xFF3B82F6)),
    A(Color(0xFF7C4DFF)),
    S(Color(0xFFE8A317))
}
