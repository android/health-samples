package com.example.healthconnectsample.presentation.screen.recordlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Velocity
import com.example.healthconnectsample.R
import com.example.healthconnectsample.formatDisplayTimeStartEnd
import com.example.healthconnectsample.presentation.theme.HealthConnectTheme
import java.time.Instant
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now
import java.time.temporal.ChronoUnit
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            /*horizontalAlignment = Alignment.CenterHorizontally*/ ) {
            if (!permissionsGranted) {
                item {
                    Button(onClick = { onPermissionsLaunch(permissions) }) {
                        Text(text = stringResource(R.string.permissions_button_label))
                    }
                }
            } else {
                item {
                    Text(
                        text = "${recordType.clazz.simpleName} [$uid]",
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1)
                }
                item {
                    Text(
                        text = "${seriesRecordsType.clazz.simpleName} list",
                    )
                }
                when (seriesRecordsType) {
                    SeriesRecordsType.STEPS -> {
                        for (record in recordList.map { it as StepsRecord }) {
                            renderData(record, record.startTime, record.endTime) {
                                Text(text = "count: " + record.count)
                            }
                        }
                    }
                    SeriesRecordsType.DISTANCE -> {
                        for (record in recordList.map { it as DistanceRecord }) {
                            renderData(record, record.startTime, record.endTime) {
                                Text(text = "count: " + record.distance + " m")
                            }
                        }
                    }
                    SeriesRecordsType.CALORIES -> {
                        for (record in recordList.map { it as TotalCaloriesBurnedRecord }) {
                            renderData(record, record.startTime, record.endTime) {
                                Text(text = "Energy: " + record.energy)
                            }
                        }
                    }
                    SeriesRecordsType.HEARTRATE -> {
                        for (record in recordList.map { it as HeartRateRecord }) {
                            renderData(record, record.startTime, record.endTime) {
                                Text(
                                    text =
                                    "Heartbeat Samples: " +
                                            record.samples.map { it.beatsPerMinute }.joinToString(", "))
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
        startTime = sessionStartTime.toInstant(),
        startZoneOffset = sessionStartTime.offset,
        endTime = sessionEndTime.toInstant(),
        endZoneOffset = sessionEndTime.offset,
        count = Random.nextInt(9000).toLong() + 1000,
        metadata = Metadata(id = UUID.randomUUID().toString()))

fun LazyListScope.renderData(
    record: Record,
    startTime: Instant,
    endTime: Instant,
    content: @Composable () -> Unit
) {
    item {
        Text(text = "id: " + record.metadata.id)
        Text(text = formatDisplayTimeStartEnd(startTime, null, endTime, null))
        content()
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
