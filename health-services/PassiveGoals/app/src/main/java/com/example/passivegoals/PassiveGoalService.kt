package com.example.passivegoals

import android.util.Log
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveGoal
import com.google.common.util.concurrent.Futures
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class PassiveGoalService : PassiveListenerService() {
    @Inject
    lateinit var repository: PassiveGoalsRepository

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
