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
import javax.inject.Inject

// Goals we want to create.
val dailyStepsGoal by lazy {
    // 10000 steps per day
    val condition = DataTypeCondition(
        dataType = DataType.STEPS_DAILY,
        threshold = 10,
        comparisonType = ComparisonType.GREATER_THAN_OR_EQUAL
    )
    // For DAILY_* DataTypes, REPEATED goals trigger only once per 24 period, resetting each day
    // at midnight local time.
    PassiveGoal(condition, PassiveGoal.TriggerFrequency.REPEATED)
}

val floorsGoal by lazy {
    // A goal that triggers for every 3 floors climbed.
    val condition = DataTypeCondition(
        dataType = DataType.FLOORS,
        threshold = 3.0,
        comparisonType = ComparisonType.GREATER_THAN_OR_EQUAL
    )
    // REPEATED means we will be notified on every occurrence until we unsubscribe.
    PassiveGoal(condition, PassiveGoal.TriggerFrequency.REPEATED)
}

val hrGoal by lazy {
    val condition = DataTypeCondition(
        dataType = DataType.HEART_RATE_BPM,
        threshold = 50.0,
        comparisonType = ComparisonType.GREATER_THAN_OR_EQUAL
    )
    PassiveGoal(condition, PassiveGoal.TriggerFrequency.REPEATED)
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
                DataType.FLOORS,
                DataType.HEART_RATE_BPM
            )
        )
    }

    fun subscribeForGoals() {
        Log.i(TAG, "Subscribing for goals")
        val passiveListenerConfig = PassiveListenerConfig.builder()
            .setPassiveGoals(setOf(dailyStepsGoal, floorsGoal, hrGoal))
            .build()
        val future = passiveMonitoringClient.setPassiveListenerServiceAsync(
            PassiveGoalService::class.java,
            passiveListenerConfig
        )
        Futures.addCallback(
            future, object : FutureCallback<Void> {
                override fun onSuccess(result: Void?) {
                }

                override fun onFailure(t: Throwable) {
                }
            },
            // causes the callbacks to be executed on the main (UI) thread
            context.mainExecutor
        )
    }

    suspend fun unsubscribeFromGoals() {
        Log.i(TAG, "Unsubscribing from goals")
        passiveMonitoringClient.clearPassiveListenerServiceAsync().await()
    }
}
