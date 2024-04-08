/*
 * Copyright 2024 The Android Open Source Project
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

import com.example.exercisesamplecompose.data.ServiceState
import com.example.exercisesamplecompose.presentation.preparing.PreparingExerciseScreen
import com.example.exercisesamplecompose.presentation.preparing.PreparingScreenState
import com.example.exercisesamplecompose.presentation.preparing.PreparingViewModel
import com.example.exercisesamplecompose.service.ExerciseServiceState
import com.google.android.horologist.compose.ambient.AmbientState
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ResponsiveTimeText
import com.google.android.horologist.screenshots.FixedTimeSource
import com.google.android.horologist.screenshots.rng.WearDevice
import com.google.android.horologist.screenshots.rng.WearDeviceScreenshotTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class PreparingExerciseScreenTest(override val device: WearDevice) :
    WearDeviceScreenshotTest(device) {
    @Test
    fun preparing() = runTest {
        AppScaffold(
            timeText = { ResponsiveTimeText(timeSource = FixedTimeSource) }
        ) {
            PreparingExerciseScreen(
                ambientState = AmbientState.Interactive,
                onStart = {},
                uiState = PreparingScreenState.Preparing(
                    serviceState = ServiceState.Connected(
                        ExerciseServiceState()
                    ),
                    isTrackingInAnotherApp = false,
                    requiredPermissions = PreparingViewModel.permissions,
                    hasExerciseCapabilities = true
                )
            )
        }
    }

    @Test
    fun failed() = runTest {
        AppScaffold(
            timeText = { ResponsiveTimeText(timeSource = FixedTimeSource) }
        ) {
            PreparingExerciseScreen(
                ambientState = AmbientState.Ambient(),
                onStart = {},
                uiState = PreparingScreenState.Preparing(
                    serviceState = ServiceState.Connected(
                        ExerciseServiceState()
                    ),
                    isTrackingInAnotherApp = false,
                    requiredPermissions = PreparingViewModel.permissions,
                    hasExerciseCapabilities = true
                )
            )
        }
    }
}
