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
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.exercisesamplecompose.data

import android.content.Context
import androidx.health.services.client.data.LocationAvailability
import com.example.exercisesamplecompose.di.bindService
import com.example.exercisesamplecompose.service.ExerciseLogger
import com.example.exercisesamplecompose.service.ExerciseService
import com.example.exercisesamplecompose.service.ExerciseServiceState
import dagger.hilt.android.ActivityRetainedLifecycle
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@ActivityRetainedScoped
class HealthServicesRepository
@Inject
constructor(
    @ApplicationContext private val applicationContext: Context,
    val exerciseClientManager: ExerciseClientManager,
    val logger: ExerciseLogger,
    val coroutineScope: CoroutineScope,
    val lifecycle: ActivityRetainedLifecycle
) {
    private val binderConnection =
        lifecycle.bindService<ExerciseService.LocalBinder, ExerciseService>(applicationContext)

    private val exerciseServiceStateUpdates: Flow<ExerciseServiceState> =
        binderConnection.flowWhenConnected(
            ExerciseService.LocalBinder::exerciseServiceState
        )

    private var errorState: MutableStateFlow<String?> = MutableStateFlow(null)

    val serviceState: StateFlow<ServiceState> =
        exerciseServiceStateUpdates
            .combine(errorState) { exerciseServiceState, errorString ->
                ServiceState.Connected(exerciseServiceState.copy(error = errorString))
            }.stateIn(
                coroutineScope,
                started = SharingStarted.Eagerly,
                initialValue = ServiceState.Disconnected
            )

    suspend fun hasExerciseCapability(): Boolean = getExerciseCapabilities() != null

    private suspend fun getExerciseCapabilities() =
        exerciseClientManager.getExerciseCapabilities()

    suspend fun isExerciseInProgress(): Boolean =
        exerciseClientManager.exerciseClient.isExerciseInProgress()

    suspend fun isTrackingExerciseInAnotherApp(): Boolean =
        exerciseClientManager.exerciseClient.isTrackingExerciseInAnotherApp()

    fun prepareExercise() = serviceCall { prepareExercise() }

    private fun serviceCall(function: suspend ExerciseService.() -> Unit) =
        coroutineScope.launch {
            binderConnection.runWhenConnected {
                function(it.getService())
            }
        }

    fun startExercise() =
        serviceCall {
            try {
                errorState.value = null
                startExercise()
            } catch (e: Exception) {
                errorState.value = e.message
                logger.error("Error starting exercise", e.fillInStackTrace())
            }
        }

    fun pauseExercise() = serviceCall { pauseExercise() }

    fun endExercise() = serviceCall { endExercise() }

    fun resumeExercise() = serviceCall { resumeExercise() }
}

/** Store exercise values in the service state. While the service is connected,
 * the values will persist.**/
sealed class ServiceState {
    data object Disconnected : ServiceState()

    data class Connected(
        val exerciseServiceState: ExerciseServiceState
    ) : ServiceState() {
        val locationAvailabilityState: LocationAvailability =
            exerciseServiceState.locationAvailability
    }
}
