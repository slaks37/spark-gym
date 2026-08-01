package com.sparkgym.domain.model

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * The six attributes the System tracks. Each one is fed by a different real
 * signal, so you cannot max the sheet by only doing what you already like.
 */
enum class Attribute(val short: String, val displayName: String, val sourceHint: String) {
    STRENGTH("STR", "Strength", "Heavy top sets and estimated 1RM growth"),
    VITALITY("VIT", "Vitality", "Weekly training volume across all muscles"),
    AGILITY("AGI", "Agility", "Steps and daily movement"),
    ENDURANCE("END", "Endurance", "Cardio minutes and active calories"),
    INTELLECT("INT", "Intellect", "Nutrition logging and macro adherence"),
    PERCEPTION("PER", "Perception", "Sleep duration and resting heart rate")
}

enum class Rank(val label: String, val minLevel: Int) {
    E("E", 1),
    D("D", 10),
    C("C", 20),
    B("B", 32),
    A("A", 46),
    S("S", 62);

    companion object {
        fun forLevel(level: Int): Rank = entries.last { level >= it.minLevel }

        /** Level at which the next rank unlocks, or null at S-rank. */
        fun nextThreshold(level: Int): Int? =
            entries.firstOrNull { it.minLevel > level }?.minLevel
    }
}

/**
 * Class titles unlock from your training bias. They are cosmetic, but they are
 * the thing people screenshot, so they are computed honestly.
 */
enum class HunterClass(val displayName: String, val description: String) {
    AWAKENED("Awakened", "No dominant pattern yet — keep training"),
    FIGHTER("Fighter", "Balanced across push, pull and legs"),
    TANK("Tank", "Squat and hinge dominant"),
    ASSASSIN("Assassin", "High volume, high frequency, low rest"),
    RANGER("Ranger", "Cardio and step dominant"),
    MAGE("Mage", "Nutrition and recovery dialled in"),
    MONARCH("Monarch", "S-rank across every attribute")
}

/**
 * XP curve. Deliberately super-linear so early levels come fast and later ones
 * need real consistency: total XP to reach level L is 100 * (L-1)^1.5.
 */
object LevelCurve {
    private const val BASE = 100.0
    private const val EXPONENT = 1.5

    fun totalXpForLevel(level: Int): Long {
        if (level <= 1) return 0
        return (BASE * (level - 1).toDouble().pow(EXPONENT)).roundToInt().toLong()
    }

    fun levelForTotalXp(totalXp: Long): Int {
        var level = 1
        while (totalXp >= totalXpForLevel(level + 1) && level < 200) level++
        return level
    }

    fun xpIntoLevel(totalXp: Long): Long = totalXp - totalXpForLevel(levelForTotalXp(totalXp))

    fun xpNeededForNextLevel(totalXp: Long): Long {
        val level = levelForTotalXp(totalXp)
        return totalXpForLevel(level + 1) - totalXpForLevel(level)
    }

    fun progressInLevel(totalXp: Long): Float {
        val needed = xpNeededForNextLevel(totalXp)
        if (needed <= 0) return 1f
        return (xpIntoLevel(totalXp).toFloat() / needed.toFloat()).coerceIn(0f, 1f)
    }
}

/** A snapshot of the player, assembled from the DB for the status window. */
data class HunterProfile(
    val name: String,
    val totalXp: Long,
    val attributes: Map<Attribute, Int>,
    val unspentPoints: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val hunterClass: HunterClass,
    val title: String?
) {
    val level: Int get() = LevelCurve.levelForTotalXp(totalXp)
    val rank: Rank get() = Rank.forLevel(level)
    val xpIntoLevel: Long get() = LevelCurve.xpIntoLevel(totalXp)
    val xpForNextLevel: Long get() = LevelCurve.xpNeededForNextLevel(totalXp)
    val levelProgress: Float get() = LevelCurve.progressInLevel(totalXp)

    /** Total attribute score, used for the "power" headline number. */
    val power: Int get() = attributes.values.sum()
}
