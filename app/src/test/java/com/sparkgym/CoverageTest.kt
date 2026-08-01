package com.sparkgym

import com.sparkgym.data.seed.BodyweightSeed
import com.sparkgym.data.seed.ExerciseSeed
import com.sparkgym.data.seed.StretchSeed
import com.sparkgym.domain.model.Muscle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Content-coverage guarantees.
 *
 * Both of these are promises made to the user rather than internal details, so
 * they get pinned: every muscle the heat map can shade is trainable from the
 * bundled library, and every one of them has a stretch to go with it.
 */
class CoverageTest {

    private val allExercises = ExerciseSeed.exercises + BodyweightSeed.exercises

    @Test
    fun `every muscle has at least ten exercises`() {
        val counts = Muscle.entries.associateWith { muscle ->
            allExercises.count { muscle in it.primary || muscle in it.secondary }
        }
        val short = counts.filterValues { it < 10 }
        assertTrue("under-served muscles: $short", short.isEmpty())
    }

    @Test
    fun `every muscle can be trained directly, not only as a synergist`() {
        val noPrimary = Muscle.entries.filter { muscle ->
            allExercises.none { muscle in it.primary }
        }
        assertTrue("no exercise targets these directly: $noPrimary", noPrimary.isEmpty())
    }

    @Test
    fun `every muscle has a stretch`() {
        Muscle.entries.forEach {
            assertNotNull("no stretch defined for $it", StretchSeed.forMuscle(it))
        }
        assertEquals(Muscle.entries.size, StretchSeed.stretches.size)
    }

    @Test
    fun `stretches are filled in for both languages`() {
        StretchSeed.stretches.forEach {
            assertTrue("${it.muscle} missing English name", it.nameEn.isNotBlank())
            assertTrue("${it.muscle} missing Indonesian name", it.nameId.isNotBlank())
            assertTrue("${it.muscle} missing English cue", it.howToEn.isNotBlank())
            assertTrue("${it.muscle} missing Indonesian cue", it.howToId.isNotBlank())
            assertTrue("${it.muscle} has a silly hold time", it.holdSeconds in 10..90)
        }
    }

    @Test
    fun `stretch lookup follows an exercise's own muscles`() {
        val bench = allExercises.first { it.slug == "barbell-bench-press" }
        val found = StretchSeed.forExercise(bench.primary.toSet(), bench.secondary.toSet())
        assertTrue("bench press should suggest a chest stretch", found.any { it.muscle == Muscle.CHEST })
        assertEquals("prime mover's stretch comes first", Muscle.CHEST, found.first().muscle)
    }
}
