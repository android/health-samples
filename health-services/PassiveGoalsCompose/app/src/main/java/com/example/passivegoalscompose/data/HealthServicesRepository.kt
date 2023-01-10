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
package com.example.passivegoalscompose.data

import android.content.Context
import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.PassiveListenerConfig
import com.example.passivegoalscompose.TAG
import com.example.passivegoalscompose.service.PassiveGoalsService
import dailyStepsGoal
import floorsGoal

/**
 * Entry point for [HealthServicesClient] APIs. This also provides suspend functions around
 * those APIs to enable use in coroutines.
 */
class HealthServicesRepository(context: Context) {
    private val healthServicesClient = HealthServices.getClient(context)
    private val passiveMonitoringClient = healthServicesClient.passiveMonitoringClient

    private val goals = setOf(dailyStepsGoal, floorsGoal)
    private val requiredDataTypes = goals.map { it.dataTypeCondition.dataType }.toSet()

    // Note that the dataTypes in the [PassiveListenerConfig] should contain all the data types
    // required by the specified dailyGoals.
    private val passiveListenerConfig = PassiveListenerConfig(
        dataTypes = requiredDataTypes,
        shouldUserActivityInfoBeRequested = false,
        dailyGoals = goals,
        healthEventTypes = setOf()
    )

    suspend fun hasFloorsAndDailyStepsCapability(): Boolean {
        val capabilities = passiveMonitoringClient.getCapabilitiesAsync().await()
        return capabilities.supportedDataTypesPassiveGoals.containsAll(requiredDataTypes)
    }

    suspend fun subscribeForGoals() {
        Log.i(TAG, "Subscribing for goals")
        passiveMonitoringClient.setPassiveListenerServiceAsync(
            PassiveGoalsService::class.java,
            passiveListenerConfig
        ).await()
    }

    suspend fun unsubscribeFromGoals() {
        Log.i(TAG, "Unsubscribing from goals")
        passiveMonitoringClient.clearPassiveListenerServiceAsync().await()
    }
}
