package com.example.healthconnectsample.presentation.component

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.changes.Change
import androidx.health.connect.client.changes.DeletionChange
import androidx.health.connect.client.changes.UpsertionChange
import androidx.health.connect.client.records.ActivityEvent
import androidx.health.connect.client.records.ActivitySession
import androidx.health.connect.client.records.Distance
import androidx.health.connect.client.records.HeartRateSeries
import androidx.health.connect.client.records.SleepSession
import androidx.health.connect.client.records.SleepStage
import androidx.health.connect.client.records.SpeedSeries
import androidx.health.connect.client.records.Steps
import androidx.health.connect.client.records.TotalCaloriesBurned
import androidx.health.connect.client.records.Weight
import com.example.healthconnectsample.R
import com.example.healthconnectsample.data.dateTimeWithOffsetOrDefault
import com.example.healthconnectsample.presentation.TAG
import com.example.healthconnectsample.presentation.theme.HealthConnectTheme
import java.time.ZonedDateTime

/**
 * Composables for formatting [Change] objects returned from Health Connect.
 */
@Composable
fun FormattedChange(change: Change) {
    when (change) {
        is UpsertionChange -> FormattedUpsertionChange(change)
        is DeletionChange -> FormattedDeletionChange(change)
    }
}

@Composable
fun FormattedUpsertionChange(change: UpsertionChange) {
    when (change.record) {
        is ActivitySession -> {
            val activity = change.record as ActivitySession
            FormattedChangeRow(
                startTime = dateTimeWithOffsetOrDefault(
                    activity.startTime,
                    activity.startZoneOffset
                ),
                recordType = stringResource(R.string.differential_changes_type_activity_session),
                dataSource = change.record.metadata.dataOrigin.packageName
            )
        }
        is ActivityEvent -> {
            val event = change.record as ActivityEvent
            FormattedChangeRow(
                startTime = dateTimeWithOffsetOrDefault(event.startTime, event.startZoneOffset),
                recordType = stringResource(R.string.differential_changes_type_activity_event),
                dataSource = change.record.metadata.dataOrigin.packageName
            )
        }
        is Steps -> {
            val steps = change.record as Steps
            FormattedChangeRow(
                startTime = dateTimeWithOffsetOrDefault(steps.startTime, steps.startZoneOffset),
                recordType = stringResource(R.string.differential_changes_type_steps),
                dataSource = change.record.metadata.dataOrigin.packageName
            )
        }
        is SpeedSeries -> {
            val speed = change.record as SpeedSeries
            FormattedChangeRow(
                startTime = dateTimeWithOffsetOrDefault(speed.startTime, speed.startZoneOffset),
                recordType = stringResource(R.string.differential_changes_type_speed_series),
                dataSource = change.record.metadata.dataOrigin.packageName
            )
        }
        is HeartRateSeries -> {
            val hr = change.record as HeartRateSeries
            FormattedChangeRow(
                startTime = dateTimeWithOffsetOrDefault(hr.startTime, hr.startZoneOffset),
                recordType = stringResource(R.string.differential_changes_type_heart_rate_series),
                dataSource = change.record.metadata.dataOrigin.packageName
            )
        }
        is TotalCaloriesBurned -> {
            val calories = change.record as TotalCaloriesBurned
            FormattedChangeRow(
                startTime = dateTimeWithOffsetOrDefault(
                    calories.startTime,
                    calories.startZoneOffset
                ),
                recordType = stringResource(R.string.differential_changes_type_total_calories),
                dataSource = change.record.metadata.dataOrigin.packageName
            )
        }
        is SleepSession -> {
            val sleep = change.record as SleepSession
            FormattedChangeRow(
                startTime = dateTimeWithOffsetOrDefault(sleep.startTime, sleep.startZoneOffset),
                recordType = stringResource(R.string.differential_changes_type_sleep_session),
                dataSource = change.record.metadata.dataOrigin.packageName
            )
        }
        is SleepStage -> {
            val sleep = change.record as SleepStage
            FormattedChangeRow(
                startTime = dateTimeWithOffsetOrDefault(sleep.startTime, sleep.startZoneOffset),
                recordType = stringResource(R.string.differential_changes_type_sleep_stage),
                dataSource = change.record.metadata.dataOrigin.packageName
            )
        }
        is Weight -> {
            val weight = change.record as Weight
            FormattedChangeRow(
                startTime = dateTimeWithOffsetOrDefault(weight.time, weight.zoneOffset),
                recordType = stringResource(R.string.differential_changes_type_weight),
                dataSource = change.record.metadata.dataOrigin.packageName
            )
        }
        is Distance -> {
            val distance = change.record as Distance
            FormattedChangeRow(
                startTime = dateTimeWithOffsetOrDefault(
                    distance.startTime,
                    distance.startZoneOffset
                ),
                recordType = stringResource(R.string.differential_changes_type_distance),
                dataSource = change.record.metadata.dataOrigin.packageName
            )
        }
        else -> {
            Log.w(TAG, "Unknown record type: ${change.record}")
        }
    }
}

@Composable
fun FormattedDeletionChange(change: DeletionChange) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = stringResource(R.string.differential_changes_deleted),
                style = MaterialTheme.typography.body2
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("UID: ${change.deletedUid}")
        }
    }
}

@Composable
fun FormattedChangeRow(
    startTime: ZonedDateTime,
    recordType: String,
    dataSource: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = stringResource(R.string.differential_changes_upserted),
                style = MaterialTheme.typography.body2
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${startTime.toLocalTime()}")
            Text(recordType)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = dataSource,
                style = MaterialTheme.typography.body2
            )
        }
    }
}

@Preview
@Composable
fun FormattedChangeRowPreview() {
    HealthConnectTheme {
        FormattedChangeRow(
            startTime = ZonedDateTime.now().withNano(0),
            recordType = stringResource(id = R.string.differential_changes_type_activity_session),
            dataSource = LocalContext.current.packageName
        )
    }
}