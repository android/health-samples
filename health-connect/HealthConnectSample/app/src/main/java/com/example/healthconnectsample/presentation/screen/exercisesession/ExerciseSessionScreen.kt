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
package com.example.healthconnectsample.presentation.screen.exercisesession

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.permission.HealthPermission.Companion.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND
import androidx.health.connect.client.records.ExerciseSessionRecord
import com.example.healthconnectsample.R
import com.example.healthconnectsample.data.ExerciseSession
import com.example.healthconnectsample.data.HealthConnectAppInfo
import com.example.healthconnectsample.presentation.component.ExerciseSessionRow
import com.example.healthconnectsample.presentation.theme.HealthConnectTheme
import java.time.ZonedDateTime
import java.util.UUID

/**
 * Shows a list of [ExerciseSessionRecord]s from today.
 */
@Composable
fun ExerciseSessionScreen(
    permissions: Set<String>,
    permissionsGranted: Boolean,
    backgroundReadAvailable: Boolean,
    backgroundReadGranted: Boolean,
    sessionsList: List<ExerciseSession>,
    uiState: ExerciseSessionViewModel.UiState,
    onInsertClick: () -> Unit = {},
    onDetailsClick: (String) -> Unit = {},
    onDeleteClick: (String) -> Unit = {},
    onError: (Throwable?) -> Unit = {},
    onPermissionsResult: () -> Unit = {},
    onPermissionsLaunch: (Set<String>) -> Unit = {}
) {

    // Remember the last error ID, such that it is possible to avoid re-launching the error
    // notification for the same error when the screen is recomposed, or configuration changes etc.
    val errorId = rememberSaveable { mutableStateOf(UUID.randomUUID()) }

    LaunchedEffect(uiState) {
        // If the initial data load has not taken place, attempt to load the data.
        if (uiState is ExerciseSessionViewModel.UiState.Uninitialized) {
            onPermissionsResult()
        }

        // The [ExerciseSessionViewModel.UiState] provides details of whether the last action was a
        // success or resulted in an error. Where an error occurred, for example in reading and
        // writing to Health Connect, the user is notified, and where the error is one that can be
        // recovered from, an attempt to do so is made.
        if (uiState is ExerciseSessionViewModel.UiState.Error && errorId.value != uiState.uuid) {
            onError(uiState.exception)
            errorId.value = uiState.uuid
        }
    }

    if (uiState != ExerciseSessionViewModel.UiState.Uninitialized) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!permissionsGranted) {
                item {
                    Button(
                        onClick = {
                            onPermissionsLaunch(permissions)
                        }
                    ) {
                        Text(text = stringResource(R.string.permissions_button_label))
                    }
                }
            } else {
                item {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(4.dp),
                        onClick = {
                            onInsertClick()
                        }
                    ) {
                        Text(stringResource(id = R.string.insert_exercise_session))
                    }
                }
                if (!backgroundReadGranted) {
                    item {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(4.dp),
                            onClick = {
                                onPermissionsLaunch(setOf(PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND))
                            },
                            enabled = backgroundReadAvailable,
                        ) {
                            if (backgroundReadAvailable){
                                Text("Request Background Read")
                            } else {
                                Text("Background Read Is Not Available")
                            }
                        }
                    }
                }

                items(sessionsList) { session ->
                    val appInfo = session.sourceAppInfo
                    ExerciseSessionRow(
                        start = session.startTime,
                        end = session.endTime,
                        uid = session.id,
                        name = session.title ?: stringResource(R.string.no_title),
                        sourceAppName = appInfo?.appLabel ?: stringResource(R.string.unknown_app),
                        sourceAppIcon = appInfo?.icon,
                        onDeleteClick = { uid ->
                            onDeleteClick(uid)
                        },
                        onDetailsClick = { uid ->
                            onDetailsClick(uid)
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ExerciseSessionScreenPreview() {
    val context = LocalContext.current
    HealthConnectTheme {
        val runningStartTime = ZonedDateTime.now()
        val runningEndTime = runningStartTime.plusMinutes(30)
        val walkingStartTime = ZonedDateTime.now().minusMinutes(120)
        val walkingEndTime = walkingStartTime.plusMinutes(30)

        val appInfo = HealthConnectAppInfo(
            packageName = "com.example.myfitnessapp",
            appLabel = "My Fitness App",
            icon = context.getDrawable(R.drawable.ic_launcher_foreground)!!
        )

        ExerciseSessionScreen(
            permissions = setOf(),
            permissionsGranted = true,
            backgroundReadAvailable = false,
            backgroundReadGranted = false,
            sessionsList = listOf(
                ExerciseSession(
                    title = "Running",
                    startTime = runningStartTime,
                    endTime = runningEndTime,
                    id = UUID.randomUUID().toString(),
                    sourceAppInfo = appInfo
                ),
                ExerciseSession(
                    title = "Walking",
                    startTime = walkingStartTime,
                    endTime = walkingEndTime,
                    id = UUID.randomUUID().toString(),
                    sourceAppInfo = appInfo
                )
            ),
            uiState = ExerciseSessionViewModel.UiState.Done
        )
    }
}
