package com.example.passivedata

import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPoint
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class PassiveDataService : PassiveListenerService() {
    @Inject
    lateinit var repository: PassiveDataRepository

    override fun onNewDataPointsReceived(dataPoints: List<DataPoint>) {
        runBlocking {
            dataPoints.latestHeartRate()?.let {
                repository.storeLatestHeartRate(it)
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
