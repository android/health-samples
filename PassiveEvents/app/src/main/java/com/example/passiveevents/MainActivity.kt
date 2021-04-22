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

import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.passiveevents.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Activity displaying the app UI. Notably, this binds data from [MainViewModel] to views on screen,
 * and performs the permission check when enabling passive events.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private val viewModel: MainViewModel by viewModels()

    private val timeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                when (result) {
                    true -> {
                        Log.i(TAG, "Activity recognition permission granted")
                        viewModel.togglePassiveEvents(true)
                    }
                    false -> {
                        Log.i(TAG, "Activity recognition permission not granted")
                        viewModel.togglePassiveEvents(false)
                    }
                }
            }

        binding.enablePassiveEvents.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Make sure we have the necessary permission first.
                permissionLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
            } else {
                viewModel.togglePassiveEvents(false)
            }
        }

        // Bind viewmodel state to the UI.
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect {
                updateViewVisiblity(it)
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.passiveEventsEnabled.collect {
                binding.enablePassiveEvents.isChecked = it
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.latestEvent.collect {
                displayEvent(it)
            }
        }
    }

    private fun updateViewVisiblity(uiState: UiState) {
        (uiState is UiState.Startup).let {
            binding.progress.isVisible = it
        }
        // These views are visible when heart rate capability is not available.
        (uiState is UiState.StepsPerMinuteNotAvailable).let {
            binding.notAvailableIcon.isVisible = it
            binding.notAvailableText.isVisible = it
        }
        // These views are visible when the capability is available.
        (uiState is UiState.StepsPerMinuteAvailable).let {
            binding.enablePassiveEvents.isVisible = it
            binding.eventIcon.isVisible = it
            binding.eventText.isVisible = it
        }
    }

    private fun displayEvent(event: PassiveEvent) {
        binding.eventIcon.setImageResource(when (event.type) {
            PassiveEventType.UNKNOWN -> R.drawable.ic_waiting_dots
            PassiveEventType.FAST_STEPS_PER_MINUTE -> R.drawable.ic_trending_up
            PassiveEventType.SLOW_STEPS_PER_MINUTE -> R.drawable.ic_trending_down
        })
        binding.eventText.text = when (event.type) {
            PassiveEventType.UNKNOWN -> getString(R.string.waiting_for_event)
            PassiveEventType.FAST_STEPS_PER_MINUTE ->
                getString(R.string.fast_steps_event, timeFormatter.format(event.timestamp))
            PassiveEventType.SLOW_STEPS_PER_MINUTE ->
                getString(R.string.slow_steps_event, timeFormatter.format(event.timestamp))
        }
    }
}
