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
import android.content.pm.PackageManager
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Passive goals subscriptions are not persisted across device restarts. This receiver checks if
 * we enabled passive goals and, if so, registers them again.
 */
@AndroidEntryPoint
class StartupReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: PassiveGoalsRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        runBlocking {
            if (repository.passiveGoalsEnabled.first()) {
                // Make sure we have permission.
                val result =
                    context.checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION)
                if (result == PackageManager.PERMISSION_GRANTED) {
                    scheduleWorker(context)
                } else {
                    // We may have lost the permission somehow. Mark that goal subscriptions are
                    // disabled so the state is consistent the next time the user opens the app UI.
                    repository.setPassiveGoalsEnabled(false)
                }
            }
        }
    }

    private fun scheduleWorker(context: Context) {
        // BroadcastReceiver's onReceive must complete within 10 seconds. During device startup,
        // sometimes the call to subscribe for goals takes longer than that and our
        // BroadcastReceiver gets destroyed before it completes. Instead we schedule a WorkManager
        // job to perform the registration.
        Log.i(TAG, "Enqueuing worker")
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<SubscribeForEventsWorker>().build()
        )
    }
}

@HiltWorker
class SubscribeForEventsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val healthServicesManager: HealthServicesManager
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Log.i(TAG, "Worker running")
        runBlocking {
            healthServicesManager.subscribeForGoals()
        }
        return Result.success()
    }
}
