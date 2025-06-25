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
package com.example.exercisesamplecompose.presentation.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.exercisesamplecompose.app.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow

@HiltViewModel
class SummaryViewModel
@Inject
constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val uiState =
        MutableStateFlow(
            SummaryScreenState(
                averageHeartRate =
                savedStateHandle
                    .get<Float>(Screen.Summary.averageHeartRateArg)!!
                    .toDouble(),
                totalDistance =
                savedStateHandle
                    .get<Float>(Screen.Summary.totalDistanceArg)!!
                    .toDouble(),
                totalCalories =
                savedStateHandle
                    .get<Float>(Screen.Summary.totalCaloriesArg)!!
                    .toDouble(),
                elapsedTime =
                Duration.parse(
                    savedStateHandle.get(Screen.Summary.elapsedTimeArg)!!
                )
            )
        )
}
