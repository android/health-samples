/*
 * Copyright 2022 The Android Open Source Project
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
package com.example.passivegoalscompose.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passivegoalscompose.data.HealthServicesRepository
import com.example.passivegoalscompose.data.PassiveGoalsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant

class PassiveGoalsViewModel(
    private val healthServicesRepository: HealthServicesRepository,
    private val passiveGoalsRepository: PassiveGoalsRepository
) : ViewModel() {
    val latestFloorsTime = passiveGoalsRepository.latestFloorsGoalTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Instant.EPOCH)

    val stepsGoalAchieved = passiveGoalsRepository.dailyStepsGoalAchieved
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val goalsEnabled = passiveGoalsRepository.passiveGoalsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val uiState: MutableState<UiState> = mutableStateOf(UiState.Startup)

    init {
        viewModelScope.launch {
            val supported = healthServicesRepository.hasFloorsAndDailyStepsCapability()
            uiState.value = if (supported) {
                UiState.Supported
            } else {
                UiState.NotSupported
            }
        }

        viewModelScope.launch {
            passiveGoalsRepository.passiveGoalsEnabled.distinctUntilChanged().collect { enabled ->
                if (enabled) {
                    healthServicesRepository.subscribeForGoals()
                } else {
                    healthServicesRepository.unsubscribeFromGoals()
                }
            }
        }
    }

    fun toggleEnabled() {
        viewModelScope.launch {
            val newEnabledStatus = !goalsEnabled.value
            passiveGoalsRepository.setPassiveGoalsEnabled(newEnabledStatus)
        }
    }
}

class PassiveGoalsViewModelFactory(
    private val healthServicesRepository: HealthServicesRepository,
    private val passiveGoalsRepository: PassiveGoalsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PassiveGoalsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PassiveGoalsViewModel(
                healthServicesRepository = healthServicesRepository,
                passiveGoalsRepository = passiveGoalsRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class UiState {
    object Startup : UiState()
    object NotSupported : UiState()
    object Supported : UiState()
}
