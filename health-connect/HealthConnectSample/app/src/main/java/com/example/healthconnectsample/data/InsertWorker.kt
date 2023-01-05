package com.example.healthconnectsample.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.healthconnectsample.presentation.screen.exercisesession.ExerciseSessionViewModel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InsertWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }
    private val mutex = Mutex()

    override suspend fun doWork(): Result {
        mutex.withLock {
            healthConnectClient.insertRecords(ExerciseSessionViewModel.sharedInsertQueue.toList())
            ExerciseSessionViewModel.emptySharedQueue()
        }

        return Result.success()
    }
}