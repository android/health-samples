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
package com.example.exercisesamplecompose.data

import android.annotation.SuppressLint
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.ComparisonType
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeCondition
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseGoal
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseTypeCapabilities
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.LocationAvailability
import androidx.health.services.client.data.WarmUpConfig
import androidx.health.services.client.endExercise
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.markLap
import androidx.health.services.client.pauseExercise
import androidx.health.services.client.prepareExercise
import androidx.health.services.client.resumeExercise
import androidx.health.services.client.startExercise
import com.example.exercisesamplecompose.service.ExerciseLogger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow

/**
 * Entry point for [HealthServicesClient] APIs, wrapping them in coroutine-friendly APIs.
 */
@SuppressLint("RestrictedApi")
@Singleton
class ExerciseClientManager
@Inject
constructor(
    healthServicesClient: HealthServicesClient,
    private val logger: ExerciseLogger
) {
    val exerciseClient: ExerciseClient = healthServicesClient.exerciseClient

    suspend fun getExerciseCapabilities(): ExerciseTypeCapabilities? {
        val capabilities = exerciseClient.getCapabilities()

        return if (ExerciseType.RUNNING in capabilities.supportedExerciseTypes) {
            capabilities.getExerciseTypeCapabilities(ExerciseType.RUNNING)
        } else {
            null
        }
    }

    private var thresholds = Thresholds(0.0, Duration.ZERO)

    fun updateGoals(newThresholds: Thresholds) {
        thresholds = newThresholds.copy()
    }

    suspend fun startExercise() {
        logger.log("Starting exercise")
        // Types for which we want to receive metrics. Only ask for ones that are supported.
        val capabilities = getExerciseCapabilities()

        if (capabilities == null) {
            logger.log("No capabilities")
            return
        }

        val dataTypes =
            setOf(
                DataType.HEART_RATE_BPM,
                DataType.HEART_RATE_BPM_STATS,
                DataType.CALORIES_TOTAL,
                DataType.DISTANCE_TOTAL
            ).intersect(capabilities.supportedDataTypes)
        val exerciseGoals = mutableListOf<ExerciseGoal<*>>()
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

        // Set a distance goal if it's supported by the exercise and the user has entered one
        if (supportsDistanceMilestone(capabilities) && thresholds.distanceIsSet) {
            exerciseGoals.add(
                ExerciseGoal.createOneTimeGoal(
                    condition =
                    DataTypeCondition(
                        dataType = DataType.DISTANCE_TOTAL,
                        threshold = thresholds.distance * 1000, // our app uses kilometers
                        comparisonType = ComparisonType.GREATER_THAN_OR_EQUAL
                    )
                )
            )
        }

        // Set a duration goal if it's supported by the exercise and the user has entered one
        if (supportsDurationMilestone(capabilities) && thresholds.durationIsSet) {
            exerciseGoals.add(
                ExerciseGoal.createOneTimeGoal(
                    DataTypeCondition(
                        dataType = DataType.ACTIVE_EXERCISE_DURATION_TOTAL,
                        threshold = thresholds.duration.inWholeSeconds,
                        comparisonType = ComparisonType.GREATER_THAN_OR_EQUAL
                    )
                )
            )
        }

        val supportsAutoPauseAndResume = capabilities.supportsAutoPauseAndResume

        val config =
            ExerciseConfig(
                exerciseType = ExerciseType.RUNNING,
                dataTypes = dataTypes,
                isAutoPauseAndResumeEnabled = supportsAutoPauseAndResume,
                isGpsEnabled = true,
                exerciseGoals = exerciseGoals
            )

        exerciseClient.startExercise(config)
        logger.log("Started exercise")
    }

    /***
     * Note: don't call this method from outside of ExerciseService.kt
     * when acquiring calories or distance.
     */
    suspend fun prepareExercise() {
        logger.log("Preparing an exercise")
        val warmUpConfig =
            WarmUpConfig(
                exerciseType = ExerciseType.RUNNING,
                dataTypes = setOf(DataType.HEART_RATE_BPM, DataType.LOCATION)
            )
        try {
            exerciseClient.prepareExercise(warmUpConfig)
        } catch (e: Exception) {
            logger.log("Prepare exercise failed - ${e.message}")
        }
    }

    suspend fun endExercise() {
        logger.log("Ending exercise")
        exerciseClient.endExercise()
    }

    suspend fun pauseExercise() {
        logger.log("Pausing exercise")
        exerciseClient.pauseExercise()
    }

    suspend fun resumeExercise() {
        logger.log("Resuming exercise")
        exerciseClient.resumeExercise()
    }

    /** Wear OS 3.0 reserves two buttons for the OS. For devices with more than 2 buttons,
     * consider implementing a "press" to mark lap feature**/
    suspend fun markLap() {
        if (exerciseClient.isExerciseInProgress()) {
            exerciseClient.markLap()
        }
    }

    /**
     * When the flow starts, it will register an [ExerciseUpdateCallback] and start to emit
     * messages. When there are no more subscribers, or when the coroutine scope is
     * cancelled, this flow will unregister the listener.
     * [callbackFlow] is used to bridge between a callback-based API and Kotlin flows.
     */
    val exerciseUpdateFlow =
        callbackFlow {
            val callback =
                object : ExerciseUpdateCallback {
                    override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                        trySendBlocking(ExerciseMessage.ExerciseUpdateMessage(update))
                    }

                    override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {
                        trySendBlocking(ExerciseMessage.LapSummaryMessage(lapSummary))
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
                            trySendBlocking(
                                ExerciseMessage.LocationAvailabilityMessage(availability)
                            )
                        }
                    }
                }

            exerciseClient.setUpdateCallback(callback)
            awaitClose {
                // Ignore async result
                exerciseClient.clearUpdateCallbackAsync(callback)
            }
        }

    private companion object {
        const val CALORIES_THRESHOLD = 250.0
    }
}

data class Thresholds(
    var distance: Double,
    var duration: Duration,
    var durationIsSet: Boolean = duration != Duration.ZERO,
    var distanceIsSet: Boolean = distance != 0.0
)

sealed class ExerciseMessage {
    class ExerciseUpdateMessage(
        val exerciseUpdate: ExerciseUpdate
    ) : ExerciseMessage()

    class LapSummaryMessage(
        val lapSummary: ExerciseLapSummary
    ) : ExerciseMessage()

    class LocationAvailabilityMessage(
        val locationAvailability: LocationAvailability
    ) : ExerciseMessage()
}
