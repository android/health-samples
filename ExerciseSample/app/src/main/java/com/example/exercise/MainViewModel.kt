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

package com.example.exercise

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Coordinates messages between [MainActivity] and [ExerciseFragment].
 */
@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _ambientEventChannel = Channel<AmbientEvent>(capacity = Channel.CONFLATED)
    val ambientEventFlow = _ambientEventChannel.receiveAsFlow()

    private val _keyPressChannel = Channel<Unit>(capacity = Channel.CONFLATED)
    val keyPressFlow = _keyPressChannel.receiveAsFlow()

    fun sendAmbientEvent(event: AmbientEvent) {
        viewModelScope.launch {
            _ambientEventChannel.send(event)
        }
    }

    fun sendKeyPress() {
        viewModelScope.launch {
            _keyPressChannel.send(Unit)
        }
    }
}

sealed class AmbientEvent {
    class Enter(val ambientDetails: Bundle) : AmbientEvent()
    object Exit : AmbientEvent()
    object Update : AmbientEvent()
}
