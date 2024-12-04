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
package com.example.healthconnectsample.presentation.screen.inputreadings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.units.Mass
import com.example.healthconnectsample.R
import com.example.healthconnectsample.data.HealthConnectAppInfo
import com.example.healthconnectsample.data.WeightData
import com.example.healthconnectsample.presentation.theme.HealthConnectTheme
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID

@Composable
fun InputReadingsScreen(
    permissions: Set<String>,
    permissionsGranted: Boolean,
    readingsList: List<WeightData>,
    uiState: InputReadingsViewModel.UiState,
    onInsertClick: (Double) -> Unit = {},
    onDeleteClick: (String) -> Unit = {},
    onError: (Throwable?) -> Unit = {},
    onPermissionsResult: () -> Unit = {},
    weeklyAvg: Mass?,
    onPermissionsLaunch: (Set<String>) -> Unit = {}
) {

    // Remember the last error ID, such that it is possible to avoid re-launching the error
    // notification for the same error when the screen is recomposed, or configuration changes etc.
    val errorId = rememberSaveable { mutableStateOf(UUID.randomUUID()) }

    LaunchedEffect(uiState) {
        // If the initial data load has not taken place, attempt to load the data.
        if (uiState is InputReadingsViewModel.UiState.Uninitialized) {
            onPermissionsResult()
        }

        // The [InputReadingsScreenViewModel.UiState] provides details of whether the last action
        // was a success or resulted in an error. Where an error occurred, for example in reading
        // and writing to Health Connect, the user is notified, and where the error is one that can
        // be recovered from, an attempt to do so is made.
        if (uiState is InputReadingsViewModel.UiState.Error && errorId.value != uiState.uuid) {
            onError(uiState.exception)
            errorId.value = uiState.uuid
        }
    }

    var weightInput by remember { mutableStateOf("") }

    // Check if the input value is a valid weight
    fun hasValidDoubleInRange(weight: String): Boolean {
        val tempVal = weight.toDoubleOrNull()
        return if (tempVal == null) {
            false
        } else tempVal <= 1000
    }

    if (uiState != InputReadingsViewModel.UiState.Uninitialized) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!permissionsGranted) {
                item {
                    Button(
                        onClick = { onPermissionsLaunch(permissions) }
                    ) {
                        Text(text = stringResource(R.string.permissions_button_label))
                    }
                }
            } else {
                item {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = {
                            weightInput = it
                        },

                        label = {
                            Text(stringResource(id = R.string.weight_input))
                        },
                        isError = !hasValidDoubleInRange(weightInput),
                        keyboardActions = KeyboardActions { !hasValidDoubleInRange(weightInput) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    if (!hasValidDoubleInRange(weightInput)) {
                        Text(
                            text = stringResource(id = R.string.valid_weight_error_message),
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Button(
                        enabled = hasValidDoubleInRange(weightInput),
                        onClick = {
                            onInsertClick(weightInput.toDouble())
                            // clear TextField when new weight is entered
                            weightInput = ""
                        },

                        modifier = Modifier.fillMaxHeight()

                    ) {
                        Text(text = stringResource(id = R.string.add_readings_button))
                    }

                    Text(
                        text = stringResource(id = R.string.previous_readings),
                        fontSize = 24.sp,
                        color = MaterialTheme.colors.primary
                    )
                }
                val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                items(readingsList) { reading ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            modifier = Modifier
                                .padding(2.dp, 2.dp)
                                .height(16.dp)
                                .width(16.dp),
                            painter = rememberDrawablePainter(drawable = reading.sourceAppInfo?.icon),
                            contentDescription = "App Icon"
                        )
                        Text(
                            text = "%.1f ${stringResource(id = R.string.kilograms)}"
                                .format(reading.weight.inKilograms)
                        )
                        Text(text = formatter.format(reading.time))
                        IconButton(
                            onClick = { onDeleteClick(reading.id) },
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                stringResource(R.string.delete_button_readings)
                            )
                        }
                    }
                }
                item {
                    Text(
                        text = stringResource(id = R.string.weekly_avg), fontSize = 24.sp,
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                    if (weeklyAvg == null) {
                        Text(text = "0.0")
                    } else {
                        Text(
                            text = "%.1f ${stringResource(id = R.string.kilograms)}"
                                .format(weeklyAvg.inKilograms)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun InputReadingsScreenPreview() {
    val context = LocalContext.current
    val appInfo = HealthConnectAppInfo(
        packageName = "com.example.myfitnessapp",
        appLabel = "My Fitness App",
        icon = context.getDrawable(R.drawable.ic_launcher_foreground)!!
    )
    HealthConnectTheme(darkTheme = false) {
        InputReadingsScreen(
            permissions = setOf(),
            weeklyAvg = Mass.kilograms(54.5),
            permissionsGranted = true,
            readingsList = listOf(
                WeightData(
                    weight = Mass.kilograms(54.01231),
                    id = UUID.randomUUID().toString(),
                    time = ZonedDateTime.now(),
                    sourceAppInfo = appInfo
                ),
                WeightData(
                    weight = Mass.kilograms(55.578),
                    id = UUID.randomUUID().toString(),
                    time = ZonedDateTime.now().minusMinutes(5),
                    sourceAppInfo = appInfo
                )
            ),
            uiState = InputReadingsViewModel.UiState.Done
        )

    }
}


