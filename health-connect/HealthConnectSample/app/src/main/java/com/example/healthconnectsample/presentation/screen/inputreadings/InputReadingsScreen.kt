package com.example.healthconnectsample.presentation.screen.inputreadings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.metadata.Metadata
import androidx.health.connect.client.permission.HealthDataRequestPermissions
import androidx.health.connect.client.permission.Permission
import androidx.health.connect.client.records.Weight
import com.example.healthconnectsample.R
import com.example.healthconnectsample.presentation.theme.HealthConnectTheme
import java.time.Instant
import java.util.*

@Composable
fun InputReadingsScreen(
    permissions: Set<Permission>,
    permissionsGranted: Boolean,
    readingsList: List<Weight>,
    uiState: InputReadingsViewModel.UiState,
    onInsertClick: (Weight) -> Unit = {},
    onDeleteClick: (String) -> Unit = {},
    onError: (Throwable?) -> Unit = {},
    onPermissionsResult: () -> Unit = {},
    weeklyAvg: Double
) {

    val launcher = rememberLauncherForActivityResult(HealthDataRequestPermissions()) {
        onPermissionsResult()
    }

    // Remember the last error ID, such that it is possible to avoid re-launching the error
    // notification for the same error when the screen is recomposed, or configuration changes etc.
    val errorId = rememberSaveable { mutableStateOf(UUID.randomUUID()) }

    // The [InputReadingsScreenViewModel.UiState] provides details of whether the last action was a
    // success or resulted in an error. Where an error occurred, for example in reading and writing
    // to Health Connect, the user is notified, and where the error is one that can be recovered
    // from, an attempt to do so is made.

    LaunchedEffect(uiState) {
        if (uiState is InputReadingsViewModel.UiState.Error && errorId.value != uiState.uuid) {
            onError(uiState.exception)
            errorId.value = uiState.uuid
        }
    }

    var weightInput by remember { mutableStateOf("") }


    if (uiState != InputReadingsViewModel.UiState.Loading) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
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
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = {
                            weightInput = it

                        },
                        label = {
                            Text("New Record (Kg)")
                        },

                        )

                    Button(
                        onClick = {
                            onInsertClick(
                                Weight(
                                    weightInput.toDouble(),
                                    time = Instant.now(),
                                    zoneOffset = null,
                                    metadata = Metadata(UUID.randomUUID().toString())
                                )
                            )
                        },
                        modifier = Modifier.fillMaxHeight()
                    )
                    {
                        Text(text = "Add")
                    }
                    Text(
                        text = "Previous Readings",
                        fontSize = 24.sp,
                        color = MaterialTheme.colors.primary
                    )
                }

                items(readingsList) { reading ->
                    val uid = reading.metadata.uid
                    Text(
                        text = reading.weightKg.toString() + "Kg" + "   " + Date.from(reading.time),
                        textAlign = TextAlign.Start
                    )
                    IconButton(
                        onClick = {
                            if (uid != null) {
                                onDeleteClick(uid)
                            }
                        },
                    ) {
                        Icon(Icons.Default.Delete, stringResource(R.string.delete_button_readings))
                    }
                }
                item {
                    Text(
                        text = "\r\nWeekly Average", fontSize = 24.sp,
                        color = MaterialTheme.colors.primary
                    )
                    Text(text = weeklyAvg.toString().take(5) + "Kg")

                }

            }

        }

    }
}

@Preview
@Composable
fun InputReadingsScreenPreview() {
    val inputTime = Instant.now()
    HealthConnectTheme(darkTheme = false) {
        InputReadingsScreen(
            permissions = setOf(),
            weeklyAvg = 54.5,
            permissionsGranted = true,
            readingsList = listOf(
                Weight(
                    54.0,
                    time = inputTime,
                    zoneOffset = null
                ),
                Weight(
                    55.0,
                    time = inputTime,
                    zoneOffset = null
                )
            ),

            uiState = InputReadingsViewModel.UiState.Done
        )
    }
}