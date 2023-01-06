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
package com.example.exercisesamplecompose.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberScalingLazyListState
import com.example.exercisesamplecompose.R
import com.example.exercisesamplecompose.presentation.component.SummaryFormat
import com.example.exercisesamplecompose.presentation.component.TopOfScreenTime
import com.example.exercisesamplecompose.theme.ExerciseSampleTheme

/**End-of-workout summary screen**/

@Composable
fun SummaryScreen(
    averageHeartRate: String,
    totalDistance: String,
    totalCalories: String,
    elapsedTime: String,
    onRestartClick: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    ExerciseSampleTheme {
        Scaffold(positionIndicator = {
            PositionIndicator(
                scalingLazyListState = listState
            )
        }, timeText = { TopOfScreenTime() }) {
            ScalingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background),
                verticalArrangement = Arrangement.Center,
                state = listState
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(id = R.string.workout_complete))
                    }
                }
                item {
                    SummaryFormat(
                        value = elapsedTime,
                        metric = stringResource(id = R.string.duration),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    SummaryFormat(
                        value = averageHeartRate,
                        metric = stringResource(id = R.string.avgHR),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    SummaryFormat(
                        value = totalDistance,
                        metric = stringResource(id = R.string.distance),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    SummaryFormat(
                        value = totalCalories,
                        metric = stringResource(id = R.string.calories),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp)
                    ) {
                        Button(
                            onClick = {
                                onRestartClick()
                            }, modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = stringResource(id = R.string.restart))
                        }
                    }
                }

            }
        }
    }

}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun SummaryScreenPreview() {
    SummaryScreen(
        averageHeartRate = "75.0",
        totalDistance = "2 km",
        totalCalories = "100",
        elapsedTime = "17m01",
        onRestartClick = {}
    )


}
