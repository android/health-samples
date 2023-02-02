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

import android.Manifest
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exercisesamplecompose.data.HealthServicesRepository
import com.example.exercisesamplecompose.data.ServiceState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch


@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val healthServicesRepository: HealthServicesRepository
) : ViewModel() {

    val permissions = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACTIVITY_RECOGNITION
    )

    var hasExerciseCapabilities = mutableStateOf(true)
    var isTrackingAnotherExercise = mutableStateOf(false)

    private var _exerciseServiceState: MutableState<ServiceState> =
        healthServicesRepository.exerciseServiceState
    val exerciseServiceState = _exerciseServiceState

    init {
        viewModelScope.launch {
            hasExerciseCapabilities.value = healthServicesRepository.hasExerciseCapability()
            isTrackingAnotherExercise.value =
                healthServicesRepository.isTrackingExerciseInAnotherApp()

            healthServicesRepository.createService()
        }

    }

    suspend fun isExerciseInProgress(): Boolean {
        return healthServicesRepository.isExerciseInProgress()
    }

    fun prepareExercise() = viewModelScope.launch { healthServicesRepository.prepareExercise() }
    fun startExercise() = viewModelScope.launch { healthServicesRepository.startExercise() }
    fun pauseExercise() = viewModelScope.launch { healthServicesRepository.pauseExercise() }
    fun endExercise() = viewModelScope.launch { healthServicesRepository.endExercise() }
    fun resumeExercise() = viewModelScope.launch { healthServicesRepository.resumeExercise() }
}



