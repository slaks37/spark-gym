package com.sparkgym

import com.sparkgym.data.seed.BodyweightWorkouts
import com.sparkgym.data.seed.ExerciseSeed
import com.sparkgym.domain.engine.BodyweightEngine
import com.sparkgym.domain.model.BodyweightFocus
import com.sparkgym.domain.model.BodyweightLevel
import com.sparkgym.domain.model.Equipment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BodyweightEngineTest {

    private val circuit = BodyweightWorkouts.circuits.first()

    @Test
    fun `level two is the definition as written`() {
        val resolved = BodyweightEngine.resolve(circuit, BodyweightLevel.TWO)
        circuit.moves.zip(resolved.moves).forEach { (base, out) ->
            if (base.isTimed) assertEquals(base.seconds, out.seconds)
            else assertEquals(base.reps, out.reps)
        }
        assertEquals(circuit.restSeconds, resolved.restSeconds)
    }

    @Test
    fun `reps go down at level one and up at level three`() {
        val one = BodyweightEngine.scaleReps(20, BodyweightLevel.ONE)
        val two = BodyweightEngine.scaleReps(20, BodyweightLevel.TWO)
        val three = BodyweightEngine.scaleReps(20, BodyweightLevel.THREE)
        assertTrue("$one should be under $two", one < two)
        assertTrue("$three should be over $two", three > two)
        assertEquals(20, two)
    }

    @Test
    fun `a single rep never scales away to nothing`() {
        assertTrue(BodyweightEngine.scaleReps(1, BodyweightLevel.ONE) >= 1)
    }

    @Test
    fun `holds scale more gently than reps`() {
        // Doubling a plank is a far bigger jump than doubling ten squats, so the
        // seconds curve is deliberately flatter than the rep curve.
        val repGrowth = BodyweightEngine.scaleReps(100, BodyweightLevel.THREE) / 100.0
        val holdGrowth = BodyweightEngine.scaleSeconds(100, BodyweightLevel.THREE) / 100.0
        assertTrue("holds ($holdGrowth) grew at least as fast as reps ($repGrowth)", holdGrowth < repGrowth)
    }

    @Test
    fun `hold lengths land on readable five second steps`() {
        BodyweightLevel.entries.forEach { level ->
            listOf(25, 30, 40, 45, 60).forEach { base ->
                val scaled = BodyweightEngine.scaleSeconds(base, level)
                assertEquals("$base s at $level is not a round number", 0, scaled % 5)
                assertTrue("$base s at $level collapsed to $scaled", scaled >= 10)
            }
        }
    }

    @Test
    fun `harder levels mean more rounds and less rest`() {
        val easy = BodyweightEngine.resolve(circuit, BodyweightLevel.ONE)
        val hard = BodyweightEngine.resolve(circuit, BodyweightLevel.THREE)
        assertTrue(hard.rounds > easy.rounds)
        assertTrue("rest should shrink, not grow", hard.restSeconds < easy.restSeconds)
    }

    @Test
    fun `estimated duration rises with level and is never zero`() {
        val minutes = BodyweightLevel.entries.map {
            BodyweightEngine.resolve(circuit, it).estimatedMinutes
        }
        assertEquals(minutes.sorted(), minutes)
        assertTrue(minutes.all { it > 0 })
    }

    @Test
    fun `every bundled circuit lands in a believable session length`() {
        BodyweightWorkouts.circuits.forEach { c ->
            BodyweightLevel.entries.forEach { level ->
                val minutes = BodyweightEngine.resolve(c, level).estimatedMinutes
                assertTrue(
                    "${c.slug} at $level estimates $minutes min",
                    minutes in 3..60
                )
            }
        }
    }

    @Test
    fun `a heavier person burns more for the same circuit`() {
        val resolved = BodyweightEngine.resolve(circuit, BodyweightLevel.TWO)
        val light = BodyweightEngine.caloriesFor(resolved, 55.0)
        val heavy = BodyweightEngine.caloriesFor(resolved, 95.0)
        assertTrue("$heavy should exceed $light", heavy > light)
    }

    @Test
    fun `stretching costs less than sprinting on the spot`() {
        val mobility = BodyweightEngine.calories(BodyweightFocus.MOBILITY, 20, 70.0)
        val cardio = BodyweightEngine.calories(BodyweightFocus.CARDIO, 20, 70.0)
        assertTrue("$mobility should be well under $cardio", mobility * 2 < cardio)
    }

    @Test
    fun `the daily pick is stable within a day and moves between days`() {
        val circuits = BodyweightWorkouts.circuits
        val day = 20_000L
        assertEquals(
            BodyweightEngine.workoutOfTheDay(circuits, day),
            BodyweightEngine.workoutOfTheDay(circuits, day)
        )
        assertTrue(
            BodyweightEngine.workoutOfTheDay(circuits, day) !=
                BodyweightEngine.workoutOfTheDay(circuits, day + 1)
        )
    }

    @Test
    fun `the daily pick eventually covers the whole shelf`() {
        val circuits = BodyweightWorkouts.circuits
        val seen = (0L until circuits.size * 4L)
            .mapNotNull { BodyweightEngine.workoutOfTheDay(circuits, it)?.slug }
            .toSet()
        assertEquals("some circuits are never picked", circuits.size, seen.size)
    }

    @Test
    fun `an empty shelf has no pick rather than crashing`() {
        assertNull(BodyweightEngine.workoutOfTheDay(emptyList(), 1L))
    }
}

class BodyweightContentTest {

    private val library = ExerciseSeed.exercises.associateBy { it.slug }

    @Test
    fun `every move in every circuit exists in the library`() {
        BodyweightWorkouts.circuits.forEach { circuit ->
            circuit.moves.forEach { move ->
                assertNotNull(
                    "${circuit.slug} references unknown exercise '${move.exerciseSlug}'",
                    library[move.exerciseSlug]
                )
            }
        }
    }

    @Test
    fun `no circuit asks for equipment`() {
        // The whole promise of this feature is that nothing is needed. A barbell
        // sneaking into a circuit would break it silently.
        val allowed = setOf(Equipment.BODYWEIGHT, Equipment.OTHER)
        BodyweightWorkouts.circuits.forEach { circuit ->
            circuit.moves.forEach { move ->
                val exercise = library.getValue(move.exerciseSlug)
                assertTrue(
                    "${circuit.slug} needs a ${exercise.equipment} for ${move.exerciseSlug}",
                    exercise.equipment in allowed
                )
            }
        }
    }

    @Test
    fun `circuit slugs are unique`() {
        val slugs = BodyweightWorkouts.circuits.map { it.slug }
        assertEquals(slugs.size, slugs.toSet().size)
    }

    @Test
    fun `a move is counted in reps or in seconds, never both and never neither`() {
        BodyweightWorkouts.circuits.forEach { circuit ->
            circuit.moves.forEach { move ->
                val counted = (move.reps > 0) xor (move.seconds > 0)
                assertTrue(
                    "${circuit.slug}/${move.exerciseSlug} has reps=${move.reps} seconds=${move.seconds}",
                    counted
                )
            }
        }
    }

    @Test
    fun `every focus has at least one circuit`() {
        BodyweightFocus.entries.forEach { focus ->
            assertTrue(
                "nothing to show when filtering by $focus",
                BodyweightWorkouts.circuits.any { it.focus == focus }
            )
        }
    }

    @Test
    fun `circuits are long enough to be a session and short enough to finish`() {
        BodyweightWorkouts.circuits.forEach { circuit ->
            assertTrue("${circuit.slug} has ${circuit.moves.size} moves", circuit.moves.size in 4..8)
        }
    }

    @Test
    fun `the low impact circuits contain nothing that leaves the floor`() {
        // Somebody picks this filter because jumping is off the table today.
        val jumps = setOf(
            "jump-squat", "tuck-jump", "star-jump", "burpee", "skater-hop",
            "jumping-jack", "plank-jack", "high-knees", "butt-kick"
        )
        BodyweightWorkouts.circuits
            .filter { it.focus == BodyweightFocus.LOW_IMPACT }
            .forEach { circuit ->
                circuit.moves.forEach { move ->
                    assertTrue(
                        "${circuit.slug} is meant to be low impact but includes ${move.exerciseSlug}",
                        move.exerciseSlug !in jumps
                    )
                }
            }
    }

    @Test
    fun `bodyweight moves are logged by reps or time, never by weight`() {
        val bodyweightSlugs = BodyweightWorkouts.circuits
            .flatMap { it.moves }
            .map { it.exerciseSlug }
            .toSet()
        bodyweightSlugs.forEach { slug ->
            val tracking = library.getValue(slug).tracking
            assertTrue(
                "$slug is tracked as $tracking, which asks the user for a weight",
                tracking.name != "WEIGHT_REPS"
            )
        }
    }
}
