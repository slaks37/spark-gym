package com.sparkgym.data.seed

import com.sparkgym.domain.model.Equipment
import com.sparkgym.domain.model.ExerciseDifficulty as Diff
import com.sparkgym.domain.model.ExerciseForce as Force
import com.sparkgym.domain.model.Muscle
import com.sparkgym.domain.model.TrackingType as Track

/**
 * The second half of the library: the machine work, cable angles, unilateral
 * variations and Olympic derivatives that separate a competitive bodybuilding
 * programme from a beginner's full-body plan.
 *
 * Instructions here are written the way a coach actually cues them — what the
 * movement is *for*, not just how to hold the bar.
 */
object ExerciseSeedAdvanced {

    val exercises: List<SeedExercise> = listOf(

        // ---------------- Chest: angles and isolation ----------------
        SeedExercise(
            "pec-deck", "Pec Deck (Machine Fly)", Equipment.MACHINE, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.CHEST), listOf(Muscle.FRONT_DELTS),
            "The safest way to train the chest in a full stretch. Set the seat so the handles sit at nipple height and pause a beat at the squeeze."
        ),
        SeedExercise(
            "low-to-high-cable-fly", "Low-to-High Cable Fly", Equipment.CABLE, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.CHEST), listOf(Muscle.FRONT_DELTS),
            "Pulleys at the bottom, hands finishing at chin height. This is the upper-chest fly most people are missing."
        ),
        SeedExercise(
            "high-to-low-cable-fly", "High-to-Low Cable Fly", Equipment.CABLE, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.CHEST),
            instructions = "Pulleys high, hands meeting at the waist. Biases the lower chest and the sternal fibres."
        ),
        SeedExercise(
            "incline-cable-press", "Incline Cable Press", Equipment.CABLE, Force.PUSH,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.CHEST, Muscle.FRONT_DELTS), listOf(Muscle.TRICEPS),
            "Constant tension through a pressing pattern — a great finisher when your shoulders are done with barbells."
        ),
        SeedExercise(
            "floor-press", "Floor Press", Equipment.BARBELL, Force.PUSH,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.CHEST, Muscle.TRICEPS), listOf(Muscle.FRONT_DELTS),
            "The floor caps your range at the point where the shoulder gets vulnerable. Build lockout strength without paying for it later."
        ),
        SeedExercise(
            "larsen-press", "Larsen Press", Equipment.BARBELL, Force.PUSH,
            Diff.ADVANCED, Track.WEIGHT_REPS,
            listOf(Muscle.CHEST), listOf(Muscle.TRICEPS, Muscle.FRONT_DELTS),
            "Bench press with the legs straight out and no leg drive. Removes the cheat and exposes what your chest can actually do."
        ),
        SeedExercise(
            "dumbbell-pullover", "Dumbbell Pullover", Equipment.DUMBBELL, Force.PULL,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.LATS, Muscle.CHEST), listOf(Muscle.TRICEPS, Muscle.ABS),
            "The old-school ribcage builder. Keep the hips low and feel the stretch across the lats, not the shoulder joint."
        ),
        SeedExercise(
            "svend-press", "Svend Press", Equipment.PLATE, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.CHEST),
            instructions = "Crush two plates together at chest height and press straight out. Pure inner-chest contraction with almost no joint stress."
        ),

        // ---------------- Back: thickness and width ----------------
        SeedExercise(
            "chest-supported-row", "Chest-Supported Row", Equipment.MACHINE, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.LATS, Muscle.TRAPS), listOf(Muscle.REAR_DELTS, Muscle.BICEPS),
            "The pad removes your lower back from the equation, so every failed rep is genuinely the back giving out."
        ),
        SeedExercise(
            "meadows-row", "Meadows Row", Equipment.BARBELL, Force.PULL,
            Diff.ADVANCED, Track.WEIGHT_REPS,
            listOf(Muscle.LATS), listOf(Muscle.TRAPS, Muscle.REAR_DELTS, Muscle.BICEPS),
            "Landmine setup, staggered stance, one arm. The angle hammers the outer lat in a way a standard row never reaches."
        ),
        SeedExercise(
            "seal-row", "Seal Row", Equipment.BARBELL, Force.PULL,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.LATS, Muscle.TRAPS), listOf(Muscle.REAR_DELTS, Muscle.BICEPS),
            "Face down on a raised bench. Zero body English is possible, which is exactly the point."
        ),
        SeedExercise(
            "kroc-row", "Kroc Row", Equipment.DUMBBELL, Force.PULL,
            Diff.ADVANCED, Track.WEIGHT_REPS,
            listOf(Muscle.LATS, Muscle.TRAPS), listOf(Muscle.BICEPS, Muscle.FOREARMS, Muscle.LOWER_BACK),
            "One heavy dumbbell, 20+ reps, some hip movement allowed. Brutal for grip and upper-back mass."
        ),
        SeedExercise(
            "neutral-grip-pulldown", "Neutral-Grip Pulldown", Equipment.CABLE, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.LATS), listOf(Muscle.BICEPS, Muscle.REAR_DELTS),
            "The most shoulder-friendly pulldown, and usually the one you can load heaviest."
        ),
        SeedExercise(
            "single-arm-pulldown", "Single-Arm Cable Pulldown", Equipment.CABLE, Force.PULL,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.LATS), listOf(Muscle.BICEPS),
            "One side at a time buys you extra range at the top and fixes side-to-side differences."
        ),
        SeedExercise(
            "reverse-grip-pulldown", "Reverse-Grip Pulldown", Equipment.CABLE, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.LATS), listOf(Muscle.BICEPS, Muscle.LOWER_BACK),
            "Supinated and narrow. Shifts the load to the lower lats and gives the biceps a real share."
        ),
        SeedExercise(
            "rack-pull", "Rack Pull", Equipment.BARBELL, Force.HINGE,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.TRAPS, Muscle.LOWER_BACK), listOf(Muscle.LATS, Muscle.GLUTES, Muscle.FOREARMS),
            "Pins just below the knee. Overload the top half of the deadlift and build the traps that a shrug never will."
        ),
        SeedExercise(
            "snatch-grip-deadlift", "Snatch-Grip Deadlift", Equipment.BARBELL, Force.HINGE,
            Diff.ADVANCED, Track.WEIGHT_REPS,
            listOf(Muscle.HAMSTRINGS, Muscle.GLUTES, Muscle.TRAPS), listOf(Muscle.LOWER_BACK, Muscle.LATS, Muscle.FOREARMS),
            "Wide grip drops the hips and lengthens the pull. More upper-back work, less weight — leave the ego at the rack."
        ),
        SeedExercise(
            "cable-pullover", "Cable Pullover", Equipment.CABLE, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.LATS), listOf(Muscle.CHEST, Muscle.ABS),
            "Straight arms, rope or bar, high pulley. Isolates the lat without the biceps stealing the set."
        ),
        SeedExercise(
            "weighted-hyperextension", "Weighted Hyperextension", Equipment.BARBELL, Force.HINGE,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.LOWER_BACK, Muscle.GLUTES), listOf(Muscle.HAMSTRINGS),
            "Hold a plate at the chest. Round the upper back deliberately if you want the erectors; stay flat if you want the glutes."
        ),

        // ---------------- Shoulders: the three heads, separately ----------------
        SeedExercise(
            "machine-shoulder-press", "Machine Shoulder Press", Equipment.MACHINE, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.FRONT_DELTS, Muscle.SIDE_DELTS), listOf(Muscle.TRICEPS),
            "Where you go to failure safely. Machine presses are the right place to push past what you would risk with free weights."
        ),
        SeedExercise(
            "landmine-press", "Landmine Press", Equipment.BARBELL, Force.PUSH,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.FRONT_DELTS), listOf(Muscle.CHEST, Muscle.TRICEPS, Muscle.ABS),
            "The arc sits between a press and an incline. Ideal when overhead work bothers your shoulder."
        ),
        SeedExercise(
            "reverse-pec-deck", "Reverse Pec Deck", Equipment.MACHINE, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.REAR_DELTS), listOf(Muscle.TRAPS),
            "Rear delts respond to frequency more than load. Three sets here, three times a week, beats one heavy day."
        ),
        SeedExercise(
            "cable-rear-delt-fly", "Cable Rear Delt Fly", Equipment.CABLE, Force.PULL,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.REAR_DELTS), listOf(Muscle.TRAPS),
            "Cross the cables and pull wide and slightly up. Tension stays on through the whole arc."
        ),
        SeedExercise(
            "lu-raise", "Lu Raise", Equipment.DUMBBELL, Force.PUSH,
            Diff.ADVANCED, Track.WEIGHT_REPS,
            listOf(Muscle.SIDE_DELTS), listOf(Muscle.FRONT_DELTS, Muscle.TRAPS),
            "Lateral raise carried all the way overhead until the dumbbells touch. Light weight, long range, enormous burn."
        ),
        SeedExercise(
            "front-raise", "Front Raise", Equipment.DUMBBELL, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.FRONT_DELTS),
            instructions = "Mostly redundant if you press heavily. Add it only when the front delt is a genuine weak point."
        ),
        SeedExercise(
            "y-raise", "Prone Y-Raise", Equipment.DUMBBELL, Force.PULL,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.TRAPS, Muscle.REAR_DELTS),
            instructions = "Face down on an incline, arms sweeping to a Y. Builds the lower traps that hold your posture together."
        ),
        SeedExercise(
            "cable-upright-row", "Cable Upright Row", Equipment.CABLE, Force.PULL,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.SIDE_DELTS, Muscle.TRAPS), listOf(Muscle.BICEPS),
            "Rope attachment, pull to the collarbone, elbows leading and flaring wide."
        ),

        // ---------------- Arms: every angle ----------------
        SeedExercise(
            "concentration-curl", "Concentration Curl", Equipment.DUMBBELL, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.BICEPS),
            instructions = "Elbow braced on the thigh. The highest measured biceps activation of any curl variation."
        ),
        SeedExercise(
            "spider-curl", "Spider Curl", Equipment.EZ_BAR, Force.PULL,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.BICEPS),
            instructions = "Chest on an incline, arms hanging straight down. Peak contraction with zero momentum available."
        ),
        SeedExercise(
            "drag-curl", "Drag Curl", Equipment.BARBELL, Force.PULL,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.BICEPS), listOf(Muscle.FOREARMS),
            "Drag the bar up the torso with the elbows travelling back. Keeps tension on the long head at the top."
        ),
        SeedExercise(
            "reverse-curl", "Reverse Curl", Equipment.EZ_BAR, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.FOREARMS, Muscle.BICEPS),
            instructions = "Pronated grip. This is the brachioradialis builder that makes the forearm look thick from the front."
        ),
        SeedExercise(
            "close-grip-bench", "Close-Grip Bench Press", Equipment.BARBELL, Force.PUSH,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.TRICEPS), listOf(Muscle.CHEST, Muscle.FRONT_DELTS),
            "Hands shoulder-width, not narrower — squeezing the grip in only hurts the wrists. The heaviest triceps builder there is."
        ),
        SeedExercise(
            "jm-press", "JM Press", Equipment.BARBELL, Force.PUSH,
            Diff.ADVANCED, Track.WEIGHT_REPS,
            listOf(Muscle.TRICEPS), listOf(Muscle.CHEST),
            "Half skull crusher, half close-grip press. A powerlifting staple for lockout strength."
        ),
        SeedExercise(
            "rope-pushdown", "Rope Pushdown", Equipment.CABLE, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.TRICEPS),
            instructions = "Spread the rope apart at the bottom and hold for a count. The lateral head does that last inch of work."
        ),
        SeedExercise(
            "overhead-cable-extension", "Overhead Cable Extension", Equipment.CABLE, Force.PUSH,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.TRICEPS),
            instructions = "Face away from the stack, rope overhead. The long head only grows under stretch, and this is the best stretch you can load."
        ),
        SeedExercise(
            "twenty-ones", "Biceps 21s", Equipment.EZ_BAR, Force.PULL,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.BICEPS), listOf(Muscle.FOREARMS),
            "Seven bottom-half reps, seven top-half, seven full. Log it as one set of 21 and pick a weight you will regret."
        ),
        SeedExercise(
            "plate-pinch", "Plate Pinch Hold", Equipment.PLATE, Force.CARRY,
            Diff.INTERMEDIATE, Track.DURATION,
            listOf(Muscle.FOREARMS),
            instructions = "Pinch two smooth plates together and hold. Grip that carries straight over to deadlifts and rows."
        ),

        // ---------------- Legs: the parts people skip ----------------
        SeedExercise(
            "pause-squat", "Pause Squat", Equipment.BARBELL, Force.SQUAT,
            Diff.ADVANCED, Track.WEIGHT_REPS,
            listOf(Muscle.QUADS, Muscle.GLUTES), listOf(Muscle.ABS, Muscle.ADDUCTORS, Muscle.LOWER_BACK),
            "Three full seconds in the hole with no bounce. Fixes a weak bottom position faster than anything else."
        ),
        SeedExercise(
            "box-squat", "Box Squat", Equipment.BARBELL, Force.SQUAT,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.GLUTES, Muscle.QUADS), listOf(Muscle.HAMSTRINGS, Muscle.LOWER_BACK),
            "Sit back to a box, stay tight, drive up. Teaches hip drive and gives a consistent depth every rep."
        ),
        SeedExercise(
            "zercher-squat", "Zercher Squat", Equipment.BARBELL, Force.SQUAT,
            Diff.ADVANCED, Track.WEIGHT_REPS,
            listOf(Muscle.QUADS, Muscle.GLUTES), listOf(Muscle.ABS, Muscle.LOWER_BACK, Muscle.BICEPS),
            "Bar in the crooks of the elbows. Uncomfortable, unglamorous, and one of the best core-and-quad builders going."
        ),
        SeedExercise(
            "belt-squat", "Belt Squat", Equipment.MACHINE, Force.SQUAT,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.QUADS, Muscle.GLUTES), listOf(Muscle.ADDUCTORS),
            "All the leg work, none of the spinal loading. The answer when your lower back is the thing holding your legs back."
        ),
        SeedExercise(
            "sissy-squat", "Sissy Squat", Equipment.BODYWEIGHT, Force.SQUAT,
            Diff.ADVANCED, Track.REPS_ONLY,
            listOf(Muscle.QUADS),
            instructions = "Knees travel forward, hips stay extended. Deep knee flexion under load — build up slowly or your tendons will object."
        ),
        SeedExercise(
            "single-leg-press", "Single-Leg Press", Equipment.MACHINE, Force.SQUAT,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.QUADS, Muscle.GLUTES), listOf(Muscle.HAMSTRINGS),
            "Fixes the imbalance a two-leg press hides. Foot high on the platform for glutes, low for quads."
        ),
        SeedExercise(
            "reverse-lunge", "Reverse Lunge", Equipment.DUMBBELL, Force.SQUAT,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.GLUTES, Muscle.QUADS), listOf(Muscle.HAMSTRINGS, Muscle.ABDUCTORS),
            "Stepping backwards is far kinder to the knee than stepping forwards, and biases the glute more."
        ),
        SeedExercise(
            "good-morning", "Good Morning", Equipment.BARBELL, Force.HINGE,
            Diff.ADVANCED, Track.WEIGHT_REPS,
            listOf(Muscle.HAMSTRINGS, Muscle.LOWER_BACK), listOf(Muscle.GLUTES),
            "Light, controlled, and never to failure. The hamstring and erector builder that makes squats feel easy."
        ),
        SeedExercise(
            "glute-ham-raise", "Glute-Ham Raise", Equipment.MACHINE, Force.PULL,
            Diff.ADVANCED, Track.REPS_ONLY,
            listOf(Muscle.HAMSTRINGS, Muscle.GLUTES), listOf(Muscle.CALVES, Muscle.LOWER_BACK),
            "Trains the hamstring at both the knee and the hip at once. Almost nothing else does."
        ),
        SeedExercise(
            "seated-leg-curl", "Seated Leg Curl", Equipment.MACHINE, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.HAMSTRINGS), listOf(Muscle.CALVES),
            "Hip flexed means the hamstring is stretched — measurably better for growth than the lying version. Do both."
        ),
        SeedExercise(
            "cable-kickback", "Cable Glute Kickback", Equipment.CABLE, Force.HINGE,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.GLUTES), listOf(Muscle.HAMSTRINGS),
            "Hips square, no lower-back arch to fake range. Squeeze for a full second at the top."
        ),
        SeedExercise(
            "hip-thrust-machine", "Machine Hip Thrust", Equipment.MACHINE, Force.HINGE,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.GLUTES), listOf(Muscle.HAMSTRINGS),
            "Same movement as the barbell version without the setup or the bruised hips. Load it heavy."
        ),
        SeedExercise(
            "donkey-calf-raise", "Donkey Calf Raise", Equipment.MACHINE, Force.PUSH,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.CALVES),
            instructions = "Hinged at the hip with the calves pre-stretched. Arnold's favourite, and still the best calf builder in the gym."
        ),
        SeedExercise(
            "leg-press-calf-raise", "Leg Press Calf Raise", Equipment.MACHINE, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.CALVES),
            instructions = "Balls of the feet on the bottom edge of the platform. Two seconds down, one-second pause at the stretch."
        ),
        SeedExercise(
            "tibialis-raise", "Tibialis Raise", Equipment.BODYWEIGHT, Force.PULL,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.CALVES),
            instructions = "Heels against a wall, toes pulling up. Protects the knee and shin from everything else you do to them."
        ),

        // ---------------- Core: loaded and anti-rotation ----------------
        SeedExercise(
            "hanging-knee-raise", "Hanging Knee Raise", Equipment.PULLUP_BAR, Force.PULL,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.ABS), listOf(Muscle.OBLIQUES, Muscle.FOREARMS),
            "The scalable version of the leg raise. Curl the pelvis up at the top or it is just a hip flexor exercise."
        ),
        SeedExercise(
            "toes-to-bar", "Toes to Bar", Equipment.PULLUP_BAR, Force.PULL,
            Diff.ADVANCED, Track.REPS_ONLY,
            listOf(Muscle.ABS), listOf(Muscle.LATS, Muscle.OBLIQUES, Muscle.FOREARMS),
            "Strict, no kip, toes touching the bar. If you swing, drop to knee raises and earn it."
        ),
        SeedExercise(
            "pallof-press", "Pallof Press", Equipment.CABLE, Force.STATIC,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.OBLIQUES, Muscle.ABS),
            instructions = "Anti-rotation. The cable wants to twist you; your job is to refuse for the full hold."
        ),
        SeedExercise(
            "cable-woodchop", "Cable Woodchop", Equipment.CABLE, Force.PULL,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.OBLIQUES), listOf(Muscle.ABS, Muscle.SIDE_DELTS),
            "Rotate from the ribcage and let the hips follow. Power comes from the trunk, not the arms."
        ),
        SeedExercise(
            "ab-crunch-machine", "Ab Crunch Machine", Equipment.MACHINE, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.ABS), listOf(Muscle.OBLIQUES),
            "Abs are muscles: they need progressive load like anything else. Eight to twelve heavy reps beats a hundred crunches."
        ),
        SeedExercise(
            "weighted-plank", "Weighted Plank", Equipment.PLATE, Force.STATIC,
            Diff.INTERMEDIATE, Track.DURATION,
            listOf(Muscle.ABS), listOf(Muscle.OBLIQUES, Muscle.GLUTES),
            "Plate on the upper back. Once you can hold a bodyweight plank for 90 seconds, add load instead of time."
        ),
        SeedExercise(
            "dragon-flag", "Dragon Flag", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.ADVANCED, Track.REPS_ONLY,
            listOf(Muscle.ABS), listOf(Muscle.OBLIQUES, Muscle.LOWER_BACK, Muscle.LATS),
            "Bruce Lee's party trick and a genuinely elite core movement. Lower with a rigid body, one vertebra at a time."
        ),
        SeedExercise(
            "decline-sit-up", "Decline Sit-up", Equipment.BODYWEIGHT, Force.PULL,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.ABS), listOf(Muscle.OBLIQUES, Muscle.QUADS),
            "Hold a plate at the chest once bodyweight gets easy. Control the descent — that is where the work is."
        ),

        // ---------------- Power and Olympic derivatives ----------------
        SeedExercise(
            "power-clean", "Power Clean", Equipment.BARBELL, Force.HINGE,
            Diff.ADVANCED, Track.WEIGHT_REPS,
            listOf(Muscle.TRAPS, Muscle.GLUTES, Muscle.QUADS), listOf(Muscle.HAMSTRINGS, Muscle.LOWER_BACK, Muscle.FRONT_DELTS),
            "Explosive triple extension. Keep the reps low and the bar fast — a slow clean is a failed clean."
        ),
        SeedExercise(
            "hang-clean", "Hang Clean", Equipment.BARBELL, Force.HINGE,
            Diff.ADVANCED, Track.WEIGHT_REPS,
            listOf(Muscle.TRAPS, Muscle.GLUTES), listOf(Muscle.QUADS, Muscle.HAMSTRINGS, Muscle.FRONT_DELTS),
            "Starts from mid-thigh, so you learn the second pull without the floor position getting in the way."
        ),
        SeedExercise(
            "push-press", "Push Press", Equipment.BARBELL, Force.PUSH,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.FRONT_DELTS, Muscle.TRICEPS), listOf(Muscle.SIDE_DELTS, Muscle.QUADS, Muscle.ABS),
            "A short dip and drive lets you overload the top of the press well past your strict max."
        ),
        SeedExercise(
            "thruster", "Thruster", Equipment.BARBELL, Force.SQUAT,
            Diff.ADVANCED, Track.WEIGHT_REPS,
            listOf(Muscle.QUADS, Muscle.FRONT_DELTS), listOf(Muscle.GLUTES, Muscle.TRICEPS, Muscle.ABS),
            "Front squat straight into an overhead press. The most conditioning you can buy per rep."
        ),

        // ---------------- Conditioning and machines ----------------
        SeedExercise(
            "sled-push", "Sled Push", Equipment.OTHER, Force.CARDIO,
            Diff.INTERMEDIATE, Track.DISTANCE_DURATION,
            listOf(Muscle.QUADS, Muscle.GLUTES), listOf(Muscle.CALVES, Muscle.ABS, Muscle.FRONT_DELTS),
            "All concentric, so it builds work capacity without the soreness. The best conditioning for a lifter there is."
        ),
        SeedExercise(
            "assault-bike", "Air Bike", Equipment.CARDIO, Force.CARDIO,
            Diff.INTERMEDIATE, Track.DISTANCE_DURATION,
            listOf(Muscle.QUADS), listOf(Muscle.HAMSTRINGS, Muscle.LATS, Muscle.FRONT_DELTS),
            "Resistance scales with your effort, so it is exactly as bad as you make it. Ideal for intervals."
        ),
        SeedExercise(
            "stair-climber", "Stair Climber", Equipment.CARDIO, Force.CARDIO,
            Diff.BEGINNER, Track.DISTANCE_DURATION,
            listOf(Muscle.GLUTES, Muscle.QUADS), listOf(Muscle.CALVES, Muscle.HAMSTRINGS),
            "The bodybuilder's cardio: steady, low impact, and it does not eat into leg recovery the way running does."
        ),
        SeedExercise(
            "ski-erg", "Ski Erg", Equipment.CARDIO, Force.CARDIO,
            Diff.INTERMEDIATE, Track.DISTANCE_DURATION,
            listOf(Muscle.LATS, Muscle.ABS), listOf(Muscle.TRICEPS, Muscle.GLUTES),
            "Upper-body dominant conditioning. Useful on a leg-heavy week when you still want the heart rate up."
        ),
        SeedExercise(
            "elliptical", "Elliptical", Equipment.CARDIO, Force.CARDIO,
            Diff.BEGINNER, Track.DISTANCE_DURATION,
            listOf(Muscle.QUADS, Muscle.GLUTES), listOf(Muscle.HAMSTRINGS, Muscle.CALVES),
            "Zero impact. The right choice when your joints need a week off but your heart does not."
        ),
        SeedExercise(
            "swimming", "Swimming", Equipment.OTHER, Force.CARDIO,
            Diff.INTERMEDIATE, Track.DISTANCE_DURATION,
            listOf(Muscle.LATS, Muscle.FRONT_DELTS), listOf(Muscle.TRICEPS, Muscle.ABS, Muscle.GLUTES),
            "Full body, no impact, and it stretches the shoulders that pressing tightens up."
        ),
        SeedExercise(
            "box-jump", "Box Jump", Equipment.OTHER, Force.SQUAT,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.QUADS, Muscle.GLUTES), listOf(Muscle.CALVES, Muscle.HAMSTRINGS),
            "Step down, never jump down. Low reps, full recovery — this is a power exercise, not conditioning."
        ),
        // ---------------- Neck: small, unglamorous, worth doing ----------------
        SeedExercise(
            "neck-extension", "Neck Extension", Equipment.PLATE, Force.PULL,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.NECK), listOf(Muscle.TRAPS),
            "Face down on a bench, plate on the back of the head, chin tucking through a full range. Slow, light, and never to failure."
        ),
        SeedExercise(
            "neck-lateral-flexion", "Lateral Neck Flexion", Equipment.PLATE, Force.PULL,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.NECK), listOf(Muscle.TRAPS),
            "Lie on your side, ear towards the shoulder. Train both directions or you build an asymmetry you will feel."
        ),
        SeedExercise(
            "neck-harness-hold", "Neck Isometric Hold", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.NECK),
            instructions = "Push your head into your own palm and hold. The safest way to start neck work — no equipment, no range of motion to get wrong."
        ),

        SeedExercise(
            "neck-harness-extension", "Neck Harness Extension", Equipment.PLATE, Force.PULL,
            Diff.ADVANCED, Track.WEIGHT_REPS,
            listOf(Muscle.NECK), listOf(Muscle.TRAPS),
            "Harness on, plate hanging in front, hinge at the hips. Full range, three seconds down. This is the movement that actually builds neck thickness."
        ),
        SeedExercise(
            "wrestlers-bridge", "Wrestler's Bridge", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.ADVANCED, Track.DURATION,
            listOf(Muscle.NECK), listOf(Muscle.TRAPS, Muscle.LOWER_BACK),
            "Back bridge with weight through the crown of the head. Earn it over months — start with hands taking most of the load and remove them slowly."
        ),
        SeedExercise(
            "front-neck-bridge", "Front Neck Bridge", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.ADVANCED, Track.DURATION,
            listOf(Muscle.NECK),
            instructions = "Face down, forehead on a mat, hands off the floor. Trains the front of the neck, which the back bridge misses entirely."
        ),
        SeedExercise(
            "banded-neck-flexion", "Banded Neck Flexion", Equipment.BAND, Force.PULL,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.NECK),
            instructions = "Band anchored behind you, looped round the head. Tuck the chin through a full range. The safest way to load the neck without a harness."
        ),
        SeedExercise(
            "chin-tuck", "Chin Tuck", Equipment.BODYWEIGHT, Force.PULL,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.NECK),
            instructions = "Draw the chin straight back to make a double chin, hold two seconds. Trains the deep neck flexors — the ones a phone-shaped posture switches off."
        ),

        SeedExercise(
            "sandbag-carry", "Sandbag Carry", Equipment.OTHER, Force.CARRY,
            Diff.INTERMEDIATE, Track.DURATION,
            listOf(Muscle.ABS, Muscle.TRAPS), listOf(Muscle.LOWER_BACK, Muscle.GLUTES, Muscle.FOREARMS),
            "Awkward load, braced trunk, keep walking. Carries build the core that crunches never touch."
        )
    )
}
