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
package com.example.passivedatacompose.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.passivedatacompose.R
import com.example.passivedatacompose.theme.PassiveDataTheme
import kotlin.math.roundToInt

/**
 * Displays a heart rate value with icon and label.
 */
@Composable
fun HeartRateCard(
    heartRate: Double,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = {},
        enabled = false,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = stringResource(R.string.heart_description),
                tint = Color.Red,
                modifier = Modifier.padding(end = 8.dp)
            )
            Column {
                val hrText = if (heartRate.isNaN()) "--" else heartRate.roundToInt().toString()
                Text(hrText)
                Text(
                    text = stringResource(id = R.string.last_measured),
                    style = MaterialTheme.typography.bodyExtraSmall
                )
            }
        }
    }
}

@Preview
@Composable
fun HeartRateCardPreview() {
    PassiveDataTheme {
        HeartRateCard(122.2)
    }
}
