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
package com.example.healthconnectsample.presentation.screen.inputreadings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.permission.Permission
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.units.Mass
import com.example.healthconnectsample.R
import com.example.healthconnectsample.data.dateTimeWithOffsetOrDefault
import com.example.healthconnectsample.presentation.theme.HealthConnectTheme
import me.bytebeats.views.charts.line.LineChart
import me.bytebeats.views.charts.line.LineChartData
import me.bytebeats.views.charts.line.render.point.FilledCircularPointDrawer
import me.bytebeats.views.charts.line.render.xaxis.SimpleXAxisDrawer
import me.bytebeats.views.charts.line.render.yaxis.SimpleYAxisDrawer
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

@Composable
fun InputReadingsScreen(
    permissions: Set<Permission>,
    permissionsGranted: Boolean,
    readingsList: List<WeightRecord>,
    uiState: InputReadingsViewModel.UiState,
    onInsertClick: (Double) -> Unit = {},
    onDeleteClick: (String) -> Unit = {},
    onError: (Throwable?) -> Unit = {},
    onPermissionsResult: () -> Unit = {},
    weeklyAvg: Mass?,
    onPermissionsLaunch: (Set<Permission>) -> Unit = {}
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
                items(readingsList) { reading ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // show local date and time
                        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        val zonedDateTime =
                            dateTimeWithOffsetOrDefault(reading.time, reading.zoneOffset)
                        val uid = reading.metadata.uid
                        Text(
                            text = "${reading.weight} ${formatter.format(zonedDateTime)}",
                        )
                        IconButton(
                            onClick = {
                                if (uid != null) {
                                    onDeleteClick(uid)
                                }
                            },
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
                        Text(text = "0.0" + stringResource(id = R.string.kilograms))
                    } else {
                        Text(text = "$weeklyAvg".take(5) + stringResource(id = R.string.kilograms))
                    }
                }
                if (readingsList.size >= 3) {
                    var index = -1
                    val points = MutableList(readingsList.size) {
                        ++index
                        val reading = readingsList[index]

                        val zonedDateTime =
                            dateTimeWithOffsetOrDefault(reading.time, reading.zoneOffset)

                        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)

                        LineChartData.Point(
                            reading.weight.inKilograms.toFloat(),
                            formatter.format(zonedDateTime)
                        )
                    }
                    item {
                        Row(Modifier.defaultMinSize(minHeight = 160.dp)) {

                            val chartTextColor = if (isSystemInDarkTheme()) {
                                Color.White
                            } else {
                                Color.Black
                            }

                            val pointColor = if (isSystemInDarkTheme()) {
                                Color.LightGray
                            } else {
                                Color.Blue
                            }

                            LineChart(
                                lineChartData = LineChartData(
                                    points = points
                                ),
                                modifier = Modifier.height(160.dp),
                                pointDrawer = FilledCircularPointDrawer(color = pointColor),
                                xAxisDrawer = SimpleXAxisDrawer(
                                    labelTextSize = 10.sp,
                                    labelTextColor = chartTextColor,
                                    axisLineColor = chartTextColor
                                ),
                                yAxisDrawer = SimpleYAxisDrawer(
                                    labelTextSize = 10.sp,
                                    labelTextColor = chartTextColor,
                                    axisLineColor = chartTextColor
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun InputReadingsScreenPreview() {
    val inputTime = Instant.now()
    HealthConnectTheme(darkTheme = false) {
        InputReadingsScreen(
            permissions = setOf(),
            weeklyAvg = Mass.kilograms(54.5),
            permissionsGranted = true,
            readingsList = listOf(
                WeightRecord(
                    Mass.kilograms(54.0),
                    time = inputTime,
                    zoneOffset = null
                ),
                WeightRecord(
                    Mass.kilograms(55.0),
                    time = inputTime,
                    zoneOffset = null
                )
            ),
            uiState = InputReadingsViewModel.UiState.Done
        )

    }
}
