package com.sparkgym.core.design

import androidx.compose.ui.graphics.Color

/**
 * Modern Premium Light-mode palette.
 * Clean, minimalistic white surfaces with soft shadows and highly vibrant accents.
 */
object SparkColors {
    // ── Surfaces ─────────────────────────────────────────────────────
    val Void = Color(0xFFF7F9FC)            // Extremely soft, cool off-white for the main background
    val VoidElevated = Color(0xFFFFFFFF)    // Pure white for elevated cards
    val Panel = Color(0xFFFFFFFF)            // Pure white for panels
    val PanelHigh = Color(0xFFF0F4F8)       // Very subtle gray/blue for inner wells
    val Divider = Color(0xFFE2E8F0)         // Clean, light borders

    // ── Primary accents ──────────────────────────────────────────────
    val Cyan = Color(0xFF0EA5E9)            // Vibrant Sky Blue
    val CyanDim = Color(0xFF38BDF8)         // Lighter Sky Blue
    val CyanGlow = Color(0x330EA5E9)

    val Violet = Color(0xFF8B5CF6)          // Vibrant Purple
    val VioletGlow = Color(0x338B5CF6)

    val Amber = Color(0xFFF59E0B)           // Warm Amber
    val Danger = Color(0xFFEF4444)          // Crisp Red
    val Success = Color(0xFF10B981)         // Emerald Green

    // ── Text ─────────────────────────────────────────────────────────
    val TextPrimary = Color(0xFF0F172A)     // Slate 900 for high contrast headers
    val TextSecondary = Color(0xFF475569)   // Slate 600 for body copy
    val TextMuted = Color(0xFF94A3B8)       // Slate 400 for hints

    // ── Heat-map ramp ────────────────────────────────────────────────
    val HeatCold = Color(0xFFCBD5E1)
    val HeatLow = Color(0xFF7DD3FC)
    val HeatMid = Color(0xFF38BDF8)
    val HeatHigh = Color(0xFFF59E0B)
    val HeatMax = Color(0xFFEF4444)

    // ── Macro colours ────────────────────────────────────────────────
    val Protein = Color(0xFF10B981)
    val Carbs = Color(0xFF0EA5E9)
    val Fat = Color(0xFFF59E0B)
}

/** Rank colours, tuned for legibility on white panels. */
enum class RankColor(val color: Color) {
    E(Color(0xFF94A3B8)),
    D(Color(0xFF22C55E)),
    C(Color(0xFF0EA5E9)),
    B(Color(0xFF3B82F6)),
    A(Color(0xFF8B5CF6)),
    S(Color(0xFFF59E0B))
}
