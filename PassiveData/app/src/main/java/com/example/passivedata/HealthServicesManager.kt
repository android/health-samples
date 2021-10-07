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

package com.example.passivedata

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveMonitoringConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Entry point for [HealthServicesClient] APIs. This also provides suspend functions around
 * those APIs to enable use in coroutines.
 */
class HealthServicesManager @Inject constructor(
    @ApplicationContext private val context: Context,
    healthServicesClient: HealthServicesClient
) {
    private val passiveMonitoringClient = healthServicesClient.passiveMonitoringClient
    private val dataTypes = setOf(DataType.HEART_RATE_BPM)

    suspend fun hasHeartRateCapability(): Boolean {
        val capabilities = passiveMonitoringClient.capabilities.await()
        return (DataType.HEART_RATE_BPM in capabilities.supportedDataTypesPassiveMonitoring)
    }

    suspend fun registerForHeartRateData() {
        Log.i(TAG, "Registering for background data.")
        val componentName = ComponentName(context, PassiveDataReceiver::class.java)
        val config = PassiveMonitoringConfig.builder()
            .setDataTypes(dataTypes)
            .setComponentName(componentName)
            .build()
        passiveMonitoringClient.registerDataCallback(config).await()
    }

    suspend fun unregisterForHeartRateData() {
        Log.i(TAG, "Unregistering for background data.")
        passiveMonitoringClient.unregisterDataCallback().await()
    }
}
