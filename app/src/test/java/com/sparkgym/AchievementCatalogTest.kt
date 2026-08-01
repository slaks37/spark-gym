package com.sparkgym

import com.sparkgym.domain.model.AchievementCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the achievement key set.
 *
 * The badge art in AchievementsScreen is selected by these exact strings, and
 * every achievement was silently falling back to one image because the UI had
 * invented its own spelling — "first_workout" against a real "first-blood".
 * The mapping itself needs R.drawable so it cannot be unit-tested, but pinning
 * the catalog here means adding an achievement breaks this test and forces
 * whoever adds it to go and give it a badge.
 */
class AchievementCatalogTest {

    /** Every key that AchievementsScreen.badgeFor currently handles. */
    private val keysWithBadgeArt = setOf(
        "first-blood", "ten-gates", "fifty-gates", "hundred-gates",
        "tonnage-10k", "tonnage-100k", "tonnage-million",
        "streak-7", "streak-30", "streak-100",
        "record-breaker", "meal-planner", "nutritionist"
    )

    @Test
    fun `every achievement in the catalog has badge art`() {
        val catalogKeys = AchievementCatalog.all.map { it.key }.toSet()
        assertEquals(
            "catalog and badge art have drifted apart - update badgeFor in AchievementsScreen",
            keysWithBadgeArt,
            catalogKeys
        )
    }

    @Test
    fun `keys are kebab-case, never snake_case`() {
        AchievementCatalog.all.forEach {
            assertTrue("'${it.key}' must not contain an underscore", '_' !in it.key)
            assertTrue("'${it.key}' must be lowercase", it.key == it.key.lowercase())
        }
    }

    @Test
    fun `keys are unique and every definition is filled in`() {
        val keys = AchievementCatalog.all.map { it.key }
        assertEquals("duplicate achievement key", keys.size, keys.toSet().size)

        AchievementCatalog.all.forEach {
            assertTrue("${it.key} has no title", it.title.isNotBlank())
            assertTrue("${it.key} has no description", it.description.isNotBlank())
            assertTrue("${it.key} awards no XP", it.xp > 0)
        }
    }
}
