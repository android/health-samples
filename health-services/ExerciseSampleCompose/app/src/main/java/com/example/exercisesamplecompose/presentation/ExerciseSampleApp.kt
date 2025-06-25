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
package com.example.exercisesamplecompose.presentation

import ExerciseGoalsRoute
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import com.example.exercisesamplecompose.app.Screen
import com.example.exercisesamplecompose.app.Screen.Exercise
import com.example.exercisesamplecompose.app.Screen.ExerciseNotAvailable
import com.example.exercisesamplecompose.app.Screen.PreparingExercise
import com.example.exercisesamplecompose.app.Screen.Summary
import com.example.exercisesamplecompose.app.navigateToTopLevel
import com.example.exercisesamplecompose.presentation.dialogs.ExerciseNotAvailable
import com.example.exercisesamplecompose.presentation.exercise.ExerciseRoute
import com.example.exercisesamplecompose.presentation.preparing.PreparingExerciseRoute
import com.example.exercisesamplecompose.presentation.summary.SummaryRoute

/** Navigation for the exercise app. **/
@Composable
fun ExerciseSampleApp(
    navController: NavHostController,
    onFinishActivity: () -> Unit
) {
    AppScaffold {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = Exercise.route

        ) {
            composable(PreparingExercise.route) {
                PreparingExerciseRoute(
                    onStart = {
                        navController.navigate(Exercise.route) {
                            popUpTo(navController.graph.id) {
                                inclusive = false
                            }
                        }
                    },
                    onNoExerciseCapabilities = {
                        navController.navigate(ExerciseNotAvailable.route) {
                            popUpTo(navController.graph.id) {
                                inclusive = false
                            }
                        }
                    },
                    onFinishActivity = onFinishActivity,
                    onGoals = { navController.navigate(Screen.Goals.route) }
                )
            }

            composable(Exercise.route) {
                ExerciseRoute(
                    onSummary = {
                        navController.navigateToTopLevel(Summary, Summary.buildRoute(it))
                    },
                    onRestart = {
                        navController.navigateToTopLevel(PreparingExercise)
                    },
                    onFinishActivity = onFinishActivity
                )
            }

            composable(ExerciseNotAvailable.route) {
                ExerciseNotAvailable()
            }

            composable(
                Summary.route + "/{averageHeartRate}/{totalDistance}/{totalCalories}/{elapsedTime}",
                arguments = listOf(
                    navArgument(Summary.averageHeartRateArg) { type = NavType.FloatType },
                    navArgument(Summary.totalDistanceArg) { type = NavType.FloatType },
                    navArgument(Summary.totalCaloriesArg) { type = NavType.FloatType },
                    navArgument(Summary.elapsedTimeArg) { type = NavType.StringType }
                )
            ) {
                SummaryRoute(
                    onRestartClick = {
                        navController.navigateToTopLevel(PreparingExercise)
                    }
                )
            }
            composable(Screen.Goals.route) {
                ExerciseGoalsRoute(onSet = { navController.popBackStack() })
            }
        }
    }
}

val AlwaysOnRoutes = listOf(PreparingExercise.route, Exercise.route)
