package com.example.recordingapionmobilesample.screen.recordingAPIonMobile

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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

    Column{
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