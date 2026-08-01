package com.sparkgym.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.sparkgym.data.seed.SeedStretch
import com.sparkgym.domain.model.Meal
import com.sparkgym.domain.model.Muscle
import com.sparkgym.domain.model.MuscleGroup

/**
 * Bilingual string table.  Every user-visible label lives here so flipping
 * the language immediately updates every screen without touching resource XML.
 *
 * Usage in a @Composable:
 * ```
 *   Text(S.training)
 * ```
 */
object S {

    private val lang: AppLanguage
        @Composable @ReadOnlyComposable get() = LocalAppLanguage.current

    // ── Navigation & tabs ────────────────────────────────────────────
    val status @Composable @ReadOnlyComposable get() = pick("Status", "Status")
    val coach @Composable @ReadOnlyComposable get() = pick("Coach", "Pelatih")
    val quests @Composable @ReadOnlyComposable get() = pick("Quests", "Misi")
    val train @Composable @ReadOnlyComposable get() = pick("Train", "Latihan")
    val body @Composable @ReadOnlyComposable get() = pick("Body", "Tubuh")
    val fuel @Composable @ReadOnlyComposable get() = pick("Fuel", "Nutrisi")

    // ── Workout screen ───────────────────────────────────────────────
    val training @Composable @ReadOnlyComposable get() = pick("Training", "Latihan")
    val sessionInProgress @Composable @ReadOnlyComposable get() = pick("Session in progress", "Sesi sedang berlangsung")
    val resume @Composable @ReadOnlyComposable get() = pick("Resume", "Lanjut")
    val bodyweight @Composable @ReadOnlyComposable get() = pick("Bodyweight", "Tanpa Alat")
    val bodyweightDesc @Composable @ReadOnlyComposable get() = pick("No equipment needed — home circuits", "Tanpa alat — latihan di rumah")
    val routines @Composable @ReadOnlyComposable get() = pick("Routines", "Rutinitas")
    val library @Composable @ReadOnlyComposable get() = pick("Library", "Koleksi")
    val history @Composable @ReadOnlyComposable get() = pick("History", "Riwayat")
    val startEmptySession @Composable @ReadOnlyComposable get() = pick("Start empty session", "Mulai sesi kosong")
    val createRoutine @Composable @ReadOnlyComposable get() = pick("Create routine", "Buat rutinitas")
    val searchExercises @Composable @ReadOnlyComposable get() = pick("Search exercises", "Cari latihan")
    val favourites @Composable @ReadOnlyComposable get() = pick("Favourites", "Favorit")
    val homeOnly @Composable @ReadOnlyComposable get() = pick("Home only", "Rumah saja")
    val noMatchFilter @Composable @ReadOnlyComposable get() = pick("Nothing matches those filters.", "Tidak ada yang cocok dengan filter.")
    val noHistoryYet @Composable @ReadOnlyComposable get() = pick("No sessions logged yet. Your history starts with the next one.", "Belum ada sesi. Riwayat dimulai dari sesi berikutnya.")
    val volumeLast30 @Composable @ReadOnlyComposable get() = pick("Volume — last 30 days", "Volume — 30 hari terakhir")
    val personalRecords @Composable @ReadOnlyComposable get() = pick("Personal records", "Rekor pribadi")
    val days @Composable @ReadOnlyComposable get() = pick("days", "hari")
    val sets @Composable @ReadOnlyComposable get() = pick("sets", "set")
    val home @Composable @ReadOnlyComposable get() = pick("home", "rumah")

    // ── Routine detail ───────────────────────────────────────────────
    val routine @Composable @ReadOnlyComposable get() = pick("Routine", "Rutinitas")
    val setAsActive @Composable @ReadOnlyComposable get() = pick("Set as active routine", "Jadikan rutinitas aktif")
    val start @Composable @ReadOnlyComposable get() = pick("Start", "Mulai")
    val pickDay @Composable @ReadOnlyComposable get() = pick("Pick a day to see its exercises.", "Pilih hari untuk melihat latihan.")
    val rest @Composable @ReadOnlyComposable get() = pick("rest", "istirahat")

    // ── Create routine ───────────────────────────────────────────────
    val routineName @Composable @ReadOnlyComposable get() = pick("Routine name", "Nama rutinitas")
    val description @Composable @ReadOnlyComposable get() = pick("Description", "Deskripsi")
    val goal @Composable @ReadOnlyComposable get() = pick("Goal", "Tujuan")
    val level @Composable @ReadOnlyComposable get() = pick("Level", "Level")
    val daysPerWeek @Composable @ReadOnlyComposable get() = pick("Days / week", "Hari / minggu")
    val addDay @Composable @ReadOnlyComposable get() = pick("Add day", "Tambah hari")
    val dayName @Composable @ReadOnlyComposable get() = pick("Day name", "Nama hari")
    val focus @Composable @ReadOnlyComposable get() = pick("Focus", "Fokus")
    val addExercise @Composable @ReadOnlyComposable get() = pick("Add exercise", "Tambah latihan")
    val targetSets @Composable @ReadOnlyComposable get() = pick("Sets", "Set")
    val repsRange @Composable @ReadOnlyComposable get() = pick("Reps", "Repetisi")
    val restSeconds @Composable @ReadOnlyComposable get() = pick("Rest (s)", "Istirahat (d)")
    val save @Composable @ReadOnlyComposable get() = pick("Save", "Simpan")
    val cancel @Composable @ReadOnlyComposable get() = pick("Cancel", "Batal")
    val next @Composable @ReadOnlyComposable get() = pick("Next", "Lanjut")
    val back @Composable @ReadOnlyComposable get() = pick("Back", "Kembali")
    val done @Composable @ReadOnlyComposable get() = pick("Done", "Selesai")
    val delete @Composable @ReadOnlyComposable get() = pick("Delete", "Hapus")
    val deleteRoutineTitle @Composable @ReadOnlyComposable get() = pick("Delete routine?", "Hapus rutinitas?")
    val deleteRoutineBody @Composable @ReadOnlyComposable get() = pick("This custom routine will be removed. Past sessions are kept.", "Rutinitas ini akan dihapus. Riwayat sesi tetap tersimpan.")
    val strength @Composable @ReadOnlyComposable get() = pick("Strength", "Kekuatan")
    val hypertrophy @Composable @ReadOnlyComposable get() = pick("Hypertrophy", "Hipertrofi")
    val endurance @Composable @ReadOnlyComposable get() = pick("Endurance", "Daya Tahan")
    val beginner @Composable @ReadOnlyComposable get() = pick("Beginner", "Pemula")
    val intermediate @Composable @ReadOnlyComposable get() = pick("Intermediate", "Menengah")
    val advanced @Composable @ReadOnlyComposable get() = pick("Advanced", "Lanjutan")
    val custom @Composable @ReadOnlyComposable get() = pick("Custom", "Kustom")

    // ── Status / Hunter screen ───────────────────────────────────────
    val steps @Composable @ReadOnlyComposable get() = pick("Steps", "Langkah")
    val kcalIn @Composable @ReadOnlyComposable get() = pick("Kcal in", "Kcal masuk")
    val kcalOut @Composable @ReadOnlyComposable get() = pick("Kcal out", "Kcal keluar")
    val dailyQuestBoard @Composable @ReadOnlyComposable get() = pick("Daily quest board", "Papan misi harian")
    val complete @Composable @ReadOnlyComposable get() = pick("complete", "selesai")
    val boardCleared @Composable @ReadOnlyComposable get() = pick("Board cleared. The System is satisfied.", "Papan selesai. Sistem puas.")
    val muscleHeatMap @Composable @ReadOnlyComposable get() = pick("Muscle heat map", "Peta otot")
    val openMap @Composable @ReadOnlyComposable get() = pick("Open map", "Buka peta")
    val logSessionHint @Composable @ReadOnlyComposable get() = pick("Log a session to light up the map.", "Log sesi untuk menerangi peta.")
    val gatesCleared @Composable @ReadOnlyComposable get() = pick("Gates cleared", "Gerbang selesai")
    val streak @Composable @ReadOnlyComposable get() = pick("Streak", "Beruntun")
    val weekVolume @Composable @ReadOnlyComposable get() = pick("Week volume", "Volume minggu")
    val achievements @Composable @ReadOnlyComposable get() = pick("Achievements", "Pencapaian")
    val attributes @Composable @ReadOnlyComposable get() = pick("Attributes", "Atribut")
    val rank @Composable @ReadOnlyComposable get() = pick("Rank", "Peringkat")

    // ── Profile screen ───────────────────────────────────────────────
    val profile @Composable @ReadOnlyComposable get() = pick("Profile", "Profil")
    val you @Composable @ReadOnlyComposable get() = pick("You", "Kamu")
    val hunterName @Composable @ReadOnlyComposable get() = pick("Hunter name", "Nama hunter")
    val age @Composable @ReadOnlyComposable get() = pick("Age", "Usia")
    val height @Composable @ReadOnlyComposable get() = pick("Height", "Tinggi")
    val weight @Composable @ReadOnlyComposable get() = pick("Weight", "Berat")
    val sex @Composable @ReadOnlyComposable get() = pick("Sex (for the BMR formula)", "Jenis kelamin (untuk rumus BMR)")
    val activity @Composable @ReadOnlyComposable get() = pick("Activity", "Aktivitas")
    val yourDailyTargets @Composable @ReadOnlyComposable get() = pick("Your daily targets", "Target harian kamu")
    val calories @Composable @ReadOnlyComposable get() = pick("Calories", "Kalori")
    val protein @Composable @ReadOnlyComposable get() = pick("Protein", "Protein")
    val carbs @Composable @ReadOnlyComposable get() = pick("Carbs", "Karbohidrat")
    val fat @Composable @ReadOnlyComposable get() = pick("Fat", "Lemak")
    val water @Composable @ReadOnlyComposable get() = pick("Water", "Air")
    val wearablesConnect @Composable @ReadOnlyComposable get() = pick("Wearables and Health Connect", "Perangkat & Health Connect")
    val restTimer @Composable @ReadOnlyComposable get() = pick("Rest timer", "Timer istirahat")
    val language @Composable @ReadOnlyComposable get() = pick("Language", "Bahasa")

    // ── Dialogs ──────────────────────────────────────────────────────
    val acknowledge @Composable @ReadOnlyComposable get() = pick("Acknowledge", "Mengerti")

    // ── Misc ─────────────────────────────────────────────────────────
    val all @Composable @ReadOnlyComposable get() = pick("All", "Semua")
    val exercise @Composable @ReadOnlyComposable get() = pick("Exercise", "Latihan")
    val favourite @Composable @ReadOnlyComposable get() = pick("Favourite", "Favorit")
    val sessions @Composable @ReadOnlyComposable get() = pick("sessions", "sesi")
    val of @Composable @ReadOnlyComposable get() = pick("of", "dari")

    // ── Exercise detail ──────────────────────────────────────────────
    val primaryMuscles @Composable @ReadOnlyComposable get() = pick("Primary", "Otot utama")
    val secondaryMuscles @Composable @ReadOnlyComposable get() = pick("Secondary", "Otot pendukung")
    val howTo @Composable @ReadOnlyComposable get() = pick("How to do it", "Cara melakukan")
    val recentSets @Composable @ReadOnlyComposable get() = pick("Your recent sets", "Set terakhirmu")
    val musclesWorked @Composable @ReadOnlyComposable get() = pick("Muscles worked", "Otot yang dilatih")

    // ── Profile & photo ──────────────────────────────────────────────
    val profilePhoto @Composable @ReadOnlyComposable get() = pick("Profile photo", "Foto profil")
    val changePhoto @Composable @ReadOnlyComposable get() = pick("Change photo", "Ganti foto")
    val removePhoto @Composable @ReadOnlyComposable get() = pick("Remove", "Hapus")
    val tapToAddPhoto @Composable @ReadOnlyComposable get() = pick("Tap to add a photo", "Ketuk untuk menambah foto")

    // ── Stretching ───────────────────────────────────────────────────
    val stretchAfter @Composable @ReadOnlyComposable get() = pick("Stretch after", "Peregangan setelah")
    val stretchWhy @Composable @ReadOnlyComposable get() =
        pick(
            "Hold these after training, never before — stretching a cold muscle before a heavy set reduces the force it can produce.",
            "Lakukan setelah latihan, jangan sebelumnya — meregangkan otot dingin sebelum set berat menurunkan tenaga yang bisa dikeluarkan."
        )

    /** The stretch table carries both languages, so pick the right column. */
    @Composable @ReadOnlyComposable
    fun stretchName(stretch: SeedStretch): String = pick(stretch.nameEn, stretch.nameId)

    @Composable @ReadOnlyComposable
    fun stretchHowTo(stretch: SeedStretch): String = pick(stretch.howToEn, stretch.howToId)


    // ── Library search ───────────────────────────────────────────────
    val tapBodyPart @Composable @ReadOnlyComposable get() =
        pick("Or tap the body part you want to train", "Atau ketuk bagian tubuh yang ingin dilatih")
    val clear @Composable @ReadOnlyComposable get() = pick("Clear", "Hapus")
    val searchHint @Composable @ReadOnlyComposable get() =
        pick("Exercise, muscle or kit — try \"chest\"", "Latihan, otot atau alat — coba \"dada\"")
    val didYouMean @Composable @ReadOnlyComposable get() = pick("Filter by", "Saring menurut")
    val addTo @Composable @ReadOnlyComposable get() = pick("ADD TO", "TAMBAH KE")
    val resultsFor @Composable @ReadOnlyComposable get() = pick("results", "hasil")

    /** Muscle and group names follow the selected language. */
    @Composable @ReadOnlyComposable
    fun muscle(m: Muscle): String = pick(m.displayName, m.nameId)

    @Composable @ReadOnlyComposable
    fun muscleGroup(g: MuscleGroup): String = pick(g.displayName, g.nameId)

    /** Meal-slot names, so the diary and the plans read in the chosen language. */
    @Composable @ReadOnlyComposable
    fun meal(m: Meal): String = pick(m.displayName, m.nameId)

    // ── Helper ───────────────────────────────────────────────────────

    /** Reads [lang] from the composition, so it is composable like its callers. */
    @Composable
    @ReadOnlyComposable
    private fun pick(en: String, id: String): String = when (lang) {
        AppLanguage.EN -> en
        AppLanguage.ID -> id
    }
}
