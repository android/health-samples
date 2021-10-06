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
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fragment shown at startup. It's only function is to advance to the next screen based on whether
 * the exercise capability is available.
 */
@AndroidEntryPoint
class StartupFragment : Fragment(R.layout.fragment_startup) {

    @Inject
    lateinit var healthServicesManager: HealthServicesManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val destination = if (healthServicesManager.hasExerciseCapability()) {
                    R.id.prepareFragment
                } else {
                    R.id.notAvailableFragment
                }
                findNavController().navigate(destination)
            }
        }
    }
}

/**
 * Fragment shown when exercise capability is not available.
 */
@AndroidEntryPoint
class NotAvailableFragment : Fragment(R.layout.fragment_not_available)
