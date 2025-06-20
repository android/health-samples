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

import androidx.wear.compose.material3.AppScaffold
import com.example.exercisesamplecompose.presentation.summary.SummaryScreen
import com.example.exercisesamplecompose.presentation.summary.SummaryScreenState
import com.google.android.horologist.compose.layout.ResponsiveTimeText
import com.google.android.horologist.screenshots.FixedTimeSource
import com.google.android.horologist.screenshots.rng.WearDevice
import com.google.android.horologist.screenshots.rng.WearDeviceScreenshotTest
import java.time.Duration
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class SummaryScreenTest(
    override val device: WearDevice
) : WearDeviceScreenshotTest(device) {
    @Test
    fun summary() {
        runTest {
            AppScaffold(
                timeText = { ResponsiveTimeText(timeSource = FixedTimeSource) }
            ) {
                SummaryScreen(
                    uiState =
                    SummaryScreenState(
                        averageHeartRate = 75.0,
                        totalDistance = 2000.0,
                        totalCalories = 100.0,
                        elapsedTime = Duration.ofMinutes(17).plusSeconds(1)
                    ),
                    onRestartClick = {}
                )
            }
        }

        // TODO reinstate swipe tests after robolectric/compose fix
//        composeRule.onNode(hasScrollToIndexAction())
//            .performTouchInput {
//                repeat(10) {
//                    swipeUp()
//                }
//            }
//
//        captureScreenshot("_end")
    }
}
