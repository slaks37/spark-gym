package com.sparkgym.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sparkgym.core.design.SparkColors
import com.sparkgym.di.AppContainer
import com.sparkgym.ui.common.sparkViewModelFactory
import com.sparkgym.ui.connect.ConnectScreen
import com.sparkgym.ui.connect.ConnectViewModel
import com.sparkgym.ui.heatmap.HeatmapScreen
import com.sparkgym.ui.heatmap.HeatmapViewModel
import com.sparkgym.ui.hunter.AchievementsScreen
import com.sparkgym.ui.hunter.HunterViewModel
import com.sparkgym.ui.hunter.StatusScreen
import com.sparkgym.ui.nutrition.FoodSearchScreen
import com.sparkgym.ui.nutrition.NutritionScreen
import com.sparkgym.ui.nutrition.NutritionViewModel
import com.sparkgym.ui.profile.OnboardingScreen
import com.sparkgym.ui.profile.ProfileScreen
import com.sparkgym.ui.profile.ProfileViewModel
import com.sparkgym.ui.quests.QuestScreen
import com.sparkgym.ui.workout.ActiveSessionScreen
import com.sparkgym.ui.workout.ExerciseDetailScreen
import com.sparkgym.ui.workout.RoutineDetailScreen
import com.sparkgym.ui.workout.SessionViewModel
import com.sparkgym.ui.workout.WorkoutScreen
import com.sparkgym.ui.workout.WorkoutViewModel

@Composable
fun SparkNavHost(
    container: AppContainer,
    startOnboarding: Boolean,
    navController: NavHostController = rememberNavController()
) {
    val factory = sparkViewModelFactory(container)
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // The bar hides on full-screen flows so the logger keeps the whole display.
    val showBar = currentRoute in bottomTabs.map { it.destination.route }

    Scaffold(
        containerColor = SparkColors.Void,
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            AnimatedVisibility(
                visible = showBar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                SystemNavBar(currentRoute) { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(SparkColors.Void)
                .padding(padding)
        ) {
            NavHost(
                navController = navController,
                startDestination = if (startOnboarding) Destination.Onboarding.route else Destination.Status.route
            ) {
                composable(Destination.Onboarding.route) {
                    val vm: ProfileViewModel = viewModel(factory = factory)
                    OnboardingScreen(vm) {
                        navController.navigate(Destination.Status.route) {
                            popUpTo(Destination.Onboarding.route) { inclusive = true }
                        }
                    }
                }

                composable(Destination.Status.route) {
                    val vm: HunterViewModel = viewModel(factory = factory)
                    StatusScreen(
                        viewModel = vm,
                        onOpenAchievements = { navController.navigate(Destination.Achievements.route) },
                        onOpenProfile = { navController.navigate(Destination.Profile.route) },
                        onOpenConnect = { navController.navigate(Destination.Connect.route) },
                        onOpenHeatmap = { navController.navigate(Destination.Heatmap.route) }
                    )
                }

                composable(Destination.Quests.route) {
                    val vm: HunterViewModel = viewModel(factory = factory)
                    QuestScreen(vm)
                }

                composable(Destination.Achievements.route) {
                    val vm: HunterViewModel = viewModel(factory = factory)
                    AchievementsScreen(vm) { navController.popBackStack() }
                }

                composable(Destination.Workout.route) {
                    val vm: WorkoutViewModel = viewModel(factory = factory)
                    WorkoutScreen(
                        viewModel = vm,
                        onOpenSession = { id -> navController.navigate(Destination.Session.of(id)) },
                        onOpenRoutine = { id -> navController.navigate(Destination.RoutineDetail.of(id)) },
                        onOpenExercise = { id -> navController.navigate(Destination.ExerciseDetail.of(id)) }
                    )
                }

                composable(
                    route = Destination.RoutineDetail.route,
                    arguments = listOf(navArgument(Destination.RoutineDetail.ARG) { type = NavType.LongType })
                ) { entry ->
                    val routineId = entry.arguments?.getLong(Destination.RoutineDetail.ARG) ?: 0L
                    val vm: WorkoutViewModel = viewModel(factory = factory)
                    RoutineDetailScreen(
                        viewModel = vm,
                        routineId = routineId,
                        onBack = { navController.popBackStack() },
                        onStartSession = { id -> navController.navigate(Destination.Session.of(id)) },
                        onOpenExercise = { id -> navController.navigate(Destination.ExerciseDetail.of(id)) }
                    )
                }

                composable(
                    route = Destination.ExerciseDetail.route,
                    arguments = listOf(navArgument(Destination.ExerciseDetail.ARG) { type = NavType.LongType })
                ) { entry ->
                    val exerciseId = entry.arguments?.getLong(Destination.ExerciseDetail.ARG) ?: 0L
                    ExerciseDetailScreen(
                        container = container,
                        exerciseId = exerciseId,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Destination.Session.route,
                    arguments = listOf(navArgument(Destination.Session.ARG) { type = NavType.LongType })
                ) { entry ->
                    val sessionId = entry.arguments?.getLong(Destination.Session.ARG) ?: 0L
                    val vm: SessionViewModel = viewModel(factory = factory)
                    val workoutVm: WorkoutViewModel = viewModel(factory = factory)
                    ActiveSessionScreen(
                        viewModel = vm,
                        workoutViewModel = workoutVm,
                        sessionId = sessionId,
                        onExit = { navController.popBackStack() }
                    )
                }

                composable(Destination.Heatmap.route) {
                    val vm: HeatmapViewModel = viewModel(factory = factory)
                    HeatmapScreen(vm)
                }

                composable(Destination.Nutrition.route) {
                    val vm: NutritionViewModel = viewModel(factory = factory)
                    NutritionScreen(
                        viewModel = vm,
                        onAddFood = { meal -> navController.navigate(Destination.FoodSearch.of(meal.name)) }
                    )
                }

                composable(
                    route = Destination.FoodSearch.route,
                    arguments = listOf(navArgument(Destination.FoodSearch.ARG) { type = NavType.StringType })
                ) { entry ->
                    val meal = entry.arguments?.getString(Destination.FoodSearch.ARG).orEmpty()
                    val vm: NutritionViewModel = viewModel(factory = factory)
                    FoodSearchScreen(
                        viewModel = vm,
                        mealKey = meal,
                        onDone = { navController.popBackStack() }
                    )
                }

                composable(Destination.Connect.route) {
                    val vm: ConnectViewModel = viewModel(factory = factory)
                    ConnectScreen(
                        viewModel = vm,
                        container = container,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Destination.Profile.route) {
                    val vm: ProfileViewModel = viewModel(factory = factory)
                    ProfileScreen(
                        viewModel = vm,
                        onBack = { navController.popBackStack() },
                        onOpenConnect = { navController.navigate(Destination.Connect.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemNavBar(currentRoute: String?, onSelect: (Destination) -> Unit) {
    NavigationBar(
        containerColor = SparkColors.VoidElevated,
        contentColor = SparkColors.TextSecondary
    ) {
        bottomTabs.forEach { tab ->
            val selected = currentRoute == tab.destination.route
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(tab.destination) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = {
                    Text(
                        tab.label.uppercase(),
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SparkColors.Cyan,
                    selectedTextColor = SparkColors.Cyan,
                    unselectedIconColor = SparkColors.TextMuted,
                    unselectedTextColor = SparkColors.TextMuted,
                    indicatorColor = SparkColors.Cyan.copy(alpha = 0.12f)
                )
            )
        }
    }
}
