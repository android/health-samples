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
package com.example.exercisesamplecompose.presentation.preparing

import android.content.ContentValues.TAG
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.health.services.client.data.LocationAvailability
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.CurvedLayout
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.curvedText
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.example.exercisesamplecompose.R
import com.google.android.horologist.compose.ambient.AmbientAware
import com.google.android.horologist.compose.ambient.AmbientState
import com.example.exercisesamplecompose.data.ServiceState
import com.example.exercisesamplecompose.presentation.ambient.ambientGray
import com.example.exercisesamplecompose.presentation.dialogs.ExerciseInProgressAlert
import com.example.exercisesamplecompose.presentation.theme.ThemePreview
import com.example.exercisesamplecompose.service.ExerciseServiceState
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.ItemType
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Button
import com.google.android.horologist.compose.material.ButtonSize
import com.google.android.horologist.compose.material.CompactChip

@Composable
fun PreparingExerciseRoute(
    onStart: () -> Unit,
    onFinishActivity: () -> Unit,
    onNoExerciseCapabilities: () -> Unit,
    onGoals: () -> Unit
) {
    val viewModel = hiltViewModel<PreparingViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    /** Request permissions prior to launching exercise.**/
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.all { it.value }) {
            Log.d(TAG, "All required permissions granted")
        }
    }

    SideEffect {
        val preparingState = uiState
        if (preparingState is PreparingScreenState.Preparing && !preparingState.hasExerciseCapabilities) {
            onNoExerciseCapabilities()
        }
    }

    if (uiState.serviceState is ServiceState.Connected) {
        val requiredPermissions = uiState.requiredPermissions
        LaunchedEffect(requiredPermissions) {
            permissionLauncher.launch(requiredPermissions.toTypedArray())
        }
    }

    AmbientAware { ambientState ->
        PreparingExerciseScreen(
            onStart = {
                viewModel.startExercise()
                onStart()
            },
            uiState = uiState,
            onGoals = { onGoals() },
            ambientState = ambientState,
        )
    }

    if (uiState.isTrackingInAnotherApp) {
        var dismissed by remember { mutableStateOf(false) }
        ExerciseInProgressAlert(
            onNegative = onFinishActivity,
            onPositive = { dismissed = true },
            showDialog = !dismissed
        )
    }
}

/**
 * Screen that appears while the device is preparing the exercise.
 */
@Composable
fun PreparingExerciseScreen(
    uiState: PreparingScreenState,
    ambientState: AmbientState,
    onStart: () -> Unit = {},
    onGoals: () -> Unit = {}
) {
    val location = (uiState as? PreparingScreenState.Preparing)?.locationAvailability

    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Unspecified, last = ItemType.Unspecified
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .ambientGray(ambientState)
    ) {
        ScreenScaffold(
            scrollState = if (ambientState.isInteractive) columnState else null,
            timeText = {}) {
            LocationStatusText(
                updatePrepareLocationStatus(
                    locationAvailability = location ?: LocationAvailability.UNAVAILABLE
                )
            )
            ScalingLazyColumn(
                columnState = columnState
            ) {
                item {
                    Text(
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        text = stringResource(id = R.string.preparing_exercise),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.15f * LocalConfiguration.current.screenWidthDp.dp)
                    )
                }
                item {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Button(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = stringResource(id = R.string.start),
                            onClick = onStart,
                            buttonSize = ButtonSize.Small,
                            enabled = uiState is PreparingScreenState.Preparing
                        )

                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CompactChip(
                            label = stringResource(id = R.string.goal),
                            onClick = onGoals,
                        )
                    }
                }
            }
        }
    }
}

/**Return [LocationAvailability] value code as a string**/

@Composable
private fun updatePrepareLocationStatus(locationAvailability: LocationAvailability): String {
    val gpsText = when (locationAvailability) {
        LocationAvailability.ACQUIRED_TETHERED, LocationAvailability.ACQUIRED_UNTETHERED -> R.string.GPS_acquired
        LocationAvailability.NO_GNSS -> R.string.GPS_disabled // TODO Consider redirecting user to change device settings in this case
        LocationAvailability.ACQUIRING -> R.string.GPS_acquiring
        LocationAvailability.UNKNOWN -> R.string.GPS_initializing
        else -> R.string.GPS_unavailable
    }

    return stringResource(id = gpsText)
}

@Composable
private fun LocationStatusText(status: String) {
    CurvedLayout {
        curvedText(text = status, fontSize = 12.sp)
    }
}

@WearPreviewDevices
@Composable
fun PreparingExerciseScreenPreview() {
    ThemePreview {
        PreparingExerciseScreen(
            onStart = {},
            uiState = PreparingScreenState.Preparing(
                serviceState = ServiceState.Connected(
                    ExerciseServiceState()
                ),
                isTrackingInAnotherApp = false,
                requiredPermissions = PreparingViewModel.permissions,
                hasExerciseCapabilities = true
            ),
            onGoals = {},
            ambientState = AmbientState.Interactive
        )
    }
}

@WearPreviewDevices
@Composable
fun PreparingExerciseScreenPreviewAmbient() {
    ThemePreview {
        PreparingExerciseScreen(
            onStart = {},
            uiState = PreparingScreenState.Preparing(
                serviceState = ServiceState.Connected(
                    ExerciseServiceState()
                ),
                isTrackingInAnotherApp = false,
                requiredPermissions = PreparingViewModel.permissions,
                hasExerciseCapabilities = true
            ),
            onGoals = {},
            ambientState = AmbientState.Ambient()
        )
    }
}
