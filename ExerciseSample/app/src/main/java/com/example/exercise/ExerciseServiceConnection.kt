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

package com.example.exercise

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * ServiceConnection implementation. This is implemented as a LifecycleOwner so we can launch
 * coroutines bound to its lifecycle. When the service is connected, the lifecycle will be
 * [Lifecycle.State.STARTED]; otherwise it will be [Lifecycle.State.INITIALIZED].
 */
class ExerciseServiceConnection : ServiceConnection, LifecycleOwner {

    var exerciseService: ExerciseService? = null

    private val lifecycleRegistry = LifecycleRegistry(this).apply {
        currentState = Lifecycle.State.INITIALIZED
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        exerciseService = (service as ExerciseService.LocalBinder).getService()
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    override fun onServiceDisconnected(name: ComponentName) {
        lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
    }

    /**
     * Convenience function to launch a coroutine bound to the period when the service is
     * connected and cancel it when no longer connected. The service will be passed to the block.
     */
    fun launchWhenConnected(
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.(ExerciseService) -> Unit
    ): Job {
        return lifecycleScope.launch(coroutineContext) {
            lifecycle.whenStarted {
                block(exerciseService as ExerciseService)
            }
        }
    }
}
