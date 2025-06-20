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

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.exercisesamplecompose.presentation.ExerciseSampleApp
import com.example.exercisesamplecompose.presentation.exercise.ExerciseViewModel
import com.example.exercisesamplecompose.presentation.preparing.PreparingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private lateinit var navController: NavHostController
    private val exerciseViewModel by viewModels<ExerciseViewModel>()
    private val preparingViewModel by viewModels<PreparingViewModel>()

    // Register the permissions callback
    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // All permissions granted, proceed with exercise
            exerciseViewModel.startExercise()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        var pendingNavigation = true

        splash.setKeepOnScreenCondition { pendingNavigation }

        super.onCreate(savedInstanceState)

        // Request permissions when activity is created
        requestPermissions.launch(PreparingViewModel.permissions.toTypedArray())

        setContent {
            navController = rememberSwipeDismissableNavController()

            ExerciseSampleApp(
                navController,
                onFinishActivity = { this.finish() }
            )

            LaunchedEffect(Unit) {
                prepareIfNoExercise()
                pendingNavigation = false
            }
        }
    }

    private suspend fun prepareIfNoExercise() {
        /** Check if we have an active exercise. If true, set our destination as the
         * Exercise Screen. If false, route to preparing a new exercise. **/
        val isRegularLaunch =
            navController.currentDestination?.route == Screen.Exercise.route
        if (isRegularLaunch && !exerciseViewModel.isExerciseInProgress()) {
            navController.navigate(Screen.PreparingExercise.route)
        }
    }
}
