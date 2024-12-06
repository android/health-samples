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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.expandableItems
import androidx.wear.compose.foundation.rememberExpandableState
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.InlineSlider
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.example.exercisesamplecompose.R
import com.example.exercisesamplecompose.data.Thresholds
import com.example.exercisesamplecompose.presentation.goals.ExerciseGoalsViewModel
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.ItemType
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.ToggleChip
import com.google.android.horologist.compose.material.ToggleChipToggleControl
import com.google.android.horologist.images.base.paintable.ImageVectorPaintable.Companion.asPaintable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit


@Composable
fun ExerciseGoalsRoute(
    onSet: () -> Unit,
) {
    val viewModel: ExerciseGoalsViewModel = hiltViewModel()

    ExerciseGoalsScreen(onSet = onSet, setGoals = { thresholds: Thresholds ->
        viewModel.setGoals(thresholds)
    })
}

@Composable
fun ExerciseGoalsScreen(
    onSet: () -> Unit = {}, setGoals: (Thresholds) -> Unit
) {
    // Clear up screen real-estate while toggling goal values
    val showDistanceRow = rememberExpandableState(initiallyExpanded = false)
    val showDurationRow = rememberExpandableState(initiallyExpanded = false)

    var thresholds by remember { mutableStateOf(Thresholds(0.0, Duration.ZERO)) }

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Chip,
            last = ItemType.Unspecified,
        )
    )
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            columnState = columnState,
        ) {
            item {
                ToggleChip(
                    checked = showDistanceRow.expanded,
                    onCheckedChanged = { showDistanceRow.expanded = it },
                    label = stringResource(R.string.distance),
                    icon = Icons.Default.LocationOn.asPaintable(),
                    toggleControl = ToggleChipToggleControl.Switch,
                    secondaryLabel = if (showDistanceRow.expanded) "${thresholds.distance}" + stringResource(
                        R.string.km
                    ) else null
                )
            }
            expandableItems(showDistanceRow, 1) {
                DistanceGoalSlider(thresholds.distance) { newValue ->
                    thresholds =
                        thresholds.copy(distance = newValue.toDouble(), distanceIsSet = true)
                }
            }
            item {
                ToggleChip(
                    checked = showDurationRow.expanded,
                    onCheckedChanged = { showDurationRow.expanded = it },
                    label = stringResource(R.string.duration),
                    icon = Icons.Default.Timer.asPaintable(),
                    toggleControl = ToggleChipToggleControl.Switch,
                    secondaryLabel = if (showDurationRow.expanded) "${
                        thresholds.duration.inWholeMinutes
                    }" + stringResource(R.string.minutes) else null
                )
            }
            expandableItems(showDurationRow, 1) {
                DurationGoalSlider(thresholds.duration) { newValue ->
                    thresholds = thresholds.copy(
                        duration = newValue, durationIsSet = true
                    )
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CompactChip(
                        label = { Text(stringResource(R.string.set_goal)) },
                        // Set the goal and pass the values to the view model
                        onClick = {
                            setGoals(thresholds)
                            onSet() // Close the screen
                        })
                }
            }
        }
    }
}

@Composable
fun DistanceGoalSlider(distanceValue: Double, onDistanceValueChange: (Float) -> Unit) {
    InlineSlider(
        distanceValue.toFloat(),
        onValueChange = { val newValue = it; onDistanceValueChange(newValue) },
        //Range from 0 to 10 kilometers; In a production app, you might allow the user to input
        //custom values or adjust the units.
        steps = 9,
        valueRange = 0.0f..10.0f,
        increaseIcon = {
            IncreaseIcon()
        },
        decreaseIcon = {
            DecreaseIcon()
        },
    )
}

@Composable
fun DurationGoalSlider(durationValue: Duration, onDurationValueChange: (Duration) -> Unit) {
    InlineSlider(
        durationValue.toDouble(DurationUnit.MINUTES).toFloat(),
        onValueChange = { newValue ->
            onDurationValueChange(newValue.toLong().minutes)
        },
        //range from 0 to 60 minutes; In a production app, you might allow the user to input
        //custom times
        steps = 11,
        valueRange = 0.0f..60.0f,
        increaseIcon = {
            IncreaseIcon()
        },
        decreaseIcon = {
            DecreaseIcon()
        },
    )
}

@Composable
fun IncreaseIcon() {
    com.google.android.horologist.compose.material.Icon(
        modifier = Modifier.size(26.dp),
        paintable = Icons.Default.Add.asPaintable(),
        contentDescription = stringResource(R.string.increase),
    )
}

@Composable
fun DecreaseIcon() {
    com.google.android.horologist.compose.material.Icon(
        modifier = Modifier.size(26.dp),
        paintable = Icons.Default.Remove.asPaintable(),
        contentDescription = stringResource(R.string.decrease),
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
