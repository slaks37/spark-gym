package com.sparkgym.domain.engine

import com.sparkgym.domain.model.Muscle
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The coach.
 *
 * Everything here is a rule over the user's own logged data — no black box, no
 * "the AI thinks". Each insight states what was measured, what it means, and
 * what to do about it, because advice you cannot act on is just noise.
 *
 * Rules are ordered by how much the user's next session should change if they
 * are true, which is what [Priority] encodes.
 */
object CoachEngine {

    enum class Priority { CRITICAL, HIGH, MEDIUM, LOW, PRAISE }

    enum class Category(val label: String) {
        RECOVERY("Recovery"),
        VOLUME("Volume"),
        BALANCE("Balance"),
        PROGRESSION("Progression"),
        NUTRITION("Nutrition"),
        CONSISTENCY("Consistency"),
        TECHNIQUE("Programming")
    }

    data class Insight(
        val priority: Priority,
        val category: Category,
        val title: String,
        val body: String,
        /** A concrete next step, when there is one. */
        val action: String? = null
    )

    data class Snapshot(
        val heat: Map<Muscle, HeatmapEngine.MuscleHeat>,
        val weeklyEffectiveSets: Double,
        val sessionsThisWeek: Int,
        val daysSinceLastSession: Int,
        val trainingStreakDays: Int,
        val consecutiveTrainingWeeks: Int,
        val stalledLifts: List<String>,
        val recentPrCount: Int,
        val avgSleepMinutes: Double,
        val avgDailySteps: Double,
        val restingHrDelta: Int?,
        val proteinTargetG: Int,
        val avgProteinG: Double,
        val avgCaloriesLogged: Double,
        val calorieTarget: Int,
        val daysLoggedThisWeek: Int,
        val weightTrendKgPerWeek: Double?,
        val goalIsCut: Boolean,
        val goalIsBulk: Boolean,
        val totalSessions: Int
    )

    fun analyse(s: Snapshot): List<Insight> {
        val out = mutableListOf<Insight>()

        out += recoveryRules(s)
        out += consistencyRules(s)
        out += volumeRules(s)
        out += balanceRules(s)
        out += progressionRules(s)
        out += nutritionRules(s)
        out += praise(s)

        return out.sortedBy { it.priority.ordinal }
    }

    // ------------------------------------------------------------- recovery

    private fun recoveryRules(s: Snapshot): List<Insight> {
        val out = mutableListOf<Insight>()

        val deload = ProgressionEngine.deloadCheck(
            weeklyEffectiveSets = s.weeklyEffectiveSets,
            avgSleepMinutes = s.avgSleepMinutes,
            consecutiveTrainingWeeks = s.consecutiveTrainingWeeks,
            stalledLifts = s.stalledLifts.size,
            restingHrTrend = s.restingHrDelta
        )

        if (deload.recommended) {
            out += Insight(
                priority = Priority.CRITICAL,
                category = Category.RECOVERY,
                title = "Take a deload week",
                body = deload.reasons.joinToString(" ") +
                    " These add up to accumulated fatigue, not a motivation problem.",
                action = "Run the bundled Deload Week: half the sets, two-thirds the load, nothing near failure. " +
                    "You will come back stronger in seven days."
            )
        }

        if (s.avgSleepMinutes in 1.0..360.0 && !deload.recommended) {
            out += Insight(
                priority = Priority.HIGH,
                category = Category.RECOVERY,
                title = "Sleep is capping your results",
                body = "You are averaging ${(s.avgSleepMinutes / 60).roundToInt()} hours. Under six, muscle " +
                    "protein synthesis drops, appetite regulation goes, and your top sets suffer before you " +
                    "notice anything else.",
                action = "One extra hour of sleep will do more for your next four weeks than any change to your programme."
            )
        }

        s.restingHrDelta?.let {
            if (it >= 5 && !deload.recommended) {
                out += Insight(
                    priority = Priority.MEDIUM,
                    category = Category.RECOVERY,
                    title = "Resting heart rate is elevated",
                    body = "Up $it bpm on your baseline. That is usually under-recovery, illness coming, " +
                        "or stress from outside the gym.",
                    action = "Keep the load but cut a set or two per exercise this week and see if it settles."
                )
            }
        }

        return out
    }

    // ---------------------------------------------------------- consistency

    private fun consistencyRules(s: Snapshot): List<Insight> {
        val out = mutableListOf<Insight>()

        if (s.totalSessions == 0) {
            out += Insight(
                priority = Priority.HIGH,
                category = Category.CONSISTENCY,
                title = "Start with one session",
                body = "There is nothing logged yet, so there is nothing to analyse. Everything on this " +
                    "screen becomes real the moment you finish a workout.",
                action = "Pick Full Body — 3× Week if you are new, or Push / Pull / Legs if you are not."
            )
            return out
        }

        if (s.daysSinceLastSession >= 7) {
            out += Insight(
                priority = Priority.HIGH,
                category = Category.CONSISTENCY,
                title = "${s.daysSinceLastSession} days since your last session",
                body = "Strength holds up for about two to three weeks off, so nothing is lost yet. What " +
                    "does decay fast is the habit.",
                action = "Do a short, easy session today. Getting back in the room matters more than what you do there."
            )
        } else if (s.sessionsThisWeek == 0 && s.daysSinceLastSession >= 3) {
            out += Insight(
                priority = Priority.MEDIUM,
                category = Category.CONSISTENCY,
                title = "Nothing logged this week yet",
                body = "Three or more days off. Not a problem on its own, but two of these weeks in a row is a trend.",
                action = "Book the next session in now, even if it is a short one."
            )
        }

        if (s.avgDailySteps in 1.0..4000.0) {
            out += Insight(
                priority = Priority.MEDIUM,
                category = Category.CONSISTENCY,
                title = "Daily movement is low",
                body = "Averaging ${s.avgDailySteps.roundToInt()} steps. Non-exercise activity is the largest " +
                    "and most controllable part of daily energy expenditure — far larger than the workout itself.",
                action = "Target 8,000 steps. A twenty-minute walk after your two biggest meals gets you most of the way."
            )
        }

        return out
    }

    // -------------------------------------------------------------- volume

    private fun volumeRules(s: Snapshot): List<Insight> {
        val out = mutableListOf<Insight>()
        if (s.totalSessions == 0) return out

        if (s.weeklyEffectiveSets in 0.1..40.0 && s.sessionsThisWeek >= 2) {
            out += Insight(
                priority = Priority.MEDIUM,
                category = Category.VOLUME,
                title = "Weekly volume is below the growth threshold",
                body = "${s.weeklyEffectiveSets.roundToInt()} effective sets across the whole body. Most " +
                    "muscles need around ten hard sets a week before they respond at all.",
                action = "Add one or two sets to your main lifts rather than adding another training day."
            )
        }

        val overreached = s.heat.values.filter { it.status == HeatmapEngine.Status.OVERREACHED }
        if (overreached.size >= 2) {
            out += Insight(
                priority = Priority.HIGH,
                category = Category.VOLUME,
                title = "${overreached.size} muscles are past their recoverable volume",
                body = overreached.joinToString(", ") { it.muscle.displayName } +
                    " are taking more work than most people recover from. Junk volume does not just fail to " +
                    "help — it eats the recovery your productive sets need.",
                action = "Cut three to four sets a week from each and put that effort into your coldest regions."
            )
        }

        return out
    }

    // ------------------------------------------------------------- balance

    private fun balanceRules(s: Snapshot): List<Insight> {
        val out = mutableListOf<Insight>()
        if (s.totalSessions < 3) return out

        val balance = HeatmapEngine.balanceScore(s.heat)
        val cold = HeatmapEngine.weakestLinks(s.heat, 3).filter { it.intensity < 0.4f }

        if (cold.size >= 2) {
            out += Insight(
                priority = Priority.HIGH,
                category = Category.BALANCE,
                title = "Cold regions on the heat map",
                body = cold.joinToString(", ") { it.muscle.displayName } +
                    " are getting almost no work. Unbalanced training is how physiques end up looking " +
                    "incomplete and how joints end up injured.",
                action = "Put one of these first in your next session, while you are fresh. Whatever goes first " +
                    "gets your best effort — that is the only programming rule that matters here."
            )
        }

        // Classic push/pull imbalance: pressing volume well ahead of rowing.
        val push = (s.heat[Muscle.CHEST]?.effectiveSets ?: 0.0) +
            (s.heat[Muscle.FRONT_DELTS]?.effectiveSets ?: 0.0)
        val pull = (s.heat[Muscle.LATS]?.effectiveSets ?: 0.0) +
            (s.heat[Muscle.REAR_DELTS]?.effectiveSets ?: 0.0)
        if (push > 6 && pull > 0 && push / pull > 1.5) {
            out += Insight(
                priority = Priority.MEDIUM,
                category = Category.BALANCE,
                title = "You press far more than you pull",
                body = "Pushing volume is ${(push / pull * 10).roundToInt() / 10.0}× your pulling volume. " +
                    "Over time that rounds the shoulders forward and is the most common cause of the " +
                    "shoulder pain lifters blame on bench press.",
                action = "Match them. For every pressing set, do a pulling set — and put rear delts and face " +
                    "pulls in three times a week."
            )
        }

        // Quad-dominant leg training with no hamstring work.
        val quads = s.heat[Muscle.QUADS]?.effectiveSets ?: 0.0
        val hams = s.heat[Muscle.HAMSTRINGS]?.effectiveSets ?: 0.0
        if (quads > 8 && hams < quads * 0.5) {
            out += Insight(
                priority = Priority.MEDIUM,
                category = Category.BALANCE,
                title = "Hamstrings are trailing your quads",
                body = "Squats and presses do not train the hamstring meaningfully at the knee. A ratio this " +
                    "far apart is the single best-documented predictor of hamstring strain.",
                action = "Add two sets of seated leg curls and one Romanian deadlift session a week."
            )
        }

        if (balance in 80..100 && s.totalSessions > 10 && cold.isEmpty()) {
            out += Insight(
                priority = Priority.PRAISE,
                category = Category.BALANCE,
                title = "Your training is well balanced",
                body = "Balance score $balance. Nothing is being neglected and nothing is being hammered. " +
                    "This is what a well-run programme looks like on the map.",
                action = null
            )
        }

        return out
    }

    // ---------------------------------------------------------- progression

    private fun progressionRules(s: Snapshot): List<Insight> {
        val out = mutableListOf<Insight>()

        if (s.stalledLifts.isNotEmpty() && s.stalledLifts.size < 3) {
            out += Insight(
                priority = Priority.MEDIUM,
                category = Category.PROGRESSION,
                title = "${s.stalledLifts.joinToString(" and ")} has stalled",
                body = "No improvement in estimated 1RM across the last three sessions. One stalled lift is " +
                    "a programming problem, not a recovery problem.",
                action = "Drop the load 10% and build back over three weeks, or swap to a close variation — " +
                    "a paused or incline version of the same movement — for a month."
            )
        }

        if (s.recentPrCount >= 3) {
            out += Insight(
                priority = Priority.PRAISE,
                category = Category.PROGRESSION,
                title = "${s.recentPrCount} personal records recently",
                body = "Whatever you are doing is working. The temptation now is to change something — don't.",
                action = "Keep the programme and keep adding the smallest jump that still counts."
            )
        }

        if (s.totalSessions in 1..5) {
            out += Insight(
                priority = Priority.LOW,
                category = Category.TECHNIQUE,
                title = "Leave reps in the tank for now",
                body = "In your first month the limit is skill, not muscle. Sets taken to failure with a " +
                    "pattern you have not learned yet just teach you to do it badly under fatigue.",
                action = "Stop every set two reps short of failure and let the weight climb steadily."
            )
        }

        return out
    }

    // ------------------------------------------------------------ nutrition

    private fun nutritionRules(s: Snapshot): List<Insight> {
        val out = mutableListOf<Insight>()

        if (s.daysLoggedThisWeek == 0) {
            out += Insight(
                priority = Priority.MEDIUM,
                category = Category.NUTRITION,
                title = "No food logged this week",
                body = "Training is the stimulus; food is the material. Without one of them the other is wasted.",
                action = "Log three days — not seven. Three honest days tell you almost everything about how you eat."
            )
            return out
        }

        if (s.daysLoggedThisWeek >= 3 && s.avgProteinG > 0) {
            val gap = s.proteinTargetG - s.avgProteinG
            if (gap > 25) {
                out += Insight(
                    priority = Priority.HIGH,
                    category = Category.NUTRITION,
                    title = "Protein is ${gap.roundToInt()} g short every day",
                    body = "Averaging ${s.avgProteinG.roundToInt()} g against a ${s.proteinTargetG} g target. " +
                        "Of everything in nutrition, this is the one that measurably changes how much muscle " +
                        "you keep and build.",
                    action = "Add one protein-anchored meal: 150 g of chicken, two eggs plus tempe, or a scoop " +
                        "of whey. That is the whole fix."
                )
            }
        }

        if (s.daysLoggedThisWeek >= 3 && s.weightTrendKgPerWeek != null) {
            val trend = s.weightTrendKgPerWeek
            when {
                s.goalIsCut && trend > -0.1 -> out += Insight(
                    priority = Priority.HIGH,
                    category = Category.NUTRITION,
                    title = "Weight is not moving on a cut",
                    body = "Trending ${fmtTrend(trend)} per week while eating an average of " +
                        "${s.avgCaloriesLogged.roundToInt()} kcal against a ${s.calorieTarget} target. " +
                        "Either the deficit is not real, or the logging is not complete — it is almost always the second.",
                    action = "Weigh your food for one week, including oil and sauces. If the trend is still flat, " +
                        "cut 200 kcal from your target."
                )
                s.goalIsCut && trend < -1.0 -> out += Insight(
                    priority = Priority.HIGH,
                    category = Category.NUTRITION,
                    title = "You are losing weight too fast",
                    body = "Dropping ${fmtTrend(trend)} per week. Past about 1% of bodyweight a week, an " +
                        "increasing share of what you lose is muscle.",
                    action = "Add 250 kcal back, mostly as carbohydrate around training."
                )
                s.goalIsBulk && trend > 0.6 -> out += Insight(
                    priority = Priority.MEDIUM,
                    category = Category.NUTRITION,
                    title = "Gaining faster than you can build",
                    body = "Up ${fmtTrend(trend)} per week. Nobody builds muscle that fast — the surplus above " +
                        "roughly 0.25 to 0.5 kg a month is fat you will pay to remove later.",
                    action = "Cut 300 kcal and hold. A slower bulk is a shorter cut."
                )
                s.goalIsBulk && trend < 0.05 -> out += Insight(
                    priority = Priority.MEDIUM,
                    category = Category.NUTRITION,
                    title = "Not gaining on a bulk",
                    body = "Trend is ${fmtTrend(trend)} per week. If the scale has been flat for two weeks, " +
                        "you are eating at maintenance regardless of what the target says.",
                    action = "Add 250 kcal a day and reassess in two weeks."
                )
            }
        }

        if (s.daysLoggedThisWeek >= 6) {
            out += Insight(
                priority = Priority.PRAISE,
                category = Category.NUTRITION,
                title = "Food logged ${s.daysLoggedThisWeek} days this week",
                body = "Consistent tracking is the single strongest predictor of whether people reach a body " +
                    "composition goal. You are doing the boring part.",
                action = null
            )
        }

        return out
    }

    // --------------------------------------------------------------- praise

    private fun praise(s: Snapshot): List<Insight> {
        val out = mutableListOf<Insight>()
        if (s.trainingStreakDays >= 7) {
            out += Insight(
                priority = Priority.PRAISE,
                category = Category.CONSISTENCY,
                title = "${s.trainingStreakDays}-day training streak",
                body = "Consistency compounds in a way that no individual session does.",
                action = null
            )
        }
        return out
    }

    /**
     * A single line for the dashboard: the most important thing right now.
     */
    fun headline(insights: List<Insight>): String =
        insights.firstOrNull { it.priority != Priority.PRAISE }?.title
            ?: insights.firstOrNull()?.title
            ?: "Everything looks good. Go and train."

    private fun fmtTrend(kg: Double): String {
        val sign = if (kg >= 0) "+" else "−"
        return "$sign${String.format(java.util.Locale.US, "%.2f", abs(kg))} kg"
    }
}
