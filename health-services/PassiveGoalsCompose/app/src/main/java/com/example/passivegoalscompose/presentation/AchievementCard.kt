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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import com.example.passivegoalscompose.R
import com.example.passivegoalscompose.theme.PassiveGoalsTheme

/**
 * Displays an achievement and whether it has been achieved.
 */
@Composable
fun AchievementCard(
    isAchieved: Boolean,
    achievedText: String,
    notAchievedText: String,
    achievementDescription: String,
    imageVector: ImageVector,
    imageDescription: String,
    modifier: Modifier = Modifier,
    transformation: SurfaceTransformation? = null
) {
    Card(
        onClick = {},
        enabled = false,
        modifier = modifier,
        transformation = transformation,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = imageDescription,
                tint = if (isAchieved) Color.Green else Color.LightGray,
                modifier = Modifier.padding(end = 8.dp)
            )
            Column {
                val stepsText = if (isAchieved) {
                    achievedText
                } else {
                    notAchievedText
                }
                Text(stepsText)
                Text(
                    text = achievementDescription,
                    style = MaterialTheme.typography.bodyExtraSmall
                )
            }
        }
    }
}

@Preview
@Composable
fun StepsChipPreview() {
    PassiveGoalsTheme {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            AchievementCard(
                isAchieved = true,
                achievedText = stringResource(id = R.string.steps_goal_achieved),
                notAchievedText = stringResource(id = R.string.steps_goal_not_yet_achieved),
                achievementDescription = stringResource(id = R.string.steps_goals_description),
                imageVector = Icons.Default.Celebration,
                imageDescription = stringResource(id = R.string.steps_description),
            )
        }
    }
}
