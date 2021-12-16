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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.HrAccuracy
import androidx.health.services.client.data.PassiveMonitoringUpdate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Receives heart rate updates passively and saves it to the repository.
 */
@AndroidEntryPoint
class PassiveDataReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: PassiveDataRepository

    override fun onReceive(context: Context, intent: Intent) {
        val state = PassiveMonitoringUpdate.fromIntent(intent) ?: return
        // Get the most recent heart rate measurement.
        val latestDataPoint = state.dataPoints
            // dataPoints can have multiple types (e.g. if the app registered for multiple types).
            .filter { it.dataType == DataType.HEART_RATE_BPM }
            // where accuracy information is available, only show readings that are of medium or
            // high accuracy. (Where accuracy information isn't available, show the reading if it is
            // a positive value).
            .filter {
                it.accuracy == null ||
                setOf(
                    HrAccuracy.SensorStatus.ACCURACY_MEDIUM,
                    HrAccuracy.SensorStatus.ACCURACY_HIGH
                ).contains((it.accuracy as HrAccuracy).sensorStatus)
            }
            .filter {
                it.value.asDouble() > 0
            }
            // HEART_RATE_BPM is a SAMPLE type, so start and end times are the same.
            .maxByOrNull { it.endDurationFromBoot }
        // If there were no data points, the previous function returns null.
            ?: return

        val latestHeartRate = latestDataPoint.value.asDouble() // HEART_RATE_BPM is a Float type.
        Log.d(TAG, "Received latest heart rate in background: $latestHeartRate")

        runBlocking {
            repository.storeLatestHeartRate(latestHeartRate)
        }
    }
}
