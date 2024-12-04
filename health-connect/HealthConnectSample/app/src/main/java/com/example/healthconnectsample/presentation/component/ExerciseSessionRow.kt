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
package com.example.healthconnectsample.presentation.component

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.records.ExerciseSessionRecord
import com.example.healthconnectsample.R
import com.example.healthconnectsample.presentation.theme.HealthConnectTheme
import java.time.ZonedDateTime
import java.util.UUID

/**
 * Creates a row to represent an [ExerciseSessionRecord]
 */
@Composable
fun ExerciseSessionRow(
    start: ZonedDateTime,
    end: ZonedDateTime,
    uid: String,
    name: String,
    sourceAppName: String,
    sourceAppIcon: Drawable?,
    onDeleteClick: (String) -> Unit = {},
    onDetailsClick: (String) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ExerciseSessionInfoColumn(
            start = start,
            end = end,
            uid = uid,
            name = name,
            sourceAppName = sourceAppName,
            sourceAppIcon = sourceAppIcon,
            onClick = onDetailsClick
        )
        IconButton(
            onClick = { onDeleteClick(uid) },
        ) {
            Icon(Icons.Default.Delete, stringResource(R.string.delete_button))
        }
        IconButton(
            onClick = { onDetailsClick(uid) },
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(R.string.details_button))
        }
    }
}

@Preview
@Composable
fun ExerciseSessionRowPreview() {
    val context = LocalContext.current
    HealthConnectTheme {
        ExerciseSessionRow(
            ZonedDateTime.now().minusMinutes(30),
            ZonedDateTime.now(),
            UUID.randomUUID().toString(),
            "Running",
            sourceAppName = "My Fitness app",
            sourceAppIcon = context.getDrawable(R.drawable.ic_launcher_foreground)
        )
    }
}
