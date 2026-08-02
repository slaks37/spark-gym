package com.sparkgym.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Schema migrations.
 *
 * The database used to be built with `fallbackToDestructiveMigration()`, which
 * is fine while nobody is using the app and catastrophic the moment somebody
 * is: it answers "the schema moved" by deleting every workout, set, personal
 * record, food entry and quest the user ever logged. Training history is the
 * one thing this app holds that cannot be re-downloaded or re-derived.
 *
 * So every version bump gets a real migration, and the builder no longer has a
 * destructive fallback to fall into.
 */

/**
 * 1 → 2: routine folders, progress photos, supersets, and set types.
 *
 * The interesting part is `set_logs`. Version 1 had a boolean `isWarmup`;
 * version 2 replaces it with a richer `setType` (normal / warm-up / drop set /
 * failure). SQLite before 3.35 cannot drop a column, and Room validates the
 * final shape exactly, so the table is rebuilt — and every set previously
 * flagged as a warm-up is carried across as `WARMUP` rather than quietly
 * becoming a working set, which would inflate historical volume and PRs.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // ---- new tables ------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `routine_folders` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `colorHex` TEXT
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `progress_photos` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `dateEpochDay` INTEGER NOT NULL,
                `weightKg` REAL,
                `imageUri` TEXT NOT NULL
            )
            """.trimIndent()
        )

        // ---- added columns ---------------------------------------------
        db.execSQL("ALTER TABLE `exercises` ADD COLUMN `imageUri` TEXT")
        db.execSQL("ALTER TABLE `routines` ADD COLUMN `folderId` INTEGER")
        db.execSQL("ALTER TABLE `routine_exercises` ADD COLUMN `supersetId` TEXT")

        // ---- set_logs: isWarmup (boolean) becomes setType (enum) --------
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `set_logs_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `sessionId` INTEGER NOT NULL,
                `exerciseId` INTEGER NOT NULL,
                `orderIndex` INTEGER NOT NULL,
                `setNumber` INTEGER NOT NULL,
                `weightKg` REAL NOT NULL,
                `reps` INTEGER NOT NULL,
                `durationSeconds` INTEGER NOT NULL,
                `distanceMeters` REAL NOT NULL,
                `rpe` REAL,
                `setType` TEXT NOT NULL,
                `isCompleted` INTEGER NOT NULL,
                `completedAt` INTEGER,
                `isPersonalRecord` INTEGER NOT NULL,
                `supersetId` TEXT,
                FOREIGN KEY(`sessionId`) REFERENCES `workout_sessions`(`id`)
                    ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`exerciseId`) REFERENCES `exercises`(`id`)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `set_logs_new` (
                id, sessionId, exerciseId, orderIndex, setNumber, weightKg, reps,
                durationSeconds, distanceMeters, rpe, setType, isCompleted,
                completedAt, isPersonalRecord, supersetId
            )
            SELECT
                id, sessionId, exerciseId, orderIndex, setNumber, weightKg, reps,
                durationSeconds, distanceMeters, rpe,
                CASE WHEN isWarmup = 1 THEN 'WARMUP' ELSE 'NORMAL' END,
                isCompleted, completedAt, isPersonalRecord, NULL
            FROM `set_logs`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `set_logs`")
        db.execSQL("ALTER TABLE `set_logs_new` RENAME TO `set_logs`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_set_logs_sessionId` ON `set_logs` (`sessionId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_set_logs_exerciseId` ON `set_logs` (`exerciseId`)")
    }
}

/** Everything, in order, for the database builder. */
val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2)
