package com.example.exercisesamplecompose.presentation.preparing

import androidx.health.services.client.data.LocationAvailability
import com.example.exercisesamplecompose.data.ServiceState

data class PreparingScreenState(
    val serviceState: ServiceState,
    val isTrackingAnotherExercise: Boolean,
    val requiredPermissions: List<String>,
    val hasExerciseCapabilities: Boolean
) {
    val locationAvailability: LocationAvailability =
        (serviceState as? ServiceState.Connected)?.locationAvailabilityState
            ?: LocationAvailability.UNKNOWN
}