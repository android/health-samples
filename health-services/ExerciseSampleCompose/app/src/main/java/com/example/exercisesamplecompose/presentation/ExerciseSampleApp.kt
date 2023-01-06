/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.exercisesamplecompose.presentation

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import com.example.exercisesamplecompose.Screens
import com.example.exercisesamplecompose.data.HealthServicesManager
import kotlinx.coroutines.launch

/** Navigation for the exercise app. **/

@Composable
fun ExerciseSampleApp(
    healthServicesManager: HealthServicesManager,
    navController: NavHostController,
    startDestination: String
) {
    val composableScope = rememberCoroutineScope()

    SwipeDismissableNavHost(
        navController = navController, startDestination = startDestination
    ) {
        composable(Screens.StartingUp.route) {
            val viewModel: ExerciseViewModel = viewModel(
                factory = ExerciseViewModelFactory(
                    healthServicesManager = healthServicesManager, LocalContext.current
                )
            )

            StartingUp(onAvailable = {
                navController.navigate(Screens.PreparingExercise.route)
            }, onUnavailable = {
                navController.navigate(Screens.ExerciseNotAvailable.route)
            }, hasCapabilities = viewModel.hasExerciseCapabilities.value
                //healthServicesManager = healthServicesManager
            )
            Log.d(TAG, viewModel.hasExerciseCapabilities.value.toString())
        }
        composable(Screens.PreparingExercise.route) {
            val viewModel: ExerciseViewModel = viewModel(
                factory = ExerciseViewModelFactory(
                    healthServicesManager = healthServicesManager, LocalContext.current
                )
            )
            val serviceState by viewModel.exerciseServiceState
            val permissions = viewModel.permissions
            PreparingExercise(onStartClick = {
                viewModel.connectToService()
                navController.navigate(Screens.ExerciseScreen.route)
            },
                prepareExercise = { composableScope.launch { viewModel.prepareExercise() } },
                onStart = { composableScope.launch { viewModel.startExercise() } },
                serviceState = serviceState,
                permissions = permissions
            )
        }
        composable(Screens.ExerciseScreen.route) {
            val viewModel: ExerciseViewModel = viewModel(
                factory = ExerciseViewModelFactory(
                    healthServicesManager = healthServicesManager, LocalContext.current
                )
            )

            val serviceState by viewModel.exerciseServiceState
            ExerciseScreen(onPauseClick = { composableScope.launch { viewModel.pauseExercise() } },
                onEndClick = {
                    composableScope.launch {
                        viewModel.endExercise()
                        viewModel.disconnectFromService()
                    }
                },
                onResumeClick = { composableScope.launch { viewModel.resumeExercise() } },
                onStartClick = { composableScope.launch { viewModel.startExercise() } },
                serviceState = serviceState,
                navController = navController
            )
        }
        composable(Screens.ExerciseNotAvailable.route) {
            ExerciseNotAvailable()
        }
        composable(Screens.SummaryScreen.route + "/{averageHeartRate}/{totalDistance}/{totalCalories}/{elapsedTime}",
            arguments = listOf(navArgument("averageHeartRate") { type = NavType.StringType },
                navArgument("totalDistance") { type = NavType.StringType },
                navArgument("totalCalories") { type = NavType.StringType },
                navArgument("elapsedTime") { type = NavType.StringType })) {
            SummaryScreen(averageHeartRate = it.arguments?.getString("averageHeartRate")!!,
                totalDistance = it.arguments?.getString("totalDistance")!!,
                totalCalories = it.arguments?.getString("totalCalories")!!,
                elapsedTime = it.arguments?.getString("elapsedTime")!!,
                onRestartClick = { navController.navigate(Screens.StartingUp.route) })
        }
    }


}
