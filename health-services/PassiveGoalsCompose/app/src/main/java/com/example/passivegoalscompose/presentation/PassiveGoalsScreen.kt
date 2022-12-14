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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.ScalingLazyColumn
import com.example.passivegoalscompose.theme.PassiveGoalsTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import java.time.Instant

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PassiveGoalsScreen(
    latestFloorsTime: Instant,
    stepsGoalAchieved: Boolean,
    goalsEnabled: Boolean,
    onEnableClick: (Boolean) -> Unit,
    permissionState: PermissionState
) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            PassiveGoalsToggle(
                modifier = Modifier.fillMaxWidth(),
                checked = goalsEnabled,
                onCheckedChange = onEnableClick,
                permissionState = permissionState
            )
        }
        item {
            StepsChip(
                modifier = Modifier.fillMaxWidth(),
                stepsGoalAchieved = stepsGoalAchieved
            )
        }
        item {
            FloorsChip(
                modifier = Modifier.fillMaxWidth(),
                floorGoalsLastAchieved = latestFloorsTime
            )
        }
    }
}

@ExperimentalPermissionsApi
@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showBackground = false,
    showSystemUi = true
)
@Composable
fun PassiveGoalsScreenPreview() {
    val permissionState = object : PermissionState {
        override val permission = "android.permission.ACTIVITY_RECOGNITION"
        override val status: PermissionStatus = PermissionStatus.Granted
        override fun launchPermissionRequest() {}
    }
    PassiveGoalsTheme {
        PassiveGoalsScreen(
            latestFloorsTime = Instant.now(),
            stepsGoalAchieved = true,
            goalsEnabled = true,
            onEnableClick = {},
            permissionState = permissionState
        )
    }
}
