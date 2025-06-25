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
package com.example.exercisesamplecompose.presentation.goals

import androidx.compose.runtime.State
import androidx.compose.runtime.asDoubleState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.exercisesamplecompose.data.ExerciseClientManager
import com.example.exercisesamplecompose.data.Thresholds
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExerciseGoalsViewModel
@Inject
constructor(
    private val exerciseClientManager: ExerciseClientManager
) : ViewModel() {
    private val _distanceGoal = mutableDoubleStateOf(0.0)
    val distanceGoal: State<Double> = _distanceGoal.asDoubleState()

    private val _durationGoal = mutableStateOf(kotlin.time.Duration.ZERO)
    val durationGoal: State<kotlin.time.Duration> = _durationGoal

    // Set new goal thresholds and update the values in ExerciseClientManager
    fun setGoals(thresholds: Thresholds) {
        _distanceGoal.value = thresholds.distance
        _durationGoal.value = thresholds.duration
        exerciseClientManager.updateGoals(thresholds)
    }
}
