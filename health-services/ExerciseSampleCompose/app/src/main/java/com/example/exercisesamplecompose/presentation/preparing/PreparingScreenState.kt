package com.example.exercisesamplecompose.presentation.preparing

import androidx.health.services.client.data.LocationAvailability
import com.example.exercisesamplecompose.data.ServiceState

sealed class PreparingScreenState {
    abstract val serviceState: ServiceState
    abstract val isTrackingAnotherExercise: Boolean
    abstract val requiredPermissions: List<String>

    data class Disconnected(
        override val serviceState: ServiceState.Disconnected,
        override val isTrackingAnotherExercise: Boolean,
        override val requiredPermissions: List<String>
    ) : PreparingScreenState()

    data class Preparing(
        override val serviceState: ServiceState.Connected,
        override val isTrackingAnotherExercise: Boolean,
        override val requiredPermissions: List<String>,
        val hasExerciseCapabilities: Boolean
    ) : PreparingScreenState() {
        val locationAvailability: LocationAvailability =
            (serviceState as? ServiceState.Connected)?.locationAvailabilityState
                ?: LocationAvailability.UNKNOWN
    }
}