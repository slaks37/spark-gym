package com.sparkgym.data.repository

import com.sparkgym.core.util.Dates
import com.sparkgym.data.local.AchievementEntity
import com.sparkgym.data.local.HunterStateEntity
import com.sparkgym.data.local.QuestEntity
import com.sparkgym.data.local.SparkGymDatabase
import com.sparkgym.data.local.XpEventEntity
import com.sparkgym.domain.engine.AttributeEngine
import com.sparkgym.domain.engine.QuestEngine
import com.sparkgym.domain.engine.XpEngine
import com.sparkgym.domain.model.Achievement
import com.sparkgym.domain.model.AchievementCatalog
import com.sparkgym.domain.model.Attribute
import com.sparkgym.domain.model.DailyQuest
import com.sparkgym.domain.model.HunterProfile
import com.sparkgym.domain.model.LevelCurve
import com.sparkgym.domain.model.QuestMetric
import com.sparkgym.domain.model.QuestSource
import com.sparkgym.domain.model.Rank
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The System. Owns XP, levels, attributes, the daily quest board and the
 * penalty that lands when the board is ignored.
 */
class GameRepository(private val db: SparkGymDatabase) {

    private val dao get() = db.gameDao()

    // ----------------------------------------------------------------- state

    fun observeProfile(): Flow<HunterProfile> = dao.observeState().map { it.toProfile() }

    suspend fun state(): HunterStateEntity = dao.state() ?: HunterStateEntity().also { dao.upsertState(it) }

    private fun HunterStateEntity?.toProfile(): HunterProfile {
        val s = this ?: HunterStateEntity()
        return HunterProfile(
            name = s.name,
            totalXp = s.totalXp,
            attributes = Attribute.entries.associateWith { s.attribute(it) },
            unspentPoints = s.unspentPoints,
            currentStreak = s.currentStreak,
            longestStreak = s.longestStreak,
            hunterClass = s.hunterClass,
            title = s.title
        )
    }

    suspend fun setName(name: String) {
        dao.upsertState(state().copy(name = name.ifBlank { "Hunter" }))
    }

    // -------------------------------------------------------------------- XP

    data class XpResult(
        val awarded: Int,
        val newTotal: Long,
        val levelsGained: IntRange?,
        val pointsGained: Int,
        val newRank: Rank?
    )

    suspend fun awardXp(amount: Int, reason: String): XpResult {
        val current = state()
        val safeAmount = amount.coerceAtLeast(0)
        val newTotal = current.totalXp + safeAmount

        val levels = XpEngine.levelsGained(current.totalXp, newTotal)
        val points = levels?.sumOf { XpEngine.pointsForLevelUp(it) } ?: 0

        val oldRank = Rank.forLevel(LevelCurve.levelForTotalXp(current.totalXp))
        val newRank = Rank.forLevel(LevelCurve.levelForTotalXp(newTotal))

        dao.upsertState(
            current.copy(
                totalXp = newTotal,
                unspentPoints = current.unspentPoints + points
            )
        )
        if (safeAmount > 0) dao.insertXpEvent(XpEventEntity(amount = safeAmount, reason = reason))

        return XpResult(
            awarded = safeAmount,
            newTotal = newTotal,
            levelsGained = levels,
            pointsGained = points,
            newRank = newRank.takeIf { it != oldRank }
        )
    }

    /** XP is never removed below the floor of the current level. */
    suspend fun applyPenalty(amount: Int, reason: String): Int {
        val current = state()
        val level = LevelCurve.levelForTotalXp(current.totalXp)
        val floor = LevelCurve.totalXpForLevel(level)
        val newTotal = (current.totalXp - amount).coerceAtLeast(floor)
        val actual = (current.totalXp - newTotal).toInt()
        if (actual > 0) {
            dao.upsertState(current.copy(totalXp = newTotal))
            dao.insertXpEvent(XpEventEntity(amount = -actual, reason = reason))
        }
        return actual
    }

    fun observeXpEvents() = dao.observeXpEvents()

    // ------------------------------------------------------------ attributes

    suspend fun spendPoint(attribute: Attribute): Boolean {
        val current = state()
        if (current.unspentPoints <= 0) return false
        val value = current.attribute(attribute)
        if (value >= 99) return false
        dao.upsertState(
            current.withAttribute(attribute, value + 1).copy(unspentPoints = current.unspentPoints - 1)
        )
        return true
    }

    /** Recomputes the sheet from real data. Called after a sync or a session. */
    suspend fun recalculateAttributes(inputs: AttributeEngine.Inputs) {
        val current = state()
        val currentMap = Attribute.entries.associateWith { current.attribute(it) }
        val stepped = AttributeEngine.step(currentMap, AttributeEngine.targets(inputs))
        var updated = current
        stepped.forEach { (attr, value) -> updated = updated.withAttribute(attr, value) }
        val rank = Rank.forLevel(LevelCurve.levelForTotalXp(updated.totalXp))
        dao.upsertState(updated.copy(hunterClass = AttributeEngine.classify(stepped, rank)))
    }

    // -------------------------------------------------------------- quests

    fun observeQuests(day: Long = Dates.today()): Flow<List<DailyQuest>> =
        dao.observeQuests(day).map { list -> list.map { it.toDomain() } }

    private fun QuestEntity.toDomain() = DailyQuest(
        id = id,
        metric = metric,
        target = target,
        progress = progress,
        xpReward = xpReward,
        source = source,
        isCore = isCore
    )

    /**
     * Ensures today's board exists. Also settles yesterday: an unfinished core
     * board increments the penalty counter and breaks the streak.
     */
    suspend fun ensureTodaysQuests(level: Int, proteinTargetG: Int?, weightKg: Double?): List<DailyQuest> {
        val today = Dates.today()
        val current = state()

        if (current.lastQuestDay in 1 until today) {
            settleDay(current.lastQuestDay)
        }

        if (dao.questCount(today) == 0) {
            dao.insertQuests(
                QuestEngine.generate(level, proteinTargetG, weightKg).map {
                    QuestEntity(
                        dateEpochDay = today,
                        metric = it.metric,
                        target = it.target,
                        xpReward = it.xpReward,
                        source = it.source,
                        isCore = it.isCore
                    )
                }
            )
        }

        if (state().lastQuestDay != today) {
            dao.upsertState(state().copy(lastQuestDay = today))
        }

        return dao.quests(today).map { it.toDomain() }
    }

    /** Closes out a past day: awards nothing new, but applies the penalty. */
    private suspend fun settleDay(day: Long) {
        val quests = dao.quests(day)
        if (quests.isEmpty()) return
        val core = quests.filter { it.isCore }
        val allCoreDone = core.isNotEmpty() && core.all { it.progress >= it.target }

        val current = state()
        if (allCoreDone) {
            // The streak was already credited the moment the board was cleared,
            // so settling a successful day only needs to clear the penalty count.
            if (current.penaltyDays != 0) dao.upsertState(current.copy(penaltyDays = 0))
        } else {
            val missed = current.penaltyDays + 1
            dao.upsertState(current.copy(currentStreak = 0, penaltyDays = missed))
            applyPenalty(QuestEngine.penalty(missed), "Daily quest failed")
        }
    }

    /** Manual quests (push-ups and friends) increment by tap. */
    suspend fun addQuestProgress(questId: Long, delta: Double): DailyQuest? {
        val today = Dates.today()
        val quest = dao.quests(today).firstOrNull { it.id == questId } ?: return null
        val updated = quest.copy(progress = (quest.progress + delta).coerceAtLeast(0.0))
        dao.updateQuest(updated)
        maybeClaim(updated)
        return updated.toDomain()
    }

    /** Automatic quests are pushed here by the sync and logging paths. */
    suspend fun setQuestProgress(metric: QuestMetric, progress: Double, day: Long = Dates.today()) {
        val quest = dao.quest(day, metric.name) ?: return
        if (quest.source != QuestSource.AUTOMATIC) return
        if (progress <= quest.progress) return
        val updated = quest.copy(progress = progress)
        dao.updateQuest(updated)
        maybeClaim(updated)
    }

    /** Awards a quest's XP exactly once, the moment it is finished. */
    private suspend fun maybeClaim(quest: QuestEntity) {
        if (quest.claimedAt != null) return
        if (quest.progress < quest.target) return
        dao.updateQuest(quest.copy(claimedAt = System.currentTimeMillis()))
        awardXp(quest.xpReward, "Quest: ${quest.metric.displayName}")

        // Board bonus when every core quest is done.
        val quests = dao.quests(quest.dateEpochDay)
        val core = quests.filter { it.isCore }
        if (core.isNotEmpty() && core.all { it.claimedAt != null || it.progress >= it.target }) {
            val bonusKey = "board-${quest.dateEpochDay}"
            if (dao.achievement(bonusKey) == null) {
                dao.unlock(AchievementEntity(bonusKey, System.currentTimeMillis()))
                awardXp(QuestEngine.completionBonus(core.size, true), "Daily quest board cleared")
                val s = state()
                dao.upsertState(
                    s.copy(
                        currentStreak = s.currentStreak + 1,
                        longestStreak = maxOf(s.longestStreak, s.currentStreak + 1),
                        penaltyDays = 0
                    )
                )
            }
        }
    }

    // -------------------------------------------------------- achievements

    fun observeAchievements(): Flow<List<Achievement>> =
        dao.observeAchievements().map { unlocked ->
            val byKey = unlocked.associateBy { it.key }
            AchievementCatalog.all.map { def ->
                Achievement(def.key, def.title, def.description, def.xp, byKey[def.key]?.unlockedAt)
            }
        }

    suspend fun unlockIfNeeded(key: String): Boolean {
        val def = AchievementCatalog.all.firstOrNull { it.key == key } ?: return false
        if (dao.achievement(key) != null) return false
        dao.unlock(AchievementEntity(key, System.currentTimeMillis()))
        awardXp(def.xp, "Achievement: ${def.title}")
        return true
    }

    /** Checks every milestone that can be evaluated from counters. */
    suspend fun evaluateAchievements(
        completedWorkouts: Int,
        totalVolumeKg: Double,
        streak: Int,
        prCount: Int,
        loggedFoodDays: Int
    ): List<String> {
        val unlocked = mutableListOf<String>()
        suspend fun check(key: String, condition: Boolean) {
            if (condition && unlockIfNeeded(key)) unlocked += key
        }

        check("first-blood", completedWorkouts >= 1)
        check("ten-gates", completedWorkouts >= 10)
        check("fifty-gates", completedWorkouts >= 50)
        check("hundred-gates", completedWorkouts >= 100)
        check("tonnage-10k", totalVolumeKg >= 10_000)
        check("tonnage-100k", totalVolumeKg >= 100_000)
        check("tonnage-million", totalVolumeKg >= 1_000_000)
        check("streak-7", streak >= 7)
        check("streak-30", streak >= 30)
        check("streak-100", streak >= 100)
        check("record-breaker", prCount >= 10)
        check("meal-planner", loggedFoodDays >= 7)
        check("nutritionist", loggedFoodDays >= 30)

        return unlocked
    }
}
