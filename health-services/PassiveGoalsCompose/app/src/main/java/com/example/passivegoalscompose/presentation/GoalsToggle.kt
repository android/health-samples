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
package com.example.passivegoalscompose.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import com.example.passivegoalscompose.PERMISSION
import com.example.passivegoalscompose.R
import com.example.passivegoalscompose.theme.PassiveGoalsTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted

/**
 * A [ToggleChip] for enabling / disabling passive goals.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PassiveGoalsToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    permissionState: PermissionState,
    modifier: Modifier = Modifier
) {
    ToggleChip(
        modifier = modifier,
        checked = checked,
        colors = ToggleChipDefaults.toggleChipColors(),
        onCheckedChange = { enabled ->
            if (permissionState.status.isGranted) {
                onCheckedChange(enabled)
            } else {
                permissionState.launchPermissionRequest()
            }
        },
        label = { Text(stringResource(id = R.string.goals_toggle)) },
        toggleControl = {
            Icon(
                imageVector = ToggleChipDefaults.switchIcon(checked),
                contentDescription = stringResource(id = R.string.goals_toggle)
            )
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showSystemUi = true
)
@Composable
fun HeartRateTogglePreview() {
    val permissionState = object : PermissionState {
        override val permission = PERMISSION
        override val status: PermissionStatus = PermissionStatus.Granted
        override fun launchPermissionRequest() {}
    }
    PassiveGoalsTheme {
        PassiveGoalsToggle(
            checked = true,
            onCheckedChange = {},
            permissionState = permissionState
        )
    }
}
