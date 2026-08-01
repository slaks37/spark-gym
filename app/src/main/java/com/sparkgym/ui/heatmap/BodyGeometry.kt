package com.sparkgym.ui.heatmap

import com.sparkgym.domain.model.Muscle

/**
 * Hand-plotted body geometry, in a 100 × 200 coordinate space that the drawing
 * code scales to whatever the canvas is. Each region is a closed polygon; a
 * muscle can own several (left and right sides, mostly).
 *
 * These are stylised rather than anatomically exact — the goal is that a chest
 * day and a back day are instantly distinguishable at thumbnail size.
 */
object BodyGeometry {

    const val VIEW_WIDTH = 100f
    const val VIEW_HEIGHT = 200f

    data class Poly(val points: List<Pair<Float, Float>>)

    private fun poly(vararg pts: Pair<Float, Float>) = Poly(pts.toList())

    /** Reflects a polygon across the vertical centre line. */
    private fun Poly.mirrored(): Poly = Poly(points.map { (x, y) -> (VIEW_WIDTH - x) to y })

    private fun pair(p: Poly): List<Poly> = listOf(p, p.mirrored())

    // ------------------------------------------------------------------
    // Base silhouette — drawn underneath the muscles so untrained regions
    // still read as part of a body rather than floating shapes.
    // ------------------------------------------------------------------

    val silhouetteFront: List<Poly> = listOf(
        // head
        poly(50f to 4f, 58f to 8f, 60f to 17f, 56f to 25f, 44f to 25f, 40f to 17f, 42f to 8f),
        // neck
        poly(44f to 24f, 56f to 24f, 57f to 33f, 43f to 33f),
        // torso
        poly(
            35f to 33f, 65f to 33f, 69f to 45f, 67f to 62f,
            66f to 80f, 67f to 99f, 33f to 99f, 34f to 80f, 33f to 62f, 31f to 45f
        )
    ) + pair(poly(32f to 34f, 22f to 40f, 19f to 58f, 20f to 71f, 29f to 73f, 31f to 55f)) +   // upper arm
        pair(poly(29f to 72f, 20f to 70f, 15f to 96f, 23f to 100f)) +                          // forearm
        pair(poly(23f to 100f, 15f to 96f, 14f to 106f, 22f to 108f)) +                        // hand
        pair(poly(34f to 99f, 49f to 99f, 48f to 141f, 37f to 142f)) +                         // thigh
        pair(poly(37f to 142f, 48f to 141f, 47f to 183f, 39f to 184f)) +                       // shin
        pair(poly(39f to 184f, 47f to 183f, 48f to 191f, 37f to 192f))                         // foot

    val silhouetteBack: List<Poly> = silhouetteFront

    // ------------------------------------------------------------------
    // Front muscles
    // ------------------------------------------------------------------

    val front: Map<Muscle, List<Poly>> = buildMap {
        put(Muscle.NECK, listOf(poly(45f to 25f, 55f to 25f, 55f to 32f, 45f to 32f)))
        put(
            Muscle.TRAPS,
            listOf(poly(43f to 26f, 57f to 26f, 66f to 35f, 58f to 38f, 50f to 34f, 42f to 38f, 34f to 35f))
        )
        put(Muscle.FRONT_DELTS, pair(poly(33f to 34f, 25f to 39f, 24f to 52f, 31f to 55f, 34f to 45f)))
        put(Muscle.SIDE_DELTS, pair(poly(24f to 38f, 20f to 48f, 20f to 62f, 26f to 60f, 26f to 45f)))
        put(
            Muscle.CHEST,
            pair(poly(36f to 37f, 49f to 40f, 49f to 58f, 39f to 58f, 34f to 49f))
        )
        put(Muscle.ABS, listOf(poly(42f to 59f, 58f to 59f, 58f to 90f, 50f to 96f, 42f to 90f)))
        put(Muscle.OBLIQUES, pair(poly(35f to 60f, 42f to 60f, 42f to 90f, 37f to 88f, 33f to 74f)))
        put(Muscle.BICEPS, pair(poly(26f to 50f, 33f to 51f, 32f to 70f, 24f to 69f)))
        put(Muscle.FOREARMS, pair(poly(24f to 71f, 32f to 72f, 28f to 98f, 19f to 95f)))
        put(Muscle.QUADS, pair(poly(35f to 100f, 45f to 100f, 44f to 139f, 37f to 140f)))
        put(Muscle.ADDUCTORS, pair(poly(45f to 100f, 50f to 100f, 49f to 132f, 44f to 130f)))
    }

    // ------------------------------------------------------------------
    // Back muscles
    // ------------------------------------------------------------------

    val back: Map<Muscle, List<Poly>> = buildMap {
        put(Muscle.NECK, listOf(poly(44f to 25f, 56f to 25f, 56f to 33f, 44f to 33f)))
        put(
            Muscle.TRAPS,
            listOf(poly(43f to 27f, 57f to 27f, 66f to 36f, 60f to 58f, 50f to 65f, 40f to 58f, 34f to 36f))
        )
        put(Muscle.REAR_DELTS, pair(poly(33f to 34f, 24f to 40f, 22f to 53f, 30f to 56f, 35f to 45f)))
        put(Muscle.LATS, pair(poly(35f to 52f, 47f to 62f, 47f to 84f, 38f to 88f, 32f to 70f)))
        put(Muscle.LOWER_BACK, listOf(poly(41f to 80f, 59f to 80f, 61f to 97f, 39f to 97f)))
        put(Muscle.TRICEPS, pair(poly(25f to 49f, 33f to 50f, 32f to 71f, 23f to 70f)))
        put(Muscle.FOREARMS, pair(poly(24f to 71f, 32f to 72f, 28f to 98f, 19f to 95f)))
        put(Muscle.GLUTES, pair(poly(37f to 97f, 50f to 97f, 50f to 121f, 38f to 118f)))
        put(Muscle.ABDUCTORS, pair(poly(32f to 98f, 38f to 98f, 38f to 117f, 31f to 113f)))
        put(Muscle.HAMSTRINGS, pair(poly(37f to 121f, 48f to 121f, 47f to 150f, 38f to 151f)))
        put(Muscle.CALVES, pair(poly(38f to 153f, 48f to 152f, 47f to 179f, 39f to 180f)))
    }

    /** Every muscle that has geometry on at least one view. */
    val drawable: Set<Muscle> = front.keys + back.keys

    fun polysFor(muscle: Muscle, isFront: Boolean): List<Poly> =
        (if (isFront) front else back)[muscle].orEmpty()

    /**
     * Hit testing for tap-to-inspect. Standard ray-casting point-in-polygon,
     * checked against every muscle on the visible side.
     */
    fun muscleAt(x: Float, y: Float, isFront: Boolean): Muscle? {
        val source = if (isFront) front else back
        return source.entries.firstOrNull { (_, polys) ->
            polys.any { contains(it, x, y) }
        }?.key
    }

    private fun contains(poly: Poly, x: Float, y: Float): Boolean {
        val pts = poly.points
        var inside = false
        var j = pts.size - 1
        for (i in pts.indices) {
            val (xi, yi) = pts[i]
            val (xj, yj) = pts[j]
            if ((yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) {
                inside = !inside
            }
            j = i
        }
        return inside
    }
}
