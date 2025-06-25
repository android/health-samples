/*
 * Copyright 2025 The Android Open Source Project
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
package com.example.exercisesamplecompose.presentation.preparing

import android.Manifest
import android.health.connect.HealthPermissions
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exercisesamplecompose.data.HealthServicesRepository
import com.example.exercisesamplecompose.data.ServiceState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PreparingViewModel
@Inject
constructor(
    private val healthServicesRepository: HealthServicesRepository
) : ViewModel() {
    init {
        viewModelScope.launch {
            healthServicesRepository.prepareExercise()
        }
    }

    fun startExercise() {
        healthServicesRepository.startExercise()
    }

    val uiState: StateFlow<PreparingScreenState> =
        healthServicesRepository.serviceState
            .map {
                val isTrackingInAnotherApp =
                    healthServicesRepository
                        .isTrackingExerciseInAnotherApp()
                val hasExerciseCapabilities = healthServicesRepository.hasExerciseCapability()
                toUiState(
                    serviceState = it,
                    isTrackingInAnotherApp = isTrackingInAnotherApp,
                    hasExerciseCapabilities = hasExerciseCapabilities
                )
            }.stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(5.seconds),
                initialValue = toUiState(healthServicesRepository.serviceState.value)
            )

    private fun toUiState(
        serviceState: ServiceState,
        isTrackingInAnotherApp: Boolean = false,
        hasExerciseCapabilities: Boolean = true
    ): PreparingScreenState =
        if (serviceState is ServiceState.Disconnected) {
            PreparingScreenState.Disconnected(serviceState, isTrackingInAnotherApp, permissions)
        } else {
            PreparingScreenState.Preparing(
                serviceState = serviceState as ServiceState.Connected,
                isTrackingInAnotherApp = isTrackingInAnotherApp,
                requiredPermissions = permissions,
                hasExerciseCapabilities = hasExerciseCapabilities
            )
        }

    companion object {
        val permissions = buildList {
            add(Manifest.permission.BODY_SENSORS)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACTIVITY_RECOGNITION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                add(Manifest.permission.POST_NOTIFICATIONS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA)
                add(HealthPermissions.READ_HEART_RATE)
        }
    }
}
