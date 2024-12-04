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
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.records.ExerciseSessionRecord
import com.example.healthconnectsample.presentation.theme.HealthConnectTheme
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import java.time.ZonedDateTime
import java.util.UUID

/**
 * Displays summary information about the [ExerciseSessionRecord]
 */

@Composable
fun ExerciseSessionInfoColumn(
    start: ZonedDateTime,
    end: ZonedDateTime,
    uid: String,
    name: String,
    sourceAppName: String,
    sourceAppIcon: Drawable?,
    onClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier.clickable {
            onClick(uid)
        }
    ) {
        Text(
            color = MaterialTheme.colors.primary,
            text = "${start.toLocalTime()} - ${end.toLocalTime()}",
            style = MaterialTheme.typography.caption
        )
        Text(name)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .padding(4.dp, 2.dp)
                    .height(16.dp)
                    .width(16.dp),
                painter = rememberDrawablePainter(drawable = sourceAppIcon),
                contentDescription = "App Icon"
            )
            Text(
                text = sourceAppName,
                fontStyle = FontStyle.Italic
            )
        }
        Text(uid)
    }
}

@Preview
@Composable
fun ExerciseSessionInfoColumnPreview() {
    HealthConnectTheme {
        ExerciseSessionInfoColumn(
            ZonedDateTime.now().minusMinutes(30),
            ZonedDateTime.now(),
            UUID.randomUUID().toString(),
            "Running",
            "My Fitness App",
            null
        )
    }
}
