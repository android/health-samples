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
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope

/**
 * ServiceConnection implementation. This is implemented as a LifecycleOwner so we can launch
 * coroutines bound to its lifecycle. When the service is connected, the lifecycle will be
 * [Lifecycle.State.STARTED]; otherwise it will be [Lifecycle.State.INITIALIZED].
 */
class ExerciseServiceConnection : ServiceConnection, LifecycleOwner {

    var exerciseService: ExerciseService? = null

    override val lifecycle = LifecycleRegistry(this).apply {
        currentState = Lifecycle.State.INITIALIZED
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        exerciseService = (service as ExerciseService.LocalBinder).getService()
        lifecycle.currentState = Lifecycle.State.STARTED
    }

    override fun onServiceDisconnected(name: ComponentName) {
        lifecycle.currentState = Lifecycle.State.INITIALIZED
    }

    /**
     * Runs the given [block] in a new coroutine when the service is connected and suspends the
     * execution until this Lifecycle is [Lifecycle.State.DESTROYED]. The [block] will cancel and
     * re-launch as the service becomes connected or disconnected. The connected service is passed
     * to the [block] so that clients can interact with it.
     */
    suspend fun repeatWhenConnected(block: suspend CoroutineScope.(ExerciseService) -> Unit) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            block(exerciseService as ExerciseService)
        }
    }
}
