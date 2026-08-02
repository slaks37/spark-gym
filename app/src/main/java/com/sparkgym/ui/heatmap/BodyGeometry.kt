package com.sparkgym.ui.heatmap

import com.sparkgym.domain.model.Muscle

enum class ViewAngle(val label: String, val labelId: String, val degrees: Int) {
    FRONT("Front", "Depan (0°)", 0),
    FRONT_RIGHT("Quarter R", "Serong Kanan (45°)", 45),
    SIDE_RIGHT("Side R", "Samping Kanan (90°)", 90),
    BACK("Back", "Belakang (180°)", 180),
    FRONT_LEFT("Quarter L", "Serong Kiri (270°)", 270);

    fun next(): ViewAngle = when (this) {
        FRONT -> FRONT_RIGHT
        FRONT_RIGHT -> SIDE_RIGHT
        SIDE_RIGHT -> BACK
        BACK -> FRONT_LEFT
        FRONT_LEFT -> FRONT
    }

    fun previous(): ViewAngle = when (this) {
        FRONT -> FRONT_LEFT
        FRONT_LEFT -> BACK
        BACK -> SIDE_RIGHT
        SIDE_RIGHT -> FRONT_RIGHT
        FRONT_RIGHT -> FRONT
    }
}

/**
 * Hand-plotted body geometry across 5 viewing angles (0°, 45°, 90°, 180°, 270°).
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
    // Silhouettes for all 5 angles
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

    val silhouetteSide: List<Poly> = listOf(
        // head
        poly(50f to 4f, 60f to 8f, 62f to 17f, 54f to 25f, 44f to 23f, 44f to 12f),
        // neck
        poly(44f to 23f, 54f to 25f, 56f to 33f, 46f to 33f),
        // torso & back curve
        poly(
            46f to 33f, 62f to 38f, 64f to 55f, 59f to 75f,
            58f to 99f, 41f to 99f, 39f to 75f, 42f to 50f
        ),
        // arm
        poly(50f to 34f, 60f to 42f, 58f to 68f, 52f to 96f, 44f to 96f, 48f to 68f, 46f to 42f),
        // thigh
        poly(41f to 99f, 58f to 99f, 59f to 141f, 43f to 142f),
        // calf
        poly(43f to 142f, 59f to 141f, 57f to 183f, 44f to 184f),
        // foot
        poly(44f to 184f, 65f to 188f, 65f to 192f, 44f to 192f)
    )

    val silhouetteQuarter: List<Poly> = listOf(
        // head
        poly(50f to 4f, 59f to 8f, 61f to 17f, 55f to 25f, 43f to 24f, 41f to 14f),
        // neck
        poly(43f to 24f, 55f to 25f, 56f to 33f, 44f to 33f),
        // torso
        poly(
            38f to 33f, 64f to 34f, 68f to 45f, 65f to 62f,
            63f to 80f, 64f to 99f, 35f to 99f, 36f to 80f, 35f to 62f, 34f to 45f
        ),
        // upper arm
        poly(33f to 34f, 24f to 40f, 21f to 58f, 22f to 71f, 30f to 73f, 32f to 55f),
        poly(64f to 34f, 72f to 40f, 74f to 58f, 73f to 71f, 66f to 73f, 64f to 55f),
        // legs
        poly(36f to 99f, 49f to 99f, 48f to 141f, 38f to 142f),
        poly(49f to 99f, 62f to 99f, 60f to 141f, 49f to 142f),
        poly(38f to 142f, 48f to 141f, 47f to 183f, 39f to 184f),
        poly(49f to 142f, 60f to 141f, 58f to 183f, 48f to 184f)
    )

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

    // ------------------------------------------------------------------
    // Side profile muscles (90°)
    // ------------------------------------------------------------------

    val side: Map<Muscle, List<Poly>> = buildMap {
        put(Muscle.SIDE_DELTS, listOf(poly(47f to 34f, 59f to 42f, 56f to 56f, 46f to 50f)))
        put(Muscle.FRONT_DELTS, listOf(poly(56f to 38f, 62f to 44f, 60f to 54f, 56f to 52f)))
        put(Muscle.REAR_DELTS, listOf(poly(44f to 35f, 48f to 38f, 46f to 52f, 42f to 46f)))
        put(Muscle.CHEST, listOf(poly(58f to 42f, 64f to 50f, 61f to 64f, 56f to 60f)))
        put(Muscle.LATS, listOf(poly(42f to 48f, 52f to 54f, 50f to 80f, 41f to 75f)))
        put(Muscle.OBLIQUES, listOf(poly(52f to 55f, 60f to 58f, 58f to 88f, 50f to 84f)))
        put(Muscle.ABS, listOf(poly(60f to 58f, 64f to 60f, 61f to 86f, 58f to 84f)))
        put(Muscle.TRICEPS, listOf(poly(44f to 50f, 52f to 52f, 50f to 72f, 43f to 70f)))
        put(Muscle.BICEPS, listOf(poly(52f to 52f, 58f to 54f, 55f to 72f, 50f to 70f)))
        put(Muscle.FOREARMS, listOf(poly(44f to 71f, 56f to 72f, 52f to 96f, 43f to 94f)))
        put(Muscle.GLUTES, listOf(poly(39f to 96f, 49f to 96f, 47f to 122f, 40f to 118f)))
        put(Muscle.QUADS, listOf(poly(50f to 99f, 59f to 99f, 58f to 141f, 48f to 142f)))
        put(Muscle.HAMSTRINGS, listOf(poly(41f to 99f, 50f to 99f, 48f to 141f, 42f to 141f)))
        put(Muscle.CALVES, listOf(poly(42f to 142f, 52f to 141f, 49f to 183f, 43f to 184f)))
    }

    /** Every muscle that has geometry on at least one view. */
    val drawable: Set<Muscle> = front.keys + back.keys + side.keys

    fun silhouetteFor(angle: ViewAngle): List<Poly> = when (angle) {
        ViewAngle.FRONT -> silhouetteFront
        ViewAngle.FRONT_RIGHT -> silhouetteQuarter
        ViewAngle.SIDE_RIGHT -> silhouetteSide
        ViewAngle.BACK -> silhouetteBack
        ViewAngle.FRONT_LEFT -> silhouetteQuarter
    }

    fun musclesForAngle(angle: ViewAngle): Map<Muscle, List<Poly>> = when (angle) {
        ViewAngle.FRONT -> front
        ViewAngle.FRONT_RIGHT -> front + side
        ViewAngle.SIDE_RIGHT -> side
        ViewAngle.BACK -> back
        ViewAngle.FRONT_LEFT -> front + side
    }

    fun polysFor(muscle: Muscle, isFront: Boolean): List<Poly> =
        (if (isFront) front else back)[muscle].orEmpty()

    fun muscleAtAngle(x: Float, y: Float, angle: ViewAngle): Muscle? {
        val source = musclesForAngle(angle)
        return source.entries.firstOrNull { (_, polys) ->
            polys.any { contains(it, x, y) }
        }?.key
    }

    fun muscleAt(x: Float, y: Float, isFront: Boolean): Muscle? =
        muscleAtAngle(x, y, if (isFront) ViewAngle.FRONT else ViewAngle.BACK)

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
