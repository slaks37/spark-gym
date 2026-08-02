package com.sparkgym.domain.engine

import com.sparkgym.domain.model.Muscle

/**
 * What to show when someone taps a muscle on the body.
 *
 * Pure functions over the exercise/muscle join, so the answer the panel gives
 * is unit-testable rather than something you can only check by tapping around
 * on a phone. The ViewModel adapts the database rows onto [Item] and delegates.
 */
object MusclePanel {

    /** The minimum a caller needs to know about an exercise to rank it here. */
    interface Item {
        /** Contribution of [muscle]: 1.0 prime mover, between 0 and 1 synergist, null untouched. */
        fun contributionTo(muscle: Muscle): Float?

        /** True when it needs nothing but a floor and a wall. */
        val homeFriendly: Boolean
    }

    /**
     * Movements where this muscle leads.
     *
     * Strictly the join table. There is deliberately no "if empty, show
     * something" fallback: the panel answers "what trains this", and filling a
     * gap with unrelated exercises turns an honest blank into a wrong training
     * decision.
     */
    fun <T : Item> primary(muscle: Muscle, all: List<T>): List<T> =
        interleaveKit(all.filter { (it.contributionTo(muscle) ?: 0f) >= 1f })

    /** Movements where it assists rather than leads. */
    fun <T : Item> secondary(muscle: Muscle, all: List<T>): List<T> =
        interleaveKit(all.filter { val c = it.contributionTo(muscle) ?: 0f; c > 0f && c < 1f })

    /**
     * Alternates home-friendly and equipment options.
     *
     * Whatever the natural order is, it tends to group one kind together — so
     * someone training in a bedroom scrolls past six barbell lifts before
     * reaching a push-up. Alternating means the first two cards always cover
     * both cases, and neither kind can be buried by the other.
     */
    fun <T : Item> interleaveKit(matches: List<T>): List<T> {
        val (home, gym) = matches.partition { it.homeFriendly }
        val out = ArrayList<T>(matches.size)
        val a = home.iterator()
        val b = gym.iterator()
        while (a.hasNext() || b.hasNext()) {
            if (a.hasNext()) out += a.next()
            if (b.hasNext()) out += b.next()
        }
        return out
    }
}
