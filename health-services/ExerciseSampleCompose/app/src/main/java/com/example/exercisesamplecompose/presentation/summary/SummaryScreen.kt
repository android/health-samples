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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.example.exercisesamplecompose.R
import com.example.exercisesamplecompose.presentation.component.SummaryFormat
import com.example.exercisesamplecompose.presentation.component.formatCalories
import com.example.exercisesamplecompose.presentation.component.formatDistanceKm
import com.example.exercisesamplecompose.presentation.component.formatElapsedTime
import com.example.exercisesamplecompose.presentation.component.formatHeartRate
import com.example.exercisesamplecompose.presentation.theme.ThemePreview
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.Title
import java.time.Duration

/**End-of-workout summary screen**/
@Composable
fun SummaryRoute(
    onRestartClick: () -> Unit,
    columnState: ScalingLazyColumnState,
) {
    val viewModel = hiltViewModel<SummaryViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SummaryScreen(uiState = uiState, onRestartClick = onRestartClick, columnState = columnState)
}


@Composable
fun SummaryScreen(
    uiState: SummaryScreenState,
    onRestartClick: () -> Unit,
    columnState: ScalingLazyColumnState,
) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState
    ) {
        item { Title(text = stringResource(id = R.string.workout_complete)) }
        item {
            SummaryFormat(
                value = formatElapsedTime(uiState.elapsedTime, includeSeconds = true),
                metric = stringResource(id = R.string.duration),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            SummaryFormat(
                value = formatHeartRate(uiState.averageHeartRate),
                metric = stringResource(id = R.string.avgHR),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            SummaryFormat(
                value = formatDistanceKm(uiState.totalDistance),
                metric = stringResource(id = R.string.distance),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            SummaryFormat(
                value = formatCalories(uiState.totalCalories),
                metric = stringResource(id = R.string.calories),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Chip(
                label = stringResource(id = R.string.restart),
                onClick = onRestartClick,
                modifier = Modifier
                    .padding(6.dp)
            )
        }
    }
}

@WearPreviewDevices
@Composable
fun SummaryScreenPreview() {
    ThemePreview {
        SummaryScreen(
            uiState = SummaryScreenState(
                averageHeartRate = 75.0,
                totalDistance = 2000.0,
                totalCalories = 100.0,
                elapsedTime = Duration.ofMinutes(17).plusSeconds(1)
            ),
            onRestartClick = {},
            columnState = ScalingLazyColumnDefaults.belowTimeText().create()
        )
    }
}
