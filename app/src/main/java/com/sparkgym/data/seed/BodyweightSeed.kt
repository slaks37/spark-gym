package com.sparkgym.data.seed

import com.sparkgym.domain.model.Equipment
import com.sparkgym.domain.model.ExerciseDifficulty as Diff
import com.sparkgym.domain.model.ExerciseForce as Force
import com.sparkgym.domain.model.Muscle
import com.sparkgym.domain.model.TrackingType as Track

/**
 * The no-equipment half of the library.
 *
 * The gym seeds assume a rack and a stack of plates. These assume a floor, a
 * wall and maybe a chair — which is what most people actually have most days.
 * Progression here comes from leverage, tempo and volume rather than load, so
 * the same movement appears in easier and harder guises (wall → incline → knee
 * → full → archer push-up) and the circuits pick the rung that fits the level.
 *
 * Everything is written for [BodyweightWorkouts] to reference by slug.
 */
object BodyweightSeed {

    val exercises: List<SeedExercise> = listOf(

        // ------------------------------------------------ push / upper body
        SeedExercise(
            "wall-push-up", "Wall Push-up", Equipment.BODYWEIGHT, Force.PUSH,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.CHEST), listOf(Muscle.FRONT_DELTS, Muscle.TRICEPS),
            "Hands on the wall at chest height, feet a stride back. The further back the feet, the harder it gets — this is the rung to start on if a knee push-up still folds you."
        ),
        SeedExercise(
            "incline-push-up", "Incline Push-up", Equipment.BODYWEIGHT, Force.PUSH,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.CHEST), listOf(Muscle.FRONT_DELTS, Muscle.TRICEPS, Muscle.ABS),
            "Hands on a table, sofa arm or stair. Keep the body one straight line from ear to heel — the incline removes load, not the plank."
        ),
        SeedExercise(
            "knee-push-up", "Knee Push-up", Equipment.BODYWEIGHT, Force.PUSH,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.CHEST), listOf(Muscle.FRONT_DELTS, Muscle.TRICEPS),
            "Knees down, hips forward so the line runs knee-to-shoulder. Sitting back onto the heels turns this into a half rep."
        ),
        SeedExercise(
            "wide-push-up", "Wide Push-up", Equipment.BODYWEIGHT, Force.PUSH,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.CHEST), listOf(Muscle.FRONT_DELTS),
            "Hands roughly one and a half shoulder widths apart. Shifts the work off the triceps and onto the chest."
        ),
        SeedExercise(
            "archer-push-up", "Archer Push-up", Equipment.BODYWEIGHT, Force.PUSH,
            Diff.ADVANCED, Track.REPS_ONLY,
            listOf(Muscle.CHEST, Muscle.TRICEPS), listOf(Muscle.FRONT_DELTS, Muscle.ABS),
            "Wide hands, lower toward one hand while the other arm straightens. The straight arm assists just enough to make a one-arm push-up reachable."
        ),
        SeedExercise(
            "hindu-push-up", "Hindu Push-up", Equipment.BODYWEIGHT, Force.PUSH,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.CHEST, Muscle.FRONT_DELTS), listOf(Muscle.TRICEPS, Muscle.LATS),
            "Start hips high, swoop the chest through low and finish looking up, then reverse. Half strength work, half shoulder mobility."
        ),
        SeedExercise(
            "shoulder-tap", "Shoulder Tap", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.ABS), listOf(Muscle.FRONT_DELTS, Muscle.OBLIQUES),
            "High plank, tap the opposite shoulder one hand at a time. Widen the feet if the hips start rocking — the point is that they do not."
        ),
        SeedExercise(
            "plank-up-down", "Plank Up-Down", Equipment.BODYWEIGHT, Force.PUSH,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.ABS, Muscle.TRICEPS), listOf(Muscle.FRONT_DELTS, Muscle.CHEST),
            "Forearm plank to high plank and back, leading with alternate arms each rep. Count one up-and-down as a rep."
        ),
        SeedExercise(
            "wall-handstand-hold", "Wall Handstand Hold", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.ADVANCED, Track.DURATION,
            listOf(Muscle.FRONT_DELTS), listOf(Muscle.TRICEPS, Muscle.TRAPS, Muscle.ABS),
            "Walk the feet up the wall until the chest is close to it. Push the floor away and keep the ribs down rather than arching into the wall."
        ),
        SeedExercise(
            "doorway-row", "Doorway Row", Equipment.BODYWEIGHT, Force.PULL,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.LATS, Muscle.BICEPS), listOf(Muscle.REAR_DELTS, Muscle.TRAPS),
            "Grip a solid door frame, feet close to it, lean back with straight arms and pull the chest to the frame. The closest thing to a row when there is no bar in the house."
        ),
        SeedExercise(
            "towel-curl-isometric", "Self-resisted Curl", Equipment.BODYWEIGHT, Force.PULL,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.BICEPS), listOf(Muscle.FOREARMS),
            "Loop a towel under one foot and curl against it, resisting with the leg. Three seconds up, three seconds down — tension is the only load available."
        ),
        SeedExercise(
            "prone-y-raise", "Prone Y Raise", Equipment.BODYWEIGHT, Force.PULL,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.REAR_DELTS, Muscle.TRAPS), listOf(Muscle.LOWER_BACK),
            "Face down, arms overhead in a Y, thumbs up. Lift the arms a few centimetres and hold — small range, unglamorous, and the best antidote to a day at a desk."
        ),
        SeedExercise(
            "wall-slide", "Wall Slide", Equipment.BODYWEIGHT, Force.PULL,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.TRAPS, Muscle.REAR_DELTS), listOf(Muscle.SIDE_DELTS),
            "Back to the wall, elbows and wrists touching it, slide the arms overhead without letting either lift away. Most people fail this on rep one and that is the information."
        ),

        // ---------------------------------------------------------- core
        SeedExercise(
            "hollow-hold", "Hollow Body Hold", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.INTERMEDIATE, Track.DURATION,
            listOf(Muscle.ABS), listOf(Muscle.QUADS),
            "Lower back pressed flat into the floor, shoulders and legs lifted. Drop the legs lower to make it harder, raise them to make it survivable."
        ),
        SeedExercise(
            "hollow-rock", "Hollow Rock", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.ADVANCED, Track.REPS_ONLY,
            listOf(Muscle.ABS), listOf(Muscle.OBLIQUES, Muscle.QUADS),
            "Hold the hollow shape and rock from shoulders to hips. The shape must not change — if it does, go back to the static hold."
        ),
        SeedExercise(
            "flutter-kick", "Flutter Kick", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.ABS), listOf(Muscle.QUADS),
            "On your back, hands under the hips, legs straight and alternating in small scissors. Stop the moment the lower back lifts off the floor."
        ),
        SeedExercise(
            "leg-raise-floor", "Lying Leg Raise", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.ABS), listOf(Muscle.ADDUCTORS),
            "Legs together, lower them slowly to just above the floor and lift back. Bend the knees to shorten the lever if the back complains."
        ),
        SeedExercise(
            "reverse-crunch", "Reverse Crunch", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.ABS), listOf(Muscle.OBLIQUES),
            "Curl the hips off the floor toward the ribs rather than swinging the legs. Small, deliberate, and far harder than it looks when done without momentum."
        ),
        SeedExercise(
            "bicycle-crunch", "Bicycle Crunch", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.OBLIQUES, Muscle.ABS), emptyList(),
            "Opposite elbow toward opposite knee, the other leg extended. Rotate through the ribcage, not by yanking on the neck. Count both sides as one rep."
        ),
        SeedExercise(
            "v-up", "V-Up", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.ABS), listOf(Muscle.QUADS),
            "Arms and legs meet over the hips in one movement. Bend the knees into a tuck-up if the straight-leg version turns into a sit-up with extra flailing."
        ),
        SeedExercise(
            "plank-jack", "Plank Jack", Equipment.BODYWEIGHT, Force.CARDIO,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.ABS), listOf(Muscle.FRONT_DELTS, Muscle.ABDUCTORS),
            "High plank, jump the feet wide and back together. The hips should stay level — the jack happens below them, not through them."
        ),
        SeedExercise(
            "plank-knee-tuck", "Plank Knee Tuck", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.ABS), listOf(Muscle.OBLIQUES, Muscle.FRONT_DELTS),
            "From a high plank draw one knee under the chest and return it, slowly. The slow version trains the core; the fast version is a mountain climber."
        ),
        SeedExercise(
            "bird-dog", "Bird Dog", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.LOWER_BACK, Muscle.ABS), listOf(Muscle.GLUTES),
            "On all fours, extend the opposite arm and leg and pause. If the hips tilt, you have gone further than your core can currently hold."
        ),
        SeedExercise(
            "side-plank-dip", "Side Plank Dip", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.OBLIQUES), listOf(Muscle.ABS, Muscle.ABDUCTORS),
            "From a side plank, lower the hip toward the floor and drive it back up. Per side."
        ),
        SeedExercise(
            "heel-tap", "Heel Tap", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.OBLIQUES), emptyList(),
            "Lying with knees bent and shoulders just off the floor, reach side to side to tap each heel. Both sides make one rep."
        ),

        // --------------------------------------------------------- lower body
        SeedExercise(
            "chair-squat", "Chair Squat", Equipment.BODYWEIGHT, Force.SQUAT,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.QUADS, Muscle.GLUTES), listOf(Muscle.HAMSTRINGS),
            "Sit back until the seat just touches, then stand without rocking forward. The chair sets the depth and removes the fear of not getting back up."
        ),
        SeedExercise(
            // Distinct from the loaded "reverse-lunge" in ExerciseSeedAdvanced: same
            // pattern, but tracked as reps only so a circuit never asks for a dumbbell.
            "reverse-lunge-bodyweight", "Bodyweight Reverse Lunge", Equipment.BODYWEIGHT, Force.SQUAT,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.QUADS, Muscle.GLUTES), listOf(Muscle.HAMSTRINGS, Muscle.ADDUCTORS),
            "Step back rather than forward — the knee tracks better and it is far kinder to cranky joints. Count each leg."
        ),
        SeedExercise(
            "lateral-lunge", "Lateral Lunge", Equipment.BODYWEIGHT, Force.SQUAT,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.ADDUCTORS, Muscle.QUADS), listOf(Muscle.GLUTES, Muscle.ABDUCTORS),
            "Step wide, sit into that hip and keep the trailing leg straight. Almost nothing else trains the inner thigh without a machine."
        ),
        SeedExercise(
            "split-squat", "Split Squat", Equipment.BODYWEIGHT, Force.SQUAT,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.QUADS, Muscle.GLUTES), listOf(Muscle.HAMSTRINGS),
            "Feet planted in a stride, drop straight down until the back knee kisses the floor. No stepping — the stance never changes."
        ),
        SeedExercise(
            "cossack-squat", "Cossack Squat", Equipment.BODYWEIGHT, Force.SQUAT,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.ADDUCTORS, Muscle.QUADS), listOf(Muscle.GLUTES, Muscle.HAMSTRINGS),
            "Wide stance, shift entirely onto one bent leg with the other straight and toes up. Hold a door frame for balance until the ankles cooperate."
        ),
        SeedExercise(
            "jump-squat", "Jump Squat", Equipment.BODYWEIGHT, Force.SQUAT,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.QUADS, Muscle.GLUTES), listOf(Muscle.CALVES, Muscle.HAMSTRINGS),
            "Squat, then leave the floor. Land through the whole foot and absorb into the next rep — landing stiff-legged is where knees get angry."
        ),
        SeedExercise(
            "single-leg-glute-bridge", "Single-leg Glute Bridge", Equipment.BODYWEIGHT, Force.HINGE,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.GLUTES), listOf(Muscle.HAMSTRINGS, Muscle.LOWER_BACK),
            "One foot planted, the other knee hugged in. Drive through the heel and keep the pelvis level — the free side wants to drop."
        ),
        SeedExercise(
            "step-up-bodyweight", "Bodyweight Step-up", Equipment.BODYWEIGHT, Force.SQUAT,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.QUADS, Muscle.GLUTES), listOf(Muscle.CALVES, Muscle.HAMSTRINGS),
            "A stair or a solid chair. Drive through the top foot without pushing off the bottom one. Count each leg."
        ),
        SeedExercise(
            "good-morning-bodyweight", "Bodyweight Good Morning", Equipment.BODYWEIGHT, Force.HINGE,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.HAMSTRINGS, Muscle.LOWER_BACK), listOf(Muscle.GLUTES),
            "Hands behind the head, soft knees, hinge at the hip until the hamstrings load. Teaches the hinge pattern before anything heavy touches your back."
        ),
        SeedExercise(
            "donkey-kick", "Donkey Kick", Equipment.BODYWEIGHT, Force.HINGE,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.GLUTES), listOf(Muscle.HAMSTRINGS, Muscle.LOWER_BACK),
            "On all fours, drive one heel toward the ceiling with the knee bent. Stop where the low back would start to arch. Per side."
        ),
        SeedExercise(
            "fire-hydrant", "Fire Hydrant", Equipment.BODYWEIGHT, Force.HINGE,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.ABDUCTORS, Muscle.GLUTES), emptyList(),
            "On all fours, lift the bent knee out to the side. Unremarkable to look at and the difference between hips that hold up under running and hips that do not."
        ),
        SeedExercise(
            "skater-hop", "Skater Hop", Equipment.BODYWEIGHT, Force.CARDIO,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.GLUTES, Muscle.ABDUCTORS), listOf(Muscle.QUADS, Muscle.CALVES),
            "Bound sideways from foot to foot, landing soft and balanced before the next. Both sides make one rep."
        ),
        SeedExercise(
            "wall-sit-calf-raise", "Wall Sit Calf Raise", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.CALVES), listOf(Muscle.QUADS),
            "Hold a wall sit and raise the heels. The quads are already burning, which is the point — the calves get no help."
        ),

        // ------------------------------------------------------ cardio / combat
        SeedExercise(
            "squat-thrust", "Squat Thrust", Equipment.BODYWEIGHT, Force.CARDIO,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.QUADS, Muscle.ABS), listOf(Muscle.FRONT_DELTS, Muscle.GLUTES),
            "A burpee without the push-up or the jump. Hands down, feet back, feet in, stand. The version that lets you keep moving for ten minutes."
        ),
        SeedExercise(
            "butt-kick", "Butt Kick", Equipment.BODYWEIGHT, Force.CARDIO,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.HAMSTRINGS), listOf(Muscle.CALVES, Muscle.GLUTES),
            "Jog on the spot flicking the heels to the glutes. Stay tall — leaning back turns it into a shuffle."
        ),
        SeedExercise(
            "star-jump", "Star Jump", Equipment.BODYWEIGHT, Force.CARDIO,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.QUADS), listOf(Muscle.SIDE_DELTS, Muscle.CALVES, Muscle.GLUTES),
            "From a quarter squat, explode into a wide star and land soft back into the squat."
        ),
        SeedExercise(
            "tuck-jump", "Tuck Jump", Equipment.BODYWEIGHT, Force.CARDIO,
            Diff.ADVANCED, Track.REPS_ONLY,
            listOf(Muscle.QUADS, Muscle.ABS), listOf(Muscle.CALVES, Muscle.GLUTES),
            "Jump and pull both knees to the chest. High impact — skip it on tired legs or a hard floor, the skater hop trains the same quality for less."
        ),
        SeedExercise(
            "lateral-shuffle", "Lateral Shuffle", Equipment.BODYWEIGHT, Force.CARDIO,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.ABDUCTORS, Muscle.QUADS), listOf(Muscle.CALVES, Muscle.GLUTES),
            "Stay low and shuffle two or three steps each way. Sideways work almost nothing else in a home session covers."
        ),
        SeedExercise(
            "shadow-boxing", "Shadow Boxing", Equipment.BODYWEIGHT, Force.CARDIO,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.FRONT_DELTS, Muscle.OBLIQUES), listOf(Muscle.ABS, Muscle.CALVES),
            "Hands up, feet moving, punches thrown from the hips and returned to the chin. Never lock the elbow at the end of a punch."
        ),
        SeedExercise(
            "front-kick", "Front Kick", Equipment.BODYWEIGHT, Force.CARDIO,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.QUADS, Muscle.ABS), listOf(Muscle.GLUTES, Muscle.CALVES),
            "Chamber the knee first, then extend, then re-chamber before the foot lands. Kicking straight from the floor is how hamstrings get pulled. Per side."
        ),
        SeedExercise(
            "knee-strike", "Knee Strike", Equipment.BODYWEIGHT, Force.CARDIO,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.ABS, Muscle.QUADS), listOf(Muscle.OBLIQUES, Muscle.GLUTES),
            "Pull imaginary shoulders down as the knee drives up, and exhale on impact. Per side."
        ),
        SeedExercise(
            "marching-in-place", "March in Place", Equipment.BODYWEIGHT, Force.CARDIO,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.QUADS), listOf(Muscle.ABS, Muscle.CALVES),
            "Knees to hip height, arms swinging. The zero-impact, zero-noise, downstairs-neighbour-approved cardio base."
        ),

        // -------------------------------------------------- mobility / recovery
        SeedExercise(
            "cat-cow", "Cat-Cow", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.LOWER_BACK), listOf(Muscle.ABS, Muscle.TRAPS),
            "On all fours, alternate rounding and arching the spine with the breath. Move one vertebra at a time rather than hinging at one spot."
        ),
        SeedExercise(
            "worlds-greatest-stretch", "World's Greatest Stretch", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.INTERMEDIATE, Track.REPS_ONLY,
            listOf(Muscle.ADDUCTORS, Muscle.HAMSTRINGS), listOf(Muscle.OBLIQUES, Muscle.GLUTES),
            "Deep lunge, drop the back elbow inside the front foot, then rotate the top arm to the ceiling. Hits hips, hamstrings and thoracic spine in one shape. Per side."
        ),
        SeedExercise(
            "hip-flexor-stretch", "Kneeling Hip Flexor Stretch", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.QUADS), listOf(Muscle.GLUTES, Muscle.LOWER_BACK),
            "Half-kneeling, squeeze the back glute and tuck the pelvis before leaning in. Without the tuck you are just arching your lower back."
        ),
        SeedExercise(
            "hamstring-stretch-standing", "Standing Hamstring Stretch", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.HAMSTRINGS), listOf(Muscle.CALVES, Muscle.LOWER_BACK),
            "One heel forward, toes up, hinge from the hip with a flat back. Rounding the spine takes the stretch off the hamstring and puts it on your discs."
        ),
        SeedExercise(
            "thoracic-rotation", "Thoracic Rotation", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.OBLIQUES), listOf(Muscle.TRAPS, Muscle.REAR_DELTS),
            "Side-lying, knees stacked and bent, open the top arm across the floor and follow it with the eyes. Per side."
        ),
        SeedExercise(
            "childs-pose", "Child's Pose", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.LATS, Muscle.LOWER_BACK), emptyList(),
            "Knees wide, hips to heels, arms long. Breathe into the back of the ribs rather than the belly."
        ),
        SeedExercise(
            "downward-dog", "Downward Dog", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.HAMSTRINGS, Muscle.CALVES), listOf(Muscle.LATS, Muscle.FRONT_DELTS),
            "Hips high, spine long. Bend the knees as much as needed — a straight back matters more here than straight legs."
        ),
        SeedExercise(
            "seated-forward-fold", "Seated Forward Fold", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.HAMSTRINGS), listOf(Muscle.LOWER_BACK, Muscle.CALVES),
            "Legs out, hinge forward from the hips and let the head be heavy. Chasing the toes with a rounded back is not the goal."
        ),
        SeedExercise(
            "couch-stretch", "Couch Stretch", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.INTERMEDIATE, Track.DURATION,
            listOf(Muscle.QUADS), listOf(Muscle.GLUTES),
            "Back foot up on a sofa or wall, front foot planted, pelvis tucked and torso tall. Brutal, and the best answer there is to a day spent sitting. Per side."
        ),
        SeedExercise(
            "ankle-circle", "Ankle Circle", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.CALVES), listOf(Muscle.FOREARMS),
            "Slow, full circles in both directions. Two minutes here saves a lot of calf strains later. Per side."
        ),
        SeedExercise(
            "neck-rotation", "Neck Rotation", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.NECK), emptyList(),
            "Slow half-circles, chin to one shoulder and across the chest to the other. Never roll the head backwards."
        ),
        SeedExercise(
            "shoulder-pass-through", "Towel Shoulder Pass-through", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.FRONT_DELTS, Muscle.TRAPS), listOf(Muscle.CHEST),
            "Hold a towel wide, take it overhead and behind, then back. Narrow the grip a little each week as the shoulders allow."
        ),

        // ------------------------------------------------ seated / low impact
        SeedExercise(
            "seated-march", "Seated March", Equipment.BODYWEIGHT, Force.CARDIO,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.QUADS, Muscle.ABS), emptyList(),
            "Sit tall away from the backrest and drive the knees up alternately. Cardio that works on a bad-knee day or from an office chair."
        ),
        SeedExercise(
            "seated-punch", "Seated Punch", Equipment.BODYWEIGHT, Force.CARDIO,
            Diff.BEGINNER, Track.DURATION,
            listOf(Muscle.FRONT_DELTS, Muscle.OBLIQUES), listOf(Muscle.ABS),
            "Punch across the body from a tall seated position, rotating through the ribs. Elbows stay soft at the end of each punch."
        ),
        SeedExercise(
            "seated-leg-extension-bw", "Seated Leg Extension", Equipment.BODYWEIGHT, Force.SQUAT,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.QUADS), emptyList(),
            "Straighten one leg from a seated position, pause for a beat at the top, lower slowly. Per side."
        ),
        SeedExercise(
            "standing-knee-raise", "Standing Knee Raise", Equipment.BODYWEIGHT, Force.STATIC,
            Diff.BEGINNER, Track.REPS_ONLY,
            listOf(Muscle.ABS, Muscle.QUADS), listOf(Muscle.OBLIQUES),
            "Hold a wall if needed, raise one knee to hip height and lower it under control. Abs work without ever going to the floor. Per side."
        )
    )
}
