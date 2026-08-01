package com.sparkgym.domain.model

/**
 * Muscle regions the heat map can shade. Kept coarse enough that every region
 * has a drawable silhouette, fine enough that "chest day" and "back day" look
 * visibly different on the body.
 */
enum class Muscle(
    val displayName: String,
    /** Indonesian name, shown when the app language is ID. */
    val nameId: String,
    val group: MuscleGroup,
    val side: BodySide,
    /**
     * What people actually type. Nobody searching for lat pulldowns types
     * "latissimus" — they type "punggung", "sayap" or "back". Gym slang and
     * everyday body-part words matter more here than anatomical correctness.
     */
    val aliases: List<String> = emptyList()
) {
    CHEST("Chest", "Dada", MuscleGroup.PUSH, BodySide.FRONT, listOf("pecs", "pectoral", "dada atas", "dada bawah")),
    FRONT_DELTS("Front Delts", "Bahu Depan", MuscleGroup.PUSH, BodySide.FRONT, listOf("shoulder", "bahu", "deltoid", "delt")),
    SIDE_DELTS("Side Delts", "Bahu Samping", MuscleGroup.PUSH, BodySide.FRONT, listOf("shoulder", "bahu", "deltoid", "delt", "lateral")),
    REAR_DELTS("Rear Delts", "Bahu Belakang", MuscleGroup.PULL, BodySide.BACK, listOf("shoulder", "bahu", "deltoid", "delt")),
    TRICEPS("Triceps", "Trisep", MuscleGroup.PUSH, BodySide.BACK, listOf("arm", "lengan", "lengan belakang")),
    BICEPS("Biceps", "Bisep", MuscleGroup.PULL, BodySide.FRONT, listOf("arm", "lengan", "lengan depan", "otot lengan")),
    FOREARMS("Forearms", "Lengan Bawah", MuscleGroup.PULL, BodySide.FRONT, listOf("grip", "genggaman", "pergelangan", "arm", "lengan")),
    ABS("Abs", "Perut", MuscleGroup.CORE, BodySide.FRONT, listOf("sixpack", "six pack", "core", "inti", "abdominal")),
    OBLIQUES("Obliques", "Perut Samping", MuscleGroup.CORE, BodySide.FRONT, listOf("pinggang", "core", "inti", "samping")),
    LATS("Lats", "Sayap", MuscleGroup.PULL, BodySide.BACK, listOf("punggung", "back", "latissimus", "punggung samping")),
    TRAPS("Traps", "Trapezius", MuscleGroup.PULL, BodySide.BACK, listOf("punggung", "back", "pundak", "punggung atas")),
    LOWER_BACK("Lower Back", "Punggung Bawah", MuscleGroup.PULL, BodySide.BACK, listOf("punggung", "back", "pinggang", "erector")),
    GLUTES("Glutes", "Bokong", MuscleGroup.LEGS, BodySide.BACK, listOf("pantat", "butt", "pinggul", "glute")),
    QUADS("Quads", "Paha Depan", MuscleGroup.LEGS, BodySide.FRONT, listOf("leg", "kaki", "paha", "quadriceps")),
    HAMSTRINGS("Hamstrings", "Paha Belakang", MuscleGroup.LEGS, BodySide.BACK, listOf("leg", "kaki", "paha", "hamstring")),
    ADDUCTORS("Adductors", "Paha Dalam", MuscleGroup.LEGS, BodySide.FRONT, listOf("leg", "kaki", "paha", "inner thigh", "selangkangan")),
    ABDUCTORS("Abductors", "Paha Luar", MuscleGroup.LEGS, BodySide.BACK, listOf("leg", "kaki", "paha", "outer thigh", "pinggul")),
    CALVES("Calves", "Betis", MuscleGroup.LEGS, BodySide.BACK, listOf("leg", "kaki", "calf")),
    NECK("Neck", "Leher", MuscleGroup.CORE, BodySide.BACK, listOf("neck", "tengkuk"));

    companion object {
        fun fromKey(key: String): Muscle? = entries.firstOrNull { it.name == key }
    }
}

enum class MuscleGroup(val displayName: String, val nameId: String) {
    PUSH("Push", "Dorong"), PULL("Pull", "Tarik"), LEGS("Legs", "Kaki"), CORE("Core", "Inti")
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
