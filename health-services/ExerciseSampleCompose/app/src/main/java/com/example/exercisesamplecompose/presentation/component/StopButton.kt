@file:OptIn(ExperimentalHorologistApi::class)

package com.example.exercisesamplecompose.presentation.component


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material.Button
import com.example.exercisesamplecompose.R
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.material.Icon

@Composable
fun StopButton(onEndClick: () -> Unit) {
    Button(onClick = { onEndClick() }) {
        Icon(
            imageVector = Icons.Default.Stop,
            contentDescription = stringResource(
                id = R.string.startOrEnd
            )
        )
    }
}