/*
 * Copyright 2021 The Android Open Source Project
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
package com.example.healthplatformsample.presentation.ui.SessionDetailScreen

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthplatformsample.R
import com.example.healthplatformsample.data.HealthPlatformManager
import com.example.healthplatformsample.data.formatValue
import com.example.healthplatformsample.presentation.components.SessionDetailsMinMaxAvg
import com.example.healthplatformsample.presentation.components.sessionDetailsItem
import com.example.healthplatformsample.presentation.components.sessionDetailsSamples
import com.example.healthplatformsample.presentation.components.sessionDetailsSeries
import java.util.UUID

@Composable
fun SessionDetailScreen(
    healthPlatformManager: HealthPlatformManager,
    uid: String,
    onError: (Context, Throwable?) -> Unit,
    viewModel: SessionDetailViewModel = viewModel(
        factory = SessionDetailViewModelFactory(
            healthPlatformManager = healthPlatformManager
        )
    )
) {
    val details by viewModel.sessionDetails
    val state = viewModel.uiState
    val context = LocalContext.current
    // Remember the last error ID, such that it is possible to avoid re-launching the error
    // notification for the same error when the screen is recomposed, or configuration changes etc.
    val errorId = rememberSaveable { mutableStateOf(UUID.randomUUID()) }

    // The [MainModel.UiState] provides details of whether the last action was a success or resulted
    // in an error. Where an error occurred, for example in reading and writing to Health Platform,
    // the user is notified, and where the error is one that can be recovered from, an attempt to
    // do so is made.
    LaunchedEffect(state) {
        if (state is SessionDetailViewModel.UiState.Error && errorId.value != state.uuid) {
            onError(context, state.exception)
            errorId.value = state.uuid
        }
    }

    LaunchedEffect(uid) {
        viewModel.readSessionData(uid)
    }

    val modifier = Modifier.padding(4.dp)
    details?.let {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            sessionDetailsItem(labelId = R.string.total_steps) {
                Text(it.totalSteps.total?.longValue.toString())
            }
            sessionDetailsItem(labelId = R.string.total_distance) {
                Text(it.totalDistance.total?.doubleValue.toString())
            }
            sessionDetailsItem(labelId = R.string.total_energy) {
                Text(it.totalEnergy.total?.doubleValue.toString())
            }
            sessionDetailsItem(labelId = R.string.speed_stats) {
                with(it.speedStats) {
                    SessionDetailsMinMaxAvg(
                        min?.formatValue(),
                        max?.formatValue(),
                        avg?.formatValue()
                    )
                }
            }
            sessionDetailsSamples(labelId = R.string.speed_samples, samples = it.speedSamples.data)
            sessionDetailsItem(labelId = R.string.hr_stats) {
                with(it.hrStats) {
                    SessionDetailsMinMaxAvg(
                        min?.formatValue(),
                        max?.formatValue(),
                        avg?.formatValue()
                    )
                }
            }
            sessionDetailsSeries(labelId = R.string.hr_series, series = it.hrSeries.data[0].values)
        }
    }
}
