package com.sparkgym.data.seed

data class SeedPrescription(
    val exerciseSlug: String,
    val sets: Int,
    val repsMin: Int,
    val repsMax: Int,
    val restSeconds: Int,
    val notes: String = ""
)

data class SeedDay(
    val name: String,
    val focus: String,
    val exercises: List<SeedPrescription>
)

data class SeedRoutine(
    val slug: String,
    val name: String,
    val description: String,
    val goal: String,
    val level: String,
    val homeFriendly: Boolean,
    val days: List<SeedDay>
)

/**
 * Bundled programmes. Two gym splits, one home/no-equipment plan, one pure
 * conditioning block — enough that a new user never faces an empty screen.
 */
object RoutineSeed {

    /** Starter plans plus the competitive shelf in RoutineSeedPro. */
    val routines: List<SeedRoutine> get() = starter + RoutineSeedPro.routines

    private val starter: List<SeedRoutine> = listOf(

        SeedRoutine(
            slug = "push-pull-legs",
            name = "Push / Pull / Legs",
            description = "The six-day classic. Each muscle gets hit twice a week with enough volume to grow and enough rest to recover.",
            goal = "Hypertrophy",
            level = "Intermediate",
            homeFriendly = false,
            days = listOf(
                SeedDay("Push A", "Chest, shoulders, triceps", listOf(
                    SeedPrescription("barbell-bench-press", 4, 5, 8, 180, "Top set then back-offs"),
                    SeedPrescription("dumbbell-shoulder-press", 3, 8, 12, 120),
                    SeedPrescription("incline-dumbbell-press", 3, 8, 12, 120),
                    SeedPrescription("lateral-raise", 4, 12, 20, 60),
                    SeedPrescription("triceps-pushdown", 3, 10, 15, 60),
                    SeedPrescription("overhead-triceps-extension", 3, 10, 15, 60)
                )),
                SeedDay("Pull A", "Back and biceps", listOf(
                    SeedPrescription("pull-up", 4, 5, 10, 180, "Add weight once you hit 10"),
                    SeedPrescription("barbell-row", 4, 6, 10, 150),
                    SeedPrescription("seated-cable-row", 3, 10, 12, 90),
                    SeedPrescription("face-pull", 3, 15, 20, 60),
                    SeedPrescription("barbell-curl", 3, 8, 12, 60),
                    SeedPrescription("hammer-curl", 3, 10, 15, 60)
                )),
                SeedDay("Legs A", "Quad focused", listOf(
                    SeedPrescription("back-squat", 4, 5, 8, 210),
                    SeedPrescription("romanian-deadlift", 3, 8, 10, 150),
                    SeedPrescription("leg-press", 3, 10, 15, 120),
                    SeedPrescription("leg-curl", 3, 10, 15, 90),
                    SeedPrescription("standing-calf-raise", 4, 10, 15, 60),
                    SeedPrescription("hanging-leg-raise", 3, 8, 15, 60)
                )),
                SeedDay("Push B", "Shoulder led", listOf(
                    SeedPrescription("overhead-press", 4, 5, 8, 180),
                    SeedPrescription("incline-barbell-press", 3, 8, 10, 150),
                    SeedPrescription("cable-crossover", 3, 12, 15, 60),
                    SeedPrescription("cable-lateral-raise", 4, 12, 20, 60),
                    SeedPrescription("skull-crusher", 3, 10, 12, 75),
                    SeedPrescription("dips-triceps", 3, 8, 12, 90)
                )),
                SeedDay("Pull B", "Width and rear delts", listOf(
                    SeedPrescription("lat-pulldown", 4, 8, 12, 120),
                    SeedPrescription("t-bar-row", 4, 8, 12, 120),
                    SeedPrescription("straight-arm-pulldown", 3, 12, 15, 60),
                    SeedPrescription("rear-delt-fly", 4, 15, 20, 45),
                    SeedPrescription("incline-dumbbell-curl", 3, 10, 12, 60),
                    SeedPrescription("barbell-shrug", 3, 10, 15, 60)
                )),
                SeedDay("Legs B", "Posterior chain", listOf(
                    SeedPrescription("deadlift", 3, 3, 5, 240, "Leave one rep in reserve"),
                    SeedPrescription("bulgarian-split-squat", 3, 8, 12, 120),
                    SeedPrescription("hip-thrust", 3, 10, 12, 120),
                    SeedPrescription("nordic-curl", 3, 5, 8, 120),
                    SeedPrescription("seated-calf-raise", 4, 12, 20, 45),
                    SeedPrescription("cable-crunch", 3, 12, 15, 60)
                ))
            )
        ),

        SeedRoutine(
            slug = "upper-lower-4",
            name = "Upper / Lower — 4 Day",
            description = "Four sessions a week, the best strength-to-time ratio there is. Heavy compound first, accessories after.",
            goal = "Strength & size",
            level = "Beginner",
            homeFriendly = false,
            days = listOf(
                SeedDay("Upper Power", "Heavy pressing and rowing", listOf(
                    SeedPrescription("barbell-bench-press", 4, 4, 6, 210),
                    SeedPrescription("barbell-row", 4, 5, 8, 180),
                    SeedPrescription("overhead-press", 3, 6, 8, 150),
                    SeedPrescription("lat-pulldown", 3, 8, 12, 120),
                    SeedPrescription("dumbbell-curl", 3, 10, 12, 60),
                    SeedPrescription("triceps-pushdown", 3, 10, 12, 60)
                )),
                SeedDay("Lower Power", "Squat and hinge", listOf(
                    SeedPrescription("back-squat", 4, 4, 6, 240),
                    SeedPrescription("romanian-deadlift", 3, 6, 8, 180),
                    SeedPrescription("leg-press", 3, 10, 12, 120),
                    SeedPrescription("leg-curl", 3, 10, 15, 90),
                    SeedPrescription("standing-calf-raise", 4, 10, 15, 60),
                    SeedPrescription("plank", 3, 1, 1, 60, "45 to 90 seconds per set")
                )),
                SeedDay("Upper Hypertrophy", "Volume and pump", listOf(
                    SeedPrescription("incline-dumbbell-press", 4, 8, 12, 120),
                    SeedPrescription("seated-cable-row", 4, 10, 12, 90),
                    SeedPrescription("dumbbell-shoulder-press", 3, 10, 12, 90),
                    SeedPrescription("cable-crossover", 3, 12, 15, 60),
                    SeedPrescription("face-pull", 3, 15, 20, 45),
                    SeedPrescription("hammer-curl", 3, 12, 15, 45)
                )),
                SeedDay("Lower Hypertrophy", "Glutes and quads", listOf(
                    SeedPrescription("front-squat", 3, 8, 10, 150),
                    SeedPrescription("hip-thrust", 4, 10, 12, 120),
                    SeedPrescription("walking-lunge", 3, 10, 12, 90),
                    SeedPrescription("leg-extension", 3, 12, 15, 60),
                    SeedPrescription("seated-calf-raise", 4, 12, 20, 45),
                    SeedPrescription("hanging-leg-raise", 3, 10, 15, 60)
                ))
            )
        ),

        SeedRoutine(
            slug = "home-no-equipment",
            name = "Home Gym — Zero Equipment",
            description = "Nothing but the floor and a wall. Progression comes from tempo, range and reps rather than plates.",
            goal = "Conditioning & muscle",
            level = "Beginner",
            homeFriendly = true,
            days = listOf(
                SeedDay("Full Body A", "Push dominant", listOf(
                    SeedPrescription("push-up", 4, 8, 20, 90),
                    SeedPrescription("bodyweight-squat", 4, 15, 25, 90),
                    SeedPrescription("pike-push-up", 3, 6, 12, 75),
                    SeedPrescription("bench-dip", 3, 10, 20, 60),
                    SeedPrescription("plank", 3, 1, 1, 60, "Hold 45 to 90 seconds"),
                    SeedPrescription("glute-bridge", 3, 15, 25, 60)
                )),
                SeedDay("Full Body B", "Pull and posterior", listOf(
                    SeedPrescription("inverted-row", 4, 8, 15, 90),
                    SeedPrescription("walking-lunge", 3, 12, 20, 90),
                    SeedPrescription("superman", 3, 1, 1, 45, "Hold 30 to 45 seconds"),
                    SeedPrescription("reverse-snow-angel", 3, 12, 20, 45),
                    SeedPrescription("side-plank", 3, 1, 1, 45, "Per side"),
                    SeedPrescription("calf-raise-bodyweight", 4, 20, 30, 45)
                )),
                SeedDay("Full Body C", "Conditioning", listOf(
                    SeedPrescription("burpee", 4, 8, 15, 90),
                    SeedPrescription("mountain-climber", 4, 1, 1, 60, "40 seconds on"),
                    SeedPrescription("jumping-jack", 4, 30, 50, 45),
                    SeedPrescription("high-knees", 4, 1, 1, 45, "30 seconds on"),
                    SeedPrescription("sit-up", 3, 15, 25, 45),
                    SeedPrescription("wall-sit", 3, 1, 1, 60, "Hold to near failure")
                ))
            )
        ),

        SeedRoutine(
            slug = "daily-quest-block",
            name = "The Daily Quest",
            description = "The original. One hundred push-ups, one hundred sit-ups, one hundred squats and a ten kilometre run. Break the sets up however you need to — just finish before midnight.",
            goal = "Discipline",
            level = "Advanced",
            homeFriendly = true,
            days = listOf(
                SeedDay("Daily Quest", "Every single day", listOf(
                    SeedPrescription("push-up", 10, 10, 10, 60, "100 total reps"),
                    SeedPrescription("sit-up", 10, 10, 10, 60, "100 total reps"),
                    SeedPrescription("bodyweight-squat", 10, 10, 10, 60, "100 total reps"),
                    SeedPrescription("outdoor-run", 1, 1, 1, 0, "10 km")
                ))
            )
        )
    )
}
