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
