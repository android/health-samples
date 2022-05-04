package com.example.passivedata

import android.util.Log
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPoint
import androidx.health.services.client.data.HealthEvent
import androidx.health.services.client.data.PassiveGoal
import androidx.health.services.client.data.UserActivityInfo
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class PassiveDataService : PassiveListenerService() {
    @Inject
    lateinit var repository: PassiveDataRepository

    override fun onNewDataPoints(dataPoints: List<DataPoint>) {
        runBlocking {
            dataPoints.latestHeartRate()?.let {
                repository.storeLatestHeartRate(it)
            }
        }
    }
}
