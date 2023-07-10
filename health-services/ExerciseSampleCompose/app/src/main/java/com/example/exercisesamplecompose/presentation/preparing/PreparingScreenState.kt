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