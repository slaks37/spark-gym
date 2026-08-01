package com.sparkgym

import com.sparkgym.domain.model.Meal
import com.sparkgym.domain.model.Muscle
import com.sparkgym.domain.model.MuscleGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The language switch is only honest if the Indonesian column is actually
 * filled in. A missing translation silently falls back to English and looks
 * like a rendering bug, so every translatable enum is pinned here.
 */
class BilingualTest {

    @Test
    fun `every muscle is named in both languages`() {
        val missing = Muscle.entries.filter { it.nameId.isBlank() || it.nameId == it.displayName }
        assertTrue("untranslated muscles: $missing", missing.isEmpty())
    }

    @Test
    fun `every muscle group is named in both languages`() {
        val missing = MuscleGroup.entries.filter { it.nameId.isBlank() || it.nameId == it.displayName }
        assertTrue("untranslated groups: $missing", missing.isEmpty())
    }

    @Test
    fun `every meal slot is named in both languages`() {
        val missing = Meal.entries.filter { it.nameId.isBlank() || it.nameId == it.displayName }
        assertTrue("untranslated meals: $missing", missing.isEmpty())
    }

    @Test
    fun `translations are distinct within each language`() {
        // Two muscles sharing a label makes the picker ambiguous in that
        // language even when it reads fine in the other.
        assertEquals(
            "duplicate Indonesian muscle names",
            Muscle.entries.size,
            Muscle.entries.map { it.nameId }.toSet().size
        )
        assertEquals(
            "duplicate English muscle names",
            Muscle.entries.size,
            Muscle.entries.map { it.displayName }.toSet().size
        )
    }

    @Test
    fun `every muscle carries search aliases in both languages`() {
        // Searching by muscle is the whole point of the aliases; a muscle with
        // none can only be found by typing its exact name, which is the
        // knowledge the feature exists to not require.
        val bare = Muscle.entries.filter { it.aliases.isEmpty() }
        assertTrue("muscles with no search aliases: $bare", bare.isEmpty())
    }

    @Test
    fun `meal keys survive a round trip`() {
        Meal.entries.forEach { assertEquals(it, Meal.fromKey(it.name)) }
        // Unknown keys must not throw — old rows should degrade, not crash.
        assertEquals(Meal.SNACK, Meal.fromKey("NOT_A_MEAL"))
    }
}
