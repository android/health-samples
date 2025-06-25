/*
 * Copyright 2025 The Android Open Source Project
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
package com.example.exercisesamplecompose.di

import android.app.Service
import android.content.Context
import android.os.IBinder
import com.google.android.horologist.health.service.BinderConnection
import dagger.hilt.android.ActivityRetainedLifecycle

inline fun <reified T : IBinder, reified S : Service> ActivityRetainedLifecycle.bindService(
    context: Context
): BinderConnection<T> {
    val connection = BinderConnection(context, T::class)
    BinderConnection.bindService(context, S::class, connection)
    addOnClearedListener {
        connection.unbind()
    }
    return connection
}
