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

package com.example.exercisesamplecompose.presentation.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.filled._360
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.example.exercisesamplecompose.R
import com.example.exercisesamplecompose.presentation.component.CaloriesText
import com.example.exercisesamplecompose.presentation.component.DistanceText
import com.example.exercisesamplecompose.presentation.component.HRText
import com.example.exercisesamplecompose.presentation.component.PauseButton
import com.example.exercisesamplecompose.presentation.component.ResumeButton
import com.example.exercisesamplecompose.presentation.component.StartButton
import com.example.exercisesamplecompose.presentation.component.StopButton
import com.example.exercisesamplecompose.presentation.component.formatElapsedTime
import com.example.exercisesamplecompose.presentation.summary.SummaryScreenState
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState

@Composable
fun ExerciseRoute(
    columnState: ScalingLazyColumnState,
    modifier: Modifier = Modifier,
    onSummary: (SummaryScreenState) -> Unit
) {
    val viewModel = hiltViewModel<ExerciseViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isEnding) {
        SideEffect {
            onSummary(uiState.toSummary())
        }
    }

    ExerciseScreen(
        onPauseClick = { viewModel.pauseExercise() },
        onEndClick = { viewModel.endExercise() },
        onResumeClick = { viewModel.resumeExercise() },
        onStartClick = { viewModel.startExercise() },
        uiState = uiState,
        columnState = columnState,
        modifier = modifier
    )
}

/**
 * Shows while an exercise is in progress
 */
@Composable
fun ExerciseScreen(
    onPauseClick: () -> Unit,
    onEndClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStartClick: () -> Unit,
    uiState: ExerciseScreenState,
    columnState: ScalingLazyColumnState,
    modifier: Modifier = Modifier
) {
    ScalingLazyColumn(
        modifier = modifier.fillMaxSize(),
        columnState = columnState
    ) {
        item {
            DurationRow(uiState)
        }

        item {
            HeartRateAndCaloriesRow(uiState)
        }

        item {
            DistanceAndLapsRow(uiState)
        }

        item {
            ExerciseControlButtons(uiState, onStartClick, onEndClick, onResumeClick, onPauseClick)
        }
    }
}

@Composable
private fun ExerciseControlButtons(
    uiState: ExerciseScreenState,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    onResumeClick: () -> Unit,
    onPauseClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        if (uiState.isEnding) {
            StartButton(onStartClick)
        } else {
            StopButton(onEndClick)
        }

        if (uiState.isPaused) {
            ResumeButton(onResumeClick)
        } else {
            PauseButton(onPauseClick)
        }
    }
}

@Composable
private fun DistanceAndLapsRow(uiState: ExerciseScreenState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.TrendingUp,
            contentDescription = stringResource(id = R.string.distance)
        )
        DistanceText(uiState.distance)
        Icon(
            imageVector = Icons.Default._360,
            contentDescription = stringResource(id = R.string.laps)
        )
        Text(text = uiState.exerciseLaps?.toString() ?: "--")
    }
}

@Composable
private fun HeartRateAndCaloriesRow(uiState: ExerciseScreenState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = stringResource(id = R.string.heart_rate)
        )
        HRText(
            hr = uiState.heartRate
        )
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = stringResource(id = R.string.calories)
        )
        if (uiState.calories != null) {
            CaloriesText(
                uiState.calories!!
            )
        }

    }
}

@Composable
private fun DurationRow(uiState: ExerciseScreenState) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.WatchLater,
            contentDescription = stringResource(id = R.string.duration)
        )
        Text(
            formatElapsedTime(uiState.elapsedTime?.duration)
        )
    }
}








