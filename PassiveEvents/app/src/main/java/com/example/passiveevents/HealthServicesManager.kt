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

package com.example.passiveevents

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.concurrent.futures.await
import com.google.android.libraries.wear.whs.client.WearHealthServicesClient
import com.google.android.libraries.wear.whs.data.*
import com.google.android.libraries.wear.whs.data.event.Event
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
    private fun broadcastEvent(actionString: String) = lazy {
        val intent = Intent(context, PassiveEventsReceiver::class.java).apply {
            action = actionString
        }
        PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    // PendingIntents to send when an event occurs. The receiver will not be given any information
    // about the event that triggered the broadcast, so we have two options:
    //   1. Use a different BroadcastReceiver for each event.
    //   2. Use the same BroadcastReceiver and send different PendingIntents.
    // Here we use option 2, where the PendingIntents differ in the Intent action string used.
    private val fastStepsPendingIntent by broadcastEvent(PassiveEventsReceiver.ACTION_FAST_STEPS_PER_MINUTE)
    private val slowStepsPendingIntent by broadcastEvent(PassiveEventsReceiver.ACTION_SLOW_STEPS_PER_MINUTE)

    // Events we want to subscribe to.
    private val fastStepsEvent by lazy {
        // steps per minute >= 150
        val condition = DataTypeCondition.builder()
            .setDataType(DataType.STEPS_PER_MINUTE)
            .setComparisonType(ComparisonType.GREATER_THAN_OR_EQUAL)
            .setThreshold(Value.ofInt(150))
            .build()
        // REPEATED means we will be notified on every occurrence until we unsubscribe.
        Event.create(condition, Event.TriggerType.REPEATED)
    }
    private val slowStepsEvent by lazy {
        // steps per minute <= 60
        val condition = DataTypeCondition.builder()
            .setDataType(DataType.STEPS_PER_MINUTE)
            .setComparisonType(ComparisonType.LESS_THAN_OR_EQUAL)
            .setThreshold(Value.ofInt(60))
            .build()
        // REPEATED means we will be notified on every occurrence until we unsubscribe.
        Event.create(condition, Event.TriggerType.REPEATED)
    }

    suspend fun hasStepsPerMinuteCapability(): Boolean {
        val capabilities = whsClient.capabilities.await()
        return (DataType.STEPS_PER_MINUTE in capabilities.supportedDataTypesEvents())
    }

    suspend fun subscribeForEvents() {
        Log.i(TAG, "Subscribing for events")
        // Each event is a separate subscription.
        whsClient.passiveMonitoringClient
            .registerEventCallback(fastStepsEvent, fastStepsPendingIntent)
            .await()
        whsClient.passiveMonitoringClient
            .registerEventCallback(slowStepsEvent, slowStepsPendingIntent)
            .await()
    }

    suspend fun unsubscribeFromEvents() {
        Log.i(TAG, "Unsubscribing from events")
        whsClient.passiveMonitoringClient.unregisterEventCallback(fastStepsEvent).await()
        whsClient.passiveMonitoringClient.unregisterEventCallback(slowStepsEvent).await()
    }
}
