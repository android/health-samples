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
package com.example.passivegoalscompose.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.AppScaffold
import com.example.passivegoalscompose.PERMISSION
import com.example.passivegoalscompose.data.HealthServicesRepository
import com.example.passivegoalscompose.data.PassiveGoalsRepository
import com.example.passivegoalscompose.theme.PassiveGoalsTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PassiveGoalsApp(
    healthServicesRepository: HealthServicesRepository,
    passiveGoalsRepository: PassiveGoalsRepository
) {
    PassiveGoalsTheme {
        AppScaffold(
            modifier = Modifier.fillMaxSize(),
        ) {
            val viewModel: PassiveGoalsViewModel = viewModel(
                factory = PassiveGoalsViewModelFactory(
                    healthServicesRepository = healthServicesRepository,
                    passiveGoalsRepository = passiveGoalsRepository
                )
            )
            val latestFloorsTime by viewModel.latestFloorsTime.collectAsState()
            val stepsGoalAchieved by viewModel.stepsGoalAchieved.collectAsState()
            val goalsEnabled by viewModel.goalsEnabled.collectAsState()
            val uiState by viewModel.uiState

            if (uiState == UiState.Supported) {
                val permissionState = rememberPermissionState(
                    permission = PERMISSION,
                    onPermissionResult = { granted ->
                        if (granted) viewModel.toggleEnabled()
                    }
                )
                PassiveGoalsScreen(
                    latestFloorsTime = latestFloorsTime,
                    goalsEnabled = goalsEnabled,
                    stepsGoalAchieved = stepsGoalAchieved,
                    onEnableClick = { viewModel.toggleEnabled() },
                    permissionState = permissionState
                )
            } else if (uiState == UiState.NotSupported) {
                NotSupportedScreen()
            }
        }
    }
}
