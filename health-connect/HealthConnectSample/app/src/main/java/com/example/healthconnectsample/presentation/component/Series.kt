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

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.metadata.Metadata
import com.example.healthconnectsample.R
import com.example.healthconnectsample.data.dateTimeWithOffsetOrDefault
import com.example.healthconnectsample.presentation.theme.HealthConnectTheme
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Displays a list of [SpeedRecord] data in the [LazyColumn].
 */
fun LazyListScope.speedSeries(
    @StringRes labelId: Int,
    series: List<SpeedRecord>
) {
    seriesHeading(labelId)
    series.forEach { serie ->
        seriesDateTimeHeading(
            start = serie.startTime,
            startZoneOffset = serie.startZoneOffset,
            end = serie.endTime,
            endZoneOffset = serie.endZoneOffset
        )
        items(serie.samples) { SeriesRow(String.format("%.1f", it.speed.inMetersPerSecond)) }
    }
}

/**
 * Displays a list of [HeartRateRecord] data in the [LazyColumn].
 */
fun LazyListScope.heartRateSeries(
    @StringRes labelId: Int,
    series: List<HeartRateRecord>
) {
    seriesHeading(labelId)
    series.forEach { serie ->
        seriesDateTimeHeading(
            start = serie.startTime,
            startZoneOffset = serie.startZoneOffset,
            end = serie.endTime,
            endZoneOffset = serie.endZoneOffset
        )
        items(serie.samples) { SeriesRow(it.beatsPerMinute.toString()) }
    }
}

fun LazyListScope.seriesHeading(labelId: Int) {
    item {
        Text(
            text = stringResource(id = labelId),
            style = MaterialTheme.typography.h5,
            color = MaterialTheme.colors.primary
        )
    }
}

fun LazyListScope.seriesDateTimeHeading(
    start: Instant,
    startZoneOffset: ZoneOffset?,
    end: Instant,
    endZoneOffset: ZoneOffset?
) {
    item {
        val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)
        val startTime = dateTimeWithOffsetOrDefault(start, startZoneOffset)
        val endTime = dateTimeWithOffsetOrDefault(end, endZoneOffset)
        val dateLabel = dateFormatter.format(startTime)
        val startLabel = timeFormatter.format(startTime)
        val endLabel = timeFormatter.format(endTime)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                color = MaterialTheme.colors.secondary,
                text = "$dateLabel: $startLabel - $endLabel",
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SeriesRow(value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Sample: $value")
    }
}

@Preview
@Composable
fun HeartRateSeriesPreview() {
    HealthConnectTheme {
        LazyColumn {
            val time1 = Instant.now()
            val time2 = time1.minusSeconds(60)
            heartRateSeries(
                labelId = R.string.hr_series,
                series = listOf(
                    HeartRateRecord(
                        metadata = Metadata.manualEntry(),
                        startTime = time2,
                        startZoneOffset = ZoneId.systemDefault().rules.getOffset(time2),
                        endTime = time1,
                        endZoneOffset = ZoneId.systemDefault().rules.getOffset(time1),
                        samples = listOf(
                            HeartRateRecord.Sample(
                                beatsPerMinute = 103,
                                time = time1
                            ),
                            HeartRateRecord.Sample(
                                beatsPerMinute = 85,
                                time = time2
                            )
                        )
                    )
                )
            )
        }
    }
}
