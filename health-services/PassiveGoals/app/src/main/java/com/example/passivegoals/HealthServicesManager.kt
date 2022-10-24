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

package com.example.passivegoals

import android.content.Context
import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.data.*
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject

// Goals we want to create.
val dailyStepsGoal by lazy {
    val condition = DataTypeCondition(
        dataType = DataType.STEPS_DAILY,
        threshold = 10_000, // trigger every "threshold" steps
        comparisonType = ComparisonType.GREATER_THAN_OR_EQUAL
    )
    PassiveGoal(condition)
}

val floorsGoal by lazy {
    val condition = DataTypeCondition(
        dataType = DataType.FLOORS_DAILY,
        threshold = 3.0, // trigger every "threshold" floors climbed
        comparisonType = ComparisonType.GREATER_THAN_OR_EQUAL
    )
    PassiveGoal(condition)
}

/**
 * Entry point for [HealthServicesClient] APIs. This also provides suspend functions around
 * those APIs to enable use in coroutines.
 */
class HealthServicesManager @Inject constructor(
    @ApplicationContext private val context: Context,
    healthServicesClient: HealthServicesClient
) {
    private val passiveMonitoringClient = healthServicesClient.passiveMonitoringClient

    suspend fun hasFloorsAndDailyStepsCapability(): Boolean {
        val capabilities = passiveMonitoringClient.getCapabilitiesAsync().await()
        return capabilities.supportedDataTypesPassiveGoals.containsAll(
            setOf(
                DataType.STEPS_DAILY,
                DataType.FLOORS_DAILY,
            )
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun subscribeForGoals(): Boolean {
        Log.i(TAG, "Subscribing for goals")
        val passiveListenerConfig =
            PassiveListenerConfig.builder().setDailyGoals(setOf(dailyStepsGoal, floorsGoal))
                .build()
        return try {
            passiveMonitoringClient.setPassiveListenerServiceAsync(
                PassiveGoalService::class.java,
                passiveListenerConfig
            ).await()
            true
        } catch (t: Throwable) {
            false
        }
    }

    suspend fun unsubscribeFromGoals() {
        Log.i(TAG, "Unsubscribing from goals")
        passiveMonitoringClient.clearPassiveListenerServiceAsync().await()
    }
}
