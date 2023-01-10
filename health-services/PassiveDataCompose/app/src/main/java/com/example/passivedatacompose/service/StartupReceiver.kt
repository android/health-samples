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
package com.example.passivedatacompose.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.passivedatacompose.PERMISSION
import com.example.passivedatacompose.TAG
import com.example.passivedatacompose.data.HealthServicesRepository
import com.example.passivedatacompose.data.PassiveDataRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Background data subscriptions are not persisted across device restarts. This receiver checks if
 * we enabled background data and, if so, registers again.
 */
class StartupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val repository = PassiveDataRepository(context)
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        runBlocking {
            if (repository.passiveDataEnabled.first()) {
                // Make sure we have permission.
                val result = context.checkSelfPermission(PERMISSION)
                if (result == PackageManager.PERMISSION_GRANTED) {
                    scheduleWorker(context)
                } else {
                    // We may have lost the permission somehow. Mark that background data is
                    // disabled so the state is consistent the next time the user opens the app UI.
                    repository.setPassiveDataEnabled(false)
                }
            }
        }
    }

    private fun scheduleWorker(context: Context) {
        // BroadcastReceiver's onReceive must complete within 10 seconds. During device startup,
        // sometimes the call to register for background data takes longer than that and our
        // BroadcastReceiver gets destroyed before it completes. Instead we schedule a WorkManager
        // job to perform the registration.
        Log.i(TAG, "Enqueuing worker")
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<RegisterForBackgroundDataWorker>().build()
        )
    }
}

class RegisterForBackgroundDataWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.i(TAG, "Worker running")
        val healthServicesRepository = HealthServicesRepository(appContext)
        healthServicesRepository.registerForHeartRateData()
        return Result.success()
    }
}
