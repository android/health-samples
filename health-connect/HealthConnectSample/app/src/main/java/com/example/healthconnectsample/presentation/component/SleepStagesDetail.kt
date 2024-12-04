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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.records.SleepSessionRecord
import com.example.healthconnectsample.formatDisplayTimeStartEnd
import com.example.healthconnectsample.presentation.theme.HealthConnectTheme
import java.time.ZonedDateTime

@Composable
fun SleepStagesDetail(sleepStages: List<SleepSessionRecord.Stage>) {
    sleepStages.forEach { stage ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {
            val startEndLabel = formatDisplayTimeStartEnd(
                stage.startTime, null, stage.endTime, null
            )
            Text(
                modifier = Modifier.weight(0.5f),
                text = startEndLabel,
                color = MaterialTheme.colors.primary,
                textAlign = TextAlign.Start
            )
            Text(
                modifier = Modifier
                    .weight(0.4f),
                text = SleepSessionRecord.STAGE_TYPE_INT_TO_STRING_MAP[stage.stage] ?: "unknown",
                textAlign = TextAlign.Start
            )
        }
    }
}

@Preview
@Composable
fun SleepStagesDetailPreview() {
    HealthConnectTheme {
        val end2 = ZonedDateTime.now()
        val start2 = end2.minusHours(1)
        val start1 = start2.minusHours(1)
        Column {
            SleepStagesDetail(
                sleepStages = listOf(
                    SleepSessionRecord.Stage(
                        stage = SleepSessionRecord.STAGE_TYPE_DEEP,
                        startTime = start2.toInstant(),
                        endTime = end2.toInstant()
                    ),
                    SleepSessionRecord.Stage(
                        stage = SleepSessionRecord.STAGE_TYPE_LIGHT,
                        startTime = start1.toInstant(),
                        endTime = start2.toInstant()
                    )
                )
            )
        }
    }
}
