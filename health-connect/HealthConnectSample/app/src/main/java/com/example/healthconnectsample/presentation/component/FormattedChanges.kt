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
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepStageRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
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
        is ExerciseSessionRecord -> {
            val activity = change.record as ExerciseSessionRecord
            FormattedChangeRow(
                startTime = dateTimeWithOffsetOrDefault(
                    activity.startTime,
                    activity.startZoneOffset
                ),
                recordType = stringResource(R.string.differential_changes_type_exercise_session),
                dataSource = change.record.metadata.dataOrigin.packageName
            )
        }
        is StepsRecord -> {
            val steps = change.record as StepsRecord
            FormattedChangeRow(
                startTime = dateTimeWithOffsetOrDefault(steps.startTime, steps.startZoneOffset),
                recordType = stringResource(R.string.differential_changes_type_steps),
                dataSource = change.record.metadata.dataOrigin.packageName
            )
        }
        is SpeedRecord -> {
            val speed = change.record as SpeedRecord
            FormattedChangeRow(
                startTime = dateTimeWithOffsetOrDefault(speed.startTime, speed.startZoneOffset),
                recordType = stringResource(R.string.differential_changes_type_speed_series),
                dataSource = change.record.metadata.dataOrigin.packageName
            )
        }
        is HeartRateRecord -> {
            val hr = change.record as HeartRateRecord
            FormattedChangeRow(
                startTime = dateTimeWithOffsetOrDefault(hr.startTime, hr.startZoneOffset),
                recordType = stringResource(R.string.differential_changes_type_heart_rate_series),
                dataSource = change.record.metadata.dataOrigin.packageName
            )
        }
        is TotalCaloriesBurnedRecord -> {
            val calories = change.record as TotalCaloriesBurnedRecord
            FormattedChangeRow(
                startTime = dateTimeWithOffsetOrDefault(
                    calories.startTime,
                    calories.startZoneOffset
                ),
                recordType = stringResource(R.string.differential_changes_type_total_calories),
                dataSource = change.record.metadata.dataOrigin.packageName
            )
        }
        is SleepSessionRecord -> {
            val sleep = change.record as SleepSessionRecord
            FormattedChangeRow(
                startTime = dateTimeWithOffsetOrDefault(sleep.startTime, sleep.startZoneOffset),
                recordType = stringResource(R.string.differential_changes_type_sleep_session),
                dataSource = change.record.metadata.dataOrigin.packageName
            )
        }
        is SleepStageRecord -> {
            val sleep = change.record as SleepStageRecord
            FormattedChangeRow(
                startTime = dateTimeWithOffsetOrDefault(sleep.startTime, sleep.startZoneOffset),
                recordType = stringResource(R.string.differential_changes_type_sleep_stage),
                dataSource = change.record.metadata.dataOrigin.packageName
            )
        }
        is WeightRecord -> {
            val weight = change.record as WeightRecord
            FormattedChangeRow(
                startTime = dateTimeWithOffsetOrDefault(weight.time, weight.zoneOffset),
                recordType = stringResource(R.string.differential_changes_type_weight),
                dataSource = change.record.metadata.dataOrigin.packageName
            )
        }
        is DistanceRecord -> {
            val distance = change.record as DistanceRecord
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
            Text("UID: ${change.recordId}")
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
            recordType = stringResource(id = R.string.differential_changes_type_exercise_session),
            dataSource = LocalContext.current.packageName
        )
    }
}