package com.sparkgym.domain.model

/**
 * Muscle regions the heat map can shade. Kept coarse enough that every region
 * has a drawable silhouette, fine enough that "chest day" and "back day" look
 * visibly different on the body.
 */
enum class Muscle(
    val displayName: String,
    val group: MuscleGroup,
    val side: BodySide
) {
    CHEST("Chest", MuscleGroup.PUSH, BodySide.FRONT),
    FRONT_DELTS("Front Delts", MuscleGroup.PUSH, BodySide.FRONT),
    SIDE_DELTS("Side Delts", MuscleGroup.PUSH, BodySide.FRONT),
    REAR_DELTS("Rear Delts", MuscleGroup.PULL, BodySide.BACK),
    TRICEPS("Triceps", MuscleGroup.PUSH, BodySide.BACK),
    BICEPS("Biceps", MuscleGroup.PULL, BodySide.FRONT),
    FOREARMS("Forearms", MuscleGroup.PULL, BodySide.FRONT),
    ABS("Abs", MuscleGroup.CORE, BodySide.FRONT),
    OBLIQUES("Obliques", MuscleGroup.CORE, BodySide.FRONT),
    LATS("Lats", MuscleGroup.PULL, BodySide.BACK),
    TRAPS("Traps", MuscleGroup.PULL, BodySide.BACK),
    LOWER_BACK("Lower Back", MuscleGroup.PULL, BodySide.BACK),
    GLUTES("Glutes", MuscleGroup.LEGS, BodySide.BACK),
    QUADS("Quads", MuscleGroup.LEGS, BodySide.FRONT),
    HAMSTRINGS("Hamstrings", MuscleGroup.LEGS, BodySide.BACK),
    ADDUCTORS("Adductors", MuscleGroup.LEGS, BodySide.FRONT),
    ABDUCTORS("Abductors", MuscleGroup.LEGS, BodySide.BACK),
    CALVES("Calves", MuscleGroup.LEGS, BodySide.BACK),
    NECK("Neck", MuscleGroup.CORE, BodySide.BACK);

    companion object {
        fun fromKey(key: String): Muscle? = entries.firstOrNull { it.name == key }
    }
}

enum class MuscleGroup(val displayName: String) {
    PUSH("Push"), PULL("Pull"), LEGS("Legs"), CORE("Core")
}

/** Which silhouette a muscle is drawn on. Some are drawn on both. */
enum class BodySide { FRONT, BACK }

enum class Equipment(val displayName: String, val isHomeFriendly: Boolean) {
    BARBELL("Barbell", false),
    DUMBBELL("Dumbbell", true),
    MACHINE("Machine", false),
    CABLE("Cable", false),
    SMITH("Smith Machine", false),
    EZ_BAR("EZ Bar", false),
    KETTLEBELL("Kettlebell", true),
    BAND("Resistance Band", true),
    BODYWEIGHT("Bodyweight", true),
    PULLUP_BAR("Pull-up Bar", true),
    BENCH("Bench", true),
    CARDIO("Cardio Machine", false),
    OTHER("Other", true)
}

enum class ExerciseForce { PUSH, PULL, STATIC, HINGE, SQUAT, CARRY, CARDIO }

enum class ExerciseDifficulty(val displayName: String) {
    BEGINNER("Beginner"), INTERMEDIATE("Intermediate"), ADVANCED("Advanced")
}

/** How a set is measured — the logger renders different inputs per kind. */
enum class TrackingType {
    /** Weight × reps. The default for anything loaded. */
    WEIGHT_REPS,

    /** Reps only, e.g. push-ups. Bodyweight is used for volume maths. */
    REPS_ONLY,

    /** Reps plus optional added weight, e.g. weighted pull-ups / dips. */
    BODYWEIGHT_PLUS,

    /** Seconds under tension, e.g. planks and hangs. */
    DURATION,

    /** Distance + duration, e.g. running and rowing. */
    DISTANCE_DURATION
}
