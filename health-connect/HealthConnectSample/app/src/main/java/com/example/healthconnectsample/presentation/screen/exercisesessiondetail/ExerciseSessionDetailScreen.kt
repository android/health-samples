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
package com.example.healthconnectsample.presentation.screen.exercisesessiondetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Length
import androidx.health.connect.client.units.Velocity
import com.example.healthconnectsample.R
import com.example.healthconnectsample.data.ExerciseSessionData
import com.example.healthconnectsample.data.formatTime
import com.example.healthconnectsample.presentation.component.ExerciseSessionDetailsMinMaxAvg
import com.example.healthconnectsample.presentation.component.heartRateSeries
import com.example.healthconnectsample.presentation.component.sessionDetailsItem
import com.example.healthconnectsample.presentation.component.speedSeries
import com.example.healthconnectsample.presentation.theme.HealthConnectTheme
import java.time.Duration
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.random.Random

/**
 * Shows a details of a given [ExerciseSessionRecord], including aggregates and underlying raw data.
 */
@Composable
fun ExerciseSessionDetailScreen(
    permissions: Set<String>,
    permissionsGranted: Boolean,
    sessionMetrics: ExerciseSessionData,
    uiState: ExerciseSessionDetailViewModel.UiState,
    onError: (Throwable?) -> Unit = {},
    onPermissionsResult: () -> Unit = {},
    onPermissionsLaunch: (Set<String>) -> Unit = {}
) {

    // Remember the last error ID, such that it is possible to avoid re-launching the error
    // notification for the same error when the screen is recomposed, or configuration changes etc.
    val errorId = rememberSaveable { mutableStateOf(UUID.randomUUID()) }

    LaunchedEffect(uiState) {
        // If the initial data load has not taken place, attempt to load the data.
        if (uiState is ExerciseSessionDetailViewModel.UiState.Uninitialized) {
            onPermissionsResult()
        }

        // The [ExerciseSessionDetailViewModel.UiState] provides details of whether the last action
        // was a success or resulted in an error. Where an error occurred, for example in reading
        // and writing to Health Connect, the user is notified, and where the error is one that can
        // be recovered from, an attempt to do so is made.
        if (uiState is ExerciseSessionDetailViewModel.UiState.Error &&
            errorId.value != uiState.uuid
        ) {
            onError(uiState.exception)
            errorId.value = uiState.uuid
        }
    }

    if (uiState != ExerciseSessionDetailViewModel.UiState.Uninitialized) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!permissionsGranted) {
                item {
                    Button(
                        onClick = { onPermissionsLaunch(permissions) }
                    ) {
                        Text(text = stringResource(R.string.permissions_button_label))
                    }
                }
            } else {
                sessionDetailsItem(labelId = R.string.total_active_duration) {
                    val activeDuration = sessionMetrics.totalActiveTime ?: Duration.ZERO
                    Text(activeDuration.formatTime())
                }
                sessionDetailsItem(labelId = R.string.total_steps) {
                    Text(sessionMetrics.totalSteps?.toString() ?: "0")
                }
                sessionDetailsItem(labelId = R.string.total_distance) {
                    Text(sessionMetrics.totalDistance?.toString() ?: "0.0")
                }
                sessionDetailsItem(labelId = R.string.total_energy) {
                    Text(sessionMetrics.totalEnergyBurned?.inCalories.toString())
                }
                sessionDetailsItem(labelId = R.string.speed_stats) {
                    ExerciseSessionDetailsMinMaxAvg(
                        sessionMetrics.minSpeed?.inMetersPerSecond.let {
                            String.format(
                                "%.1f",
                                it
                            )
                        },
                        sessionMetrics.maxSpeed?.inMetersPerSecond.let {
                            String.format(
                                "%.1f",
                                it
                            )
                        },
                        sessionMetrics.avgSpeed?.inMetersPerSecond.let { String.format("%.1f", it) }
                    )
                }
                speedSeries(
                    labelId = R.string.speed_series,
                    series = sessionMetrics.speedRecord
                )
                sessionDetailsItem(labelId = R.string.hr_stats) {
                    ExerciseSessionDetailsMinMaxAvg(
                        sessionMetrics.minHeartRate?.toString()
                            ?: stringResource(id = R.string.not_available_abbrev),
                        sessionMetrics.maxHeartRate?.toString()
                            ?: stringResource(id = R.string.not_available_abbrev),
                        sessionMetrics.avgHeartRate?.toString()
                            ?: stringResource(id = R.string.not_available_abbrev)
                    )
                }
                heartRateSeries(
                    labelId = R.string.hr_series,
                    series = sessionMetrics.heartRateSeries
                )
            }
        }
    }
}

@Preview
@Composable
fun ExerciseSessionScreenPreview() {
    HealthConnectTheme {
        val uid = UUID.randomUUID().toString()
        val sessionMetrics = ExerciseSessionData(
            uid = uid,
            totalSteps = 5152,
            totalDistance = Length.meters(11923.4),
            totalEnergyBurned = Energy.calories(1131.2),
            minHeartRate = 55,
            maxHeartRate = 103,
            avgHeartRate = 77,
            heartRateSeries = generateHeartRateSeries(),
            minSpeed = Velocity.metersPerSecond(2.5),
            maxSpeed = Velocity.metersPerSecond(3.1),
            avgSpeed = Velocity.metersPerSecond(2.8),
            speedRecord = generateSpeedData(),
        )

        ExerciseSessionDetailScreen(
            permissions = setOf(),
            permissionsGranted = true,
            sessionMetrics = sessionMetrics,
            uiState = ExerciseSessionDetailViewModel.UiState.Done
        )
    }
}

private fun generateSpeedData(): List<SpeedRecord> {
    val data = mutableListOf<SpeedRecord.Sample>()
    val end = ZonedDateTime.now()
    var time = ZonedDateTime.now()
    for (index in 1..10) {
        time = end.minusMinutes(index.toLong())
        data.add(
            SpeedRecord.Sample(
                time = time.toInstant(),
                speed = Velocity.metersPerSecond((Random.nextDouble(1.0, 5.0)))
            )
        )
    }
    return listOf(
        SpeedRecord(
            startTime = time.toInstant(),
            startZoneOffset = time.offset,
            endTime = end.toInstant(),
            endZoneOffset = end.offset,
            samples = data
        )
    )
}

private fun generateHeartRateSeries(): List<HeartRateRecord> {
    val data = mutableListOf<HeartRateRecord.Sample>()
    val end = ZonedDateTime.now()
    var time = ZonedDateTime.now()
    for (index in 1..10) {
        time = end.minusMinutes(index.toLong())
        data.add(
            HeartRateRecord.Sample(
                time = time.toInstant(),
                beatsPerMinute = Random.nextLong(55, 180)
            )
        )
    }
    return listOf(
        HeartRateRecord(
            startTime = time.toInstant(),
            startZoneOffset = time.offset,
            endTime = end.toInstant(),
            endZoneOffset = end.offset,
            samples = data
        )
    )
}
