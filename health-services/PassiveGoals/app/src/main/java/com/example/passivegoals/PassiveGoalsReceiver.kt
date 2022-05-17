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

package com.example.passivegoals

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.health.services.client.data.PassiveGoal
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class PassiveGoalsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: PassiveGoalsRepository

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("qqqqqq", "in receiver")
        if (intent?.action != PassiveGoal.ACTION_GOAL) {
            return
        }
        val passiveGoal = PassiveGoal.fromIntent(intent)
        val time = Instant.now()
        when (passiveGoal) {
            floorsGoal -> {
                runBlocking {
                    repository.updateLatestFloorsGoalTime(time)
                }
            }
            dailyStepsGoal -> {
                runBlocking {
                    repository.setLatestDailyGoalAchieved(time)
                }
            }
            hrGoal -> {
                Log.d("qqqqqq", "got hr goal!!!")
            }
        }
    }
}
