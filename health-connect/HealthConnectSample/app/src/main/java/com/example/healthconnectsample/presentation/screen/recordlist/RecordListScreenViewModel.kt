/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.healthconnectsample.presentation.screen.recordlist

import android.os.RemoteException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthconnectsample.data.HealthConnectManager
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
import kotlin.reflect.KClass

class RecordListScreenViewModel(
    private val uid: String,
    recordTypeString: String,
    seriesRecordsTypeString: String,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {
    private val recordType: KClass<out Record> = RecordType.stringToClass(recordTypeString)
    private val seriesRecordsType: KClass<out Record> = SeriesRecordsType.stringToClass(seriesRecordsTypeString)
    val permissions = setOf(HealthPermission.getReadPermission(recordType))

    var permissionsGranted = mutableStateOf(false)
        private set

    var recordList = mutableStateListOf<Record>()
        private set

    var uiState: UiState by mutableStateOf(UiState.Uninitialized)
        private set

    val permissionsLauncher = healthConnectManager.requestPermissionsActivityContract()

    fun initialLoad() {
        viewModelScope.launch {
            tryWithPermissionsCheck {
                recordList.clear()
                recordList.addAll(healthConnectManager.fetchSeriesRecordsFromUid(recordType, uid, seriesRecordsType))
            }
        }
    }

    /**
     * Provides permission check and error handling for Health Connect suspend function calls.
     *
     * Permissions are checked prior to execution of [block], and if all permissions aren't granted
     * the [block] won't be executed, and [permissionsGranted] will be set to false, which will result
     * in the UI showing the permissions button.
     *
     * Where an error is caught, of the type Health Connect is known to throw, [uiState] is set to
     * [UiState.Error], which results in the snackbar being used to show the error message.
     */
    private suspend fun tryWithPermissionsCheck(block: suspend () -> Unit) {
        permissionsGranted.value = healthConnectManager.hasAllPermissions(permissions)
        uiState =
            try {
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
        object Uninitialized : UiState()

        object Done : UiState()

        // A random UUID is used in each Error object to allow errors to be uniquely identified,
        // and recomposition won't result in multiple snackbars.
        data class Error(val exception: Throwable, val uuid: UUID = UUID.randomUUID()) : UiState()
    }
}

class RecordListViewModelFactory(
    private val uid: String,
    private val recordTypeString: String,
    private val seriesRecordsTypeString: String,
    private val healthConnectManager: HealthConnectManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordListScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecordListScreenViewModel(
                uid = uid,
                recordTypeString = recordTypeString,
                seriesRecordsTypeString = seriesRecordsTypeString,
                healthConnectManager = healthConnectManager)
                    as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

enum class RecordType(val clazz: KClass<out Record>) {
    EXERCISE_SESSION(ExerciseSessionRecord::class),
    SLEEP_SESSION(SleepSessionRecord::class);

    companion object {
        fun stringToClass(recordTypeString: String) = RecordType.valueOf(recordTypeString).clazz
    }
}

enum class SeriesRecordsType(val clazz: KClass<out Record>) {
    STEPS(StepsRecord::class),
    DISTANCE(DistanceRecord::class),
    CALORIES(TotalCaloriesBurnedRecord::class),
    HEARTRATE(HeartRateRecord::class);

    companion object {
        fun stringToClass(seriesRecordsTypeString: String) = SeriesRecordsType.valueOf(seriesRecordsTypeString).clazz
    }
}
