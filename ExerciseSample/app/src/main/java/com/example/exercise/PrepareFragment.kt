package com.example.exercise

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.health.services.client.data.LocationAvailability
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.exercise.databinding.FragmentPrepareBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PrepareFragment : Fragment(R.layout.fragment_prepare) {
    @Inject
    lateinit var healthServicesManager: HealthServicesManager

    private var serviceConnection = ExerciseServiceConnection()
    private var _binding: FragmentPrepareBinding? = null
    private val binding get() = _binding!!
    private var uiBindingJob: Job? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.all { it.value }) {
            Log.i(TAG, "All required permissions granted")
            tryPrepareExercise()
        } else {
            Log.i(TAG, "Not all required permissions granted")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPrepareBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind to our service. Views will only update once we are connected to it.
        val serviceIntent = Intent(requireContext(), ExerciseService::class.java)
        requireContext().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        bindViewsToService()

        binding.startButton.setOnClickListener {
            lifecycleScope.launch {
                healthServicesManager.startExercise()
                val destination =  R.id.exerciseFragment
                findNavController().navigate(destination)
            }
        }
        // Check permissions first.
        Log.d(TAG, "Checking permissions")
        permissionLauncher.launch(PrepareFragment.PERMISSIONS)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Unbind from the service.
        requireActivity().unbindService(serviceConnection)
        _binding = null
    }

    private fun tryPrepareExercise() {
        lifecycleScope.launchWhenStarted {
            healthServicesManager.prepareExercise()
        }
    }

    private fun bindViewsToService() {
        if (uiBindingJob != null) return

        uiBindingJob = lifecycleScope.launchWhenStarted {
            serviceConnection.repeatWhenConnected { service ->
                // Use separate launch blocks because each .collect executes indefinitely.
                launch {
                    service.locationAvailabilityState.collect {
                        updatePrepareLocationStatus(it)
                    }
                }
            }
        }
    }

    private fun updatePrepareLocationStatus(locationAvailability: LocationAvailability) {
        var gpsText = when (locationAvailability) {
            LocationAvailability.ACQUIRED_TETHERED -> R.string.gps_acquired
            LocationAvailability.ACQUIRED_UNTETHERED -> R.string.gps_acquiring
            LocationAvailability.NO_GPS -> R.string.gps_disabled // TODO Consider redirecting user to change device settings in this case
            LocationAvailability.ACQUIRING -> R.string.gps_acquiring
            else -> R.string.gps_unavailable
        }
        binding.gpsStatus.setText(gpsText)
    }
    private companion object {
        val PERMISSIONS = arrayOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
    }
}
