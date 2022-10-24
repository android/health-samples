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

package com.example.measuredata

import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Entry point for [HealthServicesClient] APIs, wrapping them in coroutine-friendly APIs.
 */
class HealthServicesManager @Inject constructor(
    healthServicesClient: HealthServicesClient
) {
    private val measureClient = healthServicesClient.measureClient

    suspend fun hasHeartRateCapability(): Boolean {
        val capabilities = measureClient.getCapabilitiesAsync().await()
        return (DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure)
    }

    /**
     * Returns a cold flow. When activated, the flow will register a callback for heart rate data
     * and start to emit messages. When the consuming coroutine is cancelled, the measure callback
     * is unregistered.
     *
     * [callbackFlow] is used to bridge between a callback-based API and Kotlin flows.
     */
    @ExperimentalCoroutinesApi
    fun heartRateMeasureFlow() = callbackFlow {
        val callback = object : MeasureCallback {
            override fun onAvailabilityChanged(dataType: DeltaDataType<*, *>, availability: Availability) {
                // Only send back DataTypeAvailability (not LocationAvailability)
                if (availability is DataTypeAvailability) {
                    trySendBlocking(MeasureMessage.MeasureAvailability(availability))
                }
            }

            override fun onDataReceived(data: DataPointContainer) {
                val heartRateBpm = data.getData(DataType.HEART_RATE_BPM)
                trySendBlocking(MeasureMessage.MeasureData(heartRateBpm))
            }
        }

        Log.d(TAG, "Registering for data")
        measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, callback)

        awaitClose {
            Log.d(TAG, "Unregistering for data")
            runBlocking {
                measureClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM, callback)
            }
        }
    }
}

sealed class MeasureMessage {
    class MeasureAvailability(val availability: DataTypeAvailability) : MeasureMessage()
    class MeasureData(val data: List<SampleDataPoint<Double>>): MeasureMessage()
}
