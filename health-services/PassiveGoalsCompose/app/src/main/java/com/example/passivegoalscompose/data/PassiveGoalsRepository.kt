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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.TimeZone

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "passive_goals")

class PassiveGoalsRepository(private val context: Context) {
    companion object {
        private val LATEST_DAILY_GOAL_ACHIEVED_TIME = longPreferencesKey("latest_daily_goal_time")
        private val PASSIVE_GOALS_ENABLED = booleanPreferencesKey("passive_goals_enabled")
        private val LATEST_FLOOR_GOAL_TIME = longPreferencesKey("latest_floor_goal_time")
    }

    val passiveGoalsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PASSIVE_GOALS_ENABLED] ?: false
    }

    suspend fun setPassiveGoalsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PASSIVE_GOALS_ENABLED] = enabled
        }
    }

    suspend fun setLatestDailyGoalAchieved(timestamp: Instant) {
        context.dataStore.edit { prefs ->
            prefs[LATEST_DAILY_GOAL_ACHIEVED_TIME] = timestamp.toEpochMilli()
        }
    }

    val dailyStepsGoalAchieved: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[LATEST_DAILY_GOAL_ACHIEVED_TIME]?.let {
            val zoneId = TimeZone.getDefault().toZoneId()
            val achievedDate =
                LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(it),
                    zoneId
                ).toLocalDate()
            achievedDate == LocalDate.now(zoneId)
        } ?: false
    }

    suspend fun updateLatestFloorsGoalTime(timestamp: Instant) {
        context.dataStore.edit { prefs ->
            prefs[LATEST_FLOOR_GOAL_TIME] = timestamp.toEpochMilli()
        }
    }

    val latestFloorsGoalTime: Flow<Instant> = context.dataStore.data.map { prefs ->
        val time = prefs[LATEST_FLOOR_GOAL_TIME] ?: 0L
        Instant.ofEpochMilli(time)
    }
}
