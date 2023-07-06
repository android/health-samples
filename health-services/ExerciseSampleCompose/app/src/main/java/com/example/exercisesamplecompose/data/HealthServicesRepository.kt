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
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.exercisesamplecompose.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.health.services.client.data.LocationAvailability
import com.example.exercisesamplecompose.service.ExerciseService
import com.example.exercisesamplecompose.service.ExerciseServiceState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


class HealthServicesRepository @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    val exerciseClientManager: ExerciseClientManager,
    val coroutineScope: CoroutineScope
) {
    private val exerciseService: MutableStateFlow<ExerciseService?> = MutableStateFlow(null)

    @Suppress("IfThenToElvis")
    val serviceState: StateFlow<ServiceState> = exerciseService.flatMapLatest { exerciseService ->
        if (exerciseService == null) {
            flowOf(ServiceState.Disconnected)
        } else {
            exerciseService.exerciseServiceMonitor.exerciseServiceState.map {
                ServiceState.Connected(it)
            }
        }
    }.stateIn(
        coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = ServiceState.Disconnected
    )

    suspend fun hasExerciseCapability(): Boolean = getExerciseCapabilities() != null

    private suspend fun getExerciseCapabilities() = exerciseClientManager.getExerciseCapabilities()

    suspend fun isExerciseInProgress(): Boolean =
        exerciseClientManager.exerciseClient.isExerciseInProgress()

    suspend fun isTrackingExerciseInAnotherApp(): Boolean =
        exerciseClientManager.exerciseClient.isTrackingExerciseInAnotherApp()

    fun prepareExercise() = coroutineScope.launch { exerciseService.value!!.prepareExercise() }
    fun startExercise() = coroutineScope.launch { exerciseService.value!!.startExercise() }
    fun pauseExercise() = coroutineScope.launch { exerciseService.value!!.pauseExercise() }
    fun endExercise() = coroutineScope.launch { exerciseService.value!!.endExercise() }
    fun resumeExercise() = coroutineScope.launch { exerciseService.value!!.resumeExercise() }

    private val connection = object : android.content.ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as ExerciseService.LocalBinder
            binder.getService().let {
                exerciseService.value = it
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            exerciseService.value = null
        }

    }

    fun createService() {
        Intent(applicationContext, ExerciseService::class.java).also { intent ->
            applicationContext.startService(intent)
            applicationContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

}

/** Store exercise values in the service state. While the service is connected,
 * the values will persist.**/
sealed class ServiceState {
    object Disconnected : ServiceState()
    data class Connected(
        val exerciseServiceState: ExerciseServiceState,
    ) : ServiceState() {
        val locationAvailabilityState: LocationAvailability =
            exerciseServiceState.locationAvailability
    }
}






