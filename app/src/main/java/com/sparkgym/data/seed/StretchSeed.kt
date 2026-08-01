package com.sparkgym.data.seed

import com.sparkgym.domain.model.Muscle

data class SeedStretch(
    val muscle: Muscle,
    val nameEn: String,
    val nameId: String,
    val howToEn: String,
    val howToId: String,
    /** Seconds to hold. 30 is the shortest hold that reliably changes range. */
    val holdSeconds: Int = 30
)

/**
 * One stretch per muscle, keyed off the muscle rather than the exercise.
 *
 * This is deliberate: the stretch you need after a set is decided by what you
 * just worked, not by which of the six rowing variations you picked. Keying it
 * this way means all 239 exercises show the right stretches without a single
 * per-exercise entry to write or keep in sync.
 *
 * Held stretches belong *after* training, not before — static stretching a cold
 * muscle before a heavy set measurably reduces force output.
 */
object StretchSeed {

    val stretches: List<SeedStretch> = listOf(
        SeedStretch(
            Muscle.CHEST, "Doorway Pec Stretch", "Peregangan Dada di Kusen Pintu",
            "Forearm flat on a door frame at shoulder height, step through and rotate away. Chest open, shoulder never shrugging up.",
            "Lengan bawah menempel di kusen setinggi bahu, melangkah maju dan putar badan menjauh. Dada terbuka, bahu jangan naik."
        ),
        SeedStretch(
            Muscle.FRONT_DELTS, "Behind-the-Back Clasp", "Kaitkan Tangan di Belakang",
            "Clasp both hands behind your back, straighten the elbows and lift. Chest up, ribs down.",
            "Kaitkan kedua tangan di belakang punggung, luruskan siku lalu angkat. Dada tegak, tulang rusuk ditarik ke bawah."
        ),
        SeedStretch(
            Muscle.SIDE_DELTS, "Cross-Body Arm Pull", "Tarik Lengan Menyilang Dada",
            "Pull one straight arm across the chest with the opposite hand at the elbow, not the wrist.",
            "Tarik satu lengan lurus menyilang dada, tahan di siku — bukan di pergelangan."
        ),
        SeedStretch(
            Muscle.REAR_DELTS, "Sleeper Stretch", "Sleeper Stretch",
            "Lie on the trained side, shoulder and elbow at 90 degrees, gently rotate the forearm toward the floor.",
            "Berbaring miring pada sisi yang dilatih, bahu dan siku 90 derajat, putar lengan bawah perlahan ke lantai."
        ),
        SeedStretch(
            Muscle.TRICEPS, "Overhead Triceps Stretch", "Peregangan Trisep di Atas Kepala",
            "Hand behind the neck, other hand pulling the elbow back and down. Keep the ribs from flaring.",
            "Tangan di belakang leher, tangan lain menarik siku ke belakang dan bawah. Jaga tulang rusuk tidak terbuka."
        ),
        SeedStretch(
            Muscle.BICEPS, "Wall Biceps Stretch", "Peregangan Bisep di Dinding",
            "Palm flat on a wall behind you at shoulder height, rotate the body away until the biceps lengthens.",
            "Telapak menempel dinding di belakang setinggi bahu, putar badan menjauh sampai bisep terulur."
        ),
        SeedStretch(
            Muscle.FOREARMS, "Wrist Flexor and Extensor Stretch", "Peregangan Pergelangan Tangan",
            "Arm straight, fingers pointing down, gently pull back. Repeat with the fingers pointing up.",
            "Lengan lurus, jari menghadap bawah, tarik perlahan. Ulangi dengan jari menghadap atas.",
            holdSeconds = 20
        ),
        SeedStretch(
            Muscle.ABS, "Cobra Stretch", "Peregangan Kobra",
            "Face down, press the chest up on straight arms, hips staying on the floor. Stop before the lower back pinches.",
            "Telungkup, dorong dada ke atas dengan lengan lurus, pinggul tetap di lantai. Berhenti sebelum punggung bawah terasa terjepit."
        ),
        SeedStretch(
            Muscle.OBLIQUES, "Standing Side Bend", "Membungkuk ke Samping Berdiri",
            "Feet hip width, one arm overhead, lean sideways from the ribcage rather than the hip.",
            "Kaki selebar pinggul, satu lengan ke atas, condongkan dari tulang rusuk bukan dari pinggul."
        ),
        SeedStretch(
            Muscle.LATS, "Hanging Lat Stretch", "Peregangan Lat Menggantung",
            "Hang from a bar with a relaxed shoulder girdle, or kneel and reach forward onto a bench and sink the chest.",
            "Bergantung di palang dengan bahu rileks, atau berlutut lalu julurkan tangan ke bangku dan turunkan dada."
        ),
        SeedStretch(
            Muscle.TRAPS, "Neck Side Stretch", "Peregangan Leher ke Samping",
            "Ear toward the shoulder, opposite hand holding the bench to keep that shoulder down. Never pull hard.",
            "Telinga ke arah bahu, tangan sebelahnya memegang bangku agar bahu tetap turun. Jangan ditarik keras.",
            holdSeconds = 20
        ),
        SeedStretch(
            Muscle.LOWER_BACK, "Child's Pose", "Pose Anak",
            "Kneel, sit back onto the heels and reach the arms forward. Breathe into the lower back.",
            "Berlutut, duduk ke arah tumit dan julurkan lengan ke depan. Bernapas ke arah punggung bawah."
        ),
        SeedStretch(
            Muscle.GLUTES, "Figure-Four Stretch", "Peregangan Angka Empat",
            "On your back, ankle across the opposite knee, pull the supporting thigh toward you.",
            "Telentang, pergelangan kaki disilangkan di atas lutut sebelahnya, tarik paha penopang ke arah dada."
        ),
        SeedStretch(
            Muscle.QUADS, "Couch Stretch", "Couch Stretch",
            "Rear shin up a wall, front foot planted, squeeze the glute on the stretching side to protect the lower back.",
            "Tulang kering belakang menempel dinding, kaki depan berpijak, kencangkan glute sisi yang diregangkan untuk melindungi punggung bawah",
            holdSeconds = 45
        ),
        SeedStretch(
            Muscle.HAMSTRINGS, "Standing Hamstring Reach", "Raihan Hamstring Berdiri",
            "One heel on a low box, hinge from the hip with a flat back. The back must not round to reach further.",
            "Satu tumit di kotak rendah, tekuk dari pinggul dengan punggung rata. Punggung tidak boleh membungkuk demi jangkauan."
        ),
        SeedStretch(
            Muscle.ADDUCTORS, "Frog Stretch", "Peregangan Katak",
            "On hands and knees, knees wide, shins in line with the thighs, rock the hips slowly back.",
            "Bertumpu tangan dan lutut, lutut dibuka lebar, tulang kering sejajar paha, goyangkan pinggul perlahan ke belakang",
            holdSeconds = 45
        ),
        SeedStretch(
            Muscle.ABDUCTORS, "Cross-Leg IT Band Stretch", "Peregangan IT Band Kaki Menyilang",
            "Standing, cross the trained leg behind the other and lean away at the hip.",
            "Berdiri, silangkan kaki yang dilatih ke belakang kaki satunya lalu condongkan pinggul menjauh."
        ),
        SeedStretch(
            Muscle.CALVES, "Wall Calf Stretch", "Peregangan Betis di Dinding",
            "Ball of the foot on a wall, heel down, drive the knee toward the wall. Repeat with a bent knee for the soleus.",
            "Ujung telapak kaki di dinding, tumit menempel lantai, dorong lutut ke dinding. Ulangi dengan lutut ditekuk untuk soleus."
        ),
        SeedStretch(
            Muscle.NECK, "Gentle Neck Rotation", "Putaran Leher Perlahan",
            "Turn the head slowly to one side until you feel a mild pull, hold, then the other. Never force the neck.",
            "Putar kepala perlahan ke satu sisi sampai terasa tarikan ringan, tahan, lalu sisi lain. Jangan pernah dipaksa.",
            holdSeconds = 15
        )
    )

    private val byMuscle: Map<Muscle, SeedStretch> = stretches.associateBy { it.muscle }

    fun forMuscle(muscle: Muscle): SeedStretch? = byMuscle[muscle]

    /** The stretches worth doing after a movement, prime movers first. */
    fun forExercise(primary: Set<Muscle>, secondary: Set<Muscle> = emptySet()): List<SeedStretch> =
        (primary.toList() + secondary.toList()).distinct().mapNotNull { byMuscle[it] }
}
