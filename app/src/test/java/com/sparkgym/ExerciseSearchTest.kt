package com.sparkgym

import com.sparkgym.data.seed.BodyweightSeed
import com.sparkgym.data.seed.ExerciseSeed
import com.sparkgym.domain.engine.ExerciseSearch
import com.sparkgym.domain.model.Equipment
import com.sparkgym.domain.model.Muscle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Search has to work for someone who does not know a single exercise name —
 * that is the whole point of it, so it gets tested against the real library
 * rather than against hand-made fixtures.
 */
class ExerciseSearchTest {

    private val library = (ExerciseSeed.exercises + BodyweightSeed.exercises).map {
        ExerciseSearch.Candidate(
            name = it.name,
            equipment = it.equipment,
            primary = it.primary.toSet(),
            secondary = it.secondary.toSet()
        )
    }

    private fun find(query: String) = library.filter { ExerciseSearch.matches(query, it) }

    @Test
    fun `an empty query matches everything`() {
        assertEquals(library.size, find("").size)
        assertEquals(library.size, find("   ").size)
    }

    @Test
    fun `searching a muscle in English finds exercises that never say it`() {
        val hits = find("chest")
        assertTrue(hits.size > 20)
        assertTrue(
            "bench press must come back for 'chest' even though it is not in the name",
            hits.any { it.name == "Barbell Bench Press" }
        )
    }

    @Test
    fun `searching a muscle in Indonesian finds the same exercises`() {
        val en = find("chest").map { it.name }.toSet()
        val id = find("dada").map { it.name }.toSet()

        // Every movement that genuinely trains the chest has to come back for
        // both spellings — that is the promise.
        val realChestWork = library.filter { Muscle.CHEST in it.allMuscles }.map { it.name }
        assertTrue(en.containsAll(realChestWork))
        assertTrue(id.containsAll(realChestWork))

        // English can additionally match on the name, because exercise names
        // are English only. "Chest-Supported Row" is a back exercise whose name
        // happens to contain the word, so it is the difference between the two.
        assertTrue("ID results must not contain anything EN misses", en.containsAll(id))
    }

    @Test
    fun `a name that merely mentions a muscle ranks below real work for it`() {
        val supportedRow = library.first { it.name == "Chest-Supported Row" }
        val realChest = library.first { Muscle.CHEST in it.primary }

        assertTrue(
            "Chest-Supported Row should still be findable by name",
            ExerciseSearch.matches("chest", supportedRow)
        )
        assertTrue(
            "but it is a back exercise and must not outrank actual chest work",
            ExerciseSearch.score("chest", realChest)!! > ExerciseSearch.score("chest", supportedRow)!!
        )
    }

    @Test
    fun `everyday words find the right muscle`() {
        assertTrue("sixpack should find ab work", find("sixpack").any { Muscle.ABS in it.primary })
        assertTrue("pantat should find glute work", find("pantat").any { Muscle.GLUTES in it.primary })
        assertTrue("sayap should find lat work", find("sayap").any { Muscle.LATS in it.primary })
        assertTrue("punggung should find back work", find("punggung").any { Muscle.LATS in it.allMuscles })
        assertTrue("betis should find calf work", find("betis").any { Muscle.CALVES in it.primary })
        assertTrue("paha should find leg work", find("paha").isNotEmpty())
    }

    @Test
    fun `searching a muscle group works in both languages`() {
        assertTrue(find("legs").isNotEmpty())
        assertTrue(find("kaki").isNotEmpty())
        assertTrue(find("push").isNotEmpty())
        assertTrue(find("dorong").isNotEmpty())
    }

    @Test
    fun `searching equipment works`() {
        val dumbbell = find("dumbbell")
        assertTrue(dumbbell.isNotEmpty())
        assertTrue(dumbbell.any { it.equipment == Equipment.DUMBBELL })
    }

    @Test
    fun `two words narrow rather than widen`() {
        val chest = find("chest").size
        val both = find("chest dumbbell").size
        assertTrue("adding a term must not return more", both < chest)
        assertTrue("but should still return something", both > 0)
    }

    @Test
    fun `nonsense matches nothing`() {
        assertTrue(find("zzzzqqq").isEmpty())
    }

    @Test
    fun `hyphens and spacing do not matter`() {
        assertEquals(find("pull up").size, find("pull-up").size)
        assertEquals(find("CHEST").size, find("  chest  ").size)
    }

    @Test
    fun `a name match outranks a muscle match`() {
        val press = library.first { it.name == "Barbell Bench Press" }
        val other = library.first { Muscle.CHEST in it.primary && it.name != "Barbell Bench Press" }
        val nameScore = ExerciseSearch.score("bench press", press)!!
        val muscleScore = ExerciseSearch.score("bench press", other)
        assertTrue("a movement matched by name should score", nameScore > 0)
        assertTrue("and should beat anything matched only by muscle", muscleScore == null || nameScore > muscleScore)
    }

    @Test
    fun `a prime mover outranks a synergist`() {
        val chestPrimary = library.first { Muscle.CHEST in it.primary }
        val chestSecondary = library.first { Muscle.CHEST in it.secondary && Muscle.CHEST !in it.primary }
        assertTrue(
            ExerciseSearch.score("chest", chestPrimary)!! > ExerciseSearch.score("chest", chestSecondary)!!
        )
    }

    @Test
    fun `every muscle is reachable by typing its own name`() {
        Muscle.entries.forEach { m ->
            assertTrue("no results when searching '${m.displayName}'", find(m.displayName).isNotEmpty())
            assertTrue("no results when searching '${m.nameId}'", find(m.nameId).isNotEmpty())
        }
    }

    @Test
    fun `typed body-part words resolve to filter chips`() {
        assertTrue(Muscle.CHEST in ExerciseSearch.musclesFor("dada"))
        assertTrue(Muscle.GLUTES in ExerciseSearch.musclesFor("pantat"))
        assertTrue(ExerciseSearch.musclesFor("bahu").size >= 3)  // all three delt heads
        assertTrue(ExerciseSearch.musclesFor("").isEmpty())
        assertFalse(Muscle.CHEST in ExerciseSearch.musclesFor("betis"))
    }
}
