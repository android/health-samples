/*
 * Copyright 2024 The Android Open Source Project
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

package com.example.healthconnectsample.presentation.screen.recordlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.metadata.Metadata
import com.example.healthconnectsample.R
import com.example.healthconnectsample.formatDisplayTimeStartEnd
import com.example.healthconnectsample.presentation.theme.HealthConnectTheme
import java.time.Instant
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now
import java.util.UUID
import kotlin.random.Random

@Composable
fun RecordListScreen(
    uid: String,
    permissions: Set<String>,
    permissionsGranted: Boolean,
    recordType: RecordType,
    seriesRecordsType: SeriesRecordsType,
    recordList: List<Record>,
    uiState: RecordListScreenViewModel.UiState,
    onError: (Throwable?) -> Unit = {},
    onPermissionsResult: () -> Unit = {},
    onPermissionsLaunch: (Set<String>) -> Unit = {}
) {
    // Remember the last error ID, such that it is possible to avoid re-launching the error
    // notification for the same error when the screen is recomposed, or configuration changes etc.
    val errorId = rememberSaveable { mutableStateOf(UUID.randomUUID()) }

    LaunchedEffect(uiState) {
        // If the initial data load has not taken place, attempt to load the data.
        if (uiState is RecordListScreenViewModel.UiState.Uninitialized) {
            onPermissionsResult()
        }

        // The [RecordListScreenViewModel.UiState] provides details of whether the last action
        // was a success or resulted in an error. Where an error occurred, for example in reading
        // and writing to Health Connect, the user is notified, and where the error is one that can
        // be recovered from, an attempt to do so is made.
        if (uiState is RecordListScreenViewModel.UiState.Error && errorId.value != uiState.uuid) {
            onError(uiState.exception)
            errorId.value = uiState.uuid
        }
    }

    if (uiState != RecordListScreenViewModel.UiState.Uninitialized) {
        HealthConnectTheme {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 5.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!permissionsGranted) {
                    item {
                        Button(onClick = { onPermissionsLaunch(permissions) }) {
                            Text(text = stringResource(R.string.permissions_button_label))
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "${recordType.clazz.simpleName}",
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.primary,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                        Text(
                            text = uid,
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.primary
                        )
                    }
                    item {
                        Text(
                            text = "${seriesRecordsType.clazz.simpleName} list",
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.primary
                        )
                    }
                    when (seriesRecordsType) {
                        SeriesRecordsType.STEPS -> {
                            for (record in recordList.map { it as StepsRecord }) {
                                renderData(record, record.startTime, record.endTime,
                                    "Count: " + record.count
                                )
                            }
                        }

                        SeriesRecordsType.DISTANCE -> {
                            for (record in recordList.map { it as DistanceRecord }) {
                                renderData(record, record.startTime, record.endTime,
                                    "Count: " + record.distance
                                )
                            }
                        }

                        SeriesRecordsType.CALORIES -> {
                            for (record in recordList.map { it as TotalCaloriesBurnedRecord }) {
                                renderData(record, record.startTime, record.endTime,
                                    "Energy: " + record.energy
                                )
                            }
                        }

                        SeriesRecordsType.HEARTRATE -> {
                            for (record in recordList.map { it as HeartRateRecord }) {
                                renderData(record, record.startTime, record.endTime,
                                    "Heartbeat Samples: " +
                                            record.samples.map { it.beatsPerMinute }
                                                .joinToString(", ")

                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildStepsSeries(sessionStartTime: ZonedDateTime, sessionEndTime: ZonedDateTime) =
    StepsRecord(
        metadata = Metadata.manualEntry(),
        startTime = sessionStartTime.toInstant(),
        startZoneOffset = sessionStartTime.offset,
        endTime = sessionEndTime.toInstant(),
        endZoneOffset = sessionEndTime.offset,
        count = Random.nextInt(9000).toLong() + 1000,
        )

fun LazyListScope.renderData(
    record: Record,
    startTime: Instant,
    endTime: Instant,
    data: String
) {
    item {
        Text(text = record.metadata.id,
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.primary)
        Text(text = formatDisplayTimeStartEnd(startTime, null, endTime, null),
            style = MaterialTheme.typography.body2)
        Text(text = data,
            style = MaterialTheme.typography.body2)
    }
}

@Preview
@Composable
fun RecordListScreenPreview() {
    HealthConnectTheme {
        val uid = UUID.randomUUID().toString()
        RecordListScreen(
            uid = uid,
            permissions = setOf(),
            permissionsGranted = true,
            recordType = RecordType.EXERCISE_SESSION,
            seriesRecordsType = SeriesRecordsType.STEPS,
            recordList =
            listOf(
                buildStepsSeries(now().minusMinutes(180), now().minusMinutes(120)),
                buildStepsSeries(now().minusMinutes(60), now())),
            uiState = RecordListScreenViewModel.UiState.Done,
        )
    }
}
