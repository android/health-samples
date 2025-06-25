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
package com.example.exercisesamplecompose.presentation.preparing

import androidx.health.services.client.data.LocationAvailability
import com.example.exercisesamplecompose.data.ServiceState

sealed class PreparingScreenState {
    abstract val serviceState: ServiceState
    abstract val isTrackingInAnotherApp: Boolean
    abstract val requiredPermissions: List<String>

    data class Disconnected(
        override val serviceState: ServiceState.Disconnected,
        override val isTrackingInAnotherApp: Boolean,
        override val requiredPermissions: List<String>
    ) : PreparingScreenState()

    data class Preparing(
        override val serviceState: ServiceState.Connected,
        override val isTrackingInAnotherApp: Boolean,
        override val requiredPermissions: List<String>,
        val hasExerciseCapabilities: Boolean
    ) : PreparingScreenState() {
        val locationAvailability: LocationAvailability =
            (serviceState as? ServiceState.Connected)?.locationAvailabilityState
                ?: LocationAvailability.UNKNOWN
    }
}
