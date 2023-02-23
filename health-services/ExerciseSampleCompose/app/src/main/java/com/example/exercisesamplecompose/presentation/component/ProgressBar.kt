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

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme

@Composable
fun ProgressBar() {
    CircularProgressIndicator(
        indicatorColor = MaterialTheme.colors.secondary,
        trackColor = MaterialTheme.colors.onBackground.copy(alpha = 0.1f),
        strokeWidth = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    )
}

@Preview
@Composable
fun ProgressBarPreview() {
    ProgressBar()
}
