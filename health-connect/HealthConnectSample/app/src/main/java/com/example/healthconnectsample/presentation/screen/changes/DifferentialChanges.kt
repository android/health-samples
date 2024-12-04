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
package com.example.healthconnectsample.presentation.screen.changes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.changes.Change
import com.example.healthconnectsample.R
import com.example.healthconnectsample.presentation.component.FormattedChange
import java.util.UUID

/**
 * Demonstrates the differential changes API.
 */
@Composable
fun DifferentialChangesScreen(
    permissions: Set<String>,
    permissionsGranted: Boolean,
    changesEnabled: Boolean,
    onChangesEnable: (Boolean) -> Unit,
    onGetChanges: () -> Unit,
    changes: List<Change>,
    changesToken: String?,
    uiState: DifferentialChangesViewModel.UiState,
    onError: (Throwable?) -> Unit = {},
    onPermissionsResult: () -> Unit = {},
    onPermissionsLaunch: (Set<String>) -> Unit = {}
) {

    // Remember the last error ID, such that it is possible to avoid re-launching the error
    // notification for the same error when the screen is recomposed, or configuration changes etc.
    val errorId = rememberSaveable { mutableStateOf(UUID.randomUUID()) }

    LaunchedEffect(uiState) {
        // If the initial data load has not taken place, attempt to load the data.
        if (uiState is DifferentialChangesViewModel.UiState.Uninitialized) {
            onPermissionsResult()
        }

        // The [DifferentialChangesViewModel.UiState] provides details of whether the last action
        // was a success or resulted in an error. Where an error occurred, for example in reading
        // and writing to Health Connect, the user is notified, and where the error is one that can
        // be recovered from, an attempt to do so is made.
        if (uiState is DifferentialChangesViewModel.UiState.Error && errorId.value != uiState.uuid) {
            onError(uiState.exception)
            errorId.value = uiState.uuid
        }
    }

    if (uiState != DifferentialChangesViewModel.UiState.Uninitialized) {
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
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(R.string.differential_changes_title_text),
                        textAlign = TextAlign.Justify
                    )
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(R.string.differential_changes_continuation_text),
                        textAlign = TextAlign.Justify
                    )
                }
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(stringResource(R.string.differential_changes_switch_text))
                        Switch(
                            checked = changesEnabled,
                            onCheckedChange = onChangesEnable
                        )
                    }
                }

                item {
                    val token = changesToken ?: stringResource(id = R.string.not_available_abbrev)
                    Text(stringResource(id = R.string.differential_changes_current_token, token))
                }

                item {
                    Button(
                        modifier = Modifier.padding(8.dp),
                        enabled = changesEnabled,
                        onClick = onGetChanges
                    ) {
                        Text(stringResource(R.string.differential_changes_button_text))
                    }
                }

                items(changes) { changeItem ->
                    FormattedChange(changeItem)
                }
                if (changes.isEmpty()) {
                    item {
                        Text(stringResource(R.string.differential_changes_empty))
                    }
                }
            }
        }
    }
}