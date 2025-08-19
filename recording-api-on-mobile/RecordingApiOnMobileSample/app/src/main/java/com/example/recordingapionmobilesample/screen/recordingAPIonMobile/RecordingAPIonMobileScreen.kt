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

package com.example.recordingapionmobilesample.screen.recordingAPIonMobile

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.recordingapionmobilesample.R
import com.example.recordingapionmobilesample.screen.home.HomeScreen
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
fun RecordingAPIonMobileScreen(
  viewModel: RecordingAPIonMobileViewModel
) {
  val hasPermission = viewModel.hasPermission.value
  val bucketDataList = viewModel.bucketDataList

  // Launcher for requesting permission
  val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
    viewModel.setPermission(isGranted)
  }

  Column(
    modifier = Modifier.padding(16.dp)
  ) {
    Text(
      text = stringResource(R.string.recording_api_on_mobile_sample),
      style = MaterialTheme.typography.headlineMedium,
      modifier = Modifier
          .fillMaxWidth()
          .wrapContentSize(Alignment.Center)
    )

    HomeScreen(
      permissionsGranted = hasPermission,
      onPermissionClick = {
        launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
      },
      localDataTypes = viewModel.localDataTypes,
      selectedLocalDataType = viewModel.selectedLocalDataType.value,
      onLocalDataTypeClick = viewModel::setSelectedLocalDataType,
      onRawClick = {
        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
          .truncatedTo(ChronoUnit.HOURS)
          .plusHours(1)
        val startTime = endTime.minusHours(24)
        viewModel.readRawData(startTime, endTime)
      },
      onAggregateClick = {
        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
          .truncatedTo(ChronoUnit.HOURS)
          .plusHours(1)
        val startTime = endTime.minusHours(24)
        viewModel.readAggregateData(startTime, endTime)
      },
      bucketDataList = bucketDataList,
    )
  }
}