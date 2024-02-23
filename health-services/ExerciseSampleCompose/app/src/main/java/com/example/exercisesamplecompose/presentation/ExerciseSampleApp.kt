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
@file:OptIn(ExperimentalHorologistApi::class)

package com.example.exercisesamplecompose.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.currentBackStackEntryAsState
import com.example.exercisesamplecompose.app.Screen.Exercise
import com.example.exercisesamplecompose.app.Screen.ExerciseNotAvailable
import com.example.exercisesamplecompose.app.Screen.PreparingExercise
import com.example.exercisesamplecompose.app.Screen.Summary
import com.example.exercisesamplecompose.app.navigateToTopLevel
import com.example.exercisesamplecompose.presentation.dialogs.ExerciseNotAvailable
import com.example.exercisesamplecompose.presentation.exercise.ExerciseRoute
import com.example.exercisesamplecompose.presentation.preparing.PreparingExerciseRoute
import com.example.exercisesamplecompose.presentation.summary.SummaryRoute
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.ambient.AmbientAware
import com.google.android.horologist.compose.ambient.AmbientState
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState

/** Navigation for the exercise app. **/
@Composable
fun ExerciseSampleApp(
    navController: NavHostController,
    onFinishActivity: () -> Unit
) {
    val currentScreen by navController.currentBackStackEntryAsState()

    val isAlwaysOnScreen = currentScreen?.destination?.route in AlwaysOnRoutes

    AmbientAware(
        isAlwaysOnScreen = isAlwaysOnScreen
    ) { ambientStateUpdate ->

        AppScaffold(
            timeText = {
                if (ambientStateUpdate.ambientState is AmbientState.Interactive) {
                    TimeText()
                }
            }
        ) {
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = Exercise.route,

                ) {
                composable(PreparingExercise.route) {
                    PreparingExerciseRoute(
                        ambientState = ambientStateUpdate.ambientState,
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
                        onFinishActivity = onFinishActivity
                    )
                }

                composable(Exercise.route) {
                    val columnState = rememberColumnState()

                    ScreenScaffold(scrollState = columnState) {
                        ExerciseRoute(
                            ambientState = ambientStateUpdate.ambientState,
                            columnState = columnState,
                            onSummary = {
                                navController.navigateToTopLevel(Summary, Summary.buildRoute(it))
                            },
                            onRestart = {
                                navController.navigateToTopLevel(PreparingExercise)
                            },
                            onFinishActivity = onFinishActivity
                        )
                    }
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
                    val columnState = rememberColumnState()

                    ScreenScaffold(scrollState = columnState) {
                        SummaryRoute(
                            columnState = columnState,
                            onRestartClick = {
                                navController.navigateToTopLevel(PreparingExercise)
                            }
                        )
                    }
                }
            }
        }
    }
}

val AlwaysOnRoutes = listOf(PreparingExercise.route, Exercise.route)


