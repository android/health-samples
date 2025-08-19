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

package com.example.recordingapionmobilesample.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.recordingapionmobilesample.component.BucketRow
import com.example.recordingapionmobilesample.data.BucketData
import com.example.recordingapionmobilesample.data.DataPointData
import com.example.recordingapionmobilesample.data.DataSetData
import com.google.android.gms.fitness.data.LocalDataType
import java.time.LocalDateTime
import java.time.ZoneOffset

@Composable
fun HomeScreen(
  permissionsGranted: Boolean,
  onPermissionClick: () -> Unit = {},
  localDataTypes: List<LocalDataType> = listOf(),
  selectedLocalDataType: LocalDataType,
  onLocalDataTypeClick: (LocalDataType) -> Unit = {},
  onRawClick: () -> Unit = {},
  onAggregateClick: () -> Unit = {},
  bucketDataList: List<BucketData>,
) {

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    if (!permissionsGranted) {
      item {
        HomeScreenButton(
          text = "Request Permissions",
          onClick = {
            onPermissionClick()
          }
        )
      }
    } else {
      // Local Data Types
      item {
        Column(
          modifier = Modifier.selectableGroup()
        ) {
          localDataTypes.forEach { localDataType ->
            Row(
              Modifier
                  .fillMaxWidth()
                  .height(56.dp)
                  .selectable(
                      selected = (localDataType == selectedLocalDataType),
                      onClick = { onLocalDataTypeClick(localDataType) },
                      role = Role.RadioButton
                  ),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = (localDataType == selectedLocalDataType),
                onClick = null
              )
              Text(
                text = when (localDataType) {
                  LocalDataType.TYPE_STEP_COUNT_DELTA -> "Steps"
                  LocalDataType.TYPE_DISTANCE_DELTA -> "Distance"
                  LocalDataType.TYPE_CALORIES_EXPENDED -> "Calories"
                  else -> "Unknown"
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 16.dp)
              )
            }
          }
        }
      }
      item {
        HomeScreenButton(
          text = "Read Raw Data in Last 24 Hours",
          onClick = {
            onRawClick()
          }
        )
      }
      item {
        HomeScreenButton(
          text = "Read Aggregate Data in Last 24 Hours",
          onClick = {
            onAggregateClick()
          }
        )
      }
      items(bucketDataList) { bucketData ->
        Card(
          modifier = Modifier.padding(8.dp)
        ) {
          BucketRow(bucketData)
        }
      }
    }
  }
}

@Composable
fun HomeScreenButton(text: String, onClick: () -> Unit) {
  Button(
    modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .padding(4.dp),
    onClick = onClick
  ) {
    Text(text)
  }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
  HomeScreen(
    permissionsGranted = true,
    localDataTypes = listOf(
      LocalDataType.TYPE_STEP_COUNT_DELTA,
      LocalDataType.TYPE_DISTANCE_DELTA,
      LocalDataType.TYPE_CALORIES_EXPENDED
    ),
    selectedLocalDataType = LocalDataType.TYPE_DISTANCE_DELTA,
    bucketDataList = listOf(
      BucketData(
        index = 0,
        startTime = LocalDateTime
          .now()
          .minusHours(2)
          .toEpochSecond(ZoneOffset.UTC),
        endTime = LocalDateTime
          .now()
          .minusHours(1)
          .toEpochSecond(ZoneOffset.UTC),
        dataSetDataList = listOf(
          DataSetData(
            index = 0,
            dataPointDataList = listOf(
              DataPointData(
                index = 0,
                startTime = LocalDateTime
                  .now()
                  .minusHours(2)
                  .plusMinutes(5)
                  .toEpochSecond(ZoneOffset.UTC),
                endTime = LocalDateTime
                  .now()
                  .minusHours(2)
                  .plusMinutes(10)
                  .toEpochSecond(ZoneOffset.UTC),
                fieldName = "Steps",
                fieldValue = "2"
              )
            )
          )
        )
      ),
      BucketData(
        index = 1,
        startTime = LocalDateTime
          .now()
          .minusHours(1)
          .toEpochSecond(ZoneOffset.UTC),
        endTime = LocalDateTime
          .now()
          .toEpochSecond(ZoneOffset.UTC),
        dataSetDataList = listOf(
          DataSetData(
            index = 0,
            dataPointDataList = listOf(
              DataPointData(
                index = 0,
                startTime = LocalDateTime
                  .now()
                  .minusHours(1)
                  .plusMinutes(5)
                  .toEpochSecond(ZoneOffset.UTC),
                endTime = LocalDateTime
                  .now()
                  .minusHours(1)
                  .plusMinutes(10)
                  .toEpochSecond(ZoneOffset.UTC),
                fieldName = "Steps",
                fieldValue = "3"
              ),
              DataPointData(
                index = 1,
                startTime = LocalDateTime
                  .now()
                  .minusHours(1)
                  .plusMinutes(15)
                  .toEpochSecond(ZoneOffset.UTC),
                endTime = LocalDateTime
                  .now()
                  .minusHours(1)
                  .plusMinutes(20)
                  .toEpochSecond(ZoneOffset.UTC),
                fieldName = "Steps",
                fieldValue = "5"
              )
            )
          )
        )
      )
    )
  )
}