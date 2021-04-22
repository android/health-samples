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

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

/**
 * Stores the latest event information and whether or not passive events are enabled.
 */
class PassiveEventsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        const val PREFERENCES_FILENAME = "passive_event_prefs"
        private val PASSIVE_EVENTS_ENABLED = booleanPreferencesKey("passive_events_enabled")
        private val LATEST_EVENT_TYPE = stringPreferencesKey("latest_event_type")
        private val LATEST_EVENT_TIME = longPreferencesKey("latest_event_time")
    }

    val passiveEventsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PASSIVE_EVENTS_ENABLED] ?: false
    }

    suspend fun setPassiveEventsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PASSIVE_EVENTS_ENABLED] = enabled
        }
    }

    suspend fun storeLatestEvent(type: PassiveEventType, timestamp: Instant) {
        dataStore.edit { prefs ->
            prefs[LATEST_EVENT_TYPE] = type.name
            prefs[LATEST_EVENT_TIME] = timestamp.toEpochMilli()
        }
    }

    val latestEvent: Flow<PassiveEvent> = dataStore.data.map { prefs ->
        val typeString = prefs[LATEST_EVENT_TYPE]
        val type = PassiveEventType.values()
            .firstOrNull { it.name == typeString }
            ?: PassiveEventType.UNKNOWN
        val time = prefs[LATEST_EVENT_TIME] ?: 0L
        PassiveEvent(type, Instant.ofEpochMilli(time))
    }
}

enum class PassiveEventType {
    UNKNOWN, FAST_STEPS_PER_MINUTE, SLOW_STEPS_PER_MINUTE
}

data class PassiveEvent(val type: PassiveEventType, val timestamp: Instant)
