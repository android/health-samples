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

import android.content.Context
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectService
import androidx.health.connect.client.metadata.DataOrigin
import androidx.health.connect.client.permission.HealthDataRequestPermissions
import androidx.health.connect.client.permission.Permission
import androidx.health.connect.client.records.ActivityEvent
import androidx.health.connect.client.records.ActivityEventTypes
import androidx.health.connect.client.records.ActivitySession
import androidx.health.connect.client.records.ActivityTypes
import androidx.health.connect.client.records.Distance
import androidx.health.connect.client.records.HeartRate
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.Speed
import androidx.health.connect.client.records.Steps
import androidx.health.connect.client.records.TotalEnergyBurned
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random
import kotlin.reflect.KClass

// The minimum android level that can use Health Connect
const val MIN_SUPPORTED_SDK = Build.VERSION_CODES.O_MR1

/**
 * Demonstrates reading and writing from Health Connect.
 */
class HealthConnectManager(private val context: Context) {
    private val healthConnectClient by lazy { HealthConnectService.getClient(context) }

    var availability = mutableStateOf(HealthConnectAvailability.NOT_SUPPORTED)
        private set

    init {
        availability.value = when {
            isAvailable() -> HealthConnectAvailability.INSTALLED
            isSupported() -> HealthConnectAvailability.NOT_INSTALLED
            else -> HealthConnectAvailability.NOT_SUPPORTED
        }
    }

    /**
     * Determines whether all the specified permissions are already granted. It is recommended to
     * call [HealthConnectClient.getGrantedPermissions] first in the permissions flow, as if the
     * permissions are already granted then there is no need to request permissions via
     * [HealthDataRequestPermissions].
     */
    suspend fun hasAllPermissions(permissions: Set<Permission>): Boolean {
        return permissions == healthConnectClient.getGrantedPermissions(permissions)
    }

    /**
     * Obtains a list of [ActivitySession]s in a specified time frame. An Activity Session is a
     * period of time given to an activity, that would make sense to a user, e.g. "Afternoon run"
     * etc. It does not necessarily mean, however, that the user was *running* for that entire time,
     * more that conceptually, this was the activity being undertaken.
     */
    suspend fun readActivitySessions(start: Instant, end: Instant): List<ActivitySession> {
        val request = ReadRecordsRequest(
            recordType = ActivitySession::class,
            timeRangeFilter = TimeRangeFilter.exact(start, end)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }

    /**
     * Writes an [ActivitySession] to Health Connect, and additionally writes underlying data for
     * the session too, such as [Steps], [Distance] etc.
     */
    suspend fun writeActivitySession(start: ZonedDateTime, end: ZonedDateTime) {
        healthConnectClient.insertRecords(
            listOf(
                ActivitySession(
                    startTime = start.toInstant(),
                    startZoneOffset = start.offset,
                    endTime = end.toInstant(),
                    endZoneOffset = end.offset,
                    activityType = ActivityTypes.RUNNING,
                    title = "My Run #${Random.nextInt(0, 60)}"
                ),
                Steps(
                    startTime = start.toInstant(),
                    startZoneOffset = start.offset,
                    endTime = end.toInstant(),
                    endZoneOffset = end.offset,
                    count = (1000 + 1000 * Random.nextInt(3)).toLong()
                ),
                // Mark a 5 minute pause during the workout
                ActivityEvent(
                    startTime = start.toInstant().plus(10, ChronoUnit.MINUTES),
                    startZoneOffset = start.offset,
                    endTime = start.toInstant().plus(15, ChronoUnit.MINUTES),
                    endZoneOffset = end.offset,
                    eventType = ActivityEventTypes.PAUSE
                ),
                Distance(
                    startTime = start.toInstant(),
                    startZoneOffset = start.offset,
                    endTime = end.toInstant(),
                    endZoneOffset = end.offset,
                    distanceMeters = (1000 + 100 * Random.nextInt(20)).toDouble()
                ),
                Speed(
                    time = start.toInstant(),
                    zoneOffset = start.offset,
                    speed = 2.5
                ),
                Speed(
                    time = start.toInstant().plus(5, ChronoUnit.MINUTES),
                    zoneOffset = start.offset,
                    speed = 2.7
                ),
                Speed(
                    time = start.toInstant().plus(10, ChronoUnit.MINUTES),
                    zoneOffset = start.offset,
                    speed = 2.9
                ),
                TotalEnergyBurned(
                    startTime = start.toInstant(),
                    startZoneOffset = start.offset,
                    endTime = end.toInstant(),
                    endZoneOffset = end.offset,
                    energy = (140 + Random.nextInt(20)) * 0.01
                )
            ) + buildHeartRateSeries(start, end)
        )
    }

    /**
     * Deletes an [ActivitySession] and underlying data.
     */
    suspend fun deleteActivitySession(uid: String) {
        val activitySession = healthConnectClient.readRecord(ActivitySession::class, uid)
        healthConnectClient.deleteRecords(
            ActivitySession::class,
            uidsList = listOf(uid),
            clientIdsList = emptyList()
        )
        val timeRangeFilter = TimeRangeFilter.exact(
            activitySession.record.startTime,
            activitySession.record.endTime
        )
        val rawDataTypes: Set<KClass<out Record>> = setOf(
            HeartRate::class,
            Speed::class,
            Distance::class,
            Steps::class,
            TotalEnergyBurned::class
        )
        rawDataTypes.forEach { rawType ->
            healthConnectClient.deleteRecords(rawType, timeRangeFilter)
        }
    }

    /**
     * Reads aggregated data and raw data for selected data types, for a given [ActivitySession].
     */
    suspend fun readAssociatedSessionData(
        uid: String
    ): ActivitySessionData {
        val activitySession = healthConnectClient.readRecord(ActivitySession::class, uid)
        // Use the start time and end time from the session, for reading raw and aggregate data.
        val timeRangeFilter = TimeRangeFilter.exact(
            startTime = activitySession.record.startTime,
            endTime = activitySession.record.endTime
        )
        val aggregateDataTypes = setOf(
            Steps.STEPS_COUNT_TOTAL,
            Distance.DISTANCE_TOTAL,
            TotalEnergyBurned.ENERGY_BURNED_TOTAL,
            HeartRate.HEART_RATE_BPM_AVG,
            HeartRate.HEART_RATE_BPM_MAX,
            HeartRate.HEART_RATE_BPM_MIN
            // TODO: Speed aggregates missing
        )
        // Limit the data read to just the application that wrote the session. This may or may not
        // be desirable depending on the use case: In some cases, it may be useful to combine with
        // data written by other apps.
        val dataOriginFilter = listOf(activitySession.record.metadata.dataOrigin)
        val aggregateData = healthConnectClient.aggregate(
            aggregateMetrics = aggregateDataTypes,
            timeRangeFilter = timeRangeFilter,
            dataOriginFilter = dataOriginFilter
        )
        val speedData = readData<Speed>(timeRangeFilter, dataOriginFilter)
        val heartRateData = readData<HeartRate>(timeRangeFilter, dataOriginFilter)
        return ActivitySessionData(
            uid = uid,
            totalSteps = aggregateData.getMetricOrNull(Steps.STEPS_COUNT_TOTAL),
            totalDistance = aggregateData.getMetricOrNull(Distance.DISTANCE_TOTAL),
            totalEnergyBurned = aggregateData.getMetricOrNull(TotalEnergyBurned.ENERGY_BURNED_TOTAL),
            minHeartRate = aggregateData.getMetricOrNull(HeartRate.HEART_RATE_BPM_MIN),
            maxHeartRate = aggregateData.getMetricOrNull(HeartRate.HEART_RATE_BPM_MAX),
            avgHeartRate = aggregateData.getMetricOrNull(HeartRate.HEART_RATE_BPM_AVG),
            speedData = speedData,
            heartRateSeries = heartRateData
        )
    }

    /**
     * Convenience function to reuse code for reading data.
     */
    private suspend inline fun <reified T : Record> readData(
        timeRangeFilter: TimeRangeFilter,
        dataOriginFilter: List<DataOrigin> = listOf()
    ): List<T> {
        val request = ReadRecordsRequest(
            recordType = T::class,
            dataOriginFilter = dataOriginFilter,
            timeRangeFilter = timeRangeFilter
        )
        return healthConnectClient.readRecords(request).records
    }

    // TODO - Currently Kotlin SDK does NOT support the creation of series data so this list of
    // HR values are just a list of sample points at the moment.
    private fun buildHeartRateSeries(
        sessionStartTime: ZonedDateTime,
        sessionEndTime: ZonedDateTime
    ): List<HeartRate> {
        val heartRateSeries = mutableListOf<HeartRate>()
        var time = sessionStartTime
        while (time.isBefore(sessionEndTime)) {
            heartRateSeries.add(
                HeartRate(
                    time = time.toInstant(),
                    zoneOffset = time.offset,
                    bpm = (80 + Random.nextInt(80)).toLong()
                )
            )
            time = time.plusSeconds(30)
        }
        return heartRateSeries
    }

    private fun isAvailable() = HealthConnectService.isAvailable(context)
    private fun isSupported() = Build.VERSION.SDK_INT >= MIN_SUPPORTED_SDK
}

/**
 * Health Connect requires that the underlying Healthcore APK is installed on the device.
 * [HealthConnectAvailability] represents whether this APK is indeed installed, whether it is not
 * installed but supported on the device, or whether the device is not supported (based on Android
 * version).
 */
enum class HealthConnectAvailability {
    INSTALLED,
    NOT_INSTALLED,
    NOT_SUPPORTED
}
