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

package com.example.recordingapionmobilesample.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.recordingapionmobilesample.data.DataPointData
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


@Composable
fun DataPointRow(
  dataPointData: DataPointData
) {
  val formatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd HH:mm:ss")
    .withZone(ZoneId.systemDefault())
  val formattedStartTime = formatter.format(Instant.ofEpochSecond(dataPointData.startTime))
  val formattedEndTime = formatter.format(Instant.ofEpochSecond(dataPointData.endTime))

  Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Text(text = "DataPoint #${dataPointData.index}")
      Text(text = "Start: $formattedStartTime")
      Text(text = "End: $formattedEndTime")
      Text(text = "${dataPointData.fieldName}: ${dataPointData.fieldValue}")
    }
  }
}

@Preview(showBackground = true)
@Composable
fun DataPointRowPreview() {
  DataPointRow(
    dataPointData = DataPointData(
      index = 0,
      startTime = LocalDateTime
        .now()
        .minusMinutes(1)
        .toEpochSecond(ZoneOffset.UTC),
      endTime = LocalDateTime
        .now()
        .toEpochSecond(ZoneOffset.UTC),
      fieldName = "Steps",
      fieldValue = "23"
    )
  )
}