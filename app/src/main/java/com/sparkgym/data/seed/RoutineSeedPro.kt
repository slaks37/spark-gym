package com.sparkgym.data.seed

/**
 * The competitive shelf: splits that built actual physiques, plus purpose-built
 * strength, cutting and powerbuilding blocks.
 *
 * Each one carries the coaching intent in its description, because a programme
 * without a "why" is just a list of exercises.
 */
object RoutineSeedPro {

    val routines: List<SeedRoutine> = listOf(

        // ------------------------------------------------------------------
        SeedRoutine(
            slug = "golden-era-split",
            name = "Golden Era Split",
            description = "The Oak's six-day double split, trimmed to something a human with a job can run. " +
                "Chest and back together so the antagonists pump against each other, arms trained after " +
                "shoulders while they are already warm, legs on their own because they deserve it. " +
                "High volume — earn it by sleeping and eating.",
            goal = "Mass",
            level = "Advanced",
            homeFriendly = false,
            days = listOf(
                SeedDay("Chest & Back A", "Superset the pairs if you can", listOf(
                    SeedPrescription("barbell-bench-press", 5, 6, 10, 120, "Pyramid up: 15, 12, 10, 8, 6"),
                    SeedPrescription("pull-up", 5, 8, 12, 120, "Superset with the bench"),
                    SeedPrescription("incline-barbell-press", 4, 8, 12, 90),
                    SeedPrescription("barbell-row", 4, 8, 12, 90, "Superset with the incline"),
                    SeedPrescription("dumbbell-fly", 3, 12, 15, 60),
                    SeedPrescription("dumbbell-pullover", 3, 12, 15, 60, "Superset — this is the ribcage work"),
                    SeedPrescription("dips-chest", 3, 10, 15, 60)
                )),
                SeedDay("Shoulders & Arms A", "Delts first, then the guns", listOf(
                    SeedPrescription("overhead-press", 5, 6, 10, 120),
                    SeedPrescription("lateral-raise", 4, 12, 20, 45, "Strict — no swinging"),
                    SeedPrescription("rear-delt-fly", 4, 15, 20, 45),
                    SeedPrescription("barbell-curl", 4, 8, 12, 60),
                    SeedPrescription("close-grip-bench", 4, 8, 12, 60, "Superset with the curl"),
                    SeedPrescription("incline-dumbbell-curl", 3, 10, 12, 45),
                    SeedPrescription("overhead-cable-extension", 3, 12, 15, 45),
                    SeedPrescription("wrist-curl", 3, 15, 25, 30)
                )),
                SeedDay("Legs A", "Nothing here is optional", listOf(
                    SeedPrescription("back-squat", 5, 6, 12, 180, "Pyramid: 12, 10, 8, 8, 6"),
                    SeedPrescription("leg-press", 4, 12, 15, 120),
                    SeedPrescription("romanian-deadlift", 4, 8, 12, 120),
                    SeedPrescription("leg-extension", 3, 15, 20, 60),
                    SeedPrescription("seated-leg-curl", 3, 12, 15, 60),
                    SeedPrescription("donkey-calf-raise", 5, 15, 20, 45, "Arnold's favourite — full stretch every rep")
                )),
                SeedDay("Chest & Back B", "Same pattern, different angles", listOf(
                    SeedPrescription("incline-dumbbell-press", 4, 8, 12, 90),
                    SeedPrescription("t-bar-row", 4, 8, 12, 90),
                    SeedPrescription("pec-deck", 4, 12, 15, 60),
                    SeedPrescription("neutral-grip-pulldown", 4, 10, 12, 60),
                    SeedPrescription("low-to-high-cable-fly", 3, 15, 20, 45),
                    SeedPrescription("seal-row", 3, 10, 12, 60),
                    SeedPrescription("straight-arm-pulldown", 3, 12, 15, 45)
                )),
                SeedDay("Shoulders & Arms B", "Volume day", listOf(
                    SeedPrescription("arnold-press", 4, 10, 12, 90),
                    SeedPrescription("cable-lateral-raise", 4, 15, 20, 45),
                    SeedPrescription("reverse-pec-deck", 4, 15, 20, 45),
                    SeedPrescription("preacher-curl", 4, 10, 12, 60),
                    SeedPrescription("skull-crusher", 4, 10, 12, 60),
                    SeedPrescription("concentration-curl", 3, 12, 15, 45),
                    SeedPrescription("rope-pushdown", 3, 15, 20, 45),
                    SeedPrescription("reverse-curl", 3, 12, 15, 45)
                )),
                SeedDay("Legs B", "Posterior chain bias", listOf(
                    SeedPrescription("front-squat", 4, 8, 12, 150),
                    SeedPrescription("bulgarian-split-squat", 4, 10, 12, 90),
                    SeedPrescription("glute-ham-raise", 4, 8, 12, 90),
                    SeedPrescription("hip-thrust", 4, 10, 15, 90),
                    SeedPrescription("sissy-squat", 3, 10, 15, 60),
                    SeedPrescription("standing-calf-raise", 5, 12, 20, 45)
                ))
            )
        ),

        // ------------------------------------------------------------------
        SeedRoutine(
            slug = "blood-and-guts",
            name = "Blood & Guts (HIT)",
            description = "Yates-style high intensity. Two warm-up sets, then ONE working set taken past " +
                "failure — forced reps or a drop set. Total working sets are tiny; the intensity is not. " +
                "Four days a week is the maximum this is survivable at. If you can walk out feeling fine, " +
                "you did not do it right.",
            goal = "Mass on minimal volume",
            level = "Advanced",
            homeFriendly = false,
            days = listOf(
                SeedDay("Chest & Biceps", "One all-out set each", listOf(
                    SeedPrescription("incline-barbell-press", 1, 6, 8, 180, "2 warm-ups first, then one set to failure"),
                    SeedPrescription("machine-chest-press", 1, 8, 10, 180, "Drop set on the last rep"),
                    SeedPrescription("pec-deck", 1, 10, 12, 150, "Hold the squeeze 2s on every rep"),
                    SeedPrescription("barbell-curl", 1, 8, 10, 150),
                    SeedPrescription("incline-dumbbell-curl", 1, 8, 10, 150, "Forced reps at the end")
                )),
                SeedDay("Back & Rear Delts", "The Yates staple", listOf(
                    SeedPrescription("reverse-grip-pulldown", 1, 8, 10, 180),
                    SeedPrescription("barbell-row", 1, 6, 8, 210, "Reverse grip, 45-degree torso"),
                    SeedPrescription("seated-cable-row", 1, 8, 10, 180),
                    SeedPrescription("rear-delt-fly", 1, 10, 12, 150),
                    SeedPrescription("deadlift", 1, 6, 8, 240, "Skip if the lower back is fried")
                )),
                SeedDay("Shoulders & Triceps", "Press then isolate", listOf(
                    SeedPrescription("machine-shoulder-press", 1, 8, 10, 180),
                    SeedPrescription("lateral-raise", 1, 10, 12, 150, "Drop set"),
                    SeedPrescription("cable-upright-row", 1, 10, 12, 150),
                    SeedPrescription("triceps-pushdown", 1, 10, 12, 150),
                    SeedPrescription("skull-crusher", 1, 8, 10, 180, "Forced reps")
                )),
                SeedDay("Legs", "The hard one", listOf(
                    SeedPrescription("leg-extension", 1, 12, 15, 120, "Pre-exhaust the quads first"),
                    SeedPrescription("leg-press", 1, 10, 12, 210),
                    SeedPrescription("hack-squat", 1, 8, 10, 210),
                    SeedPrescription("seated-leg-curl", 1, 10, 12, 150),
                    SeedPrescription("romanian-deadlift", 1, 8, 10, 180),
                    SeedPrescription("standing-calf-raise", 2, 10, 15, 90)
                ))
            )
        ),

        // ------------------------------------------------------------------
        SeedRoutine(
            slug = "fst-7",
            name = "FST-7 Hypertrophy",
            description = "Hany Rambod's fascia-stretch method. Train the muscle heavy as usual, then finish " +
                "with seven sets of 8-12 on an isolation movement with only 30-45 seconds rest, sipping water " +
                "between sets. The goal of the seven is not load — it is the pump stretching the fascia that " +
                "wraps the muscle. The last exercise of every day is the '7'.",
            goal = "Hypertrophy",
            level = "Advanced",
            homeFriendly = false,
            days = listOf(
                SeedDay("Chest & Triceps", "Finish with the 7", listOf(
                    SeedPrescription("incline-dumbbell-press", 4, 8, 12, 90),
                    SeedPrescription("barbell-bench-press", 3, 8, 10, 120),
                    SeedPrescription("machine-chest-press", 3, 10, 12, 75),
                    SeedPrescription("close-grip-bench", 3, 8, 12, 90),
                    SeedPrescription("rope-pushdown", 3, 12, 15, 60),
                    SeedPrescription("pec-deck", 7, 8, 12, 40, "THE SEVEN — 30-45s rest, water between sets")
                )),
                SeedDay("Back", "Width then thickness", listOf(
                    SeedPrescription("lat-pulldown", 4, 10, 12, 90),
                    SeedPrescription("chest-supported-row", 4, 8, 12, 90),
                    SeedPrescription("single-arm-pulldown", 3, 10, 12, 60),
                    SeedPrescription("meadows-row", 3, 10, 12, 75),
                    SeedPrescription("cable-pullover", 7, 10, 12, 40, "THE SEVEN")
                )),
                SeedDay("Shoulders", "Side delts are the priority", listOf(
                    SeedPrescription("machine-shoulder-press", 4, 8, 12, 90),
                    SeedPrescription("arnold-press", 3, 10, 12, 75),
                    SeedPrescription("reverse-pec-deck", 4, 12, 15, 60),
                    SeedPrescription("face-pull", 3, 15, 20, 45),
                    SeedPrescription("cable-lateral-raise", 7, 10, 12, 40, "THE SEVEN")
                )),
                SeedDay("Legs", "Quads led", listOf(
                    SeedPrescription("leg-extension", 4, 12, 15, 60, "Warm the knees before you load them"),
                    SeedPrescription("back-squat", 4, 8, 12, 150),
                    SeedPrescription("leg-press", 4, 12, 15, 120),
                    SeedPrescription("seated-leg-curl", 4, 10, 12, 75),
                    SeedPrescription("romanian-deadlift", 3, 10, 12, 90),
                    SeedPrescription("leg-extension", 7, 10, 12, 40, "THE SEVEN")
                )),
                SeedDay("Arms", "Antagonist supersets", listOf(
                    SeedPrescription("barbell-curl", 4, 8, 12, 75),
                    SeedPrescription("skull-crusher", 4, 8, 12, 75),
                    SeedPrescription("hammer-curl", 3, 10, 12, 60),
                    SeedPrescription("overhead-cable-extension", 3, 10, 12, 60),
                    SeedPrescription("spider-curl", 3, 12, 15, 45),
                    SeedPrescription("cable-curl", 7, 10, 12, 40, "THE SEVEN")
                ))
            )
        ),

        // ------------------------------------------------------------------
        SeedRoutine(
            slug = "gvt-10x10",
            name = "German Volume Training",
            description = "Ten sets of ten on one big lift per pattern, same weight throughout, 90 seconds " +
                "rest. Start at 60% of your 1RM — it will feel insultingly light for four sets and impossible " +
                "by set eight. Run it for four to six weeks and no longer; it is a shock block, not a lifestyle.",
            goal = "Hypertrophy shock block",
            level = "Advanced",
            homeFriendly = false,
            days = listOf(
                SeedDay("Chest & Back", "Alternate the 10x10 pair", listOf(
                    SeedPrescription("barbell-bench-press", 10, 10, 10, 90, "60% of 1RM, same weight all ten"),
                    SeedPrescription("barbell-row", 10, 10, 10, 90, "Alternate sets with the bench"),
                    SeedPrescription("dumbbell-fly", 3, 10, 12, 60),
                    SeedPrescription("straight-arm-pulldown", 3, 10, 12, 60)
                )),
                SeedDay("Legs & Abs", "The one people quit on", listOf(
                    SeedPrescription("back-squat", 10, 10, 10, 90, "60% of 1RM"),
                    SeedPrescription("leg-curl", 10, 10, 10, 90),
                    SeedPrescription("standing-calf-raise", 3, 15, 20, 60),
                    SeedPrescription("ab-crunch-machine", 3, 12, 15, 60)
                )),
                SeedDay("Arms & Shoulders", "Ten and ten", listOf(
                    SeedPrescription("dips-triceps", 10, 10, 10, 90),
                    SeedPrescription("chin-up", 10, 10, 10, 90, "Assisted if needed — all ten sets matter"),
                    SeedPrescription("lateral-raise", 3, 12, 15, 60),
                    SeedPrescription("reverse-pec-deck", 3, 15, 20, 60)
                ))
            )
        ),

        // ------------------------------------------------------------------
        SeedRoutine(
            slug = "strength-5x5",
            name = "Strength Block — 5×5",
            description = "Three days a week, five sets of five on the big three, add 2.5 kg every session " +
                "until you stall twice, then drop 10% and climb again. The most reliable way to add a hundred " +
                "kilos to your total in a year. Boring works.",
            goal = "Strength",
            level = "Beginner",
            homeFriendly = false,
            days = listOf(
                SeedDay("Day A", "Squat led", listOf(
                    SeedPrescription("back-squat", 5, 5, 5, 180, "Add 2.5 kg from last session"),
                    SeedPrescription("barbell-bench-press", 5, 5, 5, 180),
                    SeedPrescription("barbell-row", 5, 5, 5, 150),
                    SeedPrescription("plank", 3, 1, 1, 60, "60s holds"),
                    SeedPrescription("face-pull", 3, 15, 20, 45, "Shoulder insurance")
                )),
                SeedDay("Day B", "Press and pull", listOf(
                    SeedPrescription("back-squat", 5, 5, 5, 180),
                    SeedPrescription("overhead-press", 5, 5, 5, 180),
                    SeedPrescription("deadlift", 1, 5, 5, 240, "One heavy set is enough"),
                    SeedPrescription("chin-up", 3, 5, 10, 120),
                    SeedPrescription("hanging-knee-raise", 3, 10, 15, 60)
                )),
                SeedDay("Day C", "Repeat A with volume back-offs", listOf(
                    SeedPrescription("back-squat", 5, 5, 5, 180),
                    SeedPrescription("barbell-bench-press", 5, 5, 5, 180),
                    SeedPrescription("pendlay-row", 5, 5, 5, 150),
                    SeedPrescription("close-grip-bench", 3, 8, 10, 90),
                    SeedPrescription("barbell-curl", 3, 8, 12, 60)
                ))
            )
        ),

        // ------------------------------------------------------------------
        SeedRoutine(
            slug = "powerbuilding",
            name = "Powerbuilding Hybrid",
            description = "Heavy triples and fives first while you are fresh, hypertrophy work after. You get " +
                "the strength numbers to brag about and the size to back them up. Four days, and the best " +
                "default for anyone who is past the beginner stage and cannot decide what they want.",
            goal = "Strength & size",
            level = "Intermediate",
            homeFriendly = false,
            days = listOf(
                SeedDay("Upper Strength", "Bench focus", listOf(
                    SeedPrescription("barbell-bench-press", 5, 3, 5, 210, "Work up to a heavy triple"),
                    SeedPrescription("weighted-hyperextension", 3, 10, 12, 90),
                    SeedPrescription("chest-supported-row", 4, 8, 10, 90),
                    SeedPrescription("incline-dumbbell-press", 3, 8, 12, 90),
                    SeedPrescription("lateral-raise", 4, 12, 20, 45),
                    SeedPrescription("rope-pushdown", 3, 12, 15, 60)
                )),
                SeedDay("Lower Strength", "Squat focus", listOf(
                    SeedPrescription("back-squat", 5, 3, 5, 240),
                    SeedPrescription("romanian-deadlift", 4, 6, 8, 150),
                    SeedPrescription("leg-press", 3, 10, 15, 120),
                    SeedPrescription("seated-leg-curl", 3, 10, 12, 75),
                    SeedPrescription("standing-calf-raise", 4, 10, 15, 60),
                    SeedPrescription("pallof-press", 3, 1, 1, 45, "30s per side")
                )),
                SeedDay("Upper Hypertrophy", "Pump work", listOf(
                    SeedPrescription("overhead-press", 4, 6, 8, 150),
                    SeedPrescription("neutral-grip-pulldown", 4, 10, 12, 90),
                    SeedPrescription("low-to-high-cable-fly", 3, 12, 15, 60),
                    SeedPrescription("seal-row", 3, 10, 12, 75),
                    SeedPrescription("reverse-pec-deck", 4, 15, 20, 45),
                    SeedPrescription("preacher-curl", 3, 10, 12, 60),
                    SeedPrescription("overhead-cable-extension", 3, 12, 15, 60)
                )),
                SeedDay("Lower Hypertrophy", "Deadlift focus", listOf(
                    SeedPrescription("deadlift", 4, 3, 5, 240),
                    SeedPrescription("hack-squat", 4, 10, 12, 120),
                    SeedPrescription("bulgarian-split-squat", 3, 10, 12, 90),
                    SeedPrescription("glute-ham-raise", 3, 8, 12, 90),
                    SeedPrescription("seated-calf-raise", 4, 12, 20, 45),
                    SeedPrescription("hanging-leg-raise", 3, 10, 15, 60)
                ))
            )
        ),

        // ------------------------------------------------------------------
        SeedRoutine(
            slug = "shred-circuit",
            name = "Shred — Cutting Block",
            description = "Built for a calorie deficit. Compound lifts stay heavy so you keep the muscle you " +
                "spent years building, but rest periods drop and every day finishes with conditioning. " +
                "Do not add more cardio on top — the deficit is doing the work, this is protecting what you have.",
            goal = "Fat loss, muscle retention",
            level = "Intermediate",
            homeFriendly = false,
            days = listOf(
                SeedDay("Push + Conditioning", "Heavy then hot", listOf(
                    SeedPrescription("barbell-bench-press", 4, 6, 8, 120, "Keep the load — the deficit is the diet's job"),
                    SeedPrescription("machine-shoulder-press", 3, 10, 12, 60),
                    SeedPrescription("high-to-low-cable-fly", 3, 12, 15, 45),
                    SeedPrescription("rope-pushdown", 3, 12, 15, 45),
                    SeedPrescription("assault-bike", 1, 1, 1, 0, "10 rounds: 20s hard / 40s easy")
                )),
                SeedDay("Pull + Conditioning", "Rows and rope", listOf(
                    SeedPrescription("pull-up", 4, 6, 10, 120),
                    SeedPrescription("chest-supported-row", 4, 8, 12, 75),
                    SeedPrescription("reverse-pec-deck", 3, 15, 20, 45),
                    SeedPrescription("hammer-curl", 3, 12, 15, 45),
                    SeedPrescription("battle-ropes", 5, 1, 1, 60, "30s on, 60s off")
                )),
                SeedDay("Legs + Sled", "Keep the squat heavy", listOf(
                    SeedPrescription("back-squat", 4, 6, 8, 150),
                    SeedPrescription("romanian-deadlift", 3, 8, 10, 120),
                    SeedPrescription("walking-lunge", 3, 12, 16, 75),
                    SeedPrescription("standing-calf-raise", 4, 12, 20, 45),
                    SeedPrescription("sled-push", 6, 1, 1, 90, "20 m sprints")
                )),
                SeedDay("Full Body Density", "Circuit — minimal rest", listOf(
                    SeedPrescription("goblet-squat", 4, 12, 15, 30),
                    SeedPrescription("push-up", 4, 15, 25, 30),
                    SeedPrescription("inverted-row", 4, 10, 15, 30),
                    SeedPrescription("kettlebell-swing", 4, 15, 20, 30),
                    SeedPrescription("mountain-climber", 4, 1, 1, 30, "40s on"),
                    SeedPrescription("plank", 3, 1, 1, 45, "Hold to failure")
                ))
            )
        ),

        // ------------------------------------------------------------------
        SeedRoutine(
            slug = "bro-split",
            name = "Classic Bro Split",
            description = "One muscle a day, five days a week, everything trained to the ground once and then " +
                "left alone for a week. Frequency purists will tell you it is suboptimal, and per set they are " +
                "right — but it is the most fun split there is, and the programme you actually enjoy is the one " +
                "you still run in a year.",
            goal = "Hypertrophy",
            level = "Intermediate",
            homeFriendly = false,
            days = listOf(
                SeedDay("Chest Day", "Press, press, fly", listOf(
                    SeedPrescription("barbell-bench-press", 4, 6, 10, 120),
                    SeedPrescription("incline-dumbbell-press", 4, 8, 12, 90),
                    SeedPrescription("machine-chest-press", 3, 10, 12, 75),
                    SeedPrescription("pec-deck", 3, 12, 15, 60),
                    SeedPrescription("low-to-high-cable-fly", 3, 15, 20, 45),
                    SeedPrescription("dips-chest", 3, 10, 15, 60)
                )),
                SeedDay("Back Day", "Width, thickness, lats", listOf(
                    SeedPrescription("pull-up", 4, 6, 12, 120),
                    SeedPrescription("barbell-row", 4, 8, 10, 105),
                    SeedPrescription("neutral-grip-pulldown", 4, 10, 12, 75),
                    SeedPrescription("seated-cable-row", 3, 10, 12, 75),
                    SeedPrescription("single-arm-pulldown", 3, 12, 15, 60),
                    SeedPrescription("barbell-shrug", 4, 10, 15, 60)
                )),
                SeedDay("Leg Day", "All of it", listOf(
                    SeedPrescription("back-squat", 5, 6, 12, 180),
                    SeedPrescription("leg-press", 4, 12, 15, 120),
                    SeedPrescription("romanian-deadlift", 4, 8, 12, 120),
                    SeedPrescription("leg-extension", 3, 15, 20, 60),
                    SeedPrescription("seated-leg-curl", 3, 12, 15, 60),
                    SeedPrescription("donkey-calf-raise", 4, 12, 20, 45),
                    SeedPrescription("seated-calf-raise", 3, 15, 20, 45)
                )),
                SeedDay("Shoulder Day", "Three heads, three angles", listOf(
                    SeedPrescription("overhead-press", 4, 6, 10, 120),
                    SeedPrescription("arnold-press", 3, 10, 12, 90),
                    SeedPrescription("cable-lateral-raise", 4, 12, 20, 45),
                    SeedPrescription("lu-raise", 3, 12, 15, 60),
                    SeedPrescription("reverse-pec-deck", 4, 15, 20, 45),
                    SeedPrescription("barbell-shrug", 3, 12, 15, 60)
                )),
                SeedDay("Arm Day", "The reason we are all here", listOf(
                    SeedPrescription("barbell-curl", 4, 8, 12, 75),
                    SeedPrescription("close-grip-bench", 4, 8, 12, 75),
                    SeedPrescription("preacher-curl", 3, 10, 12, 60),
                    SeedPrescription("skull-crusher", 3, 10, 12, 60),
                    SeedPrescription("hammer-curl", 3, 12, 15, 45),
                    SeedPrescription("rope-pushdown", 3, 12, 15, 45),
                    SeedPrescription("twenty-ones", 2, 21, 21, 60, "Log as one set of 21"),
                    SeedPrescription("wrist-curl", 3, 15, 25, 30)
                ))
            )
        ),

        // ------------------------------------------------------------------
        SeedRoutine(
            slug = "full-body-3x",
            name = "Full Body — 3× Week",
            description = "The best programme for anyone with less than two years under the bar, and for anyone " +
                "coming back after time off. Every muscle three times a week, one hour a session, and enough " +
                "recovery between days that you actually progress. Do this before you do anything clever.",
            goal = "General strength & size",
            level = "Beginner",
            homeFriendly = false,
            days = listOf(
                SeedDay("Full Body A", "Squat and press", listOf(
                    SeedPrescription("goblet-squat", 3, 8, 12, 120),
                    SeedPrescription("dumbbell-bench-press", 3, 8, 12, 90),
                    SeedPrescription("seated-cable-row", 3, 10, 12, 90),
                    SeedPrescription("dumbbell-shoulder-press", 3, 10, 12, 75),
                    SeedPrescription("leg-curl", 3, 10, 15, 60),
                    SeedPrescription("plank", 3, 1, 1, 45, "30-60s")
                )),
                SeedDay("Full Body B", "Hinge and pull", listOf(
                    SeedPrescription("romanian-deadlift", 3, 8, 10, 120),
                    SeedPrescription("lat-pulldown", 3, 10, 12, 90),
                    SeedPrescription("incline-dumbbell-press", 3, 10, 12, 90),
                    SeedPrescription("walking-lunge", 3, 10, 12, 75),
                    SeedPrescription("face-pull", 3, 15, 20, 45),
                    SeedPrescription("dead-bug", 3, 10, 12, 45)
                )),
                SeedDay("Full Body C", "Mixed", listOf(
                    SeedPrescription("leg-press", 3, 10, 15, 120),
                    SeedPrescription("chest-supported-row", 3, 10, 12, 90),
                    SeedPrescription("machine-chest-press", 3, 10, 12, 90),
                    SeedPrescription("lateral-raise", 3, 12, 20, 45),
                    SeedPrescription("dumbbell-curl", 2, 10, 15, 45),
                    SeedPrescription("triceps-pushdown", 2, 10, 15, 45),
                    SeedPrescription("standing-calf-raise", 3, 12, 20, 45)
                ))
            )
        ),

        // ------------------------------------------------------------------
        SeedRoutine(
            slug = "deload-week",
            name = "Deload Week",
            description = "Half the volume, two-thirds the load, nothing anywhere near failure. Run this every " +
                "fifth or sixth week, or whenever the Coach tab tells you your numbers have stalled and your " +
                "sleep has gone. You do not grow in the gym — you grow when you back off from it.",
            goal = "Recovery",
            level = "Beginner",
            homeFriendly = false,
            days = listOf(
                SeedDay("Deload Upper", "Movement, not training", listOf(
                    SeedPrescription("dumbbell-bench-press", 2, 8, 10, 90, "60% of normal load, 4 reps in reserve"),
                    SeedPrescription("seated-cable-row", 2, 10, 12, 90),
                    SeedPrescription("lateral-raise", 2, 12, 15, 60),
                    SeedPrescription("face-pull", 3, 15, 20, 45),
                    SeedPrescription("band-pull-apart", 3, 20, 25, 30)
                )),
                SeedDay("Deload Lower", "Blood flow and range", listOf(
                    SeedPrescription("goblet-squat", 2, 10, 12, 90, "Light and clean"),
                    SeedPrescription("leg-curl", 2, 12, 15, 60),
                    SeedPrescription("glute-bridge", 2, 15, 20, 45),
                    SeedPrescription("tibialis-raise", 3, 15, 20, 30),
                    SeedPrescription("dead-bug", 3, 10, 12, 45)
                )),
                SeedDay("Deload Conditioning", "Easy aerobic only", listOf(
                    SeedPrescription("elliptical", 1, 1, 1, 0, "30 min, nose breathing only"),
                    SeedPrescription("pallof-press", 3, 1, 1, 45, "30s per side"),
                    SeedPrescription("y-raise", 3, 12, 15, 45),
                    SeedPrescription("superman", 3, 1, 1, 45, "30s holds")
                ))
            )
        )
    )
}
