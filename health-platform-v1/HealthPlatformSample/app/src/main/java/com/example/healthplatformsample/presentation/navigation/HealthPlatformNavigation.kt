/*
 * Copyright 2021 The Android Open Source Project
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
package com.example.healthplatformsample.presentation.navigation

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.healthplatformsample.data.HealthPlatformManager
import com.example.healthplatformsample.data.tryHealthDataResolution
import com.example.healthplatformsample.presentation.ui.ListSessionsScreen.ListSessionsScreen
import com.example.healthplatformsample.presentation.ui.SessionDetailScreen.SessionDetailScreen
import com.example.healthplatformsample.presentation.ui.TotalStepsScreen.TotalStepsScreen
import com.example.healthplatformsample.showExceptionSnackbar

@Composable
fun HealthPlatformNavigation(
    navController: NavHostController = rememberNavController(),
    healthPlatformManager: HealthPlatformManager,
    scaffoldState: ScaffoldState
) {
    val scope = rememberCoroutineScope()
    NavHost(navController = navController, startDestination = Screen.SessionList.route) {
        composable(Screen.SessionList.route) {
            ListSessionsScreen(
                healthPlatformManager = healthPlatformManager,
                onDetailsClick = { uid ->
                    val route = Screen.SessionDetail.route + "/" + uid
                    navController.navigate(route)
                },
                onError = { context, exception ->
                    showExceptionSnackbar(scaffoldState, scope, exception)
                    context.tryHealthDataResolution(exception)
                }
            )
        }
        composable(
            route = Screen.SessionDetail.route + "/{$UID_NAV_ARGUMENT}",
            arguments = listOf(
                navArgument(UID_NAV_ARGUMENT) { type = NavType.StringType }
            )
        ) {
            val uid = it.arguments?.getString(UID_NAV_ARGUMENT)!!
            SessionDetailScreen(
                healthPlatformManager = healthPlatformManager,
                uid = uid,
                onError = { context, exception ->
                    showExceptionSnackbar(scaffoldState, scope, exception)
                    context.tryHealthDataResolution(exception)
                }
            )
        }
        composable(Screen.TotalSteps.route) {
            TotalStepsScreen(
                healthPlatformManager = healthPlatformManager,
                onError = { context, exception ->
                    showExceptionSnackbar(scaffoldState, scope, exception)
                    context.tryHealthDataResolution(exception)
                }
            )
        }
    }
}
