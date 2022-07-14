package com.example.passivegoals

import android.util.Log
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class PassiveGoalService : PassiveListenerService() {
    @Inject
    lateinit var repository: PassiveGoalsRepository

    init {
        // We reach here surprisingly frequently
        Log.d("qqqqqq", "init: in service")
    }

    override fun onNewDataPointsReceived(dataPoints: List<DataPoint>) {
        Log.d("qqqqqq", "new data points")
    }

    override fun onHealthEventReceived(event: HealthEvent) {
        Log.d("qqqqqq", "health event")
    }

    override fun onUserActivityInfoReceived(info: UserActivityInfo) {
        Log.d("qqqqqq", "user activity info received")
    }

    override fun onGoalCompleted(goal: PassiveGoal) {
        Log.d("qqqqqq", "goal completed")
        Log.d("qqqqqq", goal.dataTypeCondition.toString())
        val time = Instant.now()
        when (goal.dataTypeCondition.dataType) {
            DataType.FLOORS ->
                runBlocking {
                    Log.d("qqqqqq", "got floors goal")
                    repository.updateLatestFloorsGoalTime(time)
                }
            DataType.DAILY_STEPS ->
                runBlocking {
                    Log.d("qqqqqq", "got steps goal")
                    repository.setLatestDailyGoalAchieved(time)
                }
        }
    }

    override fun onPermissionLost() {
        Log.d("qqqqq", "permission lost")
    }

    override fun onRegistered() {
        Log.d("qqqqqq", "onRegistered")
        TODO("Not yet implemented")
    }

    override fun onRegistrationFailed(throwable: Throwable) {
        Log.d("qqqqqq", "registration failed")
        TODO("Not yet implemented")
    }
}
