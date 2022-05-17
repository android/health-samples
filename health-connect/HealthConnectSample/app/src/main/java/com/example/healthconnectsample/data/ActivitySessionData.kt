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
package com.example.healthconnectsample.data

import androidx.health.connect.client.records.HeartRateSeries
import androidx.health.connect.client.records.SpeedSeries
import java.time.Duration

/**
 * Represents data, both aggregated and raw, associated with a single activity session. Used to
 * collate results from aggregate and raw reads from Health Connect in one object.
 */
data class ActivitySessionData(
    val uid: String,
    val totalActiveTime: Duration? = null,
    val totalSteps: Long? = null,
    val totalDistance: Double? = null,
    val totalEnergyBurned: Double? = null,
    val minHeartRate: Long? = null,
    val maxHeartRate: Long? = null,
    val avgHeartRate: Long? = null,
    val heartRateSeries: List<HeartRateSeries> = listOf(),
    val minSpeed: Double? = null,
    val maxSpeed: Double? = null,
    val avgSpeed: Double? = null,
    val speedSeries: List<SpeedSeries> = listOf()
)
