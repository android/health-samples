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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.health.services.client.data.LocationAvailability
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.example.exercisesamplecompose.R
import com.example.exercisesamplecompose.data.ServiceState
import com.example.exercisesamplecompose.presentation.component.AcquiredCheck
import com.example.exercisesamplecompose.presentation.component.NotAcquired
import com.example.exercisesamplecompose.presentation.component.ProgressBar
import com.example.exercisesamplecompose.presentation.dialogs.ExerciseInProgressAlert
import com.example.exercisesamplecompose.presentation.theme.ThemePreview
import com.example.exercisesamplecompose.service.ExerciseServiceState
import com.google.android.horologist.compose.ambient.AmbientState
import com.google.android.horologist.compose.material.Button
import com.google.android.horologist.compose.material.ButtonSize

@Composable
fun PreparingExerciseRoute(
    ambientState: AmbientState,
    onStart: () -> Unit,
    onFinishActivity: () -> Unit,
    onNoExerciseCapabilities: () -> Unit,
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .ambientGray(ambientState)
    ) {
        PreparingExerciseScreen(
            ambientState = ambientState,
            onStart = {
                viewModel.startExercise()
                onStart()
            },
            uiState = uiState
        )

        if (uiState.isTrackingInAnotherApp) {
            var dismissed by remember { mutableStateOf(false) }
            ExerciseInProgressAlert(
                onNegative = onFinishActivity,
                onPositive = { dismissed = true },
                showDialog = !dismissed
            )
        }
    }
}

private val grayscale = Paint().apply {
    colorFilter = ColorFilter.colorMatrix(
        ColorMatrix().apply {
            setToSaturation(0f)
        }
    )
    isAntiAlias = false
}

internal fun Modifier.ambientGray(ambientState: AmbientState): Modifier =
    if (ambientState is AmbientState.Ambient) {
        graphicsLayer {
            scaleX = 0.9f
            scaleY = 0.9f
        }.drawWithContent {
            drawIntoCanvas {
                it.withSaveLayer(size.toRect(), grayscale) {
                    drawContent()
                }
            }
        }
    } else {
        this
    }

/**
 * Screen that appears while the device is preparing the exercise.
 */
@Composable
fun PreparingExerciseScreen(
    ambientState: AmbientState,
    onStart: () -> Unit = {},
    uiState: PreparingScreenState
) {
    val location = (uiState as? PreparingScreenState.Preparing)?.locationAvailability

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.height(25.dp)
        ) {

            Text(
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.preparing_exercise),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.height(40.dp)
        ) {
            when (location) {
                LocationAvailability.ACQUIRING, LocationAvailability.UNKNOWN -> ProgressBar(
                    ambientState,
                    Modifier.fillMaxSize()
                )

                LocationAvailability.ACQUIRED_TETHERED, LocationAvailability.ACQUIRED_UNTETHERED -> AcquiredCheck()
                else -> NotAcquired()
            }
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = updatePrepareLocationStatus(
                    locationAvailability = location ?: LocationAvailability.UNAVAILABLE
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(6.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
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

@WearPreviewDevices
@Composable
fun PreparingExerciseScreenPreview() {
    ThemePreview {
        PreparingExerciseScreen(
            ambientState = AmbientState.Interactive,
            onStart = {},
            uiState = PreparingScreenState.Preparing(
                serviceState = ServiceState.Connected(
                    ExerciseServiceState()
                ),
                isTrackingInAnotherApp = false,
                requiredPermissions = PreparingViewModel.permissions,
                hasExerciseCapabilities = true
            )
        )
    }
}

@WearPreviewDevices
@Composable
fun PreparingExerciseScreenPreviewAmbient() {
    ThemePreview {
        PreparingExerciseScreen(
            ambientState = AmbientState.Ambient(),
            onStart = {},
            uiState = PreparingScreenState.Preparing(
                serviceState = ServiceState.Connected(
                    ExerciseServiceState()
                ),
                isTrackingInAnotherApp = false,
                requiredPermissions = PreparingViewModel.permissions,
                hasExerciseCapabilities = true
            )
        )
    }
}
