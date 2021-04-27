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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.wear.widget.SwipeDismissFrameLayout
import com.example.exercise.databinding.FragmentNewExerciseConfirmationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Dialog shown to the user if starting a new exercise would terminate an existing exercise in
 * another app.
 */
@AndroidEntryPoint
class NewExerciseConfirmationFragment : DialogFragment() {

    @Inject lateinit var healthServicesManager: HealthServicesManager

    private lateinit var binding: FragmentNewExerciseConfirmationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewExerciseConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeDismissContainer.addCallback(SwipeDismissCallback())
        binding.cancel.setOnClickListener {
            dismiss()
        }
        binding.ok.setOnClickListener {
            lifecycleScope.launch {
                healthServicesManager.startExercise()
                dismiss()
            }
        }
    }

    inner class SwipeDismissCallback : SwipeDismissFrameLayout.Callback() {
        override fun onDismissed(layout: SwipeDismissFrameLayout?) {
            dismiss()
        }
    }
}
