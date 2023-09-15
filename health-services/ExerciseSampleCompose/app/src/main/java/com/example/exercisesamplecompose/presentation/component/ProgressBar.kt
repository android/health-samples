/*
 * Copyright 2022 The Android Open Source Project
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
package com.example.exercisesamplecompose.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import com.google.android.horologist.compose.ambient.AmbientState

@Composable
fun ProgressBar(
    ambientState: AmbientState,
    modifier: Modifier = Modifier
) {
    when (ambientState) {
        is AmbientState.Interactive -> {
            CircularProgressIndicator(
                indicatorColor = MaterialTheme.colors.secondary,
                trackColor = MaterialTheme.colors.onBackground.copy(alpha = 0.1f),
                strokeWidth = 4.dp,
                modifier = modifier
            )
        }

        is AmbientState.Ambient -> {
            CircularProgressIndicator(
                indicatorColor = MaterialTheme.colors.secondary,
                trackColor = MaterialTheme.colors.onBackground.copy(alpha = 0.1f),
                strokeWidth = 4.dp,
                modifier = modifier,
                progress = 0f
            )
        }
    }
}

@Preview
@Composable
fun ProgressBarPreview() {
    ProgressBar(
        AmbientState.Interactive,
        Modifier
            .size(100.dp)
            .background(Color.Black)
    )
}

@Preview
@Composable
fun ProgressBarPreviewAmbient() {
    ProgressBar(
        AmbientState.Ambient(),
        Modifier
            .size(100.dp)
            .background(Color.Black)
    )
}
