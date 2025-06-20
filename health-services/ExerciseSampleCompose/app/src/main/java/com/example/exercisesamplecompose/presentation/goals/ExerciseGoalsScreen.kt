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
@file:OptIn(ExperimentalHorologistApi::class)

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rememberExpandableState
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Slider
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.example.exercisesamplecompose.R
import com.example.exercisesamplecompose.data.Thresholds
import com.example.exercisesamplecompose.presentation.goals.ExerciseGoalsViewModel
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ColumnItemType
import com.google.android.horologist.compose.layout.rememberResponsiveColumnPadding
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

@Composable
fun ExerciseGoalsRoute(
    onSet: () -> Unit
) {
    val viewModel: ExerciseGoalsViewModel = hiltViewModel()

    ExerciseGoalsScreen(onSet = onSet, setGoals = { thresholds: Thresholds ->
        viewModel.setGoals(thresholds)
    })
}

@Composable
fun ExerciseGoalsScreen(
    onSet: () -> Unit = {},
    setGoals: (Thresholds) -> Unit
) {
    // Clear up screen real-estate while toggling goal values
    val showDistanceRow = rememberExpandableState(initiallyExpanded = false)
    val showDurationRow = rememberExpandableState(initiallyExpanded = false)

    var thresholds by remember { mutableStateOf(Thresholds(0.0, Duration.ZERO)) }

    val columnState: TransformingLazyColumnState = rememberTransformingLazyColumnState()

    val contentPadding = rememberResponsiveColumnPadding(
        first = ColumnItemType.Button,
        last = ColumnItemType.Button
    )
    ScreenScaffold(scrollState = columnState, contentPadding = contentPadding) { contentPadding ->
        TransformingLazyColumn(
            contentPadding = contentPadding,
            state = columnState
        ) {
            item {
                SwitchButton(
                    checked = showDistanceRow.expanded,
                    onCheckedChange = { showDistanceRow.expanded = it },
                    label = { Text(stringResource(R.string.distance)) },
                    icon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = stringResource(R.string.distance)
                        )
                    },
                    secondaryLabel = {
                        if (showDistanceRow.expanded) {
                            Text(
                                "${
                                    thresholds.distance
                                }" + stringResource(R.string.km)
                            )
                        } else {
                            null
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                DistanceGoalSlider(thresholds.distance) { newValue ->
                    thresholds =
                        thresholds.copy(distance = newValue.toDouble(), distanceIsSet = true)
                }
            }
            item {
                SwitchButton(
                    checked = showDurationRow.expanded,
                    onCheckedChange = { showDurationRow.expanded = it },
                    label = { Text(stringResource(R.string.duration)) },
                    icon = { Icon(Icons.Default.Timer, contentDescription = "Timer") },
                    //  toggleControl = ToggleChipToggleControl.Switch,
                    secondaryLabel = {
                        if (showDurationRow.expanded) {
                            Text(
                                "${
                                    thresholds.duration.inWholeMinutes
                                }" + stringResource(R.string.minutes)
                            )
                        } else {
                            null
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                DurationGoalSlider(thresholds.duration) { newValue ->
                    thresholds = thresholds.copy(
                        duration = newValue, durationIsSet = true
                    )
                }
            }
            item {
                CompactButton(
                    label = { Text(stringResource(R.string.set_goal)) },
                    // Set the goal and pass the values to the view model
                    onClick = {
                        setGoals(thresholds)
                        onSet() // Close the screen
                    }
                )
            }
        }
    }
}

@Composable
fun DistanceGoalSlider(distanceValue: Double, onDistanceValueChange: (Float) -> Unit) {
    Slider(
        distanceValue.toFloat(),
        onValueChange = {
            val newValue = it
            onDistanceValueChange(newValue)
        },
        // Range from 0 to 10 kilometers; In a production app, you might allow the user to input
        // custom values or adjust the units.
        steps = 9,
        valueRange = 0.0f..10.0f,
        increaseIcon = {
            IncreaseIcon()
        },
        decreaseIcon = {
            DecreaseIcon()
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun DurationGoalSlider(durationValue: Duration, onDurationValueChange: (Duration) -> Unit) {
    Slider(
        durationValue.toDouble(DurationUnit.MINUTES).toFloat(),
        onValueChange = { newValue ->
            onDurationValueChange(newValue.toLong().minutes)
        },
        // range from 0 to 60 minutes; In a production app, you might allow the user to input
        // custom times
        steps = 11,
        valueRange = 0.0f..60.0f,
        increaseIcon = {
            IncreaseIcon()
        },
        decreaseIcon = {
            DecreaseIcon()
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun IncreaseIcon() {
    Icon(
        modifier = Modifier.size(26.dp),
        imageVector = Icons.Default.Add,
        contentDescription = stringResource(R.string.increase)
    )
}

@Composable
fun DecreaseIcon() {
    Icon(
        modifier = Modifier.size(26.dp),
        imageVector = Icons.Default.Remove,
        contentDescription = stringResource(R.string.decrease)
    )
}

@WearPreviewDevices
@Composable
fun ExerciseGoalsScreenPreview() {
    ExerciseGoalsScreen(setGoals = { _ -> run {} })
}

@Preview
@Composable
fun DistanceGoalRowPreview() {
    DistanceGoalSlider(10.0) { }
}

@Preview
@Composable
fun DurationGoalRowPreview() {
    DurationGoalSlider(10.minutes) { }
}
