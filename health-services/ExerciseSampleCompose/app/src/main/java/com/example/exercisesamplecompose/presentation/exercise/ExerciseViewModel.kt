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
package com.example.exercisesamplecompose.presentation.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exercisesamplecompose.data.HealthServicesRepository
import com.example.exercisesamplecompose.data.ServiceState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ExerciseViewModel
@Inject
constructor(
    private val healthServicesRepository: HealthServicesRepository
) : ViewModel() {
    val uiState: StateFlow<ExerciseScreenState> =
        healthServicesRepository.serviceState
            .map {
                ExerciseScreenState(
                    hasExerciseCapabilities = healthServicesRepository.hasExerciseCapability(),
                    isTrackingAnotherExercise =
                    healthServicesRepository
                        .isTrackingExerciseInAnotherApp(),
                    serviceState = it,
                    exerciseState = (it as? ServiceState.Connected)?.exerciseServiceState
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(3_000),
                healthServicesRepository.serviceState.value.let {
                    ExerciseScreenState(
                        true,
                        false,
                        it,
                        (it as? ServiceState.Connected)?.exerciseServiceState
                    )
                }
            )

    suspend fun isExerciseInProgress(): Boolean =
        healthServicesRepository.isExerciseInProgress()

    fun startExercise() {
        healthServicesRepository.startExercise()
    }

    fun pauseExercise() {
        healthServicesRepository.pauseExercise()
    }

    fun endExercise() {
        healthServicesRepository.endExercise()
    }

    fun resumeExercise() {
        healthServicesRepository.resumeExercise()
    }
}
