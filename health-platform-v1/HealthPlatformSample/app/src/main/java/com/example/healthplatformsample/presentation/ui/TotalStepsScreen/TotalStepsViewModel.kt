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
package com.example.healthplatformsample.presentation.ui.TotalStepsScreen

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthplatformsample.data.HealthPlatformManager
import com.google.android.libraries.healthdata.data.HealthDataException
import kotlinx.coroutines.launch
import java.util.UUID

class TotalStepsViewModel(private val healthPlatformManager: HealthPlatformManager) : ViewModel() {
    private val _totalSteps: MutableState<Long> = mutableStateOf(0L)
    val totalSteps: State<Long> = _totalSteps

    var uiState: UiState by mutableStateOf(UiState.Loading)
        private set

    init {
        readTotalSteps()
    }

    fun readTotalSteps() {
        viewModelScope.launch {
            tryHealthOperation {
                uiState = UiState.Loading
                val totalSteps = healthPlatformManager.readTotalSteps()
                _totalSteps.value = totalSteps
                uiState = UiState.Success
            }
        }
    }

    private inline fun tryHealthOperation(block: () -> Unit) {
        try {
            block()
        } catch (healthDataException: HealthDataException) {
            uiState = UiState.Error(healthDataException)
        } catch (illegalStateException: java.lang.IllegalStateException) {
            uiState = UiState.Error(illegalStateException)
        }
    }

    sealed class UiState {
        object Loading : UiState()
        object Success : UiState()
        data class Error(val exception: Throwable, val uuid: UUID = UUID.randomUUID()) : UiState()
    }
}

class TotalStepsViewModelFactory(
    private val healthPlatformManager: HealthPlatformManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TotalStepsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TotalStepsViewModel(
                healthPlatformManager = healthPlatformManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
