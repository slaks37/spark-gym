package com.sparkgym.data.seed

import com.sparkgym.domain.model.Equipment
import com.sparkgym.domain.model.ExerciseDifficulty as Diff
import com.sparkgym.domain.model.ExerciseForce as Force
import com.sparkgym.domain.model.Muscle
import com.sparkgym.domain.model.TrackingType as Track

data class SeedExercise(
    val slug: String,
    val name: String,
    val equipment: Equipment,
    val force: Force,
    val difficulty: Diff,
    val tracking: Track,
    val primary: List<Muscle>,
    val secondary: List<Muscle> = emptyList(),
    val instructions: String
)

/**
 * The bundled library. Every entry carries prime movers and synergists because
 * the heat map is only as honest as this mapping — a bench press that claims
 * chest alone would leave your triceps permanently cold.
 */
object ExerciseSeed {

    /** The full library: foundational movements plus the advanced/machine half. */
    val exercises: List<SeedExercise> get() = foundational + ExerciseSeedAdvanced.exercises

    private val foundational: List<SeedExercise> = listOf(

        // ---------------- Chest ----------------
        SeedExercise(
            "barbell-bench-press", "Barbell Bench Press", Equipment.BARBELL, Force.PUSH,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.CHEST), listOf(Muscle.FRONT_DELTS, Muscle.TRICEPS),
            "Retract the shoulder blades, lower the bar to mid-chest under control, drive back to lockout without flaring the elbows past 60 degrees."
        ),
        SeedExercise(
            "incline-barbell-press", "Incline Barbell Press", Equipment.BARBELL, Force.PUSH,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.CHEST, Muscle.FRONT_DELTS), listOf(Muscle.TRICEPS),
            "Set the bench to 30 degrees. Higher angles turn this into a shoulder press."
        ),
        SeedExercise(
            "dumbbell-bench-press", "Dumbbell Bench Press", Equipment.DUMBBELL, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.CHEST), listOf(Muscle.FRONT_DELTS, Muscle.TRICEPS),
            "Lower until the dumbbells are level with the chest, then press together without clanging them at the top."
        ),
        SeedExercise(
            "incline-dumbbell-press", "Incline Dumbbell Press", Equipment.DUMBBELL, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.CHEST, Muscle.FRONT_DELTS), listOf(Muscle.TRICEPS),
            "Bench at 30 degrees, elbows tucked to roughly 45 degrees from the torso."
        ),
        SeedExercise(
            "dumbbell-fly", "Dumbbell Fly", Equipment.DUMBBELL, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.CHEST), listOf(Muscle.FRONT_DELTS),
            "Soft elbows, wide arc, stop when you feel the stretch — this is not a press."
        ),
        SeedExercise(
            "cable-crossover", "Cable Crossover", Equipment.CABLE, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.CHEST), listOf(Muscle.FRONT_DELTS),
            "Step forward into a staggered stance and finish with the hands crossing past each other."
        ),
        SeedExercise(
            "machine-chest-press", "Machine Chest Press", Equipment.MACHINE, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.CHEST), listOf(Muscle.TRICEPS, Muscle.FRONT_DELTS),
            "Set the handles level with the mid-chest before you sit down."
        ),
        SeedExercise(
            "push-up", "Push-up", Equipment.BODYWEIGHT, Force.PUSH,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.CHEST), listOf(Muscle.TRICEPS, Muscle.FRONT_DELTS, Muscle.ABS),
            "Hands just outside the shoulders, body in one line from heel to head, chest to within a fist of the floor."
        ),
        SeedExercise(
            "diamond-push-up", "Diamond Push-up", Equipment.BODYWEIGHT, Force.PUSH,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.TRICEPS), listOf(Muscle.CHEST, Muscle.FRONT_DELTS),
            "Index fingers and thumbs touching under the sternum, elbows brushing the ribs."
        ),
        SeedExercise(
            "decline-push-up", "Decline Push-up", Equipment.BODYWEIGHT, Force.PUSH,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.CHEST, Muscle.FRONT_DELTS), listOf(Muscle.TRICEPS, Muscle.ABS),
            "Feet elevated on a chair or bench to bias the upper chest."
        ),
        SeedExercise(
            "dips-chest", "Chest Dip", Equipment.BODYWEIGHT, Force.PUSH,
            Diff.INTERMEDIATE, Track.BODYWEIGHT_PLUS,
            listOf(Muscle.CHEST), listOf(Muscle.TRICEPS, Muscle.FRONT_DELTS),
            "Lean the torso forward about 30 degrees and let the elbows travel slightly wide."
        ),

        // ---------------- Back ----------------
        SeedExercise(
            "pull-up", "Pull-up", Equipment.PULLUP_BAR, Force.PULL,
            Diff.INTERMEDIATE, Track.BODYWEIGHT_PLUS,
            listOf(Muscle.LATS), listOf(Muscle.BICEPS, Muscle.REAR_DELTS, Muscle.FOREARMS),
            "Start from a dead hang, pull the elbows down and back until the chin clears the bar."
        ),
        SeedExercise(
            "chin-up", "Chin-up", Equipment.PULLUP_BAR, Force.PULL,
            Diff.INTERMEDIATE, Track.BODYWEIGHT_PLUS,
            listOf(Muscle.LATS, Muscle.BICEPS), listOf(Muscle.FOREARMS),
            "Supinated grip at shoulder width — more biceps than a pull-up, and usually a few more reps."
        ),
        SeedExercise(
            "lat-pulldown", "Lat Pulldown", Equipment.CABLE, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.LATS), listOf(Muscle.BICEPS, Muscle.REAR_DELTS),
            "Pull to the collarbone with the chest up; never behind the neck."
        ),
        SeedExercise(
            "barbell-row", "Barbell Bent-over Row", Equipment.BARBELL, Force.PULL,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.LATS, Muscle.TRAPS), listOf(Muscle.BICEPS, Muscle.REAR_DELTS, Muscle.LOWER_BACK),
            "Hinge to roughly 45 degrees, brace hard, row to the lower ribs."
        ),
        SeedExercise(
            "pendlay-row", "Pendlay Row", Equipment.BARBELL, Force.PULL,
            Diff.ADVANCED, Track.WEIGHT_REPS,
            listOf(Muscle.LATS, Muscle.TRAPS), listOf(Muscle.LOWER_BACK, Muscle.BICEPS),
            "Torso parallel to the floor, bar resets on the ground between every rep."
        ),
        SeedExercise(
            "dumbbell-row", "One-arm Dumbbell Row", Equipment.DUMBBELL, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.LATS), listOf(Muscle.BICEPS, Muscle.TRAPS, Muscle.REAR_DELTS),
            "Support on a bench, pull the dumbbell to the hip rather than the shoulder."
        ),
        SeedExercise(
            "seated-cable-row", "Seated Cable Row", Equipment.CABLE, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.LATS, Muscle.TRAPS), listOf(Muscle.BICEPS, Muscle.REAR_DELTS),
            "Keep the torso still — if you are rowing with your lower back, drop the weight."
        ),
        SeedExercise(
            "t-bar-row", "T-Bar Row", Equipment.BARBELL, Force.PULL,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.LATS, Muscle.TRAPS), listOf(Muscle.BICEPS, Muscle.LOWER_BACK),
            "Chest supported if available; drive the elbows past the ribs."
        ),
        SeedExercise(
            "inverted-row", "Inverted Row", Equipment.BODYWEIGHT, Force.PULL,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.LATS, Muscle.TRAPS), listOf(Muscle.BICEPS, Muscle.REAR_DELTS),
            "Under a bar or sturdy table, body straight, chest to the bar."
        ),
        SeedExercise(
            "straight-arm-pulldown", "Straight-arm Pulldown", Equipment.CABLE, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.LATS), listOf(Muscle.ABS),
            "Arms locked, sweep the bar from eye level to the thighs."
        ),
        SeedExercise(
            "face-pull", "Face Pull", Equipment.CABLE, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.REAR_DELTS, Muscle.TRAPS), listOf(Muscle.BICEPS),
            "Rope at eye height, pull to the forehead and externally rotate at the end."
        ),
        SeedExercise(
            "barbell-shrug", "Barbell Shrug", Equipment.BARBELL, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.TRAPS), listOf(Muscle.FOREARMS),
            "Straight up, brief pause at the top, no rolling."
        ),
        SeedExercise(
            "back-extension", "Back Extension", Equipment.BODYWEIGHT, Force.HINGE,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.LOWER_BACK), listOf(Muscle.GLUTES, Muscle.HAMSTRINGS),
            "Round nothing; stop when the torso is in line with the legs."
        ),

        // ---------------- Legs ----------------
        SeedExercise(
            "back-squat", "Barbell Back Squat", Equipment.BARBELL, Force.SQUAT,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.QUADS, Muscle.GLUTES), listOf(Muscle.HAMSTRINGS, Muscle.ABS, Muscle.LOWER_BACK, Muscle.ADDUCTORS),
            "Brace, break at the hips and knees together, descend until the hip crease passes the knee."
        ),
        SeedExercise(
            "front-squat", "Front Squat", Equipment.BARBELL, Force.SQUAT,
            Diff.ADVANCED, Track.WEIGHT_REPS,
            listOf(Muscle.QUADS), listOf(Muscle.GLUTES, Muscle.ABS, Muscle.TRAPS),
            "Elbows high, bar resting on the front delts, torso as upright as possible."
        ),
        SeedExercise(
            "goblet-squat", "Goblet Squat", Equipment.DUMBBELL, Force.SQUAT,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.QUADS, Muscle.GLUTES), listOf(Muscle.ABS, Muscle.ADDUCTORS),
            "Hold a dumbbell at the chest — the counterweight teaches depth faster than cueing does."
        ),
        SeedExercise(
            "bodyweight-squat", "Bodyweight Squat", Equipment.BODYWEIGHT, Force.SQUAT,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.QUADS, Muscle.GLUTES), listOf(Muscle.HAMSTRINGS, Muscle.ABS),
            "Feet shoulder width, sit between the hips, full depth every rep."
        ),
        SeedExercise(
            "leg-press", "Leg Press", Equipment.MACHINE, Force.SQUAT,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.QUADS, Muscle.GLUTES), listOf(Muscle.HAMSTRINGS, Muscle.ADDUCTORS),
            "Do not let the lower back round off the pad at the bottom."
        ),
        SeedExercise(
            "hack-squat", "Hack Squat", Equipment.MACHINE, Force.SQUAT,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.QUADS), listOf(Muscle.GLUTES, Muscle.ADDUCTORS),
            "Feet low on the platform biases the quads further."
        ),
        SeedExercise(
            "bulgarian-split-squat", "Bulgarian Split Squat", Equipment.DUMBBELL, Force.SQUAT,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.QUADS, Muscle.GLUTES), listOf(Muscle.HAMSTRINGS, Muscle.ABDUCTORS),
            "Rear foot elevated; the front shin stays roughly vertical for quads, leaned forward for glutes."
        ),
        SeedExercise(
            "walking-lunge", "Walking Lunge", Equipment.DUMBBELL, Force.SQUAT,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.QUADS, Muscle.GLUTES), listOf(Muscle.HAMSTRINGS, Muscle.ABDUCTORS),
            "Long steps for glutes, short steps for quads. Knee taps just short of the floor."
        ),
        SeedExercise(
            "step-up", "Step-up", Equipment.DUMBBELL, Force.SQUAT,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.QUADS, Muscle.GLUTES), listOf(Muscle.HAMSTRINGS),
            "Drive through the heel on the box; do not push off the trailing foot."
        ),
        SeedExercise(
            "deadlift", "Conventional Deadlift", Equipment.BARBELL, Force.HINGE,
            Diff.ADVANCED, Track.WEIGHT_REPS,
            listOf(Muscle.HAMSTRINGS, Muscle.GLUTES, Muscle.LOWER_BACK), listOf(Muscle.TRAPS, Muscle.LATS, Muscle.FOREARMS, Muscle.QUADS),
            "Bar over mid-foot, lats engaged, push the floor away and lock out with the glutes."
        ),
        SeedExercise(
            "romanian-deadlift", "Romanian Deadlift", Equipment.BARBELL, Force.HINGE,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.HAMSTRINGS, Muscle.GLUTES), listOf(Muscle.LOWER_BACK, Muscle.FOREARMS),
            "Soft knees, push the hips back, stop when the hamstrings stop the descent — not when the bar hits the floor."
        ),
        SeedExercise(
            "sumo-deadlift", "Sumo Deadlift", Equipment.BARBELL, Force.HINGE,
            Diff.ADVANCED, Track.WEIGHT_REPS,
            listOf(Muscle.GLUTES, Muscle.QUADS, Muscle.ADDUCTORS), listOf(Muscle.HAMSTRINGS, Muscle.TRAPS, Muscle.LOWER_BACK),
            "Wide stance, hands inside the knees, open the hips as you take the slack out."
        ),
        SeedExercise(
            "hip-thrust", "Barbell Hip Thrust", Equipment.BARBELL, Force.HINGE,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.GLUTES), listOf(Muscle.HAMSTRINGS, Muscle.ABS),
            "Shoulders on the bench, chin tucked, finish with the hips level — not hyperextended."
        ),
        SeedExercise(
            "glute-bridge", "Glute Bridge", Equipment.BODYWEIGHT, Force.HINGE,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.GLUTES), listOf(Muscle.HAMSTRINGS),
            "Squeeze for a full second at the top of every rep."
        ),
        SeedExercise(
            "leg-curl", "Lying Leg Curl", Equipment.MACHINE, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.HAMSTRINGS), listOf(Muscle.CALVES),
            "Hips stay down; control the lowering for at least two seconds."
        ),
        SeedExercise(
            "leg-extension", "Leg Extension", Equipment.MACHINE, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.QUADS),
            instructions = "Pause at lockout, lower slowly. Great finisher, poor main lift."
        ),
        SeedExercise(
            "nordic-curl", "Nordic Hamstring Curl", Equipment.BODYWEIGHT, Force.PULL,
            Diff.ADVANCED, Track.REPS_ONLY,
            listOf(Muscle.HAMSTRINGS), listOf(Muscle.GLUTES),
            "Anchor the ankles, lower as slowly as you can, push off the floor to return."
        ),
        SeedExercise(
            "standing-calf-raise", "Standing Calf Raise", Equipment.MACHINE, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.CALVES),
            instructions = "Full stretch at the bottom, full contraction at the top, no bouncing."
        ),
        SeedExercise(
            "seated-calf-raise", "Seated Calf Raise", Equipment.MACHINE, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.CALVES),
            instructions = "Bent knee biases the soleus — go higher rep here, 12 to 20."
        ),
        SeedExercise(
            "hip-abduction", "Hip Abduction", Equipment.MACHINE, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.ABDUCTORS), listOf(Muscle.GLUTES),
            "Lean forward slightly to bias the upper glutes."
        ),
        SeedExercise(
            "hip-adduction", "Hip Adduction", Equipment.MACHINE, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.ADDUCTORS),
            instructions = "Control the stretch; this is one of the easiest muscles to strain in a hurry."
        ),

        // ---------------- Shoulders ----------------
        SeedExercise(
            "overhead-press", "Standing Overhead Press", Equipment.BARBELL, Force.PUSH,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.FRONT_DELTS, Muscle.SIDE_DELTS), listOf(Muscle.TRICEPS, Muscle.ABS, Muscle.TRAPS),
            "Squeeze the glutes, move the head back out of the way, finish with the bar over the mid-foot."
        ),
        SeedExercise(
            "dumbbell-shoulder-press", "Dumbbell Shoulder Press", Equipment.DUMBBELL, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.FRONT_DELTS, Muscle.SIDE_DELTS), listOf(Muscle.TRICEPS),
            "Seated with back support if the lower back is the limiting factor."
        ),
        SeedExercise(
            "arnold-press", "Arnold Press", Equipment.DUMBBELL, Force.PUSH,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.FRONT_DELTS, Muscle.SIDE_DELTS), listOf(Muscle.TRICEPS),
            "Start palms-in, rotate out as you press."
        ),
        SeedExercise(
            "lateral-raise", "Dumbbell Lateral Raise", Equipment.DUMBBELL, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.SIDE_DELTS), listOf(Muscle.TRAPS),
            "Lead with the elbows, stop at shoulder height, resist the swing."
        ),
        SeedExercise(
            "cable-lateral-raise", "Cable Lateral Raise", Equipment.CABLE, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.SIDE_DELTS),
            instructions = "Constant tension through the whole range — the best version of this movement."
        ),
        SeedExercise(
            "rear-delt-fly", "Rear Delt Fly", Equipment.DUMBBELL, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.REAR_DELTS), listOf(Muscle.TRAPS),
            "Hinge over, thumbs down, pull wide rather than back."
        ),
        SeedExercise(
            "upright-row", "Upright Row", Equipment.BARBELL, Force.PULL,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.SIDE_DELTS, Muscle.TRAPS), listOf(Muscle.BICEPS),
            "Wider grip, stop at chest height to keep the shoulder happy."
        ),
        SeedExercise(
            "pike-push-up", "Pike Push-up", Equipment.BODYWEIGHT, Force.PUSH,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.FRONT_DELTS), listOf(Muscle.TRICEPS, Muscle.CHEST),
            "Hips high, crown of the head to the floor between the hands."
        ),

        // ---------------- Arms ----------------
        SeedExercise(
            "barbell-curl", "Barbell Curl", Equipment.BARBELL, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.BICEPS), listOf(Muscle.FOREARMS),
            "Elbows pinned to the ribs, no hip drive."
        ),
        SeedExercise(
            "dumbbell-curl", "Dumbbell Curl", Equipment.DUMBBELL, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.BICEPS), listOf(Muscle.FOREARMS),
            "Supinate as you curl; squeeze at the top."
        ),
        SeedExercise(
            "hammer-curl", "Hammer Curl", Equipment.DUMBBELL, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.BICEPS, Muscle.FOREARMS),
            instructions = "Neutral grip throughout — this is the brachialis builder."
        ),
        SeedExercise(
            "preacher-curl", "Preacher Curl", Equipment.EZ_BAR, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.BICEPS), listOf(Muscle.FOREARMS),
            "Do not fully lock out at the bottom under heavy load."
        ),
        SeedExercise(
            "incline-dumbbell-curl", "Incline Dumbbell Curl", Equipment.DUMBBELL, Force.PULL,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.BICEPS), listOf(Muscle.FOREARMS),
            "Arms hanging behind the torso for the deepest long-head stretch."
        ),
        SeedExercise(
            "cable-curl", "Cable Curl", Equipment.CABLE, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.BICEPS), listOf(Muscle.FOREARMS),
            "Constant tension; great for high-rep finishing sets."
        ),
        SeedExercise(
            "triceps-pushdown", "Triceps Pushdown", Equipment.CABLE, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.TRICEPS),
            instructions = "Elbows locked at the sides, full extension, controlled return."
        ),
        SeedExercise(
            "overhead-triceps-extension", "Overhead Triceps Extension", Equipment.DUMBBELL, Force.PUSH,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.TRICEPS),
            instructions = "Overhead position stretches the long head — the part most programmes neglect."
        ),
        SeedExercise(
            "skull-crusher", "Skull Crusher", Equipment.EZ_BAR, Force.PUSH,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.TRICEPS),
            instructions = "Lower behind the head rather than to the forehead to keep tension on the long head."
        ),
        SeedExercise(
            "dips-triceps", "Triceps Dip", Equipment.BODYWEIGHT, Force.PUSH,
            Diff.INTERMEDIATE, Track.BODYWEIGHT_PLUS,
            listOf(Muscle.TRICEPS), listOf(Muscle.CHEST, Muscle.FRONT_DELTS),
            "Torso upright, elbows straight back."
        ),
        SeedExercise(
            "bench-dip", "Bench Dip", Equipment.BODYWEIGHT, Force.PUSH,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.TRICEPS), listOf(Muscle.FRONT_DELTS),
            "Hands on a bench behind you, feet out front. Easy to scale by moving the feet."
        ),
        SeedExercise(
            "wrist-curl", "Wrist Curl", Equipment.DUMBBELL, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.FOREARMS),
            instructions = "Forearms on the thighs, full range, high reps."
        ),
        SeedExercise(
            "farmers-carry", "Farmer's Carry", Equipment.DUMBBELL, Force.CARRY,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.FOREARMS, Muscle.TRAPS), listOf(Muscle.ABS, Muscle.OBLIQUES, Muscle.GLUTES),
            "Heavy, tall, and slow. Time under load is the whole point."
        ),

        // ---------------- Core ----------------
        SeedExercise(
            "plank", "Plank", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.ABS), listOf(Muscle.OBLIQUES, Muscle.GLUTES),
            "Ribs down, glutes squeezed. If it is easy at 60 seconds, you are sagging."
        ),
        SeedExercise(
            "side-plank", "Side Plank", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.OBLIQUES), listOf(Muscle.ABS, Muscle.ABDUCTORS),
            "Stack the feet, drive the bottom hip up."
        ),
        SeedExercise(
            "hanging-leg-raise", "Hanging Leg Raise", Equipment.PULLUP_BAR, Force.PULL,
            Diff.ADVANCED, Track.REPS_ONLY,
            listOf(Muscle.ABS), listOf(Muscle.OBLIQUES, Muscle.FOREARMS),
            "Posteriorly tilt the pelvis at the top — swinging legs do nothing."
        ),
        SeedExercise(
            "cable-crunch", "Cable Crunch", Equipment.CABLE, Force.PULL,
            Diff.BEGINNER, Track.WEIGHT_REPS,
            listOf(Muscle.ABS), listOf(Muscle.OBLIQUES),
            "Kneel, flex the spine down toward the knees, hips stay fixed."
        ),
        SeedExercise(
            "crunch", "Crunch", Equipment.BODYWEIGHT, Force.PULL,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.ABS),
            instructions = "Short range, slow tempo, exhale at the top."
        ),
        SeedExercise(
            "sit-up", "Sit-up", Equipment.BODYWEIGHT, Force.PULL,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.ABS), listOf(Muscle.OBLIQUES, Muscle.QUADS),
            "Full range from the floor to upright."
        ),
        SeedExercise(
            "russian-twist", "Russian Twist", Equipment.BODYWEIGHT, Force.PULL,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.OBLIQUES), listOf(Muscle.ABS),
            "Rotate the ribcage, not just the arms."
        ),
        SeedExercise(
            "mountain-climber", "Mountain Climber", Equipment.BODYWEIGHT, Force.CARDIO,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.ABS), listOf(Muscle.OBLIQUES, Muscle.FRONT_DELTS, Muscle.QUADS),
            "Hips level, quick knee drives."
        ),
        SeedExercise(
            "ab-wheel-rollout", "Ab Wheel Rollout", Equipment.OTHER, Force.STATIC,
            Diff.ADVANCED, Track.REPS_ONLY,
            listOf(Muscle.ABS), listOf(Muscle.OBLIQUES, Muscle.LATS, Muscle.LOWER_BACK),
            "Roll only as far as you can go without the lower back arching."
        ),
        SeedExercise(
            "dead-bug", "Dead Bug", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.ABS),
            instructions = "Lower back glued to the floor the entire time."
        ),
        SeedExercise(
            "neck-curl", "Neck Curl", Equipment.BODYWEIGHT, Force.PULL,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.NECK),
            instructions = "Slow and light. The neck responds fast and complains faster."
        ),

        // ---------------- Conditioning ----------------
        SeedExercise(
            "burpee", "Burpee", Equipment.BODYWEIGHT, Force.CARDIO,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.QUADS, Muscle.CHEST), listOf(Muscle.ABS, Muscle.FRONT_DELTS, Muscle.GLUTES),
            "Chest to the floor, full jump at the top, no half reps."
        ),
        SeedExercise(
            "jump-rope", "Jump Rope", Equipment.OTHER, Force.CARDIO,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.CALVES), listOf(Muscle.FOREARMS, Muscle.QUADS),
            "Wrists do the work, not the arms. Land softly."
        ),
        SeedExercise(
            "treadmill-run", "Treadmill Run", Equipment.CARDIO, Force.CARDIO,
            Diff.BEGINNER, Track.DISTANCE_DURATION,
            listOf(Muscle.QUADS, Muscle.CALVES), listOf(Muscle.HAMSTRINGS, Muscle.GLUTES),
            "Log distance and time; the app converts it to endurance XP."
        ),
        SeedExercise(
            "outdoor-run", "Outdoor Run", Equipment.OTHER, Force.CARDIO,
            Diff.BEGINNER, Track.DISTANCE_DURATION,
            listOf(Muscle.QUADS, Muscle.CALVES), listOf(Muscle.HAMSTRINGS, Muscle.GLUTES),
            "Fitbit or Health Connect can fill this in automatically after a sync."
        ),
        SeedExercise(
            "rowing-machine", "Rowing Machine", Equipment.CARDIO, Force.CARDIO,
            Diff.BEGINNER, Track.DISTANCE_DURATION,
            listOf(Muscle.LATS, Muscle.QUADS), listOf(Muscle.HAMSTRINGS, Muscle.BICEPS, Muscle.LOWER_BACK),
            "Legs, then hips, then arms. Reverse on the way back."
        ),
        SeedExercise(
            "cycling", "Cycling", Equipment.CARDIO, Force.CARDIO,
            Diff.BEGINNER, Track.DISTANCE_DURATION,
            listOf(Muscle.QUADS), listOf(Muscle.GLUTES, Muscle.HAMSTRINGS, Muscle.CALVES),
            "Stationary or outdoor; both count toward endurance."
        ),
        SeedExercise(
            "kettlebell-swing", "Kettlebell Swing", Equipment.KETTLEBELL, Force.HINGE,
            Diff.INTERMEDIATE, Track.WEIGHT_REPS,
            listOf(Muscle.GLUTES, Muscle.HAMSTRINGS), listOf(Muscle.LOWER_BACK, Muscle.ABS, Muscle.FOREARMS),
            "Hip snap, not a squat. The arms are ropes."
        ),
        SeedExercise(
            "battle-ropes", "Battle Ropes", Equipment.OTHER, Force.CARDIO,
            Diff.INTERMEDIATE, Track.DURATION,
            listOf(Muscle.FRONT_DELTS, Muscle.FOREARMS), listOf(Muscle.ABS, Muscle.LATS),
            "Athletic stance, keep the waves reaching the anchor."
        ),
        SeedExercise(
            "high-knees", "High Knees", Equipment.BODYWEIGHT, Force.CARDIO,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.QUADS), listOf(Muscle.CALVES, Muscle.ABS),
            "Knees above hip height, quick ground contacts."
        ),
        SeedExercise(
            "jumping-jack", "Jumping Jack", Equipment.BODYWEIGHT, Force.CARDIO,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.CALVES, Muscle.SIDE_DELTS), listOf(Muscle.ABDUCTORS),
            "The universal warm-up. Full arm extension overhead."
        ),

        // ---------------- Bands & home ----------------
        SeedExercise(
            "band-pull-apart", "Band Pull-apart", Equipment.BAND, Force.PULL,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.REAR_DELTS), listOf(Muscle.TRAPS),
            "Arms straight, squeeze the shoulder blades, slow return."
        ),
        SeedExercise(
            "band-row", "Band Row", Equipment.BAND, Force.PULL,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.LATS, Muscle.TRAPS), listOf(Muscle.BICEPS, Muscle.REAR_DELTS),
            "Anchor at chest height and row to the ribs."
        ),
        SeedExercise(
            "band-chest-press", "Band Chest Press", Equipment.BAND, Force.PUSH,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.CHEST), listOf(Muscle.TRICEPS, Muscle.FRONT_DELTS),
            "Anchor behind you, press and squeeze at full extension."
        ),
        SeedExercise(
            "band-lateral-walk", "Band Lateral Walk", Equipment.BAND, Force.PUSH,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.ABDUCTORS), listOf(Muscle.GLUTES),
            "Band above the knees, athletic stance, no torso sway."
        ),
        SeedExercise(
            "pistol-squat", "Pistol Squat", Equipment.BODYWEIGHT, Force.SQUAT,
            Diff.ADVANCED, Track.REPS_ONLY,
            listOf(Muscle.QUADS, Muscle.GLUTES), listOf(Muscle.ABS, Muscle.CALVES),
            "Full single-leg squat. Hold a counterweight until the balance comes."
        ),
        SeedExercise(
            "wall-sit", "Wall Sit", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.QUADS), listOf(Muscle.GLUTES),
            "Thighs parallel, back flat against the wall."
        ),
        SeedExercise(
            "calf-raise-bodyweight", "Bodyweight Calf Raise", Equipment.BODYWEIGHT, Force.PUSH,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.CALVES),
            instructions = "Off a step for the full stretch. Very high reps."
        ),
        SeedExercise(
            "superman", "Superman Hold", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.LOWER_BACK), listOf(Muscle.GLUTES, Muscle.REAR_DELTS),
            "Lift the chest and thighs off the floor and hold."
        ),
        SeedExercise(
            "reverse-snow-angel", "Reverse Snow Angel", Equipment.BODYWEIGHT, Force.PULL,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.REAR_DELTS, Muscle.TRAPS), listOf(Muscle.LOWER_BACK),
            "Face down, arms sweeping from hips to overhead without touching the floor."
        )
    )
}
