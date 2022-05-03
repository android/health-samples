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
package com.example.healthconnectsample.presentation.navigation

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.healthconnectsample.data.HealthConnectManager
import com.example.healthconnectsample.presentation.screen.WelcomeScreen
import com.example.healthconnectsample.presentation.screen.activitysession.ActivitySessionScreen
import com.example.healthconnectsample.presentation.screen.activitysession.ActivitySessionViewModel
import com.example.healthconnectsample.presentation.screen.activitysession.ActivitySessionViewModelFactory
import com.example.healthconnectsample.presentation.screen.activitysessiondetail.ActivitySessionDetailScreen
import com.example.healthconnectsample.presentation.screen.activitysessiondetail.ActivitySessionDetailViewModel
import com.example.healthconnectsample.presentation.screen.activitysessiondetail.ActivitySessionDetailViewModelFactory
import com.example.healthconnectsample.presentation.screen.inputreadings.InputReadingsScreen
import com.example.healthconnectsample.presentation.screen.inputreadings.InputReadingsViewModel
import com.example.healthconnectsample.presentation.screen.inputreadings.InputReadingsViewModelFactory
import com.example.healthconnectsample.showExceptionSnackbar

/**
 * Provides the navigation in the app.
 */
@Composable
fun HealthConnectNavigation(
    navController: NavHostController,
    healthConnectManager: HealthConnectManager,
    scaffoldState: ScaffoldState
) {
    val scope = rememberCoroutineScope()
    val availability by healthConnectManager.availability
    NavHost(navController = navController, startDestination = Screen.WelcomeScreen.route) {
        composable(Screen.WelcomeScreen.route) {
            WelcomeScreen(
                healthConnectAvailability = availability
            )
        }
        composable(Screen.ActivitySessions.route) {
            val viewModel: ActivitySessionViewModel = viewModel(
                factory = ActivitySessionViewModelFactory(
                    healthConnectManager = healthConnectManager
                )
            )
            val permissionsGranted by viewModel.permissionsGranted
            val sessionsList by viewModel.sessionsList
            val permissions = viewModel.permissions
            ActivitySessionScreen(
                permissionsGranted = permissionsGranted,
                permissions = permissions,
                sessionsList = sessionsList,
                uiState = viewModel.uiState,
                onInsertClick = {
                    viewModel.insertActivitySession()
                },
                onDetailsClick = { uid ->
                    navController.navigate(Screen.ActivitySessionDetail.route + "/" + uid)
                },
                onDeleteClick = { uid ->
                    viewModel.deleteActivitySession(uid)
                },
                onError = { exception ->
                    showExceptionSnackbar(scaffoldState, scope, exception)
                },
                onPermissionsResult = {
                    viewModel.initialLoad()
                }
            )
        }
        composable(Screen.ActivitySessionDetail.route + "/{$UID_NAV_ARGUMENT}") {
            val uid = it.arguments?.getString(UID_NAV_ARGUMENT)!!
            val viewModel: ActivitySessionDetailViewModel = viewModel(
                factory = ActivitySessionDetailViewModelFactory(
                    uid = uid,
                    healthConnectManager = healthConnectManager
                )
            )
            val permissionsGranted by viewModel.permissionsGranted
            val sessionMetrics by viewModel.sessionMetrics
            val permissions = viewModel.permissions
            ActivitySessionDetailScreen(
                permissions = permissions,
                permissionsGranted = permissionsGranted,
                sessionMetrics = sessionMetrics,
                uiState = viewModel.uiState,
                onError = { exception ->
                    showExceptionSnackbar(scaffoldState, scope, exception)
                },
                onPermissionsResult = {
                    viewModel.initialLoad()
                }
            )
        }
        composable(Screen.InputReadings.route) {
            val viewModel: InputReadingsViewModel = viewModel(
                factory = InputReadingsViewModelFactory(
                    healthConnectManager = healthConnectManager
                )
            )
            val permissionsGranted by viewModel.permissionsGranted
            val readingsList by viewModel.readingsList
            val permissions = viewModel.permissions
            val weeklyAvg = viewModel.weeklyAvg
            InputReadingsScreen(
                permissionsGranted = permissionsGranted,
                permissions = permissions,
                uiState = viewModel.uiState,
                onInsertClick = { weightInput ->
                    viewModel.inputReadings(weightInput)
                },
                weeklyAvg = weeklyAvg.value,
                onDeleteClick = { uid ->
                    viewModel.deleteWeightInput(uid)
                },
                readingsList = readingsList,
                onError = { exception ->
                    showExceptionSnackbar(scaffoldState, scope, exception)
                },
            onPermissionsResult = {
                viewModel.initialLoad()
            })
        }


        }


}
