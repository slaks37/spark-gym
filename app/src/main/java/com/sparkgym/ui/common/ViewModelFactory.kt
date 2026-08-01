package com.sparkgym.ui.common

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sparkgym.di.AppContainer
import com.sparkgym.ui.bodyweight.BodyweightViewModel
import com.sparkgym.ui.coach.CoachViewModel
import com.sparkgym.ui.connect.ConnectViewModel
import com.sparkgym.ui.heatmap.HeatmapViewModel
import com.sparkgym.ui.hunter.HunterViewModel
import com.sparkgym.ui.nutrition.NutritionViewModel
import com.sparkgym.ui.profile.ProfileViewModel
import com.sparkgym.ui.workout.SessionViewModel
import com.sparkgym.ui.workout.WorkoutViewModel

/**
 * One factory for every ViewModel in the app. With a hand-rolled container this
 * is a dozen lines, versus a processor round-trip on every build.
 */
fun sparkViewModelFactory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
    initializer { HunterViewModel(container) }
    initializer { WorkoutViewModel(container) }
    initializer { BodyweightViewModel(container) }
    initializer { SessionViewModel(container) }
    initializer { HeatmapViewModel(container) }
    initializer { NutritionViewModel(container) }
    initializer { ConnectViewModel(container) }
    initializer { CoachViewModel(container) }
    initializer { ProfileViewModel(container) }
}
