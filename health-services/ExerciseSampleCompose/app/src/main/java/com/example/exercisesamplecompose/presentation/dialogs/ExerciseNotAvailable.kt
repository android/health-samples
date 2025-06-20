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
package com.example.exercisesamplecompose.presentation.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material3.ConfirmationDialog
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.example.exercisesamplecompose.R
import com.example.exercisesamplecompose.presentation.theme.ThemePreview

/**
 * Screen that appears if an exercise is not available for the particular device
 */
@Composable
fun ExerciseNotAvailable() {
    var showConfirmation by remember { mutableStateOf(true) }

    ConfirmationDialog(
        visible = showConfirmation,
        onDismissRequest = { showConfirmation = false },
        text = { Text(text = stringResource(id = R.string.not_avail)) }
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = stringResource(id = R.string.not_avail)
        )
    }
}

@WearPreviewDevices
@Composable
fun ExerciseNotAvailablePreview() {
    ThemePreview {
        ExerciseNotAvailable()
    }
}
