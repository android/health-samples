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
package com.example.passivedatacompose.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import com.example.passivedatacompose.PERMISSION
import com.example.passivedatacompose.R
import com.example.passivedatacompose.theme.PassiveDataTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted

/**
 * A [SwitchButton] for enabling / disabling passive monitoring.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HeartRateToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    permissionState: PermissionState,
    modifier: Modifier = Modifier
) {
    SwitchButton(
        modifier = modifier,
        checked = checked,
        onCheckedChange = { enabled ->
            if (permissionState.status.isGranted) {
                onCheckedChange(enabled)
            } else {
                permissionState.launchPermissionRequest()
            }
        },
        label = { Text(stringResource(id = R.string.heart_rate_toggle)) },
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Preview
@Composable
fun HeartRateTogglePreview() {
    val permissionState = object : PermissionState {
        override val permission = PERMISSION
        override val status: PermissionStatus = PermissionStatus.Granted
        override fun launchPermissionRequest() {}
    }
    PassiveDataTheme {
        HeartRateToggle(
            checked = true,
            onCheckedChange = {},
            permissionState = permissionState
        )
    }
}
