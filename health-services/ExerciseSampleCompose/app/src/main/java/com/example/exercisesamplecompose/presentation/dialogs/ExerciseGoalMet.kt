package com.example.exercisesamplecompose.presentation.dialogs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.dialog.DialogDefaults
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.example.exercisesamplecompose.R
import com.google.android.horologist.compose.material.Confirmation

@Composable
fun ExerciseGoalMet(
    showDialog: Boolean,
) {
    Confirmation(modifier = Modifier
        .fillMaxSize()
        .fillMaxWidth(),
        icon = {
            Icon(
                Icons.Default.SportsScore,
                contentDescription = stringResource(id = R.string.goal_achieved)
            )
        },
        title = stringResource(id = R.string.goal_achieved),
        showDialog = showDialog,
        durationMillis = DialogDefaults.LongDurationMillis,
        onTimeout = {})
}

@WearPreviewDevices
@Composable
fun ExerciseGoalMetPreview() {
    ExerciseGoalMet(true)
}
