/*
 * Copyright 2021 The Android Open Source Project
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

package com.example.passivegoals

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: PassiveGoalsRepository,
    private val healthServicesManager: HealthServicesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Startup)

    // Presents a non-mutable view of _uiState for observers.
    val uiState: StateFlow<UiState> = _uiState
    val passiveGoalsEnabled: Flow<Boolean>
    val dailyStepsGoalAchieved = repository.dailyStepsGoalAchieved
    val latestFloorsGoalTime = repository.latestFloorsGoalTime

    init {
        // Check that the device has the steps per minute capability and progress to the next state
        // accordingly.
        viewModelScope.launch {
            _uiState.value = if (healthServicesManager.hasFloorsAndDailyStepsCapability()) {
                UiState.CapabilitiesAvailable
            } else {
                UiState.CapabilitiesNotAvailable
            }
        }

        passiveGoalsEnabled = repository.passiveGoalsEnabled.distinctUntilChanged()

        viewModelScope.launch {
            passiveGoalsEnabled.collect { enabled ->
                if (enabled) {
                    if (!healthServicesManager.subscribeForGoals()) {
                        _uiState.value = UiState.CapabilitiesNotAvailable
                    }
                } else {
                    healthServicesManager.unsubscribeFromGoals()
                }
            }
        }
    }

    fun togglePassiveGoals(enabled: Boolean) {
        viewModelScope.launch {
            repository.setPassiveGoalsEnabled(enabled)
        }
    }
}

sealed class UiState {
    object Startup : UiState()
    object CapabilitiesAvailable : UiState()
    object CapabilitiesNotAvailable : UiState()
}