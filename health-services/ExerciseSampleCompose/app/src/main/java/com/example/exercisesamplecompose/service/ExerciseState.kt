package com.example.exercisesamplecompose.service

import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.ExerciseState
import androidx.health.services.client.data.ExerciseUpdate.ActiveDurationCheckpoint
import androidx.health.services.client.data.LocationAvailability
import java.time.Duration
import java.time.Instant


/** Keeps track of the last time we received an update for active exercise duration. */
data class ActiveDurationUpdate(
    /** The last active duration reported. */
    val duration: Duration = Duration.ZERO,

    /** The instant at which the last duration was reported. */
    val timestamp: Instant = Instant.now()
)

sealed class ExerciseStateChange(val exerciseState: ExerciseState) {
    data class ActiveStateChange(val durationCheckPoint: ActiveDurationCheckpoint) :
        ExerciseStateChange(
            ExerciseState.ACTIVE
        )

    data class OtherStateChange(val state: ExerciseState) : ExerciseStateChange(state)
}

//Capturing most of the values associated with our exercise in a data class
data class ExerciseServiceState(
    val exerciseState: ExerciseState = ExerciseState.ENDED,
    val exerciseMetrics: DataPointContainer? = null,
    val exerciseLaps: Int = 0,
    val exerciseDurationUpdate: ActiveDurationUpdate? = null,
    val exerciseStateChange: ExerciseStateChange = ExerciseStateChange.OtherStateChange(
        ExerciseState.ENDED
    ),
    val lastActiveDurationCheckpoint: ActiveDurationCheckpoint? = null,
    val locationAvailability: LocationAvailability = LocationAvailability.UNKNOWN
)