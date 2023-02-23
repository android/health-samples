/*
 * Copyright 2022 The Android Open Source Project
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
package com.example.exercisesamplecompose.presentation.component

import android.text.style.RelativeSizeSpan
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

private const val UNITS_RELATIVE_SIZE = .6f
private val MINUTES_PER_HOUR = TimeUnit.HOURS.toMinutes(1)
private val SECONDS_PER_MINUTE = TimeUnit.MINUTES.toSeconds(1)

fun formatElapsedTime(elapsedDuration: kotlin.time.Duration, includeSeconds: Boolean) =
    buildSpannedString {
        val hours = elapsedDuration.inWholeHours
        if (hours > 0) {
            append(hours.toString())
            inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
                append("h")
            }
        }
        val minutes = elapsedDuration.inWholeMinutes % MINUTES_PER_HOUR
        append("%02d".format(minutes))
        inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
            append("m")
        }
        if (includeSeconds) {
            val seconds = elapsedDuration.inWholeSeconds % SECONDS_PER_MINUTE
            append("%02d".format(seconds))
            inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
                append("s")
            }
        }
    }

/** Format calories burned to an integer with a "cal" suffix. */
fun formatCalories(calories: Double) = buildSpannedString {
    append(calories.roundToInt().toString())
    inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
        append(" cal")
    }
}

/** Format a distance to two decimals with a "km" suffix. */
fun formatDistanceKm(meters: Double) = buildSpannedString {
    append("%02.2f".format(meters / 1_000))
    inSpans(RelativeSizeSpan(UNITS_RELATIVE_SIZE)) {
        append("km")
    }
}
