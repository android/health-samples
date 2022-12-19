/*
 * Copyright 2022 The Android Open Source Project
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
package com.example.healthconnectsample.presentation.screen.exercisesession

import android.os.RemoteException
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseEventRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthconnectsample.data.HealthConnectManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.random.Random

class ExerciseSessionViewModel(private val healthConnectManager: HealthConnectManager) :
    ViewModel() {
    val permissions = setOf(
        HealthPermission.createWritePermission(ExerciseSessionRecord::class),
        HealthPermission.createReadPermission(ExerciseSessionRecord::class),
        HealthPermission.createWritePermission(ExerciseEventRecord::class),
        HealthPermission.createWritePermission(StepsRecord::class),
        HealthPermission.createWritePermission(SpeedRecord::class),
        HealthPermission.createWritePermission(DistanceRecord::class),
        HealthPermission.createWritePermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.createWritePermission(HeartRateRecord::class)
    )

    var permissionsGranted = mutableStateOf(false)
        private set

    var sessionsList: MutableState<List<ExerciseSessionRecord>> = mutableStateOf(listOf())
        private set

    var uiState: UiState by mutableStateOf(UiState.Uninitialized)
        private set

    val permissionsLauncher = healthConnectManager.requestPermissionsActivityContract()

    var startQueueInsertions by mutableStateOf(false)
        private set
    var startHealthConnectInsertions by mutableStateOf(false)
        private set
    private val mutex = Mutex()
    private var startedAddSessions = false
    private var startedInsertHealthConnectSessions = false

    fun initialLoad() {
        viewModelScope.launch {
            tryWithPermissionsCheck {
                readExerciseSessions()
            }
        }
    }

    fun toggleQueueInsertions() {
        startQueueInsertions = !startQueueInsertions
        if(!startedAddSessions) {
            viewModelScope.launch { insertExerciseSessionInToQueue() }
            startedAddSessions = true
        }
    }

    fun toggleHealthConnectInsertions() {
        startHealthConnectInsertions = !startHealthConnectInsertions
        if(startHealthConnectInsertions) {
            viewModelScope.launch {
                tryWithPermissionsCheck {
                    mutex.withLock {
                        healthConnectManager.startRecurrentHealthConnectInsertion()
                    }
                }
            }
            startedInsertHealthConnectSessions = true
        }
        else{
            healthConnectManager.stopRecurrentHealthConnectInsertion()
        }
    }

    fun refreshList() {
        viewModelScope.launch {
            tryWithPermissionsCheck {
                println("READING SESSIONS")
                readExerciseSessions()
            }
        }
    }

    private suspend fun insertExerciseSessionInToQueue() {
        while (true) {
            if (startQueueInsertions) {
                mutex.withLock {
                    sharedInsertQueue.add(createExerciseSessionRecord())
                }
                println("ADDED EXERCISE SESSION TO SHARED QUEUE")
            }
            delay(1000)
        }
    }

    private fun createExerciseSessionRecord(): ExerciseSessionRecord {
        val startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val latestStartOfSession = ZonedDateTime.now().minusMinutes(30)
        val offset = Random.nextDouble()

        // Generate random start time between the start of the day and (now - 30mins).
        val startOfSession = startOfDay.plusSeconds(
            (Duration.between(startOfDay, latestStartOfSession).seconds * offset).toLong()
        )
        val endOfSession = startOfSession.plusMinutes(30)

        return ExerciseSessionRecord(
            startTime = startOfSession.toInstant(),
            startZoneOffset = startOfSession.offset,
            endTime = endOfSession.toInstant(),
            endZoneOffset = endOfSession.offset,
            exerciseType = ExerciseSessionRecord.ExerciseType.RUNNING,
            title = "My Run #${Random.nextInt(0, 60)}"
        )
    }

    fun insertExerciseSession() {
        viewModelScope.launch {
            tryWithPermissionsCheck {
                val startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
                val latestStartOfSession = ZonedDateTime.now().minusMinutes(30)
                val offset = Random.nextDouble()

                // Generate random start time between the start of the day and (now - 30mins).
                val startOfSession = startOfDay.plusSeconds(
                    (Duration.between(startOfDay, latestStartOfSession).seconds * offset).toLong()
                )
                val endOfSession = startOfSession.plusMinutes(30)

                healthConnectManager.writeExerciseSession(startOfSession, endOfSession)
                readExerciseSessions()
            }
        }
    }

    fun deleteExerciseSession(uid: String) {
        viewModelScope.launch {
            tryWithPermissionsCheck {
                healthConnectManager.deleteExerciseSession(uid)
                readExerciseSessions()
            }
        }
    }

    private suspend fun readExerciseSessions() {
        val startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val now = Instant.now()

        sessionsList.value = healthConnectManager.readExerciseSessions(startOfDay.toInstant(), now)
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
        object Uninitialized : UiState()
        object Done : UiState()

        // A random UUID is used in each Error object to allow errors to be uniquely identified,
        // and recomposition won't result in multiple snackbars.
        data class Error(val exception: Throwable, val uuid: UUID = UUID.randomUUID()) : UiState()
    }

    companion object {
        var sharedInsertQueue = mutableStateListOf<Record>()
            private set

        fun emptySharedQueue() {
            sharedInsertQueue.clear()
        }
    }
}

class ExerciseSessionViewModelFactory(
    private val healthConnectManager: HealthConnectManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseSessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseSessionViewModel(
                healthConnectManager = healthConnectManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
