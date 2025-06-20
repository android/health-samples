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
@file:OptIn(ExperimentalHorologistApi::class)

package com.example.exercisesamplecompose.presentation.exercise

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.HorizontalPagerScaffold
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.example.exercisesamplecompose.R
import com.example.exercisesamplecompose.data.ServiceState
import com.example.exercisesamplecompose.presentation.ambient.ambientBlank
import com.example.exercisesamplecompose.presentation.component.CaloriesText
import com.example.exercisesamplecompose.presentation.component.DistanceText
import com.example.exercisesamplecompose.presentation.component.HRText
import com.example.exercisesamplecompose.presentation.component.PauseButton
import com.example.exercisesamplecompose.presentation.component.ResumeButton
import com.example.exercisesamplecompose.presentation.component.StartButton
import com.example.exercisesamplecompose.presentation.component.StopButton
import com.example.exercisesamplecompose.presentation.component.formatElapsedTime
import com.example.exercisesamplecompose.presentation.dialogs.ExerciseGoalMet
import com.example.exercisesamplecompose.presentation.summary.SummaryScreenState
import com.example.exercisesamplecompose.presentation.theme.ThemePreview
import com.example.exercisesamplecompose.service.ExerciseServiceState
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.ambient.AmbientAware
import com.google.android.horologist.compose.ambient.AmbientState
import com.google.android.horologist.health.composables.ActiveDurationText
import kotlinx.coroutines.launch

@Composable
fun ExerciseRoute(
    modifier: Modifier = Modifier,
    onSummary: (SummaryScreenState) -> Unit,
    onRestart: () -> Unit,
    onFinishActivity: () -> Unit
) {
    val viewModel = hiltViewModel<ExerciseViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isEnded) {
        SideEffect {
            onSummary(uiState.toSummary())
        }
    }

    if (uiState.error != null) {
        ErrorStartingExerciseScreen(
            onRestart = onRestart,
            onFinishActivity = onFinishActivity,
            uiState = uiState
        )
    } else {
        AmbientAware { ambientState ->
            ExerciseScreen(
                ambientState = ambientState,
                onPauseClick = { viewModel.pauseExercise() },
                onEndClick = { viewModel.endExercise() },
                onResumeClick = { viewModel.resumeExercise() },
                onStartClick = { viewModel.startExercise() },
                uiState = uiState,
                modifier = modifier
            )
        }
    }
}

/**
 * Shows an error that occurred when starting an exercise
 */
@Composable
fun ErrorStartingExerciseScreen(
    onRestart: () -> Unit,
    onFinishActivity: () -> Unit,
    uiState: ExerciseScreenState
) {
    AlertDialog(
        title = { Text(stringResource(id = R.string.error_starting_exercise)) },
        text = {
            "${uiState.error ?: Text(stringResource(id = R.string.unknown_error))}. ${
                Text(
                    stringResource(
                        id = R.string.try_again
                    )
                )
            }"
        },
        onDismissRequest = onFinishActivity,
        visible = true,
        confirmButton = {
            Button(
                onClick = onRestart
            ) {
                Text(stringResource(id = R.string.yes))
            }
        },
        dismissButton = {
            FilledTonalButton(
                onClick = onFinishActivity
            ) {
                Text(stringResource(id = R.string.no))
            }
        }
    )
}

/**
 * Shows while an exercise is in progress
 */
@Composable
fun ExerciseScreen(
    ambientState: AmbientState,
    onPauseClick: () -> Unit,
    onEndClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStartClick: () -> Unit,
    uiState: ExerciseScreenState,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = 1, pageCount = { 2 })

        HorizontalPagerScaffold(pagerState = pagerState) {
            HorizontalPager(
                state = pagerState
            ) { page ->
                ScreenScaffold {
                        if (page == 0) {
                            ExerciseControlButtons(
                                uiState = uiState,
                                onStartClick = onStartClick,
                                onEndClick = onEndClick,
                                onResumeClick = {
                                    onResumeClick()
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(1)
                                    }
                                },
                                onPauseClick = {
                                    onPauseClick()
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(1)
                                    }
                                }
                            )
                        } else {
                            ExerciseMetrics(uiState = uiState)
                        }
                    }
                }

            // If we meet an exercise goal, show our exercise met dialog.
            // This approach is for the sample, and doesn't guarantee processing of this event in all cases,
            // such as the user exiting the app while this is in-progress. Consider alternatives to exposing
            // state in a production app.
            uiState.exerciseState?.exerciseGoal?.let {
                Log.d("ExerciseGoalMet", "Showing exercise goal met dialog")
                ExerciseGoalMet(it.isNotEmpty())
            }
        }
}

@Composable
private fun ExerciseMetrics(
    uiState: ExerciseScreenState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HeartRateRow(uiState)

        CaloriesRow(uiState)

        DistanceAndLapsRow(uiState)

        DurationRow(uiState)
    }
}

@Composable
private fun ExerciseControlButtons(
    uiState: ExerciseScreenState,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    onResumeClick: () -> Unit,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
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
}

@Composable
private fun DistanceAndLapsRow(uiState: ExerciseScreenState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Row {
            DistanceText(uiState.exerciseState?.exerciseMetrics?.distance)
        }
    }
}

@Composable
private fun HeartRateRow(uiState: ExerciseScreenState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Row {
            HRText(
                hr = uiState.exerciseState?.exerciseMetrics?.heartRate
            )
        }
    }
}

@Composable
private fun CaloriesRow(uiState: ExerciseScreenState) {
    Row {
        CaloriesText(
            uiState.exerciseState?.exerciseMetrics?.calories
        )
    }
}

@Composable
private fun DurationRow(uiState: ExerciseScreenState) {
    val lastActiveDurationCheckpoint = uiState.exerciseState?.activeDurationCheckpoint
    val exerciseState = uiState.exerciseState?.exerciseState
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row {
            if (exerciseState != null && lastActiveDurationCheckpoint != null) {
                ActiveDurationText(
                    checkpoint = lastActiveDurationCheckpoint,
                    state = uiState.exerciseState.exerciseState
                ) {
                    Text(
                        text = formatElapsedTime(it, includeSeconds = true),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 25.sp
                    )
                }
            } else {
                Text(text = "--")
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun ExerciseScreenPreview() {
    ThemePreview {
        ExerciseScreen(
            onPauseClick = {},
            onEndClick = {},
            onResumeClick = {},
            onStartClick = {},
            uiState = ExerciseScreenState(
                hasExerciseCapabilities = true,
                isTrackingAnotherExercise = false,
                serviceState = ServiceState.Connected(
                    ExerciseServiceState()
                ),
                exerciseState = ExerciseServiceState()
            ),
            ambientState = AmbientState.Interactive
        )
    }
}

@WearPreviewDevices
@Composable
fun ErrorStartingExerciseScreenPreview() {
    ThemePreview {
        ErrorStartingExerciseScreen(
            onRestart = {},
            onFinishActivity = {},
            uiState = ExerciseScreenState(
                hasExerciseCapabilities = true,
                isTrackingAnotherExercise = false,
                serviceState = ServiceState.Connected(
                    ExerciseServiceState()
                ),
                exerciseState = ExerciseServiceState()
            )
        )
    }
}

@WearPreviewDevices
@Composable
fun ExerciseControlButtonsPreview() {
    ThemePreview {
        ExerciseControlButtons(
            uiState = ExerciseScreenState(
                hasExerciseCapabilities = true,
                isTrackingAnotherExercise = false,
                serviceState = ServiceState.Connected(
                    ExerciseServiceState()
                ),
                exerciseState = ExerciseServiceState()
            ),
            onStartClick = {},
            onEndClick = {},
            onResumeClick = {},
            onPauseClick = {}
        )
    }
}
