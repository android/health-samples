package com.example.passivedata

import android.util.Log
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
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
