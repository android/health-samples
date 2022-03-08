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
package com.example.healthconnectsample.presentation.screen.activitysessiondetail

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.health.connect.client.permission.HealthDataRequestPermissions
import androidx.health.connect.client.permission.Permission
import androidx.health.connect.client.records.ActivitySession
import androidx.health.connect.client.records.HeartRate
import androidx.health.connect.client.records.Speed
import com.example.healthconnectsample.R
import com.example.healthconnectsample.data.ActivitySessionData
import com.example.healthconnectsample.presentation.component.SessionDetailsMinMaxAvg
import com.example.healthconnectsample.presentation.component.heartRateSeries
import com.example.healthconnectsample.presentation.component.sessionDetailsItem
import com.example.healthconnectsample.presentation.component.speedSamples
import com.example.healthconnectsample.presentation.theme.HealthConnectTheme
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.random.Random

/**
 * Shows a details of a given [ActivitySession], including aggregates and underlying raw data.
 */
@Composable
fun ActivitySessionDetailScreen(
    permissions: Set<Permission>,
    permissionsGranted: Boolean,
    sessionMetrics: ActivitySessionData,
    uiState: ActivitySessionDetailViewModel.UiState,
    onError: (Throwable?) -> Unit = {},
    onPermissionsResult: () -> Unit = {},
) {
    val launcher = rememberLauncherForActivityResult(HealthDataRequestPermissions()) {
        onPermissionsResult()
    }

    // Remember the last error ID, such that it is possible to avoid re-launching the error
    // notification for the same error when the screen is recomposed, or configuration changes etc.
    val errorId = rememberSaveable { mutableStateOf(UUID.randomUUID()) }

    // The [ActivitySessionDetailViewModel.UiState] provides details of whether the last action was
    // a success or resulted in an error. Where an error occurred, for example in reading and
    // writing to Health Connect, the user is notified, and where the error is one that can be
    // recovered from, an attempt to do so is made.
    LaunchedEffect(uiState) {
        if (uiState is ActivitySessionDetailViewModel.UiState.Error && errorId.value != uiState.uuid) {
            onError(uiState.exception)
            errorId.value = uiState.uuid
        }
    }

    if (uiState != ActivitySessionDetailViewModel.UiState.Loading) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!permissionsGranted) {
                item {
                    Button(
                        onClick = { launcher.launch(permissions) }
                    ) {
                        Text(text = stringResource(R.string.permissions_button_label))
                    }
                }
            } else {
                sessionDetailsItem(labelId = R.string.total_steps) {
                    Text(sessionMetrics.totalSteps?.toString() ?: "0")
                }
                sessionDetailsItem(labelId = R.string.total_distance) {
                    Text(String.format("%.0f", sessionMetrics.totalDistance ?: 0.0))
                }
                sessionDetailsItem(labelId = R.string.total_energy) {
                    Text(String.format("%.1f", sessionMetrics.totalEnergyBurned ?: 0.0))
                }
                speedSamples(labelId = R.string.speed_samples, series = sessionMetrics.speedData)
                sessionDetailsItem(labelId = R.string.hr_stats) {
                    SessionDetailsMinMaxAvg(
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
fun ActivitySessionScreenPreview() {
    HealthConnectTheme {
        val uid = UUID.randomUUID().toString()
        val sessionMetrics = ActivitySessionData(
            uid = uid,
            totalSteps = 5152,
            totalDistance = 11923.4,
            totalEnergyBurned = 1131.2,
            minHeartRate = 55,
            maxHeartRate = 103,
            avgHeartRate = 77,
            speedData = generateSpeedData(),
            heartRateSeries = generateHeartRateSeries()
        )

        ActivitySessionDetailScreen(
            permissions = setOf(),
            permissionsGranted = true,
            sessionMetrics = sessionMetrics,
            uiState = ActivitySessionDetailViewModel.UiState.Done
        )
    }
}

private fun generateSpeedData(): List<Speed> {
    val data = mutableListOf<Speed>()
    val end = ZonedDateTime.now()
    for (index in 1..10) {
        val time = end.minusMinutes(index.toLong())
        data.add(
            Speed(
                time = time.toInstant(),
                zoneOffset = time.offset,
                speed = Random.nextDouble(1.0, 5.0)
            )
        )
    }
    return data
}

private fun generateHeartRateSeries(): List<HeartRate> {
    val data = mutableListOf<HeartRate>()
    val end = ZonedDateTime.now()
    for (index in 1..10) {
        val time = end.minusMinutes(index.toLong())
        data.add(
            HeartRate(
                time = time.toInstant(),
                zoneOffset = time.offset,
                bpm = Random.nextLong(55, 180)
            )
        )
    }
    return data
}