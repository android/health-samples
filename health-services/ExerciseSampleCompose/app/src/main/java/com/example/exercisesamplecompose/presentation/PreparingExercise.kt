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
package com.example.exercisesamplecompose.presentation

import android.content.ContentValues.TAG
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.health.services.client.data.LocationAvailability
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import com.example.exercisesamplecompose.R
import com.example.exercisesamplecompose.data.ServiceState
import com.example.exercisesamplecompose.presentation.component.AcquiredCheck
import com.example.exercisesamplecompose.presentation.component.ExerciseInProgressAlert
import com.example.exercisesamplecompose.presentation.component.NotAcquired
import com.example.exercisesamplecompose.presentation.component.ProgressBar
import com.example.exercisesamplecompose.theme.ExerciseSampleTheme
import kotlinx.coroutines.launch

/**
 * Screen that appears while the device is preparing the exercise.
 */
@Composable
fun PreparingExercise(
    onStartClick: () -> Unit = {},
    prepareExercise: () -> Unit,
    onStart: () -> Unit = {},
    serviceState: ServiceState,
    permissions: Array<String>,
    isTrackingAnotherExercise: Boolean,
) {
    if (isTrackingAnotherExercise) {
        ExerciseInProgressAlert(true)
    }
    /** Request permissions prior to launching exercise.**/
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.all { it.value }) {
            Log.d(TAG, "All required permissions granted")
        }
    }

    when (serviceState) {
        is ServiceState.Connected -> {
            LaunchedEffect(Unit) {
                launch {
                    permissionLauncher.launch(permissions)
                    prepareExercise()
                }
            }

            val location by serviceState.locationAvailabilityState.collectAsStateWithLifecycle()


            ExerciseSampleTheme {
                Scaffold(timeText =
                { TimeText(timeSource = TimeTextDefaults.timeSource(TimeTextDefaults.timeFormat())) }) {
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
                                LocationAvailability.ACQUIRING, LocationAvailability.UNKNOWN -> ProgressBar()
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
                                text = updatePrepareLocationStatus(locationAvailability = location),
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
                                    onClick = { onStartClick(); onStart() },
                                    modifier = Modifier.size(ButtonDefaults.SmallButtonSize)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = stringResource(id = R.string.start)
                                    )
                                }
                            }

                        }
                    }
                }
            }
        }
        else -> {}
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
