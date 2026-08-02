package com.sparkgym

import com.sparkgym.data.seed.BodyweightWorkouts
import com.sparkgym.data.seed.ExerciseSeed
import com.sparkgym.data.seed.RoutineSeed
import com.sparkgym.data.seed.RoutineSeedPro
import com.sparkgym.domain.model.Equipment
import com.sparkgym.domain.model.Muscle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The seed data is only useful once it reaches the database, and the two are
 * wired together by slug — a string with no compiler to check it.
 *
 * SeedRepository resolves every routine and circuit move with
 * `mapIndexedNotNull`, which drops anything it cannot find without a word. A
 * slug that stops matching therefore ships a half-empty workout and fails
 * nothing, so the contract is pinned here instead.
 */
class SeedIntegrityTest {

    /**
     * Exactly what SeedRepository inserts. ExerciseSeed.exercises already folds
     * in the advanced and bodyweight lists, so adding BodyweightSeed here again
     * would test a set the app never actually builds.
     */
    private val seeded = ExerciseSeed.exercises
    private val seededSlugs = seeded.map { it.slug }.toSet()

    @Test
    fun `slugs are unique across the whole library`() {
        val dupes = seeded.groupBy { it.slug }.filterValues { it.size > 1 }.keys
        assertTrue("duplicate slugs would collide on insert: $dupes", dupes.isEmpty())
        assertEquals(seeded.size, seededSlugs.size)
    }

    @Test
    fun `every bodyweight circuit move resolves to a seeded exercise`() {
        val missing = BodyweightWorkouts.circuits.flatMap { circuit ->
            circuit.moves.map { it.exerciseSlug }
        }.toSet() - seededSlugs
        assertTrue("circuits reference unseeded exercises: $missing", missing.isEmpty())
    }

    @Test
    fun `no bodyweight circuit ends up empty`() {
        val empty = BodyweightWorkouts.circuits.filter { circuit ->
            circuit.moves.none { it.exerciseSlug in seededSlugs }
        }
        assertTrue("circuits that would seed with no moves at all: ${empty.map { it.slug }}", empty.isEmpty())
    }

    @Test
    fun `every routine prescription resolves to a seeded exercise`() {
        val refs = (RoutineSeed.routines + RoutineSeedPro.routines)
            .flatMap { it.days }
            .flatMap { it.exercises }
            .map { it.exerciseSlug }
            .toSet()
        val missing = refs - seededSlugs
        assertTrue("routines reference unseeded exercises: $missing", missing.isEmpty())
    }

    @Test
    fun `every muscle can be trained without equipment`() {
        // The point of the bodyweight half: someone at home with no kit still
        // has to be able to work every region the heat map can shade.
        val home = seeded.filter { it.equipment.isHomeFriendly }
        val unreachable = Muscle.entries.filter { m ->
            home.none { m in it.primary }
        }
        assertTrue("no home-friendly exercise targets these: $unreachable", unreachable.isEmpty())
    }

    @Test
    fun `every muscle can be trained with equipment too`() {
        val gym = seeded.filterNot { it.equipment == Equipment.BODYWEIGHT }
        val unreachable = Muscle.entries.filter { m ->
            gym.none { m in it.primary }
        }
        assertTrue("no equipment exercise targets these: $unreachable", unreachable.isEmpty())
    }

    @Test
    fun `every exercise declares at least one primary muscle`() {
        val orphans = seeded.filter { it.primary.isEmpty() }.map { it.slug }
        assertTrue("exercises that train nothing: $orphans", orphans.isEmpty())
    }

    @Test
    fun `no exercise lists the same muscle as primary and secondary`() {
        // Seeding filters these out, but a double-counted muscle in the source
        // means the volume maths was written against a wrong intent.
        val both = seeded.filter { ex -> ex.primary.any { it in ex.secondary } }.map { it.slug }
        assertTrue("muscle listed twice: $both", both.isEmpty())
    }
}
