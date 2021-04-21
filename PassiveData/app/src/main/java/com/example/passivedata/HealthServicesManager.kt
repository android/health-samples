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

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.concurrent.futures.await
import com.google.android.libraries.wear.whs.client.WearHealthServicesClient
import com.google.android.libraries.wear.whs.data.DataType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Entry point for [WearHealthServicesClient] APIs. This also provides suspend functions around
 * those APIs to enable use in coroutines.
 */
class HealthServicesManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val whsClient: WearHealthServicesClient
) {
    private val dataTypes = setOf(DataType.HEART_RATE_BPM)

    private val pendingIntent by lazy {
        val intent = Intent(context, PassiveDataReceiver::class.java)
        PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    suspend fun hasHeartRateCapability(): Boolean {
        val capabilities = whsClient.capabilities.await()
        return (DataType.HEART_RATE_BPM in capabilities.supportedDataTypesBackground())
    }

    suspend fun registerForHeartRateData() {
        Log.i(TAG, "Registering for background data.")
        whsClient.passiveMonitoringClient.registerDataCallback(dataTypes, pendingIntent).await()
    }

    suspend fun unregisterForHeartRateData() {
        Log.i(TAG, "Unregistering for background data.")
        whsClient.passiveMonitoringClient.unregisterDataCallback().await()
    }
}
