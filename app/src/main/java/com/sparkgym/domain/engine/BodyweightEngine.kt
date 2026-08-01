package com.sparkgym.domain.engine

import com.sparkgym.domain.model.BodyweightCircuit
import com.sparkgym.domain.model.BodyweightFocus
import com.sparkgym.domain.model.BodyweightLevel
import com.sparkgym.domain.model.ResolvedCircuit
import com.sparkgym.domain.model.ResolvedMove
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Turns a circuit definition plus a level into the numbers the UI and the
 * logger both need: reps per move, rounds, rest, and honest estimates of how
 * long it will take and what it costs.
 *
 * The estimates are deliberately conservative. A duration that reads short is
 * worse than useless — people plan around it.
 */
object BodyweightEngine {

    /** Seconds a single rep takes, averaged over a controlled tempo plus transitions. */
    private const val SECONDS_PER_REP = 2.6

    /** Changing position between moves. Small, but eight moves make it two minutes. */
    private const val TRANSITION_SECONDS = 8

    /**
     * MET values for circuit training, in the range the compendium of physical
     * activities puts calisthenics: light stretching sits near 2.5, vigorous
     * circuit work near 8.
     */
    private fun met(focus: BodyweightFocus): Double = when (focus) {
        BodyweightFocus.CARDIO -> 8.0
        BodyweightFocus.COMBAT -> 7.5
        BodyweightFocus.FULL_BODY -> 7.0
        BodyweightFocus.UPPER, BodyweightFocus.LOWER -> 5.5
        BodyweightFocus.CORE -> 4.5
        BodyweightFocus.LOW_IMPACT -> 3.0
        BodyweightFocus.MOBILITY -> 2.5
    }

    fun scaleReps(baseReps: Int, level: BodyweightLevel): Int =
        max(1, (baseReps * level.repScale).roundToInt())

    /**
     * Timed moves scale more gently than rep moves — doubling a plank is a much
     * bigger jump than doubling ten squats, and holds are where people fail
     * first. Rounded to the nearest five seconds so the numbers stay readable.
     */
    fun scaleSeconds(baseSeconds: Int, level: BodyweightLevel): Int {
        val gentled = 1.0 + (level.repScale - 1.0) * 0.7
        val raw = baseSeconds * gentled
        return max(10, (raw / 5.0).roundToInt() * 5)
    }

    fun restFor(circuit: BodyweightCircuit, level: BodyweightLevel): Int =
        max(15, (circuit.restSeconds * level.restSecondsScale).roundToInt())

    fun resolve(circuit: BodyweightCircuit, level: BodyweightLevel): ResolvedCircuit {
        val moves = circuit.moves.map { move ->
            ResolvedMove(
                move = move,
                reps = if (move.isTimed) 0 else scaleReps(move.reps, level),
                seconds = if (move.isTimed) scaleSeconds(move.seconds, level) else 0
            )
        }
        val rounds = level.rounds
        val rest = restFor(circuit, level)

        val workSecondsPerRound = moves.sumOf { m ->
            val work = if (m.isTimed) m.seconds.toDouble() else m.reps * SECONDS_PER_REP
            work + TRANSITION_SECONDS
        }
        // Rest happens between rounds, so there is one fewer rest than rounds.
        val totalSeconds = workSecondsPerRound * rounds + rest.toDouble() * (rounds - 1)
        val minutes = max(1, (totalSeconds / 60.0).roundToInt())

        return ResolvedCircuit(
            circuit = circuit,
            level = level,
            moves = moves,
            rounds = rounds,
            restSeconds = rest,
            estimatedMinutes = minutes,
            estimatedCalories = calories(circuit.focus, minutes, DEFAULT_BODYWEIGHT_KG),
            totalReps = moves.sumOf { it.reps } * rounds
        )
    }

    /**
     * MET × kg × hours. Bodyweight work has no external load, so mass is the
     * only lever — which is why the same circuit costs a heavier person more.
     */
    fun calories(focus: BodyweightFocus, minutes: Int, bodyweightKg: Double): Int =
        (met(focus) * bodyweightKg * (minutes / 60.0)).roundToInt()

    fun caloriesFor(resolved: ResolvedCircuit, bodyweightKg: Double): Int =
        calories(resolved.circuit.focus, resolved.estimatedMinutes, bodyweightKg)

    /**
     * The daily pick. Deterministic on the date so it stays the same all day and
     * across reinstalls, and rotates through the whole list rather than
     * clustering on one focus — consecutive days step by a number coprime with
     * most list sizes so you rarely get two of the same focus back to back.
     */
    fun workoutOfTheDay(
        circuits: List<BodyweightCircuit>,
        epochDay: Long
    ): BodyweightCircuit? {
        if (circuits.isEmpty()) return null
        val stride = 7L
        val index = ((epochDay * stride) % circuits.size + circuits.size) % circuits.size
        return circuits[index.toInt()]
    }

    /**
     * Volume for the heat map. Bodyweight work has no kilos on a bar, so a
     * "set" is credited by the fraction of bodyweight the movement actually
     * moves — a push-up is roughly two thirds of you, a squat rather more.
     */
    fun effectiveSets(resolved: ResolvedCircuit): Double =
        resolved.moves.size.toDouble() * resolved.rounds

    const val DEFAULT_BODYWEIGHT_KG = 70.0
}
