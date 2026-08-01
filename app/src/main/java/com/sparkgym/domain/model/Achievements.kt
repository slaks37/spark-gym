package com.sparkgym.domain.model

/**
 * The achievement catalog.
 *
 * Static domain data, so it lives here rather than next to the Room repository
 * that awards it — that also lets the key set be unit-tested, which matters
 * because the badge art in the UI is matched by these exact strings.
 */
object AchievementCatalog {
    data class Definition(val key: String, val title: String, val description: String, val xp: Int)

    val all = listOf(
        Definition("first-blood", "First Gate Cleared", "Finish your first workout", 50),
        Definition("ten-gates", "Ten Gates", "Finish 10 workouts", 120),
        Definition("fifty-gates", "Gate Hunter", "Finish 50 workouts", 300),
        Definition("hundred-gates", "Gate Breaker", "Finish 100 workouts", 600),
        Definition("tonnage-10k", "Ten Tonnes", "Move 10,000 kg in total", 100),
        Definition("tonnage-100k", "Hundred Tonnes", "Move 100,000 kg in total", 250),
        Definition("tonnage-million", "Monarch's Load", "Move 1,000,000 kg in total", 800),
        Definition("streak-7", "Seven Days", "Train seven days in a row", 150),
        Definition("streak-30", "Thirty Days", "Train thirty days in a row", 400),
        Definition("streak-100", "Unbroken", "Train one hundred days in a row", 1000),
        Definition("record-breaker", "Record Breaker", "Set 10 personal records", 200),
        Definition("meal-planner", "Fuel Discipline", "Log food for 7 days", 120),
        Definition("nutritionist", "Macro Monarch", "Log food for 30 days", 350)
    )
}
