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

package com.example.passiveevents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: PassiveEventsRepository,
    private val healthServicesManager: HealthServicesManager
): ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Startup)
    // Presents a non-mutable view of _uiState for observers.
    val uiState: StateFlow<UiState> = _uiState
    val passiveEventsEnabled = repository.passiveEventsEnabled
    val latestEvent = repository.latestEvent

    init {
        // Check that the device has the steps per minute capability and progress to the next state
        // accordingly.
        viewModelScope.launch {
            _uiState.value = if (healthServicesManager.hasStepsPerMinuteCapability()) {
                UiState.StepsPerMinuteAvailable
            } else {
                UiState.StepsPerMinuteNotAvailable
            }
        }
    }

    fun togglePassiveEvents(enabled: Boolean) {
        viewModelScope.launch {
            if (passiveEventsEnabled.first() != enabled) {
                when (enabled) {
                    true -> healthServicesManager.subscribeForEvents()
                    false -> healthServicesManager.unsubscribeFromEvents()
                }
                repository.setPassiveEventsEnabled(enabled)
            }
        }
    }
}

sealed class UiState {
    object Startup: UiState()
    object StepsPerMinuteAvailable: UiState()
    object StepsPerMinuteNotAvailable: UiState()
}
