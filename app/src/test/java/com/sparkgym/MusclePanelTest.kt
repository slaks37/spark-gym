package com.sparkgym

import com.sparkgym.data.seed.ExerciseSeed
import com.sparkgym.data.seed.SeedExercise
import com.sparkgym.data.seed.StretchSeed
import com.sparkgym.domain.engine.MusclePanel
import com.sparkgym.domain.model.Muscle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tapping a muscle on the body has to answer three questions honestly: what
 * trains it, what assists it, and what to stretch afterwards.
 *
 * These run the real panel logic over the real library, so "every muscle has
 * exercises" is a fact about what ships rather than a hope.
 */
class MusclePanelTest {

    /** The seed list, adapted onto what the panel consumes. */
    private class Row(val seed: SeedExercise) : MusclePanel.Item {
        override fun contributionTo(muscle: Muscle): Float? = when {
            muscle in seed.primary -> 1.0f
            muscle in seed.secondary -> 0.5f
            else -> null
        }
        override val homeFriendly: Boolean get() = seed.equipment.isHomeFriendly
    }

    private val library = ExerciseSeed.exercises.map(::Row)

    @Test
    fun `every muscle has at least one exercise that trains it directly`() {
        val empty = Muscle.entries.filter { MusclePanel.primary(it, library).isEmpty() }
        assertTrue("tapping these would show an empty Primary tab: $empty", empty.isEmpty())
    }

    @Test
    fun `every muscle has a no-equipment option among its primary work`() {
        // The promise the bodyweight half of the library makes: whatever the
        // heat map says is cold, there is something you can do about it at home.
        val gymOnly = Muscle.entries.filter { m ->
            MusclePanel.primary(m, library).none { it.homeFriendly }
        }
        assertTrue("no home-friendly way to train these: $gymOnly", gymOnly.isEmpty())
    }

    @Test
    fun `every muscle has an equipment option among its primary work`() {
        val homeOnly = Muscle.entries.filter { m ->
            MusclePanel.primary(m, library).none { !it.homeFriendly }
        }
        assertTrue("no equipment exercise for these: $homeOnly", homeOnly.isEmpty())
    }

    @Test
    fun `a home-friendly option is never buried`() {
        // Interleaving is the point: someone with no kit should not have to
        // scroll to find something they can actually do.
        Muscle.entries.forEach { m ->
            val list = MusclePanel.primary(m, library)
            if (list.none { it.homeFriendly }) return@forEach
            val firstHome = list.indexOfFirst { it.homeFriendly }
            assertTrue(
                "$m: first home-friendly option is at index $firstHome",
                firstHome <= 1
            )
        }
    }

    @Test
    fun `primary and secondary never overlap`() {
        Muscle.entries.forEach { m ->
            val p = MusclePanel.primary(m, library).map { it.seed.slug }.toSet()
            val s = MusclePanel.secondary(m, library).map { it.seed.slug }.toSet()
            assertTrue("$m appears in both tabs: ${p intersect s}", (p intersect s).isEmpty())
        }
    }

    @Test
    fun `primary only ever lists prime movers`() {
        Muscle.entries.forEach { m ->
            MusclePanel.primary(m, library).forEach {
                assertTrue(
                    "${it.seed.slug} is listed as primary for $m but does not train it",
                    m in it.seed.primary
                )
            }
        }
    }

    @Test
    fun `an empty result stays empty rather than being padded`() {
        // The old implementation fell back to "the first four exercises", which
        // told the user a leg press trains their neck.
        val nothing = MusclePanel.primary(Muscle.CHEST, emptyList<Row>())
        assertTrue(nothing.isEmpty())
    }

    @Test
    fun `interleaving keeps every match and adds none`() {
        Muscle.entries.forEach { m ->
            val raw = library.filter { m in it.seed.primary }
            val shown = MusclePanel.primary(m, library)
            assertEquals("$m lost or gained entries", raw.size, shown.size)
            assertEquals(raw.map { it.seed.slug }.toSet(), shown.map { it.seed.slug }.toSet())
        }
    }

    @Test
    fun `every muscle has a stretch to offer`() {
        Muscle.entries.forEach { m ->
            val s = StretchSeed.forExercise(setOf(m))
            assertTrue("$m has no stretch", s.isNotEmpty())
            assertNotNull(s.first().nameId)
            assertFalse("$m stretch has no instructions", s.first().howToId.isBlank())
        }
    }
}
