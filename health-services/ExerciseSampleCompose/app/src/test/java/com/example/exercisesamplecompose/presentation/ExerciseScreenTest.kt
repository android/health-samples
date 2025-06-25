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

import androidx.compose.runtime.CompositionLocalProvider
import androidx.wear.compose.material3.AppScaffold
import com.example.exercisesamplecompose.data.ServiceState
import com.example.exercisesamplecompose.presentation.exercise.ExerciseScreen
import com.example.exercisesamplecompose.presentation.exercise.ExerciseScreenState
import com.example.exercisesamplecompose.service.ExerciseServiceState
import com.google.android.horologist.compose.ambient.AmbientState
import com.google.android.horologist.compose.ambient.LocalAmbientState
import com.google.android.horologist.compose.layout.ResponsiveTimeText
import com.google.android.horologist.screenshots.FixedTimeSource
import com.google.android.horologist.screenshots.rng.WearDevice
import com.google.android.horologist.screenshots.rng.WearDeviceScreenshotTest
import org.junit.Assume
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class ExerciseScreenTest(
    override val device: WearDevice
) : WearDeviceScreenshotTest(device) {
    override fun testName(suffix: String): String =
        "src/test/screenshots/${this.javaClass.simpleName}${
            if (testInfo.methodName.startsWith("active")) {
                ""
            } else {
                "_" +
                    testInfo.methodName.substringBefore(
                        "["
                    )
            }
        }_${device.id}$suffix.png"

    @Test
    fun active() =
        runTest {
            AppScaffold(
                timeText = { ResponsiveTimeText(timeSource = FixedTimeSource) }
            ) {
                ExerciseScreen(
                    ambientState = AmbientState.Interactive,
                    onPauseClick = {},
                    onEndClick = {},
                    onResumeClick = {},
                    onStartClick = {},
                    uiState =
                    ExerciseScreenState(
                        hasExerciseCapabilities = true,
                        isTrackingAnotherExercise = false,
                        serviceState =
                        ServiceState.Connected(
                            ExerciseServiceState()
                        ),
                        exerciseState = ExerciseServiceState()
                    ),
                )
            }
        }

    @Test
    fun ambient() =
        runTest {
            // Only run for one variant
            Assume.assumeTrue(device == WearDevice.GooglePixelWatch)

            CompositionLocalProvider(LocalAmbientState provides AmbientState.Ambient()) {
                AppScaffold(
                    timeText = { ResponsiveTimeText(timeSource = FixedTimeSource) }
                ) {
                    ExerciseScreen(
                        onPauseClick = {},
                        onEndClick = {},
                        onResumeClick = {},
                        onStartClick = {},
                        uiState =
                        ExerciseScreenState(
                            hasExerciseCapabilities = true,
                            isTrackingAnotherExercise = false,
                            serviceState =
                            ServiceState.Connected(
                                ExerciseServiceState()
                            ),
                            exerciseState = ExerciseServiceState()
                        ),
                        ambientState = AmbientState.Ambient()
                    )
                }
            }
        }
}
