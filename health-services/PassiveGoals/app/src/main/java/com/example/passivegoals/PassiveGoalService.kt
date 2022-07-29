package com.example.passivegoals

import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveGoal
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
            DataType.FLOORS ->
                runBlocking {
                    repository.updateLatestFloorsGoalTime(time)
                }
            DataType.DAILY_STEPS ->
                runBlocking {
                    repository.setLatestDailyGoalAchieved(time)
                }
        }
    }

    override fun onRegistered() {
        TODO("Not yet implemented")
    }

    override fun onRegistrationFailed(throwable: Throwable) {
        TODO("Not yet implemented")
    }
}
