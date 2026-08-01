package com.sparkgym.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destination(val route: String) {
    data object Status : Destination("status")
    data object Coach : Destination("coach")
    data object MealPlans : Destination("meal-plans")
    data object Quests : Destination("quests")
    data object Workout : Destination("workout")
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
    val label: String,
    val icon: ImageVector
)

val bottomTabs = listOf(
    BottomTab(Destination.Status, "Status", Icons.Filled.Person),
    BottomTab(Destination.Coach, "Coach", Icons.Filled.Insights),
    BottomTab(Destination.Quests, "Quests", Icons.Filled.LocalFireDepartment),
    BottomTab(Destination.Workout, "Train", Icons.Filled.FitnessCenter),
    BottomTab(Destination.Heatmap, "Body", Icons.Filled.Whatshot),
    BottomTab(Destination.Nutrition, "Fuel", Icons.Filled.Restaurant)
)
