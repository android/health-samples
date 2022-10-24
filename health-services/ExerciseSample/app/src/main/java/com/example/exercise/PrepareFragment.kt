package com.example.exercise

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.health.services.client.data.LocationAvailability
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.exercise.databinding.FragmentPrepareBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PrepareFragment : Fragment(R.layout.fragment_prepare) {
    private var serviceConnection = ExerciseServiceConnection()
    private var _binding: FragmentPrepareBinding? = null
    private val binding get() = _binding!!

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.all { it.value }) {
            Log.i(TAG, "All required permissions granted")

            viewLifecycleOwner.lifecycleScope.launch {
                // Await binding of ExerciseService, since it takes a bit of time
                // to instantiate the service.
                serviceConnection.repeatWhenConnected {
                    checkNotNull(serviceConnection.exerciseService) {
                        "Failed to achieve ExerciseService instance"
                    }.prepareExercise()
                }
            }
        } else {
            Log.w(TAG, "Not all required permissions granted")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrepareBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind to our service. Views will only update once we are connected to it.
        ExerciseService.bindService(requireContext().applicationContext, serviceConnection)
        bindViewsToService()

        binding.startButton.setOnClickListener {
            checkNotNull(serviceConnection.exerciseService) {
                "Failed to achieve ExerciseService instance"
            }.startExercise()
            findNavController().navigate(R.id.exerciseFragment)
        }
        // Check permissions first.
        Log.d(TAG, "Checking permissions")
        permissionLauncher.launch(REQUIRED_PERMISSIONS)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Unbind from the service.
        ExerciseService.unbindService(requireContext().applicationContext, serviceConnection)
        _binding = null
    }

    private fun bindViewsToService() {
        viewLifecycleOwner.lifecycleScope.launch {
            serviceConnection.repeatWhenConnected { service ->
                service.locationAvailabilityState.collect {
                    updatePrepareLocationStatus(it)
                }
            }
        }
    }

    private fun updatePrepareLocationStatus(locationAvailability: LocationAvailability) {
        val gpsText = when (locationAvailability) {
            LocationAvailability.ACQUIRED_TETHERED,
            LocationAvailability.ACQUIRED_UNTETHERED -> R.string.gps_acquired
            LocationAvailability.NO_GNSS -> R.string.gps_disabled // TODO Consider redirecting user to change device settings in this case
            LocationAvailability.ACQUIRING -> R.string.gps_acquiring
            else -> R.string.gps_unavailable
        }
        binding.gpsStatus.setText(gpsText)

        if (locationAvailability == LocationAvailability.ACQUIRING) {
            if (!binding.progressAcquiring.isAnimating) {
                binding.progressAcquiring.visibility = View.VISIBLE
            }
        } else {
            if (binding.progressAcquiring.isAnimating) {
                binding.progressAcquiring.visibility = View.INVISIBLE
            }
        }
    }

    private companion object {
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
    }
}
