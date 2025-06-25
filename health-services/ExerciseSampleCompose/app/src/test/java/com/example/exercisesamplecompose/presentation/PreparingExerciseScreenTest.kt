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

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.AppScaffold
import com.example.exercisesamplecompose.data.ServiceState
import com.example.exercisesamplecompose.presentation.preparing.PreparingExerciseScreen
import com.example.exercisesamplecompose.presentation.preparing.PreparingScreenState
import com.example.exercisesamplecompose.presentation.preparing.PreparingViewModel
import com.example.exercisesamplecompose.service.ExerciseServiceState
import com.google.android.horologist.compose.ambient.AmbientState
import com.google.android.horologist.screenshots.rng.WearDevice
import com.google.android.horologist.screenshots.rng.WearDeviceScreenshotTest
import org.junit.Assume
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class PreparingExerciseScreenTest(
    override val device: WearDevice
) : WearDeviceScreenshotTest(device) {
    override fun testName(suffix: String): String =
        "src/test/screenshots/${this.javaClass.simpleName}${
            if (testInfo.methodName.startsWith("preparing")) {
                ""
            } else {
                "_" +
                    testInfo.methodName.substringBefore(
                        "["
                    )
            }
        }_${device.id}$suffix.png"

    @Composable
   override fun TestScaffold(content: @Composable () -> Unit) {
        CorrectLayout {
            AppScaffold(timeText = {}) {
                content()
            }
        }
    }


    @Test
    fun preparing() =
        runTest {
            AppScaffold {
                    PreparingExerciseScreen(
                        uiState =
                            PreparingScreenState.Preparing(
                                serviceState =
                                    ServiceState.Connected(
                                        ExerciseServiceState()
                                    ),
                                isTrackingInAnotherApp = false,
                                requiredPermissions = PreparingViewModel.permissions,
                                hasExerciseCapabilities = true
                            ),
                        ambientState = AmbientState.Interactive
                    )
                }
        }

    @Test
    fun ambient() =
        runTest {
            // Only run for one variant
            Assume.assumeTrue(device == WearDevice.GooglePixelWatch)

            AppScaffold {
                PreparingExerciseScreen(
                    uiState =
                    PreparingScreenState.Preparing(
                        serviceState =
                        ServiceState.Connected(
                            ExerciseServiceState()
                        ),
                        isTrackingInAnotherApp = false,
                        requiredPermissions = PreparingViewModel.permissions,
                        hasExerciseCapabilities = true
                    ),
                    ambientState = AmbientState.Ambient()
                )
            }
        }
}
