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
@file:OptIn(ExperimentalHorologistApi::class)

package com.example.exercisesamplecompose.presentation.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.example.exercisesamplecompose.R
import com.example.exercisesamplecompose.presentation.component.SummaryFormat
import com.example.exercisesamplecompose.presentation.theme.ThemePreview
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState

/**End-of-workout summary screen**/

@Composable
fun SummaryScreen(
    averageHeartRate: String,
    totalDistance: String,
    totalCalories: String,
    elapsedTime: String,
    onRestartClick: () -> Unit,
    columnState: ScalingLazyColumnState,
) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState
    ) {
        item { ListHeader { Text(stringResource(id = R.string.workout_complete)) } }
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

@WearPreviewDevices
@Composable
fun SummaryScreenPreview() {
    ThemePreview {
        SummaryScreen(
            averageHeartRate = "75.0",
            totalDistance = "2 km",
            totalCalories = "100",
            elapsedTime = "17m01",
            onRestartClick = {},
            columnState = ScalingLazyColumnDefaults.belowTimeText().create()
        )
    }
}
