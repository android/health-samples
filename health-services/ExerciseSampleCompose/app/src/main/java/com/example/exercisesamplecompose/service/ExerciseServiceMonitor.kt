package com.example.exercisesamplecompose.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.ContentValues
import android.util.Log
import androidx.health.services.client.data.ExerciseEndReason
import androidx.health.services.client.data.ExerciseState
import androidx.health.services.client.data.ExerciseUpdate
import com.example.exercisesamplecompose.data.ExerciseClientManager
import com.example.exercisesamplecompose.data.ExerciseMessage
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import javax.inject.Inject
import kotlin.time.toKotlinDuration

class ExerciseServiceMonitor @Inject constructor(
    val exerciseClientManager: ExerciseClientManager,
    val service: Service
) {
    // TODO behind an interface
    val exerciseService = service as ExerciseService

    val exerciseServiceState = MutableStateFlow(ExerciseServiceState())

    suspend fun monitor() {
        println("monitor")
        exerciseClientManager.exerciseUpdateFlow.collect {
            println(it)
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
        val oldState = exerciseServiceState.value.exerciseState
        if (!oldState.isEnded && exerciseUpdate.exerciseStateInfo.state.isEnded) {
            // Our exercise ended. Gracefully handle this termination be doing the following:
            // TODO Save partial workout state, show workout summary, and let the user know why the exercise was ended.

            // Dismiss any ongoing activity notification.
            exerciseService.removeOngoingActivityNotification()

            // Custom flow for the possible states captured by the isEnded boolean
            when (exerciseUpdate.exerciseStateInfo.endReason) {
                ExerciseEndReason.AUTO_END_SUPERSEDED -> {
                    // TODO Send the user a notification (another app ended their workout)
                    Log.i(
                        ContentValues.TAG,
                        "Your exercise was terminated because another app started tracking an exercise"
                    )
                }

                ExerciseEndReason.AUTO_END_MISSING_LISTENER -> {

                    // TODO Send the user a notification
                    Log.i(
                        ContentValues.TAG,
                        "Your exercise was auto ended because there were no registered listeners"
                    )
                }

                ExerciseEndReason.AUTO_END_PERMISSION_LOST -> {

                    // TODO Send the user a notification
                    Log.w(
                        ContentValues.TAG,
                        "Your exercise was auto ended because it lost the required permissions"
                    )
                }

                else -> {
                }
            }
        } else if (oldState.isEnded && exerciseUpdate.exerciseStateInfo.state == ExerciseState.ACTIVE) {
            // Reset laps.
            exerciseServiceState.update { it.copy(exerciseLaps = 0) }
        }

        // If the state of the exercise changes, then update the ExerciseStateChange object. Change
        // in this state then causes recomposition, which can be used to start or stop a coroutine
        // in the screen for updating the timer.
        if (oldState != exerciseUpdate.exerciseStateInfo.state) {
            exerciseServiceState.update {
                it.copy(
                    exerciseStateChange = when (exerciseUpdate.exerciseStateInfo.state) {
                        // ActiveStateChange also takes an ActiveDurationCheckpoint, so that when the ticker
                        // is started in the screen, the base Duration can be set correctly.
                        ExerciseState.ACTIVE -> ExerciseStateChange.ActiveStateChange(
                            exerciseUpdate.activeDurationCheckpoint!!
                        )

                        else -> ExerciseStateChange.OtherStateChange(exerciseUpdate.exerciseStateInfo.state)
                    }
                )
            }
        }
        exerciseServiceState.update { it ->
            it.copy(exerciseState = exerciseUpdate.exerciseStateInfo.state,
                exerciseMetrics = exerciseUpdate.latestMetrics,
                exerciseDurationUpdate = exerciseUpdate.activeDurationCheckpoint?.let {
                    ActiveDurationUpdate(
                        it.activeDuration,
                        Instant.now()
                    )
                })
        }
        exerciseServiceState.update {
            it.copy(lastActiveDurationCheckpoint = exerciseUpdate.activeDurationCheckpoint)
        }
    }
}