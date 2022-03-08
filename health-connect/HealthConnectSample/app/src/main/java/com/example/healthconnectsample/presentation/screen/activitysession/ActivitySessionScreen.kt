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
package com.example.healthconnectsample.presentation.screen.activitysession

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.metadata.Metadata
import androidx.health.connect.client.permission.HealthDataRequestPermissions
import androidx.health.connect.client.permission.Permission
import androidx.health.connect.client.records.ActivitySession
import androidx.health.connect.client.records.ActivityTypes
import com.example.healthconnectsample.R
import com.example.healthconnectsample.presentation.component.SessionRow
import com.example.healthconnectsample.presentation.theme.HealthConnectTheme
import java.time.ZonedDateTime
import java.util.UUID

/**
 * Shows a list of [ActivitySession]s from today.
 */
@Composable
fun ActivitySessionScreen(
    permissions: Set<Permission>,
    permissionsGranted: Boolean,
    sessionsList: List<ActivitySession>,
    uiState: ActivitySessionViewModel.UiState,
    onInsertClick: () -> Unit = {},
    onDetailsClick: (String) -> Unit = {},
    onDeleteClick: (String) -> Unit = {},
    onError: (Throwable?) -> Unit = {},
    onPermissionsResult: () -> Unit = {},
) {
    val launcher = rememberLauncherForActivityResult(HealthDataRequestPermissions()) {
        onPermissionsResult()
    }

    // Remember the last error ID, such that it is possible to avoid re-launching the error
    // notification for the same error when the screen is recomposed, or configuration changes etc.
    val errorId = rememberSaveable { mutableStateOf(UUID.randomUUID()) }

    // The [ActivitySessionViewModel.UiState] provides details of whether the last action was a
    // success or resulted in an error. Where an error occurred, for example in reading and writing
    // to Health Connect, the user is notified, and where the error is one that can be recovered
    // from, an attempt to do so is made.
    LaunchedEffect(uiState) {
        if (uiState is ActivitySessionViewModel.UiState.Error && errorId.value != uiState.uuid) {
            onError(uiState.exception)
            errorId.value = uiState.uuid
        }
    }

    if (uiState != ActivitySessionViewModel.UiState.Loading) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!permissionsGranted) {
                item {
                    Button(
                        onClick = { launcher.launch(permissions) }
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
                        Text(stringResource(id = R.string.insert_activity_session))
                    }
                }

                items(sessionsList) { session ->
                    SessionRow(
                        ZonedDateTime.ofInstant(session.startTime, session.startZoneOffset),
                        ZonedDateTime.ofInstant(session.endTime, session.endZoneOffset),
                        session.metadata.uid ?: stringResource(R.string.not_available_abbrev),
                        session.title ?: stringResource(R.string.no_title),
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
fun ActivitySessionScreenPreview() {
    HealthConnectTheme {
        val runningStartTime = ZonedDateTime.now()
        val runningEndTime = runningStartTime.plusMinutes(30)
        val walkingStartTime = ZonedDateTime.now().minusMinutes(120)
        val walkingEndTime = walkingStartTime.plusMinutes(30)
        ActivitySessionScreen(
            permissions = setOf(),
            permissionsGranted = true,
            sessionsList = listOf(
                ActivitySession(
                    activityType = ActivityTypes.RUNNING,
                    title = "Running",
                    startTime = runningStartTime.toInstant(),
                    startZoneOffset = runningStartTime.offset,
                    endTime = runningEndTime.toInstant(),
                    endZoneOffset = runningEndTime.offset,
                    metadata = Metadata(UUID.randomUUID().toString())
                ),
                ActivitySession(
                    activityType = ActivityTypes.WALKING,
                    title = "Walking",
                    startTime = walkingStartTime.toInstant(),
                    startZoneOffset = walkingStartTime.offset,
                    endTime = walkingEndTime.toInstant(),
                    endZoneOffset = walkingEndTime.offset,
                    metadata = Metadata(UUID.randomUUID().toString())
                )
            ),
            uiState = ActivitySessionViewModel.UiState.Done
        )
    }
}