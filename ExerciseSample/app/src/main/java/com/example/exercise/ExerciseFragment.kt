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

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.health.services.client.data.DataPoint
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseState
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.wear.ambient.AmbientModeSupport
import com.example.exercise.databinding.FragmentExerciseBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Fragment showing the exercise controls and current exercise metrics.
 */
@AndroidEntryPoint
class ExerciseFragment : Fragment() {

    @Inject
    lateinit var healthServicesManager: HealthServicesManager

    private val viewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentExerciseBinding? = null
    private val binding get() = _binding!!

    private var serviceConnection = ExerciseServiceConnection()

    private var cachedExerciseState = ExerciseState.USER_ENDED
    private var activeDurationUpdate = ActiveDurationUpdate()
    private var chronoTickJob: Job? = null
    private var uiBindingJob: Job? = null

    private lateinit var ambientController: AmbientModeSupport.AmbientController
    private lateinit var ambientModeHandler: AmbientModeHandler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.startEndButton.setOnClickListener {
            startEndExercise()
        }
        binding.pauseResumeButton.setOnClickListener {
            pauseResumeExercise()
        }

        lifecycleScope.launchWhenCreated {
            val capabilities =
                healthServicesManager.getExerciseCapabilities() ?: return@launchWhenCreated
            val supportedTypes = capabilities.supportedDataTypes

            // Set enabled state for relevant text elements.
            binding.heartRateText.isEnabled = DataType.HEART_RATE_BPM in supportedTypes
            binding.caloriesText.isEnabled = DataType.TOTAL_CALORIES in supportedTypes
            binding.distanceText.isEnabled = DataType.DISTANCE in supportedTypes
            binding.lapsText.isEnabled = true
        }

        lifecycleScope.launchWhenStarted {
            viewModel.keyPressFlow.collect {
                healthServicesManager.markLap()
            }
        }

        // Ambient Mode
        ambientModeHandler = AmbientModeHandler()
        ambientController = AmbientModeSupport.attach(requireActivity())
        lifecycleScope.launchWhenStarted {
            viewModel.ambientEventFlow.collect {
                ambientModeHandler.onAmbientEvent(it)
            }
        }

        // Bind to our service. Views will only update once we are connected to it.
        val serviceIntent = Intent(requireContext(), ExerciseService::class.java)
        requireContext().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        bindViewsToService()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Unbind from the service.
        requireActivity().unbindService(serviceConnection)
        _binding = null
    }

    private fun startEndExercise() {
        if (cachedExerciseState.isEnded) {
            tryStartExercise()
        } else {
            lifecycleScope.launch {
                healthServicesManager.endExercise()
            }
        }
    }

    private fun tryStartExercise() {
        lifecycleScope.launchWhenStarted {
            if (healthServicesManager.isTrackingExerciseInAnotherApp()) {
                // Show the user a confirmation screen.
                findNavController().navigate(R.id.to_newExerciseConfirmation)
            } else if (!healthServicesManager.isExerciseInProgress()) {
                healthServicesManager.startExercise()
            }
        }
    }

    private fun pauseResumeExercise() {
        if (cachedExerciseState.isPaused) {
            lifecycleScope.launch {
                healthServicesManager.resumeExercise()
            }
        } else {
            lifecycleScope.launch {
                healthServicesManager.pauseExercise()
            }
        }
    }

    private fun bindViewsToService() {
        if (uiBindingJob != null) return

        uiBindingJob = lifecycleScope.launchWhenStarted {
            serviceConnection.repeatWhenConnected { service ->
                // Use separate launch blocks because each .collect executes indefinitely.
                launch {
                    service.exerciseState.collect {
                        updateExerciseStatus(it)
                    }
                }
                launch {
                    service.exerciseMetrics.collect {
                        updateMetrics(it)
                    }
                }
                launch {
                    service.exerciseLaps.collect {
                        updateLaps(it)
                    }
                }
                launch {
                    service.exerciseDurationUpdate.collect {
                        // We don't update the chronometer here since these updates come at irregular
                        // intervals. Instead we store the duration and update the chronometer with
                        // our own regularly-timed intervals.
                        activeDurationUpdate = it
                    }
                }
            }
        }
    }

    private fun unbindViewsFromService() {
        uiBindingJob?.cancel()
        uiBindingJob = null
    }

    private fun updateExerciseStatus(state: ExerciseState) {
        val previousStatus = cachedExerciseState
        if (previousStatus.isEnded && !state.isEnded) {
            // We're starting a new exercise. Clear metrics from any prior exercise.
            resetDisplayedFields()
        }

        if (state == ExerciseState.ACTIVE && !ambientController.isAmbient) {
            startChronometer()
        } else {
            stopChronometer()
        }

        updateButtons(state)
        cachedExerciseState = state
    }

    private fun updateButtons(state: ExerciseState) {
        binding.startEndButton.setText(if (state.isEnded) R.string.start else R.string.end)
        binding.pauseResumeButton.setText(if (state.isPaused) R.string.resume else R.string.pause)
        binding.pauseResumeButton.isEnabled = !state.isEnded
    }

    private fun updateMetrics(data: Map<DataType, List<DataPoint>>) {
        data[DataType.HEART_RATE_BPM]?.let {
            binding.heartRateText.text = it.last().value.asDouble().roundToInt().toString()
        }
        data[DataType.TOTAL_CALORIES]?.let {
            binding.caloriesText.text = formatCalories(it.last().value.asDouble())
        }
        data[DataType.DISTANCE]?.let {
            binding.distanceText.text = formatDistanceKm(it.last().value.asDouble())
        }
    }

    private fun updateLaps(laps: Int) {
        binding.lapsText.text = laps.toString()
    }

    private fun startChronometer() {
        if (chronoTickJob == null) {
            chronoTickJob = lifecycleScope.launchWhenStarted {
                while (true) {
                    delay(CHRONO_TICK_MS)
                    updateChronometer()
                }
            }
        }
    }

    private fun stopChronometer() {
        chronoTickJob?.cancel()
        chronoTickJob = null
    }

    private fun updateChronometer() {
        // We update the chronometer on our own regular intervals independent of the exercise
        // duration value received. If the exercise is still active, add the difference between
        // the last duratoin update and now.
        val difference = if (cachedExerciseState == ExerciseState.ACTIVE) {
            Duration.between(activeDurationUpdate.timestamp, Instant.now())
        } else {
            Duration.ZERO
        }
        val duration = activeDurationUpdate.duration + difference
        binding.elapsedTime.text = formatElapsedTime(duration, !ambientController.isAmbient)
    }

    private fun resetDisplayedFields() {
        getString(R.string.empty_metric).let {
            binding.heartRateText.text = it
            binding.caloriesText.text = it
            binding.distanceText.text = it
            binding.lapsText.text = it
        }
        binding.elapsedTime.text = formatElapsedTime(Duration.ZERO, true)
    }

    // -- Ambient Mode support

    private fun setAmbientUiState(isAmbient: Boolean) {
        // Change icons to white while in ambient mode.
        val iconTint = if (isAmbient) {
            Color.WHITE
        } else {
            resources.getColor(R.color.primary_orange, null)
        }
        ColorStateList.valueOf(iconTint).let {
            binding.clockIcon.imageTintList = it
            binding.heartRateIcon.imageTintList = it
            binding.caloriesIcon.imageTintList = it
            binding.distanceIcon.imageTintList = it
            binding.lapsIcon.imageTintList = it
        }

        // Hide the buttons in ambient mode.
        val buttonVisibility = if (isAmbient) View.INVISIBLE else View.VISIBLE
        buttonVisibility.let {
            binding.startEndButton.visibility = it
            binding.pauseResumeButton.visibility = it
        }
    }

    private fun performOneTimeUiUpdate() {
        serviceConnection.exerciseService?.let { service ->
            updateExerciseStatus(service.exerciseState.value)
            updateMetrics(service.exerciseMetrics.value)
            updateLaps(service.exerciseLaps.value)

            activeDurationUpdate = service.exerciseDurationUpdate.value
            updateChronometer()
        }
    }


    inner class AmbientModeHandler {

        internal fun onAmbientEvent(event: AmbientEvent) {
            when (event) {
                is AmbientEvent.Enter -> onEnterAmbient(event.ambientDetails)
                is AmbientEvent.Exit -> onExitAmbient()
                is AmbientEvent.Update -> onUpdateAmbient()
            }
        }

        private fun onEnterAmbient(ambientDetails: Bundle) {
            // Note: Apps should also handle low-bit ambient and burn-in protection.
            unbindViewsFromService()
            setAmbientUiState(true)
            performOneTimeUiUpdate()
        }

        private fun onExitAmbient() {
            performOneTimeUiUpdate()
            setAmbientUiState(false)
            bindViewsToService()
        }

        private fun onUpdateAmbient() {
            performOneTimeUiUpdate()
        }
    }

    private companion object {
        const val CHRONO_TICK_MS = 200L
    }
}
