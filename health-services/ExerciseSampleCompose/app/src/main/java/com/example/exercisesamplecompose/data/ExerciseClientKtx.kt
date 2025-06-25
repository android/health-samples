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

fun supportsDurationMilestone(capabilities: ExerciseTypeCapabilities): Boolean {
    val supported = capabilities.supportedGoals[DataType.ACTIVE_EXERCISE_DURATION_TOTAL]
    return supported != null && ComparisonType.GREATER_THAN_OR_EQUAL in supported
}
