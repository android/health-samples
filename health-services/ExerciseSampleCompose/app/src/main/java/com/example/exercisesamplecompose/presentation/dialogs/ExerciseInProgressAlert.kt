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
@file:OptIn(ExperimentalHorologistApi::class)

package com.example.exercisesamplecompose.presentation.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.example.exercisesamplecompose.R
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.material.AlertDialog


@Composable
fun ExerciseInProgressAlert(
    onNegative: () -> Unit,
    onPositive: () -> Unit,
    showDialog: Boolean,
) {
    AlertDialog(
        title = stringResource(id = R.string.exercise_in_progress),
        message = stringResource(id = R.string.ending_continue),
        onCancel = onNegative,
        onOk = onPositive,
        showDialog = showDialog,
    )
}

@WearPreviewDevices
@Composable
fun ExerciseInProgressAlertPreview() {
    ExerciseInProgressAlert(onNegative = {}, onPositive = {}, showDialog = true)
}

