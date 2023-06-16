package com.example.exercisesamplecompose.presentation.exercise

import androidx.health.services.client.data.DataType.Companion.CALORIES_TOTAL
import androidx.health.services.client.data.DataType.Companion.DISTANCE_TOTAL
import androidx.health.services.client.data.DataType.Companion.HEART_RATE_BPM
import androidx.health.services.client.data.DataType.Companion.HEART_RATE_BPM_STATS
import com.example.exercisesamplecompose.data.ServiceState
import com.example.exercisesamplecompose.presentation.summary.SummaryScreenState
import com.example.exercisesamplecompose.service.ActiveDurationUpdate
import com.example.exercisesamplecompose.service.ExerciseServiceState
import java.time.Duration

data class ExerciseScreenState(
    val hasExerciseCapabilities: Boolean = true,
    val isTrackingAnotherExercise: Boolean = false,
    val serviceState: ServiceState,
    val exerciseState: ExerciseServiceState? = null
) {
    fun toSummary(): SummaryScreenState {
        // TODO implement
//        //In a production fitness app, you might upload workout metrics to your app
//        // either via network connection or to your mobile app via the Data Layer API.
//        navController.navigate(
//            Screen.Summary.route + "/${tempAverageHeartRate.value}/${
//                formatDistanceKm(
//                    tempDistance.value
//                )
//            }/${formatCalories(tempCalories.value)}/" + formatElapsedTime(
//                activeDuration.toKotlinDuration(), true
//            ).toString()
//        ) {
//            popUpTo(Screen.Exercise.route) { inclusive = true }
//        }
        return SummaryScreenState(0.0, 0.0, 0.0, Duration.ofSeconds(0))
    }

    val exerciseLaps: Int?
        get() = exerciseState?.exerciseLaps

    val elapsedTime: ActiveDurationUpdate?
        get() = exerciseState?.exerciseDurationUpdate

    val heartRate: Double?
        get() = exerciseState?.exerciseMetrics?.getData(HEART_RATE_BPM)?.last()?.value

    val distance: Double?
        get() = exerciseState?.exerciseMetrics?.getData(DISTANCE_TOTAL)?.total

    val calories: Double?
        get() = exerciseState?.exerciseMetrics?.getData(CALORIES_TOTAL)?.total

    val heartRateAverage: Double?
        get() = exerciseState?.exerciseMetrics?.getData(HEART_RATE_BPM_STATS)?.average

    val isEnding: Boolean
        get() = exerciseState?.exerciseState?.isEnding == true
    val isPaused: Boolean
        get() = exerciseState?.exerciseState?.isPaused == true
}