package com.sparkgym.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.vector.ImageVector
import com.sparkgym.core.util.S

sealed class Destination(val route: String) {
    data object Status : Destination("status")
    data object Coach : Destination("coach")
    data object MealPlans : Destination("meal-plans")
    data object Quests : Destination("quests")
    data object Workout : Destination("workout")
    data object CreateRoutine : Destination("create_routine")
    data object Bodyweight : Destination("bodyweight")
    data object Heatmap : Destination("heatmap")
    data object Nutrition : Destination("nutrition")
    data object Connect : Destination("connect")
    data object Profile : Destination("profile")
    data object Onboarding : Destination("onboarding")
    data object Achievements : Destination("achievements")

    data object Session : Destination("session/{sessionId}") {
        fun of(sessionId: Long) = "session/$sessionId"
        const val ARG = "sessionId"
    }

    data object RoutineDetail : Destination("routine/{routineId}") {
        fun of(routineId: Long) = "routine/$routineId"
        const val ARG = "routineId"
    }

    data object ExerciseDetail : Destination("exercise/{exerciseId}") {
        fun of(exerciseId: Long) = "exercise/$exerciseId"
        const val ARG = "exerciseId"
    }

    data object FoodSearch : Destination("food/{meal}") {
        fun of(meal: String) = "food/$meal"
        const val ARG = "meal"
    }
}

data class BottomTab(
    val destination: Destination,
    val icon: ImageVector
) {
    /** Resolved per composition so flipping the language relabels the bar. */
    val label: String
        @Composable @ReadOnlyComposable get() = when (destination) {
            Destination.Status -> S.status
            Destination.Coach -> S.coach
            Destination.Quests -> S.quests
            Destination.Workout -> S.train
            Destination.Heatmap -> S.body
            Destination.Nutrition -> S.fuel
            else -> ""
        }
}

val bottomTabs = listOf(
    BottomTab(Destination.Status, Icons.Filled.Person),
    BottomTab(Destination.Coach, Icons.Filled.Insights),
    BottomTab(Destination.Quests, Icons.Filled.LocalFireDepartment),
    BottomTab(Destination.Workout, Icons.Filled.FitnessCenter),
    BottomTab(Destination.Heatmap, Icons.Filled.Whatshot),
    BottomTab(Destination.Nutrition, Icons.Filled.Restaurant)
)
