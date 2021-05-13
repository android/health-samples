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
import androidx.health.services.client.data.Value
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * Entry point for [HealthServicesClient] APIs, wrapping them in coroutine-friendly APIs.
 */
class HealthServicesManager @Inject constructor(
    healthServicesClient: HealthServicesClient
) {
    private val exerciseClient = healthServicesClient.exerciseClient

    private var exerciseCapabilities: ExerciseTypeCapabilities? = null
    private var capablitiesLoaded = false

    suspend fun getExerciseCapabilities(): ExerciseTypeCapabilities? {
        if (!capablitiesLoaded) {
            val capabilities = exerciseClient.capabilities.await()
            if (ExerciseType.RUNNING in capabilities.supportedExerciseTypes) {
                exerciseCapabilities = capabilities.getExerciseTypeCapabilities(ExerciseType.RUNNING)
            }
            capablitiesLoaded = true
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

    suspend fun startExercise() {
        Log.d(TAG, "Starting exercise")
        // Types for which we want to receive metrics. Only ask for ones that are supported.
        val capabilities = getExerciseCapabilities() ?: return
        val dataTypes = setOf(
            DataType.HEART_RATE_BPM,
            DataType.AGGREGATE_CALORIES_EXPENDED,
            DataType.AGGREGATE_DISTANCE
        ).intersect(capabilities.supportedDataTypes)

        val exerciseGoals = mutableListOf<ExerciseGoal>()
        if (supportsCalorieGoal(capabilities)) {
            // Create a one-time goal.
            exerciseGoals.add(
                ExerciseGoal.createOneTimeGoal(
                    DataTypeCondition(
                        dataType = DataType.AGGREGATE_CALORIES_EXPENDED,
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
                        dataType = DataType.AGGREGATE_DISTANCE,
                        threshold = Value.ofDouble(DISTANCE_THRESHOLD),
                        comparisonType = ComparisonType.GREATER_THAN_OR_EQUAL
                    ),
                    period = Value.ofDouble(DISTANCE_THRESHOLD)
                )
            )
        }

        val config = ExerciseConfig.builder()
            .setExerciseType(ExerciseType.RUNNING)
            .setAutoPauseAndResume(false)
            .setDataTypes(dataTypes)
            .setExerciseGoals(exerciseGoals)
            .build()
        exerciseClient.startExercise(config).await()
    }

    private fun supportsCalorieGoal(capabilities: ExerciseTypeCapabilities) : Boolean {
        val supported = capabilities.supportedGoals[DataType.AGGREGATE_CALORIES_EXPENDED]
        return supported != null && ComparisonType.GREATER_THAN_OR_EQUAL in supported
    }

    private fun supportsDistanceMilestone(capabilities: ExerciseTypeCapabilities) : Boolean {
        val supported = capabilities.supportedMilestones[DataType.AGGREGATE_DISTANCE]
        return supported != null && ComparisonType.GREATER_THAN_OR_EQUAL in supported
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
     * Returns a cold flow. When activated, the flow will register a listener for exercise state
     * and start to emit messages. When the consuming coroutine is cancelled, the exercise listener
     * is unregistered.
     *
     * [callbackFlow] is used to bridge between a callback-based API and Kotlin flows.
     */
    fun getExerciseStateFlow() = callbackFlow<ExerciseMessage> {
        val listener = object : ExerciseUpdateListener {
            override fun onExerciseUpdate(update: ExerciseUpdate) {
                sendBlocking(ExerciseMessage.ExerciseUpdateMessage(update))
            }

            override fun onLapSummary(lapSummary: ExerciseLapSummary) {
                sendBlocking(ExerciseMessage.LapSummaryMessage(lapSummary))
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
}
