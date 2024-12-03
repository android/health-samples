package com.example.recordingapionmobilesample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.recordingapionmobilesample.helper.PermissionHelper
import com.example.recordingapionmobilesample.screen.recordingAPIonMobile.RecordingAPIonMobileScreen
import com.example.recordingapionmobilesample.screen.recordingAPIonMobile.RecordingAPIonMobileViewModel
import com.example.recordingapionmobilesample.ui.theme.RecordingAPIOnMobileSampleTheme
import com.google.android.gms.fitness.LocalRecordingClient

@Composable
fun RecordingApiOnMobileApp(
    localRecordingClient: LocalRecordingClient,
    permissionHelper: PermissionHelper
){
    RecordingAPIOnMobileSampleTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column (
                modifier = Modifier.padding(innerPadding)
            ){
                RecordingAPIonMobileScreen(
                    RecordingAPIonMobileViewModel(
                        localRecordingClient,
                        permissionHelper
                    )
                )
            }
        }
    }
}
