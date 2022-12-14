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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.passivegoalscompose.R
import com.example.passivegoalscompose.theme.PassiveGoalsTheme

/**
 * Displays number of floor goals hit with icon and label.
 */
@Composable
fun StepsChip(
    stepsGoalAchieved: Boolean,
    modifier: Modifier = Modifier
) {
    Chip(
        modifier = modifier,
        onClick = {},
        label = {
            val stepsText = if (stepsGoalAchieved) {
                stringResource(id = R.string.steps_goal_achieved)
            } else {
                stringResource(id = R.string.steps_goal_not_yet_achieved)
            }
            Text(stepsText)
        },
        secondaryLabel = {
            Text(
                text = stringResource(id = R.string.steps_goals_description),
                style = MaterialTheme.typography.caption3
            )
        },
        icon = {
            Icon(
                imageVector = Icons.Default.Celebration,
                contentDescription = stringResource(R.string.steps_description),
                tint = if (stepsGoalAchieved) Color.Green else Color.LightGray
            )
        }
    )
}

@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showSystemUi = true
)
@Composable
fun StepsChipPreview() {
    PassiveGoalsTheme {
        StepsChip(true)
    }
}
