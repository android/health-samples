/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.exercisesamplecompose.app

import androidx.navigation.NavController
import com.example.exercisesamplecompose.presentation.summary.SummaryScreenState

sealed class Screen(
    val route: String
) {
    object Exercise : Screen("exercise")

    object ExerciseNotAvailable : Screen("exerciseNotAvailable")

    object PreparingExercise : Screen("preparingExercise")

    object Goals : Screen(route = "goals")

    object Summary : Screen("summaryScreen") {
        fun buildRoute(summary: SummaryScreenState): String =
            "$route/${summary.averageHeartRate}/${summary.totalDistance}" +
                "/${summary.totalCalories}/${summary.elapsedTime}"

        val averageHeartRateArg = "averageHeartRate"
        val totalDistanceArg = "totalDistance"
        val totalCaloriesArg = "totalCalories"
        val elapsedTimeArg = "elapsedTime"
    }
}

fun NavController.navigateToTopLevel(
    screen: Screen,
    route: String = screen.route
) {
    navigate(route) {
        popUpTo(graph.id) {
            inclusive = true
        }
    }
}
