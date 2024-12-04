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
package com.example.healthconnectsample.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.records.SleepSessionRecord
import com.example.healthconnectsample.R
import com.example.healthconnectsample.data.SleepSessionData
import com.example.healthconnectsample.data.dateTimeWithOffsetOrDefault
import com.example.healthconnectsample.data.formatHoursMinutes
import com.example.healthconnectsample.formatDisplayTimeStartEnd
import com.example.healthconnectsample.presentation.theme.HealthConnectTheme
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Creates a row to represent a [SleepSessionData], which encompasses data for both the sleep
 * session and any fine-grained sleep stages.
 */
@Composable
fun SleepSessionRow(
    sessionData: SleepSessionData,
    startExpanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(startExpanded) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clickable {
                expanded = !expanded
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val formatter = DateTimeFormatter.ofPattern("eee, d LLL")
        val startDateTime =
            dateTimeWithOffsetOrDefault(sessionData.startTime, sessionData.startZoneOffset)
        Text(
            modifier = Modifier
                .weight(0.4f),
            color = MaterialTheme.colors.primary,
            text = startDateTime.format(formatter)
        )
        if (!expanded) {
            Text(
                modifier = Modifier
                    .weight(0.4f),
                text = sessionData.duration?.formatHoursMinutes()
                    ?: stringResource(id = R.string.not_available_abbrev)
            )
        }
        IconButton(
            onClick = { expanded = !expanded }
        ) {
            val icon = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown
            Icon(icon, stringResource(R.string.delete_button))
        }
    }
    if (expanded) {
        val startEndLabel = formatDisplayTimeStartEnd(
            sessionData.startTime,
            sessionData.startZoneOffset,
            sessionData.endTime,
            sessionData.endZoneOffset
        )
        SleepSessionDetailRow(labelId = R.string.sleep_time, item = startEndLabel)
        SleepSessionDetailRow(
            labelId = R.string.sleep_duration,
            item = sessionData.duration?.formatHoursMinutes()
        )
        SleepSessionDetailRow(labelId = R.string.sleep_notes, item = sessionData.notes)
        if (sessionData.stages.isNotEmpty()) {
            SleepSessionDetailRow(labelId = R.string.sleep_stages, item = "")
            SleepStagesDetail(sessionData.stages)
        }
    }
}

@Preview
@Composable
fun SleepSessionRowPreview() {
    HealthConnectTheme {
        val end = ZonedDateTime.now()
        val start = end.minusHours(1)
        Column {
            SleepSessionRow(
                SleepSessionData(
                    uid = "123",
                    title = "My sleep",
                    notes = "Slept well",
                    startTime = start.toInstant(),
                    startZoneOffset = start.offset,
                    endTime = end.toInstant(),
                    endZoneOffset = end.offset,
                    duration = Duration.between(start, end),
                    stages = listOf(
                        SleepSessionRecord.Stage(
                            stage = SleepSessionRecord.STAGE_TYPE_DEEP,
                            startTime = start.toInstant(),
                            endTime = end.toInstant(),
                        )
                    )
                ),
                startExpanded = true
            )
        }
    }
}
