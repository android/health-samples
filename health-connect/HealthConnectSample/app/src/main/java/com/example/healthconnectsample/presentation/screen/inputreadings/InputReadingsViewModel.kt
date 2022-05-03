package com.example.healthconnectsample.presentation.screen.inputreadings

import android.os.RemoteException
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.permission.AccessTypes
import androidx.health.connect.client.permission.Permission
import androidx.health.connect.client.records.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthconnectsample.data.HealthConnectManager
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*


class InputReadingsViewModel(private val healthConnectManager: HealthConnectManager) :
    ViewModel() {
    val permissions = setOf(
        Permission(Weight::class, AccessTypes.WRITE),
        Permission(Weight::class, AccessTypes.READ),
    )
    var weeklyAvg: MutableState<Double> = mutableStateOf(0.0)
        private set

    var permissionsGranted = mutableStateOf(false)
        private set

    var readingsList: MutableState<List<Weight>> = mutableStateOf(listOf())
        private set

    var uiState: UiState by mutableStateOf(UiState.Loading)
        private set

    init {
        initialLoad()

    }

    fun initialLoad() {
        viewModelScope.launch {
            tryWithPermissionsCheck {
                readWeightInputs()
            }
        }
    }

    fun inputReadings(inputValue: Weight) {
        viewModelScope.launch {
            tryWithPermissionsCheck {

                healthConnectManager.writeWeightInput(inputValue)
                computeWeeklyAverage()
                readWeightInputs()
            }
        }
    }

    fun deleteWeightInput(uid: String) {
        viewModelScope.launch {
            tryWithPermissionsCheck {
                healthConnectManager.deleteWeightInput(uid)
                computeWeeklyAverage()
                readWeightInputs()

            }
        }
    }

   private suspend fun computeWeeklyAverage(){
       val startOfWeek = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
       val endofWeek = startOfWeek.toInstant().plus(7, ChronoUnit.DAYS)
       val now = Instant.now()
       weeklyAvg.value = healthConnectManager.computeWeeklyAverage(startOfWeek.toInstant(), endofWeek)


   }

    private suspend fun readWeightInputs() {
        val startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val now = Instant.now()
        readingsList.value = healthConnectManager.readWeightInputs(startOfDay.toInstant(), now)
    }

    /**
     * Provides permission check and error handling for Health Connect suspend function calls.
     *
     * Permissions are checked prior to execution of [block], and if all permissions aren't granted
     * the [block] won't be executed, and [permissionsGranted] will be set to false, which will
     * result in the UI showing the permissions button.
     *
     * Where an error is caught, of the type Health Connect is known to throw, [uiState] is set to
     * [UiState.Error], which results in the snackbar being used to show the error message.
     */
    private suspend fun tryWithPermissionsCheck(block: suspend () -> Unit) {
        permissionsGranted.value = healthConnectManager.hasAllPermissions(permissions)
        uiState = try {
            if (permissionsGranted.value) {
                block()
            }
            UiState.Done
        } catch (remoteException: RemoteException) {
            UiState.Error(remoteException)
        } catch (securityException: SecurityException) {
            UiState.Error(securityException)
        } catch (ioException: IOException) {
            UiState.Error(ioException)
        } catch (illegalStateException: IllegalStateException) {
            UiState.Error(illegalStateException)
        }
    }

    sealed class UiState {
        object Loading : UiState()
        object Done : UiState()

        // A random UUID is used in each Error object to allow errors to be uniquely identified,
        // and recomposition won't result in multiple snackbars.
        data class Error(val exception: Throwable, val uuid: UUID = UUID.randomUUID()) : UiState()
    }
}

class InputReadingsViewModelFactory(
    private val healthConnectManager: HealthConnectManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InputReadingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InputReadingsViewModel(
                healthConnectManager = healthConnectManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
