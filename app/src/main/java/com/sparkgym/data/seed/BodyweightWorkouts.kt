package com.sparkgym.data.seed

import com.sparkgym.domain.model.BodyweightCircuit
import com.sparkgym.domain.model.BodyweightFocus as Focus
import com.sparkgym.domain.model.CircuitMove

/**
 * The bundled circuits.
 *
 * Every rep count here is the **Level II** prescription; [com.sparkgym.domain.engine.BodyweightEngine]
 * scales it down for Level I and up for Level III, so one definition serves a
 * first week and a hard session a year later.
 *
 * The set is deliberately broad rather than deep: a session for a small room, a
 * session for a bad knee, a session that makes no noise, one that needs eight
 * minutes and one that needs half an hour. The gap most home training hits is
 * "none of these fit today", not "not enough workouts".
 */
object BodyweightWorkouts {

    val circuits: List<BodyweightCircuit> = listOf(

        // ------------------------------------------------------- full body
        BodyweightCircuit(
            slug = "groundwork",
            name = "Groundwork",
            tagline = "The first session. Nothing clever, nothing you cannot finish.",
            focus = Focus.FULL_BODY,
            restSeconds = 75,
            moves = listOf(
                CircuitMove("incline-push-up", reps = 10),
                CircuitMove("chair-squat", reps = 12),
                CircuitMove("glute-bridge", reps = 15),
                CircuitMove("bird-dog", reps = 8, note = "Per side"),
                CircuitMove("plank", seconds = 30)
            )
        ),
        BodyweightCircuit(
            slug = "full-house",
            name = "Full House",
            tagline = "Push, pull, squat, hinge, carry the ribs — one of each, no gaps.",
            focus = Focus.FULL_BODY,
            restSeconds = 60,
            moves = listOf(
                CircuitMove("push-up", reps = 12),
                CircuitMove("doorway-row", reps = 12),
                CircuitMove("bodyweight-squat", reps = 20),
                CircuitMove("good-morning-bodyweight", reps = 15),
                CircuitMove("hollow-hold", seconds = 30),
                CircuitMove("reverse-lunge-bodyweight", reps = 10, note = "Per side")
            )
        ),
        BodyweightCircuit(
            slug = "fifteen-no-excuses",
            name = "Fifteen, No Excuses",
            tagline = "Four moves. Set a timer. It is over before you can talk yourself out of it.",
            focus = Focus.FULL_BODY,
            restSeconds = 45,
            moves = listOf(
                CircuitMove("push-up", reps = 15),
                CircuitMove("bodyweight-squat", reps = 25),
                CircuitMove("mountain-climber", seconds = 40),
                CircuitMove("sit-up", reps = 20)
            )
        ),
        BodyweightCircuit(
            slug = "the-long-way",
            name = "The Long Way Round",
            tagline = "Eight moves, low rest. The session for a day with time and a grudge.",
            focus = Focus.FULL_BODY,
            restSeconds = 50,
            moves = listOf(
                CircuitMove("burpee", reps = 10),
                CircuitMove("wide-push-up", reps = 12),
                CircuitMove("split-squat", reps = 10, note = "Per side"),
                CircuitMove("doorway-row", reps = 12),
                CircuitMove("single-leg-glute-bridge", reps = 10, note = "Per side"),
                CircuitMove("plank-up-down", reps = 10),
                CircuitMove("v-up", reps = 12),
                CircuitMove("skater-hop", reps = 16)
            )
        ),

        // ------------------------------------------------------------ core
        BodyweightCircuit(
            slug = "core-lockdown",
            name = "Core Lockdown",
            tagline = "All holds. Nothing moves, everything burns.",
            focus = Focus.CORE,
            restSeconds = 45,
            moves = listOf(
                CircuitMove("plank", seconds = 45),
                CircuitMove("side-plank", seconds = 30, note = "Per side"),
                CircuitMove("hollow-hold", seconds = 30),
                CircuitMove("superman", seconds = 30),
                CircuitMove("wall-sit", seconds = 45)
            )
        ),
        BodyweightCircuit(
            slug = "abs-express",
            name = "Abs Express",
            tagline = "Six minutes on the floor, start to finish.",
            focus = Focus.CORE,
            restSeconds = 30,
            moves = listOf(
                CircuitMove("crunch", reps = 20),
                CircuitMove("reverse-crunch", reps = 15),
                CircuitMove("flutter-kick", seconds = 30),
                CircuitMove("plank", seconds = 40)
            )
        ),
        BodyweightCircuit(
            slug = "oblique-tax",
            name = "The Oblique Tax",
            tagline = "Rotation and anti-rotation — the half of the core most sessions skip.",
            focus = Focus.CORE,
            restSeconds = 40,
            moves = listOf(
                CircuitMove("bicycle-crunch", reps = 20),
                CircuitMove("russian-twist", reps = 20),
                CircuitMove("side-plank-dip", reps = 10, note = "Per side"),
                CircuitMove("heel-tap", reps = 20),
                CircuitMove("thoracic-rotation", reps = 8, note = "Per side")
            )
        ),
        BodyweightCircuit(
            slug = "midline-armor",
            name = "Midline Armour",
            tagline = "Harder holds and slower reps. Earn this one.",
            focus = Focus.CORE,
            restSeconds = 50,
            moves = listOf(
                CircuitMove("hollow-rock", reps = 15),
                CircuitMove("v-up", reps = 15),
                CircuitMove("leg-raise-floor", reps = 12),
                CircuitMove("plank-knee-tuck", reps = 12, note = "Slow"),
                CircuitMove("side-plank", seconds = 45, note = "Per side"),
                CircuitMove("dead-bug", reps = 12)
            )
        ),

        // ------------------------------------------------------ upper body
        BodyweightCircuit(
            slug = "floor-press-day",
            name = "Push Day, Floor Edition",
            tagline = "Chest, shoulders and triceps with nothing but the ground.",
            focus = Focus.UPPER,
            restSeconds = 70,
            moves = listOf(
                CircuitMove("push-up", reps = 12),
                CircuitMove("pike-push-up", reps = 10),
                CircuitMove("bench-dip", reps = 12),
                CircuitMove("diamond-push-up", reps = 8),
                CircuitMove("shoulder-tap", reps = 20)
            )
        ),
        BodyweightCircuit(
            slug = "pull-without-a-bar",
            name = "Pull Without a Bar",
            tagline = "Back work for a flat with no pull-up bar. A door frame and a towel will do.",
            focus = Focus.UPPER,
            restSeconds = 60,
            moves = listOf(
                CircuitMove("doorway-row", reps = 12),
                CircuitMove("prone-y-raise", reps = 12),
                CircuitMove("reverse-snow-angel", reps = 15),
                CircuitMove("superman", seconds = 30),
                CircuitMove("towel-curl-isometric", reps = 10, note = "Per side, 3 s each way")
            )
        ),
        BodyweightCircuit(
            slug = "shoulder-forge",
            name = "Shoulder Forge",
            tagline = "Overhead strength built from the wall up.",
            focus = Focus.UPPER,
            restSeconds = 75,
            moves = listOf(
                CircuitMove("pike-push-up", reps = 10),
                CircuitMove("wall-handstand-hold", seconds = 25),
                CircuitMove("hindu-push-up", reps = 8),
                CircuitMove("wall-slide", reps = 12),
                CircuitMove("plank-up-down", reps = 10)
            )
        ),

        // ------------------------------------------------------ lower body
        BodyweightCircuit(
            slug = "leg-day-no-plates",
            name = "Leg Day, No Plates",
            tagline = "Volume replaces load. Your quads will not notice the difference.",
            focus = Focus.LOWER,
            restSeconds = 70,
            moves = listOf(
                CircuitMove("bodyweight-squat", reps = 25),
                CircuitMove("reverse-lunge-bodyweight", reps = 12, note = "Per side"),
                CircuitMove("wall-sit", seconds = 45),
                CircuitMove("good-morning-bodyweight", reps = 15),
                CircuitMove("calf-raise-bodyweight", reps = 25)
            )
        ),
        BodyweightCircuit(
            slug = "glute-drive",
            name = "Glute Drive",
            tagline = "Hips, hinges and the small muscles that keep knees pointing forward.",
            focus = Focus.LOWER,
            restSeconds = 55,
            moves = listOf(
                CircuitMove("glute-bridge", reps = 20),
                CircuitMove("single-leg-glute-bridge", reps = 12, note = "Per side"),
                CircuitMove("donkey-kick", reps = 15, note = "Per side"),
                CircuitMove("fire-hydrant", reps = 15, note = "Per side"),
                CircuitMove("lateral-lunge", reps = 10, note = "Per side")
            )
        ),
        BodyweightCircuit(
            slug = "single-leg-audit",
            name = "Single-leg Audit",
            tagline = "One leg at a time, which is where the honest weakness shows up.",
            focus = Focus.LOWER,
            restSeconds = 75,
            moves = listOf(
                CircuitMove("split-squat", reps = 10, note = "Per side"),
                CircuitMove("step-up-bodyweight", reps = 12, note = "Per side"),
                CircuitMove("cossack-squat", reps = 8, note = "Per side"),
                CircuitMove("single-leg-glute-bridge", reps = 10, note = "Per side"),
                CircuitMove("seated-leg-extension-bw", reps = 15, note = "Per side")
            )
        ),

        // ---------------------------------------------------------- cardio
        BodyweightCircuit(
            slug = "small-room-hiit",
            name = "Small Room HIIT",
            tagline = "Two square metres is enough. Go hard, the rest is short.",
            focus = Focus.CARDIO,
            restSeconds = 40,
            moves = listOf(
                CircuitMove("jumping-jack", reps = 30),
                CircuitMove("mountain-climber", seconds = 40),
                CircuitMove("squat-thrust", reps = 15),
                CircuitMove("high-knees", seconds = 40),
                CircuitMove("plank-jack", seconds = 30)
            )
        ),
        BodyweightCircuit(
            slug = "quiet-cardio",
            name = "Quiet Cardio",
            tagline = "No jumping, no thumping. For late nights and downstairs neighbours.",
            focus = Focus.CARDIO,
            restSeconds = 40,
            moves = listOf(
                CircuitMove("marching-in-place", seconds = 60),
                CircuitMove("squat-thrust", reps = 12, note = "Step, do not jump"),
                CircuitMove("shadow-boxing", seconds = 60),
                CircuitMove("lateral-shuffle", seconds = 40),
                CircuitMove("standing-knee-raise", reps = 15, note = "Per side")
            )
        ),
        BodyweightCircuit(
            slug = "burpee-ladder",
            name = "Burpee Ladder",
            tagline = "One movement. Nowhere to hide.",
            focus = Focus.CARDIO,
            restSeconds = 60,
            moves = listOf(
                CircuitMove("burpee", reps = 12),
                CircuitMove("bodyweight-squat", reps = 20),
                CircuitMove("burpee", reps = 8),
                CircuitMove("push-up", reps = 12)
            )
        ),
        BodyweightCircuit(
            slug = "ten-minute-furnace",
            name = "Ten Minute Furnace",
            tagline = "Plyometric and unkind. Skip it on tired legs.",
            focus = Focus.CARDIO,
            restSeconds = 45,
            moves = listOf(
                CircuitMove("jump-squat", reps = 15),
                CircuitMove("tuck-jump", reps = 10),
                CircuitMove("skater-hop", reps = 20),
                CircuitMove("star-jump", reps = 15),
                CircuitMove("butt-kick", seconds = 40)
            )
        ),

        // ---------------------------------------------------------- combat
        BodyweightCircuit(
            slug = "round-one",
            name = "Round One",
            tagline = "Three minutes of work, a minute to breathe. Boxing's own interval.",
            focus = Focus.COMBAT,
            restSeconds = 60,
            moves = listOf(
                CircuitMove("shadow-boxing", seconds = 60),
                CircuitMove("front-kick", reps = 12, note = "Per side"),
                CircuitMove("knee-strike", reps = 15, note = "Per side"),
                CircuitMove("squat-thrust", reps = 12)
            )
        ),
        BodyweightCircuit(
            slug = "footwork-and-fists",
            name = "Footwork and Fists",
            tagline = "Move first, punch second — the order that keeps you upright.",
            focus = Focus.COMBAT,
            restSeconds = 50,
            moves = listOf(
                CircuitMove("lateral-shuffle", seconds = 45),
                CircuitMove("shadow-boxing", seconds = 60),
                CircuitMove("skater-hop", reps = 20),
                CircuitMove("push-up", reps = 12),
                CircuitMove("russian-twist", reps = 20)
            )
        ),

        // -------------------------------------------------------- mobility
        BodyweightCircuit(
            slug = "desk-undo",
            name = "Desk Undo",
            tagline = "Ten minutes against eight hours of sitting. Not a fair fight, but it helps.",
            focus = Focus.MOBILITY,
            restSeconds = 20,
            moves = listOf(
                CircuitMove("cat-cow", reps = 10),
                CircuitMove("hip-flexor-stretch", seconds = 40, note = "Per side"),
                CircuitMove("thoracic-rotation", reps = 8, note = "Per side"),
                CircuitMove("wall-slide", reps = 12),
                CircuitMove("worlds-greatest-stretch", reps = 6, note = "Per side"),
                CircuitMove("neck-rotation", reps = 8)
            )
        ),
        BodyweightCircuit(
            slug = "evening-unwind",
            name = "Evening Unwind",
            tagline = "Long holds, slow breathing. The session you do before bed.",
            focus = Focus.MOBILITY,
            restSeconds = 15,
            moves = listOf(
                CircuitMove("childs-pose", seconds = 60),
                CircuitMove("downward-dog", seconds = 45),
                CircuitMove("seated-forward-fold", seconds = 60),
                CircuitMove("couch-stretch", seconds = 45, note = "Per side"),
                CircuitMove("hamstring-stretch-standing", seconds = 40, note = "Per side"),
                CircuitMove("ankle-circle", reps = 10, note = "Per side")
            )
        ),

        // ------------------------------------------------------ low impact
        BodyweightCircuit(
            slug = "chair-session",
            name = "The Chair Session",
            tagline = "Done entirely sitting down. Travel days, sick days, desk days.",
            focus = Focus.LOW_IMPACT,
            restSeconds = 45,
            moves = listOf(
                CircuitMove("seated-march", seconds = 60),
                CircuitMove("seated-punch", seconds = 45),
                CircuitMove("seated-leg-extension-bw", reps = 15, note = "Per side"),
                CircuitMove("shoulder-pass-through", reps = 10),
                CircuitMove("neck-rotation", reps = 8)
            )
        ),
        BodyweightCircuit(
            slug = "knee-friendly-strength",
            name = "Knee-friendly Strength",
            tagline = "Real strength work with no jumping and no deep knee bend.",
            focus = Focus.LOW_IMPACT,
            restSeconds = 60,
            moves = listOf(
                CircuitMove("wall-push-up", reps = 15),
                CircuitMove("glute-bridge", reps = 20),
                CircuitMove("chair-squat", reps = 12, note = "To the seat only"),
                CircuitMove("bird-dog", reps = 10, note = "Per side"),
                CircuitMove("prone-y-raise", reps = 12),
                CircuitMove("standing-knee-raise", reps = 12, note = "Per side")
            )
        )
    )

    /** Circuits carry this in [com.sparkgym.data.local.RoutineEntity.goal] so the gym tab can filter them out. */
    const val ROUTINE_GOAL = "Bodyweight"

    fun bySlug(slug: String): BodyweightCircuit? = circuits.firstOrNull { it.slug == slug }
}
