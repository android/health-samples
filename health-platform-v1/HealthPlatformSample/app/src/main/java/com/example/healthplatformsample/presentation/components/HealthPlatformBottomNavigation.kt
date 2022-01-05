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
package com.example.healthplatformsample.presentation.components

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.healthplatformsample.R
import com.example.healthplatformsample.presentation.navigation.Screen

@Composable
fun HealthPlatformBottomNavigation(navController: NavHostController) {
    BottomNavigation {
        BottomNavigationItem(
            icon = {
                Icon(
                    Icons.Filled.List,
                    contentDescription = stringResource(R.string.show_sessions)
                )
            },
            label = { Text(stringResource(R.string.show_sessions)) },
            selected = false,
            onClick = {
                navController.navigate(Screen.SessionList.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = false
                    }
                    launchSingleTop = true
                }
            }
        )
        BottomNavigationItem(
            icon = {
                Icon(
                    Icons.Filled.DirectionsWalk,
                    contentDescription = stringResource(R.string.show_total_steps)
                )
            },
            label = { Text(stringResource(R.string.show_total_steps)) },
            selected = false,
            onClick = {
                navController.navigate(Screen.TotalSteps.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}
