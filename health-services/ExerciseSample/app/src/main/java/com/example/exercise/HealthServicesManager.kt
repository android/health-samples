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

package com.example.exercise

import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * Entry point for [HealthServicesClient] APIs, wrapping them in coroutine-friendly APIs.
 */
class HealthServicesManager @Inject constructor(
    healthServicesClient: HealthServicesClient,
    coroutineScope: CoroutineScope
) {
    private val exerciseClient = healthServicesClient.exerciseClient

    private var exerciseCapabilities: ExerciseTypeCapabilities? = null
    private var capabilitiesLoaded = false

    suspend fun getExerciseCapabilities(): ExerciseTypeCapabilities? {
        if (!capabilitiesLoaded) {
            val capabilities = exerciseClient.getCapabilitiesAsync().await()
            if (ExerciseType.RUNNING in capabilities.supportedExerciseTypes) {
                exerciseCapabilities =
                    capabilities.getExerciseTypeCapabilities(ExerciseType.RUNNING)
            }
            capabilitiesLoaded = true
        }
        return exerciseCapabilities
    }

    suspend fun hasExerciseCapability(): Boolean {
        return getExerciseCapabilities() != null
    }

    suspend fun isExerciseInProgress(): Boolean {
        val exerciseInfo = exerciseClient.getCurrentExerciseInfoAsync().await()
        return exerciseInfo.exerciseTrackedStatus == ExerciseTrackedStatus.OWNED_EXERCISE_IN_PROGRESS
    }

    suspend fun isTrackingExerciseInAnotherApp(): Boolean {
        val exerciseInfo = exerciseClient.getCurrentExerciseInfoAsync().await()
        return exerciseInfo.exerciseTrackedStatus == ExerciseTrackedStatus.OTHER_APP_IN_PROGRESS
    }

    /***
     * Note: don't call this method from outside of foreground service (ie. [ExerciseService])
     * when acquiring calories or distance.
     */
    suspend fun startExercise() {
        Log.d(TAG, "Starting exercise")
        // Types for which we want to receive metrics. Only ask for ones that are supported.
        val capabilities = getExerciseCapabilities() ?: return
        val dataTypes = setOf(
            DataType.HEART_RATE_BPM,
            DataType.CALORIES_TOTAL,
            DataType.DISTANCE
        ).intersect(capabilities.supportedDataTypes)

        val exerciseGoals = mutableListOf<ExerciseGoal<Double>>()
        if (supportsCalorieGoal(capabilities)) {
            // Create a one-time goal.
            exerciseGoals.add(
                ExerciseGoal.createOneTimeGoal(
                    DataTypeCondition(
                        dataType = DataType.CALORIES_TOTAL,
                        threshold = CALORIES_THRESHOLD,
                        comparisonType = ComparisonType.GREATER_THAN_OR_EQUAL
                    )
                )
            )
        }

        if (supportsDistanceMilestone(capabilities)) {
            // Create a milestone goal. To make a milestone for every kilometer, set the initial
            // threshold to 1km and the period to 1km.
            exerciseGoals.add(
                ExerciseGoal.createMilestone(
                    condition = DataTypeCondition(
                        dataType = DataType.DISTANCE_TOTAL,
                        threshold = DISTANCE_THRESHOLD,
                        comparisonType = ComparisonType.GREATER_THAN_OR_EQUAL
                    ),
                    period = DISTANCE_THRESHOLD
                )
            )
        }

        val config = ExerciseConfig(
            exerciseType = ExerciseType.RUNNING,
            dataTypes = dataTypes,
            isAutoPauseAndResumeEnabled = false,
            isGpsEnabled = true,
            exerciseGoals = exerciseGoals
        )
        exerciseClient.startExerciseAsync(config).await()
    }

    private fun supportsCalorieGoal(capabilities: ExerciseTypeCapabilities): Boolean {
        val supported = capabilities.supportedGoals[DataType.CALORIES_TOTAL]
        return supported != null && ComparisonType.GREATER_THAN_OR_EQUAL in supported
    }

    private fun supportsDistanceMilestone(capabilities: ExerciseTypeCapabilities): Boolean {
        val supported = capabilities.supportedMilestones[DataType.DISTANCE_TOTAL]
        return supported != null && ComparisonType.GREATER_THAN_OR_EQUAL in supported
    }

    /***
     * Note: don't call this method from outside of [ExerciseService]
     * when acquiring calories or distance.
     */
    suspend fun prepareExercise() {
        Log.d(TAG, "Preparing an exercise")

        // TODO Handle various exerciseTrackedStatus states, especially OWNED_EXERCISE_IN_PROGRESS
        // and OTHER_APP_IN_PROGRESS

        val warmUpConfig = WarmUpConfig(
            ExerciseType.RUNNING,
            setOf(
                DataType.HEART_RATE_BPM,
                DataType.LOCATION
            )
        )

        try {
            exerciseClient.prepareExerciseAsync(warmUpConfig).await()
        } catch (e: Exception) {
            Log.e(TAG, "Prepare exercise failed - ${e.message}")
        }
    }

    suspend fun endExercise() {
        Log.d(TAG, "Ending exercise")
        exerciseClient.endExerciseAsync().await()
    }

    suspend fun pauseExercise() {
        Log.d(TAG, "Pausing exercise")
        exerciseClient.pauseExerciseAsync().await()
    }

    suspend fun resumeExercise() {
        Log.d(TAG, "Resuming exercise")
        exerciseClient.resumeExerciseAsync().await()
    }

    suspend fun markLap() {
        if (isExerciseInProgress()) {
            exerciseClient.markLapAsync().await()
        }
    }

    /**
     * A flow for [ExerciseUpdate]s.
     *
     * When the flow starts, it will register an [ExerciseUpdateCallback] and start to emit
     * messages.
     *
     * [callbackFlow] is used to bridge between a callback-based API and Kotlin flows.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val exerciseUpdateFlow = callbackFlow {
        val callback = object : ExerciseUpdateCallback {
            override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                coroutineScope.runCatching {
                    trySendBlocking(ExerciseMessage.ExerciseUpdateMessage(update))
                }
            }

            override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {
                coroutineScope.runCatching {
                    trySendBlocking(ExerciseMessage.LapSummaryMessage(lapSummary))
                }
            }

            override fun onRegistered() {
            }

            override fun onRegistrationFailed(throwable: Throwable) {
                TODO("Not yet implemented")
            }

            override fun onAvailabilityChanged(
                dataType: DataType<*, *>,
                availability: Availability
            ) {
                if (availability is LocationAvailability) {
                    coroutineScope.runCatching {
                        trySendBlocking(ExerciseMessage.LocationAvailabilityMessage(availability))
                    }
                }
            }
        }
        exerciseClient.setUpdateCallback(callback)
        awaitClose {
            exerciseClient.clearUpdateCallbackAsync(callback)
        }
    }

    private companion object {
        const val CALORIES_THRESHOLD = 250.0
        const val DISTANCE_THRESHOLD = 1_000.0 // meters
    }
}

sealed class ExerciseMessage {
    class ExerciseUpdateMessage(val exerciseUpdate: ExerciseUpdate) : ExerciseMessage()
    class LapSummaryMessage(val lapSummary: ExerciseLapSummary) : ExerciseMessage()
    class LocationAvailabilityMessage(val locationAvailability: LocationAvailability) :
        ExerciseMessage()
}
