/*
 * Copyright 2021 The Android Open Source Project
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
package com.example.healthplatformsample.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.healthplatformsample.presentation.theme.HealthPlatformSampleTheme
import java.time.Instant
import java.util.UUID

@Composable
fun SessionRow(
    start: Instant,
    end: Instant,
    uid: String,
    name: String,
    modifier: Modifier = Modifier,
    onDeleteClick: (String) -> Unit = {},
    onDetailsClick: (String) -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        SessionInfoColumn(
            start,
            end,
            uid,
            name,
            modifier = modifier.weight(0.7f)
        )
        SessionButtonColumn(
            uid,
            modifier = modifier
                .weight(0.3f)
                .align(Alignment.CenterVertically),
            onDeleteClick = onDeleteClick,
            onDetailsClick = onDetailsClick
        )
    }
}

@Preview
@Composable
fun SessionRowPreview() {
    HealthPlatformSampleTheme {
        SessionRow(
            Instant.now().minusSeconds(3600),
            Instant.now(),
            UUID.randomUUID().toString(),
            "Running"
        )
    }
}
