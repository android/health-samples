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
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.ExerciseState
import androidx.health.services.client.data.LocationAvailability
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exercisesamplecompose.data.HealthServicesManager
import com.example.exercisesamplecompose.service.ActiveDurationUpdate
import com.example.exercisesamplecompose.service.ExerciseStateChange
import com.example.exercisesamplecompose.service.ForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


@HiltViewModel
class ExerciseViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val healthServicesManager: HealthServicesManager
) : ViewModel() {

    val permissions = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACTIVITY_RECOGNITION
    )

    var bound = mutableStateOf(false)
    var hasExerciseCapabilities = mutableStateOf(true)
    var isTrackingAnotherExercise = mutableStateOf(false)


    private var exerciseService: ForegroundService? = null
    var exerciseServiceState: MutableState<ServiceState> = mutableStateOf(ServiceState.Disconnected)

    private val connection = object : android.content.ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as ForegroundService.LocalBinder
            binder.getService().let {
                exerciseService = it
                exerciseServiceState.value = ServiceState.Connected(
                    exerciseMetrics = it.exerciseMetrics,
                    exerciseLaps = it.exerciseLaps,
                    exerciseDurationUpdate = it.exerciseDurationUpdate,
                    locationAvailabilityState = it.locationAvailabilityState,
                    activeDurationUpdate = it.exerciseDurationUpdate.value,
                    exerciseState = it.exerciseState,
                    exerciseStateChange = it.exerciseStateChange
                )
            }
            bound.value = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound.value = false
            exerciseService = null
            exerciseServiceState.value = ServiceState.Disconnected
        }

    }

    init {
        viewModelScope.launch {
            hasExerciseCapabilities.value = healthServicesManager.hasExerciseCapability()
            isTrackingAnotherExercise.value = healthServicesManager.isTrackingExerciseInAnotherApp()
        }
        if (!bound.value) {
            createService()
        }


    }

    private fun createService() {
        Intent(applicationContext, ForegroundService::class.java).also { intent ->
            applicationContext.startService(intent)
            applicationContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Intent(applicationContext, ForegroundService::class.java).also {
            applicationContext.unbindService(connection)
        }
    }

    fun prepareExercise() = viewModelScope.launch { exerciseService?.prepareExercise() }
    fun startExercise() = viewModelScope.launch { exerciseService?.startExercise() }
    fun pauseExercise() = viewModelScope.launch { exerciseService?.pauseExercise() }
    fun endExercise() = viewModelScope.launch { exerciseService?.endExercise() }
    fun resumeExercise() = viewModelScope.launch { exerciseService?.resumeExercise() }
}

/** Store exercise values in the service state. While the service is connected,
 * the values will persist.**/
sealed class ServiceState {
    object Disconnected : ServiceState()
    data class Connected(
        val exerciseMetrics: StateFlow<DataPointContainer?>,
        val exerciseLaps: StateFlow<Int>,
        val exerciseDurationUpdate: StateFlow<ActiveDurationUpdate?>,
        val locationAvailabilityState: StateFlow<LocationAvailability>,
        val activeDurationUpdate: ActiveDurationUpdate?,
        val exerciseState: StateFlow<ExerciseState>,
        val exerciseStateChange: StateFlow<ExerciseStateChange>
    ) : ServiceState()
}

