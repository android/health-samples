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

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.passivegoals.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Activity displaying the app UI. Notably, this binds data from [MainViewModel] to views on screen,
 * and performs the permission check when enabling passive goals.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private val viewModel: MainViewModel by viewModels()

    private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Log.i(TAG, "Activity recognition permission granted")
                    viewModel.togglePassiveGoals(true)
                } else {
                    Log.i(TAG, "Activity recognition permission not granted")
                    viewModel.togglePassiveGoals(false)
                }
            }

        binding.enablePassiveGoals.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Make sure we have the necessary permission first.
                if (ContextCompat.checkSelfPermission(
                    this, 
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                } else {
                    viewModel.togglePassiveGoals(true)
                }
            } else {
                viewModel.togglePassiveGoals(false)
            }
        }

        // Bind viewmodel state to the UI.
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // As collect is a suspend function, if you want to collect
                // multiple flows in parallel, you need to do so in
                // different coroutines
                launch {
                    viewModel.uiState.collect {
                        updateViewVisibility(it)
                    }
                }
                launch {
                    viewModel.passiveGoalsEnabled.collect {
                        binding.enablePassiveGoals.isChecked = it
                    }
                }
                launch {
                    viewModel.dailyStepsGoalAchieved.collect {
                        updateDailyStepsGoal(it)
                    }
                }
                launch {
                    viewModel.latestFloorsGoalTime.collect {
                        updateFloorsGoal(it)
                    }
                }
            }
        }
    }

    private fun updateViewVisibility(uiState: UiState) {
        (uiState is UiState.Startup).let {
            binding.progress.isVisible = it
        }
        // These views are visible when steps and floors capabilities are not available.
        (uiState is UiState.CapabilitiesNotAvailable).let {
            binding.notAvailableIcon.isVisible = it
            binding.notAvailableText.isVisible = it
        }
        // These views are visible when the capability is available.
        (uiState is UiState.CapabilitiesAvailable).let {
            binding.enablePassiveGoals.isVisible = it
            binding.dailyStepsIcon.isVisible = it
            binding.dailyStepsText.isVisible = it

            binding.floorsIcon.isVisible = it
            binding.floorsText.isVisible = it
        }
    }

    private fun updateFloorsGoal(time: Instant) {
        val iconTint = if (time.toEpochMilli() == 0L)
            Color.GRAY
        else
            resources.getColor(R.color.primary_green, null)
        binding.floorsIcon.imageTintList = ColorStateList.valueOf(iconTint)
        binding.floorsText.text = if (time.toEpochMilli() == 0L)
            getString(R.string.waiting_for_goal)
        else
            getString(R.string.floor_goal, timeFormatter.format(time))
    }

    private fun updateDailyStepsGoal(isAchieved: Boolean) {
        val iconTint = if (isAchieved)
            resources.getColor(R.color.primary_green, null)
        else
            Color.GRAY
        binding.dailyStepsIcon.imageTintList = ColorStateList.valueOf(iconTint)
        binding.dailyStepsText.text = if (isAchieved) {
            getString(R.string.daily_steps_goal_achieved)
        } else {
            getString(R.string.daily_steps_goal_not_achieved)
        }
    }
}
