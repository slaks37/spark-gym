package com.sparkgym.domain.engine

import com.sparkgym.domain.model.Equipment
import com.sparkgym.domain.model.Muscle
import com.sparkgym.domain.model.MuscleGroup

/**
 * Library search.
 *
 * Searching by exercise name only works if you already know the names, which is
 * exactly the knowledge a beginner does not have. Someone who wants to train
 * their chest types "dada", not "incline dumbbell press" — so a query is matched
 * against the muscles a movement trains, in both languages and in gym slang, as
 * well as against its name and its equipment.
 *
 * Pure functions over plain data: no Android, no database, fully unit-tested.
 */
object ExerciseSearch {

    /** Where a result matched, so the UI can explain itself. */
    enum class MatchKind { NAME, MUSCLE, GROUP, EQUIPMENT }

    data class Candidate(
        val name: String,
        val equipment: Equipment,
        val primary: Set<Muscle>,
        val secondary: Set<Muscle>
    ) {
        val allMuscles: Set<Muscle> get() = primary + secondary
    }

    /**
     * Ranked so the obvious answer is first: a name hit beats a prime-mover hit,
     * which beats a synergist hit. Returns null when nothing matched.
     */
    fun score(query: String, candidate: Candidate): Int? {
        val q = normalise(query)
        if (q.isEmpty()) return 0

        // Every word has to land somewhere, so "dada dumbbell" narrows rather
        // than widens — two-word queries are how people actually filter.
        val terms = q.split(' ').filter { it.isNotBlank() }
        var total = 0
        for (term in terms) {
            val best = scoreTerm(term, candidate) ?: return null
            total += best
        }
        return total
    }

    private fun scoreTerm(term: String, c: Candidate): Int? {
        val name = normalise(c.name)
        // Only a whole-name match is immune to the demotion below; a prefix is
        // not, because "Chest-Supported Row" starts with "chest" too.
        val exact = if (name == term) 100 else null
        val partial = when {
            exact != null -> null
            name.startsWith(term) -> 80
            name.contains(term) -> 60
            else -> null
        }

        val muscleScore = when {
            c.primary.any { it.matches(term) } -> 50
            c.secondary.any { it.matches(term) } -> 25
            c.allMuscles.any { it.group.matches(term) } -> 20
            else -> null
        }
        val equipmentScore = if (c.equipment.matches(term)) 15 else null

        // A coincidental name match must not outrank the real thing. "Chest-
        // Supported Row" contains "chest" but is a back exercise — the chest in
        // its name is the bench pad. Someone searching "chest" should still be
        // able to find it, but never above actual chest work, so demote a bare
        // substring hit when the term names a muscle this movement does not train.
        val demote = partial != null && muscleScore == null && namesAMuscle(term)
        val nameScore = exact ?: if (demote) 30 else partial

        return listOfNotNull(nameScore, muscleScore, equipmentScore).maxOrNull()
    }

    private fun namesAMuscle(term: String): Boolean = Muscle.entries.any { it.matches(term) }

    fun matches(query: String, candidate: Candidate): Boolean = score(query, candidate) != null

    /**
     * Muscles a free-text query names, so the UI can offer "did you mean Chest?"
     * and turn a typed word into a real filter chip.
     */
    fun musclesFor(query: String): List<Muscle> {
        val q = normalise(query)
        if (q.isEmpty()) return emptyList()
        return Muscle.entries.filter { m -> q.split(' ').any { m.matches(it) } }
    }

    fun groupsFor(query: String): List<MuscleGroup> {
        val q = normalise(query)
        if (q.isEmpty()) return emptyList()
        return MuscleGroup.entries.filter { g -> q.split(' ').any { g.matches(it) } }
    }

    // ------------------------------------------------------------------

    private fun Muscle.matches(term: String): Boolean =
        normalise(displayName).contains(term) ||
            normalise(nameId).contains(term) ||
            normalise(name).contains(term) ||
            aliases.any { normalise(it).contains(term) }

    private fun MuscleGroup.matches(term: String): Boolean =
        normalise(displayName) == term || normalise(nameId) == term

    private fun Equipment.matches(term: String): Boolean =
        normalise(displayName).contains(term) || normalise(name).contains(term)

    /** Lower-cased, punctuation flattened, so "pull-up" and "pull up" agree. */
    private fun normalise(value: String): String =
        value.lowercase().replace('-', ' ').replace('_', ' ').trim().replace(Regex("\\s+"), " ")
}
