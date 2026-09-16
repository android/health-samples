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
package com.example.passivedatacompose.service

import android.content.ComponentName
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.example.passivedatacompose.complication.HeartRateComplicationService
import com.example.passivedatacompose.data.PassiveDataRepository
import com.example.passivedatacompose.data.latestHeartRate
import kotlinx.coroutines.runBlocking

/**
 * Service to receive data from Health Services.
 *
 * Passive data is delivered from Health Services to this service. Override the appropriate methods
 * in [PassiveListenerService] to receive updates for new data points, goals achieved etc.
 */
class PassiveDataService : PassiveListenerService() {
    private val repository = PassiveDataRepository(this)

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        runBlocking {
            dataPoints.getData(DataType.HEART_RATE_BPM).latestHeartRate()?.let {
                repository.storeLatestHeartRate(it)
            }
        }

        // On API 33 and above the complication value is evaluated by the platform, but this keeps
        // the fallback text, and the complication on older devices, up to date.
        ComplicationDataSourceUpdateRequester.create(
            context = this,
            complicationDataSourceComponent = ComponentName(
                this,
                HeartRateComplicationService::class.java
            )
        ).requestUpdateAll()
    }
}
