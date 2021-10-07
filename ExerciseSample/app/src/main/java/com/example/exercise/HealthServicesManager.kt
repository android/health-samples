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
import androidx.health.services.client.ExerciseUpdateListener
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.ComparisonType
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeCondition
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseGoal
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseTrackedStatus
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseTypeCapabilities
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.LocationAvailability
import androidx.health.services.client.data.Value
import androidx.health.services.client.data.WarmUpConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
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
            val capabilities = exerciseClient.capabilities.await()
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
        val exerciseInfo = exerciseClient.currentExerciseInfo.await()
        return exerciseInfo.exerciseTrackedStatus == ExerciseTrackedStatus.OWNED_EXERCISE_IN_PROGRESS
    }

    suspend fun isTrackingExerciseInAnotherApp(): Boolean {
        val exerciseInfo = exerciseClient.currentExerciseInfo.await()
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
        ).intersect(capabilities.supportedDataTypes)

        val aggDataTypes = setOf(
            DataType.TOTAL_CALORIES,
            DataType.DISTANCE
        ).intersect(capabilities.supportedDataTypes)

        val exerciseGoals = mutableListOf<ExerciseGoal>()
        if (supportsCalorieGoal(capabilities)) {
            // Create a one-time goal.
            exerciseGoals.add(
                ExerciseGoal.createOneTimeGoal(
                    DataTypeCondition(
                        dataType = DataType.TOTAL_CALORIES,
                        threshold = Value.ofDouble(CALORIES_THRESHOLD),
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
                        dataType = DataType.DISTANCE,
                        threshold = Value.ofDouble(DISTANCE_THRESHOLD),
                        comparisonType = ComparisonType.GREATER_THAN_OR_EQUAL
                    ),
                    period = Value.ofDouble(DISTANCE_THRESHOLD)
                )
            )
        }

        val config = ExerciseConfig.builder()
            .setExerciseType(ExerciseType.RUNNING)
            .setShouldEnableAutoPauseAndResume(false)
            .setAggregateDataTypes(aggDataTypes)
            .setDataTypes(dataTypes)
            .setExerciseGoals(exerciseGoals)
            // Required for GPS for LOCATION data type, optional for some other types.
            .setShouldEnableGps(true)
            .build()
        exerciseClient.startExercise(config).await()
    }

    private fun supportsCalorieGoal(capabilities: ExerciseTypeCapabilities): Boolean {
        val supported = capabilities.supportedGoals[DataType.TOTAL_CALORIES]
        return supported != null && ComparisonType.GREATER_THAN_OR_EQUAL in supported
    }

    private fun supportsDistanceMilestone(capabilities: ExerciseTypeCapabilities): Boolean {
        val supported = capabilities.supportedMilestones[DataType.DISTANCE]
        return supported != null && ComparisonType.GREATER_THAN_OR_EQUAL in supported
    }

    /***
     * Note: don't call this method from outside of [ExerciseService]
     * when acquiring calories or distance.
     */
    suspend fun prepareExercise() {
        Log.d(TAG, "Preparing an exercise")

        val warmUpConfig = WarmUpConfig.builder()
            .setExerciseType(ExerciseType.RUNNING)
            .setDataTypes(
                setOf(
                    DataType.HEART_RATE_BPM,
                    DataType.LOCATION
                )
            )
            .build()

        try {
            exerciseClient.prepareExercise(warmUpConfig).await()
        } catch (e: Exception) {
            Log.e(TAG, "Prepare exercise failed - ${e.message}")
        }
    }

    suspend fun endExercise() {
        Log.d(TAG, "Ending exercise")
        exerciseClient.endExercise().await()
    }

    suspend fun pauseExercise() {
        Log.d(TAG, "Pausing exercise")
        exerciseClient.pauseExercise().await()
    }

    suspend fun resumeExercise() {
        Log.d(TAG, "Resuming exercise")
        exerciseClient.resumeExercise().await()
    }

    suspend fun markLap() {
        if (isExerciseInProgress()) {
            exerciseClient.markLap().await()
        }
    }

    /**
     * A shared flow for [ExerciseUpdate]s.
     *
     * When the flow starts, it will register an [ExerciseUpdateListener] and start to emit
     * messages. When there are no more subscribers, or when the coroutine scope of [shareIn] is
     * cancelled, this flow will unregister the listener.
     *
     * A shared flow is used because only a single [ExerciseUpdateListener] can be reigstered at a
     * time, even if there are multiple consumers of the flow.
     *
     * [callbackFlow] is used to bridge between a callback-based API and Kotlin flows.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val exerciseUpdateFlow = callbackFlow<ExerciseMessage> {
        val listener = object : ExerciseUpdateListener {
            override fun onExerciseUpdate(update: ExerciseUpdate) {
                coroutineScope.runCatching {
                    trySendBlocking(ExerciseMessage.ExerciseUpdateMessage(update))
                }
            }

            override fun onLapSummary(lapSummary: ExerciseLapSummary) {
                coroutineScope.runCatching {
                    trySendBlocking(ExerciseMessage.LapSummaryMessage(lapSummary))
                }
            }

            override fun onAvailabilityChanged(dataType: DataType, availability: Availability) {
                if (availability is LocationAvailability) {
                    coroutineScope.runCatching {
                        trySendBlocking(ExerciseMessage.LocationAvailabilityMessage(availability))
                    }
                }
            }
        }
        exerciseClient.setUpdateListener(listener)
        awaitClose {
            exerciseClient.clearUpdateListener(listener)
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
