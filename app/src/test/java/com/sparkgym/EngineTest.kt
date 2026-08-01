package com.sparkgym

import com.sparkgym.domain.engine.AttributeEngine
import com.sparkgym.domain.engine.EnergyMath
import com.sparkgym.domain.engine.HeatmapEngine
import com.sparkgym.domain.engine.QuestEngine
import com.sparkgym.domain.engine.StrengthMath
import com.sparkgym.domain.engine.XpEngine
import com.sparkgym.domain.model.Attribute
import com.sparkgym.domain.model.LevelCurve
import com.sparkgym.domain.model.Muscle
import com.sparkgym.domain.model.Rank
import com.sparkgym.domain.model.TrackingType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelCurveTest {

    @Test
    fun `level one starts at zero xp`() {
        assertEquals(0L, LevelCurve.totalXpForLevel(1))
        assertEquals(1, LevelCurve.levelForTotalXp(0))
    }

    @Test
    fun `curve is monotonic and levels round-trip`() {
        for (level in 1..80) {
            val threshold = LevelCurve.totalXpForLevel(level)
            assertEquals("level $level should round-trip", level, LevelCurve.levelForTotalXp(threshold))
            assertTrue(LevelCurve.totalXpForLevel(level + 1) > threshold)
        }
    }

    @Test
    fun `progress within a level stays in range`() {
        val xp = LevelCurve.totalXpForLevel(12) + 5
        val progress = LevelCurve.progressInLevel(xp)
        assertTrue(progress in 0f..1f)
        assertTrue(progress > 0f)
    }

    @Test
    fun `ranks unlock at their thresholds`() {
        assertEquals(Rank.E, Rank.forLevel(1))
        assertEquals(Rank.E, Rank.forLevel(9))
        assertEquals(Rank.D, Rank.forLevel(10))
        assertEquals(Rank.S, Rank.forLevel(62))
        assertEquals(Rank.S, Rank.forLevel(200))
    }
}

class StrengthMathTest {

    @Test
    fun `one rep max of a single is the weight itself`() {
        assertEquals(100.0, StrengthMath.estimatedOneRepMax(100.0, 1), 0.001)
    }

    @Test
    fun `estimated one rep max rises with reps`() {
        val five = StrengthMath.estimatedOneRepMax(100.0, 5)
        val eight = StrengthMath.estimatedOneRepMax(100.0, 8)
        assertTrue(five > 100.0)
        assertTrue(eight > five)
    }

    @Test
    fun `zero or negative inputs produce zero`() {
        assertEquals(0.0, StrengthMath.estimatedOneRepMax(0.0, 5), 0.001)
        assertEquals(0.0, StrengthMath.estimatedOneRepMax(100.0, 0), 0.001)
    }

    @Test
    fun `bodyweight sets are worth something but less than loaded ones`() {
        val pushups = StrengthMath.setVolumeKg(TrackingType.REPS_ONLY, 0.0, 20, 0, 0.0, 80.0)
        val bench = StrengthMath.setVolumeKg(TrackingType.WEIGHT_REPS, 80.0, 20, 0, 0.0, 80.0)
        assertTrue(pushups > 0)
        assertTrue(pushups < bench)
    }

    @Test
    fun `weighted pullups count bodyweight plus the added load`() {
        val volume = StrengthMath.setVolumeKg(TrackingType.BODYWEIGHT_PLUS, 20.0, 5, 0, 0.0, 80.0)
        assertEquals((80.0 + 20.0) * 5, volume, 0.001)
    }

    @Test
    fun `plate maths splits a hundred kilo bar correctly`() {
        val plates = StrengthMath.platesPerSide(100.0)
        assertEquals(40.0, plates.sum(), 0.001)
        assertEquals(listOf(25.0, 15.0), plates)
    }

    @Test
    fun `an empty bar needs no plates`() {
        assertTrue(StrengthMath.platesPerSide(20.0).isEmpty())
        assertTrue(StrengthMath.platesPerSide(10.0).isEmpty())
    }
}

class EnergyMathTest {

    @Test
    fun `mifflin st jeor matches the published formula`() {
        // 10*80 + 6.25*180 - 5*30 + 5 = 1780
        val bmr = EnergyMath.bmr(EnergyMath.Sex.MALE, 80.0, 180.0, 30)
        assertEquals(1780.0, bmr, 0.001)
    }

    @Test
    fun `cutting budgets fewer calories than bulking`() {
        val tdee = 2500.0
        val cut = EnergyMath.macroTarget(tdee, EnergyMath.Goal.CUT, 80.0)
        val bulk = EnergyMath.macroTarget(tdee, EnergyMath.Goal.BULK, 80.0)
        assertTrue(cut.calories < bulk.calories)
    }

    @Test
    fun `macros roughly add back up to the calorie target`() {
        val target = EnergyMath.macroTarget(2500.0, EnergyMath.Goal.MAINTAIN, 80.0)
        val fromMacros = target.proteinG * 4 + target.carbsG * 4 + target.fatG * 9
        assertTrue(kotlin.math.abs(fromMacros - target.calories) < 25)
    }

    @Test
    fun `training days get extra water`() {
        assertTrue(
            EnergyMath.waterTargetMl(80.0, trainedToday = true) >
                EnergyMath.waterTargetMl(80.0, trainedToday = false)
        )
    }
}

class XpEngineTest {

    @Test
    fun `a session with no completed sets earns nothing`() {
        val reward = XpEngine.sessionXp(0, 0.0, 0, 0, 0)
        assertEquals(0, reward.total)
        assertTrue(reward.breakdown.isEmpty())
    }

    @Test
    fun `records and volume both increase the reward`() {
        val plain = XpEngine.sessionXp(20, 5000.0, 60, 0, 0)
        val withPr = XpEngine.sessionXp(20, 5000.0, 60, 2, 0)
        assertTrue(withPr.total > plain.total)
    }

    @Test
    fun `streak multiplier is capped`() {
        assertTrue(XpEngine.streakMultiplier(0) == 1.0)
        assertTrue(XpEngine.streakMultiplier(365) <= 1.5)
        assertTrue(XpEngine.streakMultiplier(30) > XpEngine.streakMultiplier(3))
    }

    @Test
    fun `levels gained is null when no threshold was crossed`() {
        assertNull(XpEngine.levelsGained(0, 10))
        assertNotNull(XpEngine.levelsGained(0, LevelCurve.totalXpForLevel(3)))
    }
}

class QuestEngineTest {

    @Test
    fun `targets scale with level but respect caps`() {
        val early = QuestEngine.generate(1, null, null)
        val late = QuestEngine.generate(90, null, null)

        val earlyPushups = early.first { it.metric.name == "PUSHUPS" }.target
        val latePushups = late.first { it.metric.name == "PUSHUPS" }.target

        assertTrue(latePushups > earlyPushups)
        assertTrue("push-ups must cap at 100", latePushups <= 100.0)
    }

    @Test
    fun `protein quest follows the users own target`() {
        val quests = QuestEngine.generate(5, proteinTargetG = 165, weightKg = 75.0)
        val protein = quests.first { it.metric.name == "PROTEIN_G" }
        assertEquals(165.0, protein.target, 0.001)
    }

    @Test
    fun `penalty grows with missed days and is capped`() {
        assertTrue(QuestEngine.penalty(1) < QuestEngine.penalty(3))
        assertEquals(150, QuestEngine.penalty(20))
    }

    @Test
    fun `core quests are always generated`() {
        val quests = QuestEngine.generate(1, null, null, includeSupport = false)
        assertEquals(4, quests.size)
        assertTrue(quests.all { it.isCore })
    }
}

class HeatmapEngineTest {

    @Test
    fun `an untrained muscle is cold`() {
        val heat = HeatmapEngine.build(emptyMap())
        val chest = heat.getValue(Muscle.CHEST)
        assertEquals(0f, chest.intensity, 0.001f)
        assertEquals(HeatmapEngine.Status.UNTRAINED, chest.status)
    }

    @Test
    fun `hitting the target lands in the productive band`() {
        val target = HeatmapEngine.targetFor(Muscle.CHEST)
        val heat = HeatmapEngine.build(mapOf(Muscle.CHEST to target * 0.8))
        assertEquals(HeatmapEngine.Status.OPTIMAL, heat.getValue(Muscle.CHEST).status)
    }

    @Test
    fun `far past the target reads as overreaching`() {
        val target = HeatmapEngine.targetFor(Muscle.CHEST)
        val heat = HeatmapEngine.build(mapOf(Muscle.CHEST to target * 1.6))
        assertEquals(HeatmapEngine.Status.OVERREACHED, heat.getValue(Muscle.CHEST).status)
    }

    @Test
    fun `a 30 day window is normalised back to a weekly rate`() {
        val target = HeatmapEngine.targetFor(Muscle.QUADS)
        // Four weeks of exactly-on-target training must not read as 4x the target.
        val heat = HeatmapEngine.build(mapOf(Muscle.QUADS to target * 4), days = 28)
        assertEquals(1f, heat.getValue(Muscle.QUADS).intensity, 0.05f)
    }

    @Test
    fun `balance score rewards even coverage`() {
        val even = Muscle.entries.associateWith { HeatmapEngine.targetFor(it) }
        val lopsided = mapOf(Muscle.CHEST to 30.0, Muscle.BICEPS to 25.0)

        assertTrue(
            HeatmapEngine.balanceScore(HeatmapEngine.build(even)) >
                HeatmapEngine.balanceScore(HeatmapEngine.build(lopsided))
        )
    }

    @Test
    fun `weakest links surface the coldest muscles`() {
        // build() scores every muscle, not just the ones passed in, so a partial map
        // would leave a dozen never-trained muscles tied at zero ahead of these two.
        val sets = Muscle.entries.associateWith { HeatmapEngine.targetFor(it) } +
            mapOf(Muscle.CALVES to 0.0, Muscle.HAMSTRINGS to 1.0)

        val weakest = HeatmapEngine.weakestLinks(HeatmapEngine.build(sets), 2).map { it.muscle }
        assertEquals(listOf(Muscle.CALVES, Muscle.HAMSTRINGS), weakest)
    }
}

class AttributeEngineTest {

    private val emptyInputs = AttributeEngine.Inputs(
        weeklyEffectiveSets = 0.0,
        weeklyVolumeKg = 0.0,
        bestRelativeStrength = 0.0,
        avgDailySteps = 0.0,
        weeklyCardioMinutes = 0.0,
        weeklyActiveCalories = 0.0,
        nutritionLoggedDays = 0,
        proteinTargetHitDays = 0,
        avgSleepMinutes = 0.0,
        restingHeartRate = null
    )

    @Test
    fun `attributes never fall below the floor`() {
        val targets = AttributeEngine.targets(emptyInputs)
        assertTrue(targets.values.all { it >= 10 })
    }

    @Test
    fun `attributes step up by at most one point at a time`() {
        val current = Attribute.entries.associateWith { 10 }
        val targets = Attribute.entries.associateWith { 99 }
        val stepped = AttributeEngine.step(current, targets)
        assertTrue(stepped.values.all { it == 11 })
    }

    @Test
    fun `attributes never decrease`() {
        val current = Attribute.entries.associateWith { 50 }
        val stepped = AttributeEngine.step(current, AttributeEngine.targets(emptyInputs))
        assertTrue(stepped.values.all { it == 50 })
    }

    @Test
    fun `a balanced sheet classifies as fighter`() {
        val balanced = Attribute.entries.associateWith { 40 }
        assertEquals(
            com.sparkgym.domain.model.HunterClass.FIGHTER,
            AttributeEngine.classify(balanced, Rank.C)
        )
    }

    @Test
    fun `a strength dominant sheet classifies as tank`() {
        val sheet = Attribute.entries.associateWith { 20 }.toMutableMap()
        sheet[Attribute.STRENGTH] = 80
        assertEquals(
            com.sparkgym.domain.model.HunterClass.TANK,
            AttributeEngine.classify(sheet, Rank.C)
        )
    }
}
