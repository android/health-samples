package com.example.exercisesamplecompose.presentation.preparing

import android.Manifest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exercisesamplecompose.data.HealthServicesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class PreparingViewModel @Inject constructor(
    private val healthServicesRepository: HealthServicesRepository
) : ViewModel() {

    init {
        healthServicesRepository.createService()
    }
    
    fun prepareExercise() {
        healthServicesRepository.prepareExercise()
    }

    val uiState = healthServicesRepository.serviceState.map {
        PreparingScreenState(
            serviceState = it,
            isTrackingAnotherExercise = healthServicesRepository.isTrackingExerciseInAnotherApp(),
            requiredPermissions = permissions,
            hasExerciseCapabilities = healthServicesRepository.hasExerciseCapability(),
        )
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds),
        initialValue = PreparingScreenState(
            healthServicesRepository.serviceState.value,
            isTrackingAnotherExercise = false,
            requiredPermissions = permissions,
            hasExerciseCapabilities = true
        )
    )

    companion object {
        val permissions = listOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
    }
}