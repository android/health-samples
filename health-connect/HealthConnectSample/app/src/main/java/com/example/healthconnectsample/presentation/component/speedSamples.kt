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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.metadata.Metadata
import androidx.health.connect.client.records.Speed
import com.example.healthconnectsample.R
import com.example.healthconnectsample.data.dateTimeWithOffsetOrDefault
import com.example.healthconnectsample.presentation.theme.HealthConnectTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Displays a list of [Speed] data in the [LazyColumn].
 */
fun LazyListScope.speedSamples(
    @StringRes labelId: Int,
    series: List<Speed>
) {
    item {
        Text(
            text = stringResource(id = labelId),
            style = MaterialTheme.typography.h5,
            color = MaterialTheme.colors.primary
        )
    }
    items(series) {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val time = dateTimeWithOffsetOrDefault(it.time, it.zoneOffset)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatter.format(time)
            )
            Text(
                text = it.speed.toString()
            )
        }
    }
}

@Preview
@Composable
fun SpeedSamplesPreview() {
    HealthConnectTheme {
        LazyColumn {
            val time1 = Instant.now()
            val time2 = time1.minusSeconds(60)
            speedSamples(
                labelId = R.string.speed_samples,
                series = listOf(
                    Speed(
                        speed = 2.7,
                        time = time1,
                        zoneOffset = ZoneId.systemDefault().rules.getOffset(time1),
                        metadata = Metadata()
                    ),
                    Speed(
                        speed = 2.8,
                        time = time2,
                        zoneOffset = ZoneId.systemDefault().rules.getOffset(time2),
                        metadata = Metadata()
                    )
                )
            )
        }
    }
}
