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

import android.app.Activity
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.example.exercisesamplecompose.R
import com.example.exercisesamplecompose.theme.ExerciseSampleTheme


@Composable
fun ExerciseInProgressAlert(isTrackingExercise: Boolean) {
    val showDialog = remember { mutableStateOf(isTrackingExercise) }
    val context = LocalContext.current
    ExerciseSampleTheme {
        Dialog(showDialog = showDialog.value,
            onDismissRequest = { showDialog.value = false }) {
            Alert(
                title = {
                    Text(
                        stringResource(id = R.string.exercise_in_progress),
                        textAlign = TextAlign.Center
                    )
                },
                negativeButton = {
                    Button(
                        onClick = { (context as? Activity)?.finish() },
                        modifier = Modifier.size(ButtonDefaults.SmallButtonSize)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.deny)
                        )

                    }
                },
                positiveButton = {
                    Button(
                        onClick = { showDialog.value = false },
                        modifier = Modifier.size(ButtonDefaults.SmallButtonSize)
                    ) {
                        Icon(
                            Icons.Default.Check, contentDescription = stringResource(
                                id = R.string.confirm
                            )
                        )
                    }
                }
            ) {
                Text(
                    text = stringResource(id = R.string.ending_continue),
                    textAlign = TextAlign.Center
                )

            }
        }
    }
}

//Start interactive mode to preview the dialog
@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun ExerciseInProgressAlertPreview() {
    ExerciseInProgressAlert(true)
}

