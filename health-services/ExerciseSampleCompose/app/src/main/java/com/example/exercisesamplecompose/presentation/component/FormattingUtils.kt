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
package com.example.exercisesamplecompose.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.wear.compose.material3.MaterialTheme
import com.google.android.horologist.compose.ambient.LocalAmbientState
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

private val MINUTES_PER_HOUR = TimeUnit.HOURS.toMinutes(1)
private val SECONDS_PER_MINUTE = TimeUnit.MINUTES.toSeconds(1)

@Composable
fun formatElapsedTime(
    elapsedDuration: Duration?,
    includeSeconds: Boolean = false
) = buildAnnotatedString {
    if (elapsedDuration == null) {
        append("--")
    } else {
        val hours = elapsedDuration.toHours()
        if (hours > 0) {
            append(hours.toString())
            withStyle(style = MaterialTheme.typography.labelSmall.toSpanStyle()) {
                append("h")
            }
        }
        val minutes = elapsedDuration.toMinutes() % MINUTES_PER_HOUR
        append("%02d".format(minutes))
        withStyle(style = MaterialTheme.typography.labelSmall.toSpanStyle()) {
            append("m")
        }
        if (includeSeconds) {
            val seconds = elapsedDuration.seconds % SECONDS_PER_MINUTE
            if (LocalAmbientState.current.isInteractive) append("%02d".format(seconds))
            else append("--")
            withStyle(style = MaterialTheme.typography.labelSmall.toSpanStyle()) {
                append("s")
            }
        }
    }
}

/** Format calories burned to an integer with a "cal" suffix. */
@Composable
fun formatCalories(calories: Double?) =
    buildAnnotatedString {
        if (calories == null || calories.isNaN()) {
            append("--")
        } else {
            append(calories.roundToInt().toString())
        }
    }

/** Format a distance to two decimals with a "km" suffix. */
@Composable
fun formatDistanceKm(meters: Double?) =
    buildAnnotatedString {
        if (meters == null) {
            append("--")
        } else {
            append("%02.2f".format(meters / 1_000))
        }
    }

/** Format heart rate with a "bpm" suffix. */
@Composable
fun formatHeartRate(bpm: Double?) =
    buildAnnotatedString {
        if (bpm == null || bpm.isNaN()) {
            append("--")
        } else {
            append("%.0f".format(bpm))
            withStyle(style = MaterialTheme.typography.labelSmall.toSpanStyle()) {
                append("bpm")
            }
        }
    }
