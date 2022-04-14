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

import androidx.health.connect.client.aggregate.AggregateDataRow
import androidx.health.connect.client.aggregate.DoubleAggregateMetric
import androidx.health.connect.client.aggregate.LongAggregateMetric
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * Convenience function to retrieve metric only when available, and otherwise return null, in
 * contrast to throwing an exception, as with [AggregateDataRow.getMetric].
 */
fun AggregateDataRow.getMetricOrNull(metric: LongAggregateMetric) =
    if (this.hasMetric(metric)) {
        this.getMetric(metric)
    } else {
        null
    }

/**
 * Convenience function to retrieve metric only when available, and otherwise return null, in
 * contrast to throwing an exception, as with [AggregateDataRow.getMetric].
 */
fun AggregateDataRow.getMetricOrNull(metric: DoubleAggregateMetric) =
    if (this.hasMetric(metric)) {
        this.getMetric(metric)
    } else {
        null
    }

/**
 * Creates a [ZonedDateTime] either using the offset stored in Health Connect, or falling back on
 * the zone offset for the device, where Health Connect contains no zone offset data. This fallback
 * may be correct in a number of circumstances, but may also not apply in others, so is used here
 * just as an example.
 */
fun dateTimeWithOffsetOrDefault(time: Instant, offset: ZoneOffset?): ZonedDateTime =
    if (offset != null) {
        ZonedDateTime.ofInstant(time, offset)
    } else {
        ZonedDateTime.ofInstant(time, ZoneId.systemDefault())
    }
