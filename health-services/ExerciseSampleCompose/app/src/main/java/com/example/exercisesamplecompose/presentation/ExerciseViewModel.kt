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
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.ExerciseState
import androidx.health.services.client.data.LocationAvailability
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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


    private var exerciseService: ForegroundService? = null
    val exerciseServiceState: MutableState<ServiceState> = mutableStateOf(ServiceState.Disconnected)

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
            Log.i(TAG, "onServiceConnected")
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
        if (bound.value) {
            applicationContext.unbindService(connection)
        }
    }

    fun connectToService() = ForegroundService.bindService(context = applicationContext, connection)

    fun prepareExercise() = exerciseService?.prepareExercise()
    fun startExercise() {
        exerciseService?.startExercise()
    }

    fun pauseExercise() = exerciseService?.pauseExercise()
    fun endExercise() = exerciseService?.endExercise()
    fun resumeExercise() = exerciseService?.resumeExercise()


    suspend fun markLap() {
        exerciseService?.markLap()
    }

    fun disconnectFromService() =
        ForegroundService.unbindService(context = applicationContext, connection)

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

class ExerciseViewModelFactory(
    private val healthServicesManager: HealthServicesManager,
    @ApplicationContext private val applicationContext: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return ExerciseViewModel(
                healthServicesManager = healthServicesManager,
                applicationContext = applicationContext
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
