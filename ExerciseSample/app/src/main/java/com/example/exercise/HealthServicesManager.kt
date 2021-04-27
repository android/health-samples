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
import com.google.android.libraries.wear.whs.client.ExerciseStateListener
import com.google.android.libraries.wear.whs.client.WearHealthServicesClient
import com.google.android.libraries.wear.whs.data.ActivityType
import com.google.android.libraries.wear.whs.data.ComparisonType
import com.google.android.libraries.wear.whs.data.DataType
import com.google.android.libraries.wear.whs.data.DataTypeCondition
import com.google.android.libraries.wear.whs.data.ExerciseCapabilities
import com.google.android.libraries.wear.whs.data.ExerciseConfig
import com.google.android.libraries.wear.whs.data.ExerciseGoal
import com.google.android.libraries.wear.whs.data.ExerciseLapSummary
import com.google.android.libraries.wear.whs.data.ExerciseState
import com.google.android.libraries.wear.whs.data.Value
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * Entry point for [WearHealthServicesClient] APIs, wrapping them in coroutine-friendly APIs.
 */
class HealthServicesManager @Inject constructor(
    private val whsClient: WearHealthServicesClient
) {
    private var exerciseCapabilities: ExerciseCapabilities? = null
    private var capablitiesLoaded = false

    suspend fun getExerciseCapabilities(): ExerciseCapabilities? {
        if (!capablitiesLoaded) {
            val capabilities = whsClient.capabilities.await()
            if (ActivityType.RUNNING in capabilities.supportedExerciseTypes()) {
                exerciseCapabilities = capabilities.getExerciseCapabilities(ActivityType.RUNNING)
            }
            capablitiesLoaded = true
        }
        return exerciseCapabilities
    }

    suspend fun hasExerciseCapability(): Boolean {
        return getExerciseCapabilities() != null
    }

    suspend fun isExerciseInProgress(): Boolean {
        return whsClient.exerciseClient.currentExerciseState.await() != null
    }

    suspend fun isTrackingExerciseInAnotherApp(): Boolean {
        return whsClient.exerciseClient.isTrackingExerciseInAnotherApp.await()
    }

    suspend fun startExercise() {
        Log.d(TAG, "Starting exercise")
        // Types for which we want to receive metrics. Only ask for ones that are supported.
        val supportedTypes = getExerciseCapabilities()?.supportedDataTypes() ?: return
        val dataTypes = setOf(
            DataType.HEART_RATE_BPM,
            DataType.AGGREGATE_CALORIES_EXPENDED,
            DataType.AGGREGATE_DISTANCE
        ).intersect(supportedTypes)

        // Create a one-time goal.
        val calorieGoal = ExerciseGoal.createOneTimeGoal(
            DataTypeCondition.builder()
                .setDataType(DataType.AGGREGATE_CALORIES_EXPENDED)
                .setComparisonType(ComparisonType.GREATER_THAN_OR_EQUAL)
                .setThreshold(Value.ofFloat(CALORIES_THRESHOLD))
                .build()
        )
        // Create a milestone goal. To make a milestone for every kilometer, set the initial
        // threshold to 1km and the period to 1km.
        val distanceGoal = ExerciseGoal.createMilestone(
            /*condition = */ DataTypeCondition.builder()
                .setDataType(DataType.AGGREGATE_DISTANCE)
                .setComparisonType(ComparisonType.GREATER_THAN_OR_EQUAL)
                .setThreshold(Value.ofFloat(DISTANCE_THRESHOLD))
                .build(),
            /*period = */ Value.ofFloat(DISTANCE_THRESHOLD)
        )
        val config = ExerciseConfig.builder()
            .setActivityType(ActivityType.RUNNING)
            .setAutoPauseAndResume(false)
            .setDataTypes(dataTypes)
            .setExerciseGoals(listOf(calorieGoal, distanceGoal))
            .build()
        whsClient.exerciseClient.startExercise(config).await()
    }

    suspend fun endExercise() {
        Log.d(TAG, "Ending exercise")
        whsClient.exerciseClient.endExercise().await()
    }

    suspend fun pauseExercise() {
        Log.d(TAG, "Pausing exercise")
        whsClient.exerciseClient.pauseExercise().await()
    }

    suspend fun resumeExercise() {
        Log.d(TAG, "Resuming exercise")
        whsClient.exerciseClient.resumeExercise().await()
    }

    suspend fun markLap() {
        if (whsClient.exerciseClient.currentExerciseState.await() != null) {
            whsClient.exerciseClient.markLap().await()
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
        val listener = object : ExerciseStateListener {
            override fun onLapSummary(lapSummary: ExerciseLapSummary) {
                sendBlocking(ExerciseMessage.LapSummaryMessage)
            }

            override fun onStateUpdate(state: ExerciseState) {
                sendBlocking(ExerciseMessage.ExerciseStateMessage(state))
            }
        }
        whsClient.exerciseClient.addStateListener(listener)
        awaitClose {
            whsClient.exerciseClient.removeStateListener(listener)
        }
    }

    private companion object {
        const val CALORIES_THRESHOLD = 250f
        const val DISTANCE_THRESHOLD = 1_000f // meters
    }
}

sealed class ExerciseMessage {
    class ExerciseStateMessage(val state: ExerciseState) : ExerciseMessage()
    object LapSummaryMessage : ExerciseMessage()
}
