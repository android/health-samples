@file:SuppressLint("RestrictedApi")

package com.example.exercisesamplecompose.data

import android.annotation.SuppressLint
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.data.ComparisonType
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseTrackedStatus
import androidx.health.services.client.data.ExerciseTypeCapabilities
import androidx.health.services.client.getCurrentExerciseInfo


suspend fun ExerciseClient.isExerciseInProgress(): Boolean {
    val exerciseInfo = getCurrentExerciseInfo()
    return exerciseInfo.exerciseTrackedStatus == ExerciseTrackedStatus.OWNED_EXERCISE_IN_PROGRESS
}

suspend fun ExerciseClient.isTrackingExerciseInAnotherApp(): Boolean {
    val exerciseInfo = getCurrentExerciseInfo()
    return exerciseInfo.exerciseTrackedStatus == ExerciseTrackedStatus.OTHER_APP_IN_PROGRESS
}

fun supportsCalorieGoal(capabilities: ExerciseTypeCapabilities): Boolean {
    val supported = capabilities.supportedGoals[DataType.CALORIES_TOTAL]
    return supported != null && ComparisonType.GREATER_THAN_OR_EQUAL in supported
}

fun supportsDistanceMilestone(capabilities: ExerciseTypeCapabilities): Boolean {
    val supported = capabilities.supportedMilestones[DataType.DISTANCE_TOTAL]
    return supported != null && ComparisonType.GREATER_THAN_OR_EQUAL in supported
}

fun supportsDurationMilestone(capabilities: ExerciseTypeCapabilities) : Boolean{
    val supported = capabilities.supportedGoals[DataType.ACTIVE_EXERCISE_DURATION_TOTAL]
    return supported != null && ComparisonType.GREATER_THAN_OR_EQUAL in supported
}