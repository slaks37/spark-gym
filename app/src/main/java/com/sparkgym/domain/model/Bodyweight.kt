package com.sparkgym.domain.model

/**
 * Bodyweight training is shaped differently from gym training: there is no load
 * to add, so a session is a *circuit* — a short list of moves repeated for a
 * number of rounds — and progression happens by moving up a level rather than
 * up the rack.
 *
 * These types describe that shape. They deliberately map onto the existing
 * routine/session tables (round → set, move → prescribed exercise) so a finished
 * circuit feeds the heat map, XP and quests exactly like a gym session does.
 */

enum class BodyweightFocus(val displayName: String, val blurb: String) {
    FULL_BODY("Full Body", "Everything, once, properly"),
    CORE("Core", "Abs, obliques and the bits that hold you together"),
    UPPER("Upper Body", "Push, pull and shoulders without a rack"),
    LOWER("Lower Body", "Legs and glutes, load-free"),
    CARDIO("Cardio & HIIT", "Breathing hard in a small room"),
    COMBAT("Combat", "Punches, kicks and footwork"),
    MOBILITY("Mobility", "Undo the desk, keep the joints"),
    LOW_IMPACT("Low Impact", "Quiet, seated or knee-friendly")
}

/**
 * Three rungs per circuit. The reps in a circuit definition are the Level II
 * numbers; the other two levels scale off them, so one definition covers a
 * beginner's first week and a hard session a year later.
 */
enum class BodyweightLevel(
    val displayName: String,
    val shortLabel: String,
    val repScale: Double,
    val rounds: Int,
    val restSecondsScale: Double
) {
    ONE("Level I", "I", 0.6, 2, 1.4),
    TWO("Level II", "II", 1.0, 3, 1.0),
    THREE("Level III", "III", 1.45, 4, 0.75);

    companion object {
        fun fromName(name: String?): BodyweightLevel =
            entries.firstOrNull { it.name == name } ?: TWO
    }
}

/**
 * One move inside a circuit. A move is counted either in reps or in seconds,
 * never both — [reps] and [seconds] are mutually exclusive and exactly one is
 * non-zero.
 */
data class CircuitMove(
    val exerciseSlug: String,
    val reps: Int = 0,
    val seconds: Int = 0,
    /** "Per side", "slow tempo" — anything the rep count alone cannot say. */
    val note: String = ""
) {
    val isTimed: Boolean get() = seconds > 0
}

data class BodyweightCircuit(
    val slug: String,
    val name: String,
    val tagline: String,
    val focus: BodyweightFocus,
    /** Rest between rounds at Level II, in seconds. */
    val restSeconds: Int,
    val moves: List<CircuitMove>
)

/** A circuit resolved at a level: what the UI shows and the logger prescribes. */
data class ResolvedCircuit(
    val circuit: BodyweightCircuit,
    val level: BodyweightLevel,
    val moves: List<ResolvedMove>,
    val rounds: Int,
    val restSeconds: Int,
    val estimatedMinutes: Int,
    val estimatedCalories: Int,
    val totalReps: Int
)

data class ResolvedMove(
    val move: CircuitMove,
    val reps: Int,
    val seconds: Int
) {
    val isTimed: Boolean get() = seconds > 0

    /** "12 reps" / "40 s" — the single string the UI needs. */
    val prescription: String get() = if (isTimed) "$seconds s" else "$reps reps"
}
