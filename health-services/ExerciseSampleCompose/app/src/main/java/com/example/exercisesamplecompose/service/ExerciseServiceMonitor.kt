package com.example.exercisesamplecompose.service

import android.annotation.SuppressLint
import android.app.Service
import androidx.health.services.client.data.ExerciseUpdate
import com.example.exercisesamplecompose.data.ExerciseClientManager
import com.example.exercisesamplecompose.data.ExerciseMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class ExerciseServiceMonitor @Inject constructor(
    val exerciseClientManager: ExerciseClientManager,
    val service: Service
) {
    // TODO behind an interface
    val exerciseService = service as ExerciseService

    val exerciseServiceState = MutableStateFlow(
        ExerciseServiceState(
            exerciseState = null,
            exerciseMetrics = ExerciseMetrics()
        )
    )

    suspend fun monitor() {
        exerciseClientManager.exerciseUpdateFlow.collect {
            when (it) {
                is ExerciseMessage.ExerciseUpdateMessage ->
                    processExerciseUpdate(it.exerciseUpdate)

                is ExerciseMessage.LapSummaryMessage ->
                    exerciseServiceState.update { oldState ->
                        oldState.copy(
                            exerciseLaps = it.lapSummary.lapCount
                        )
                    }

                is ExerciseMessage.LocationAvailabilityMessage ->
                    exerciseServiceState.update { oldState ->
                        oldState.copy(
                            locationAvailability = it.locationAvailability
                        )
                    }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun processExerciseUpdate(exerciseUpdate: ExerciseUpdate) {
        // Dismiss any ongoing activity notification.
        if (exerciseUpdate.exerciseStateInfo.state.isEnded) {
            exerciseService.removeOngoingActivityNotification()
        }

        exerciseServiceState.update { old ->
            old.copy(
                exerciseState = exerciseUpdate.exerciseStateInfo.state,
                exerciseMetrics = old.exerciseMetrics.update(exerciseUpdate.latestMetrics),
                activeDurationCheckpoint = exerciseUpdate.activeDurationCheckpoint
                    ?: old.activeDurationCheckpoint,
                exerciseGoal = exerciseUpdate.latestAchievedGoals
            )
        }
    }
}