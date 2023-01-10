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
package com.example.passivegoalscompose.service

import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveGoal
import com.example.passivegoalscompose.data.PassiveGoalsRepository
import kotlinx.coroutines.runBlocking
import java.time.Instant

/**
 * Service to receive data from Health Services.
 *
 * Passive data is delivered from Health Services to this service. Override the appropriate methods
 * in [PassiveListenerService] to receive updates for new data points, goals achieved etc.
 */
class PassiveGoalsService : PassiveListenerService() {
    private val repository = PassiveGoalsRepository(this)

    override fun onGoalCompleted(goal: PassiveGoal) {
        val time = Instant.now()
        when (goal.dataTypeCondition.dataType) {
            DataType.FLOORS_DAILY ->
                runBlocking {
                    repository.updateLatestFloorsGoalTime(time)
                }
            DataType.STEPS_DAILY ->
                runBlocking {
                    repository.setLatestDailyGoalAchieved(time)
                }
        }
    }
}
