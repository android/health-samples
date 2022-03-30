/*
 * Copyright 2021 The Android Open Source Project
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
package com.example.healthplatformsample.data

import android.content.Context
import androidx.concurrent.futures.await
import com.example.healthplatformsample.R
import com.google.android.libraries.healthdata.HealthDataService
import com.google.android.libraries.healthdata.data.ActivityType
import com.google.android.libraries.healthdata.data.CumulativeAggregationSpec
import com.google.android.libraries.healthdata.data.CumulativeData
import com.google.android.libraries.healthdata.data.DataType
import com.google.android.libraries.healthdata.data.DeleteDataRequest
import com.google.android.libraries.healthdata.data.InsertDataRequest
import com.google.android.libraries.healthdata.data.IntervalData
import com.google.android.libraries.healthdata.data.IntervalDataTypes
import com.google.android.libraries.healthdata.data.IntervalReadSpec
import com.google.android.libraries.healthdata.data.Ordering
import com.google.android.libraries.healthdata.data.ReadAggregatedDataRequest
import com.google.android.libraries.healthdata.data.ReadAssociatedDataRequest
import com.google.android.libraries.healthdata.data.ReadDataRequest
import com.google.android.libraries.healthdata.data.SampleData
import com.google.android.libraries.healthdata.data.SampleDataSet
import com.google.android.libraries.healthdata.data.SampleDataTypes
import com.google.android.libraries.healthdata.data.SeriesData
import com.google.android.libraries.healthdata.data.SeriesDataSet
import com.google.android.libraries.healthdata.data.SeriesDataTypes
import com.google.android.libraries.healthdata.data.SeriesValue
import com.google.android.libraries.healthdata.data.StatisticalData
import com.google.android.libraries.healthdata.data.TimeSpec
import com.google.android.libraries.healthdata.permission.Permission
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random

/**
 * Demonstrates reading and writing from the Health Platform.
 */
class HealthPlatformManager(private val context: Context) {
    // The required Health Platform permissions for this particular sample. The sample will
    // request each of these permissions, which need to be specified in the
    // values/healthdata_permissions.xml file.
    private val allRequiredDataTypes = setOf<DataType>(
        IntervalDataTypes.ACTIVITY_SESSION,
        IntervalDataTypes.STEPS,
        IntervalDataTypes.DISTANCE,
        IntervalDataTypes.TOTAL_ENERGY_BURNED,
        SampleDataTypes.SPEED,
        SeriesDataTypes.HEART_RATE
    )

    private val readPermissions by lazy {
        allRequiredDataTypes.map { it.readPermission() }.toSet()
    }
    private val writePermissions by lazy {
        allRequiredDataTypes.map { it.writePermission() }.toSet()
    }

    private val healthDataClient by lazy { HealthDataService.getClient(context) }

    // Check for device support (Health Platform is not supported on all devices).
    fun healthPlatformSupported() = HealthDataService.isHealthDataApiSupported()

    /**
     * Inserts an activity session together with several other associated data, such as steps and
     * heart rate, at random time in the last 7 days.
     */
    suspend fun insertSessionData() {
        requestAndCheckPermissions(writePermissions)

        // Random start time in the last 7 days
        val sessionStartTime =
            Instant.now().minus((1 + Random.nextInt(168)).toLong(), ChronoUnit.HOURS)
        val sessionEndTime = sessionStartTime.plus(15, ChronoUnit.MINUTES)

        // Random "high-frequency" heart rate sensor data
        val heartRateSeries = buildHeartRateSeries(sessionStartTime, sessionEndTime)
        val insertRequest = buildInsertRequest(sessionStartTime, sessionEndTime, heartRateSeries)

        healthDataClient.insertData(insertRequest).await()
    }

    /** Shows a list of all sessions (only session metadata) in the last 7 days. */
    suspend fun readSessionsList(): List<Session> {
        requestAndCheckPermissions(readPermissions)

        val request = ReadDataRequest.builder()
            .setTimeSpec(
                TimeSpec.builder()
                    .setStartTime(Instant.now().minus(7, ChronoUnit.DAYS))
                    .setEndTime(Instant.now())
                    .build()
            )
            .addIntervalReadSpec(
                IntervalReadSpec.builder(IntervalDataTypes.ACTIVITY_SESSION)
                    .setOrdering(Ordering.DESC)
                    .build()
            )
            .build()
        val response = healthDataClient.readData(request).await()
        val sessionsList = response.intervalDataSets[0].data.map { data ->
            Session(
                data.startTime,
                data.endTime,
                data.uid,
                data.stringValues.getOrDefault(
                    IntervalDataTypes.ACTIVITY_SESSION.title,
                    context.getString(R.string.default_activity_session_title)
                )
            )
        }
        return sessionsList
    }

    /**
     * Shows total number of steps in the last 7 days.
     */
    suspend fun readTotalSteps(): Long {
        requestAndCheckPermissions(readPermissions)

        val request = ReadAggregatedDataRequest.builder()
            .setTimeSpec(
                TimeSpec.builder()
                    .setStartTime(Instant.now().minus(7, ChronoUnit.DAYS))
                    .setEndTime(Instant.now())
                    .build()
            )
            .addCumulativeAggregationSpec(
                CumulativeAggregationSpec.builder(IntervalDataTypes.STEPS).build()
            )
            .build()

        val response = healthDataClient.readAggregatedData(request).await()
        return response.cumulativeDataList[0].total?.longValue ?: 0L
    }

    /**
     * Deletes ACTIVITY_SESSION IntervalData. Note that this does not delete any other associated
     * raw data, only metadata. Underlying metrics such as steps are not deleted so continue to
     * contribute to the weekly total. To delete them, UIDs of all the other raw Data need to be
     * passed to delete request alongside the session UID.
     */
    suspend fun deleteSession(uid: String) {
        requestAndCheckPermissions(writePermissions)

        val request = DeleteDataRequest.builder()
            .addUid(IntervalDataTypes.ACTIVITY_SESSION, uid)
            .build()
        healthDataClient.deleteData(request).await()
    }

    /**
     * Displays several raw and aggregated data associated with the session of given {@code uid}.
     */
    suspend fun readSessionDetails(uid: String): SessionDetails {
        requestAndCheckPermissions(readPermissions)

        val request = ReadAssociatedDataRequest.builder()
            .setSourceDataUid(uid)
            .setSourceDataType(IntervalDataTypes.ACTIVITY_SESSION)
            .addCumulativeDataType(IntervalDataTypes.STEPS)
            .addCumulativeDataType(IntervalDataTypes.DISTANCE)
            .addCumulativeDataType(IntervalDataTypes.TOTAL_ENERGY_BURNED)
            .addSampleDataType(SampleDataTypes.SPEED)
            .addStatisticalDataType(SampleDataTypes.SPEED)
            .addSeriesDataType(SeriesDataTypes.HEART_RATE)
            .addStatisticalDataType(SeriesDataTypes.HEART_RATE)
            .build()

        val response = healthDataClient.readAssociatedData(request).await()
        return SessionDetails(
            response.cumulativeData[0],
            response.cumulativeData[1],
            response.cumulativeData[2],
            response.statisticalData[0],
            response.sampleDataSets[0],
            response.statisticalData[1],
            response.seriesDataSets[0]
        )
    }

    /**
     * Creates a series of heart rate data between the specified start and end.
     */
    private fun buildHeartRateSeries(
        sessionStartTime: Instant,
        sessionEndTime: Instant
    ): List<SeriesValue> {
        val heartRateSeries = mutableListOf<SeriesValue>()
        var time = sessionStartTime
        while (time.isBefore(sessionEndTime)) {
            heartRateSeries.add(
                SeriesValue.builder(SeriesDataTypes.HEART_RATE)
                    .setTime(time)
                    .setLongValue((80 + Random.nextInt(80)).toLong())
                    .build()
            )
            time = time.plusSeconds(30)
        }
        return heartRateSeries
    }

    /**
     * Creates an example [IntervalDataTypes.ACTIVITY_SESSION] including steps data, speed data and
     * heart rate series data.
     */
    private fun buildInsertRequest(
        sessionStartTime: Instant,
        sessionEndTime: Instant,
        heartRateSeries: List<SeriesValue>
    ) = InsertDataRequest.builder()
        .addIntervalData(
            IntervalData.builder(IntervalDataTypes.ACTIVITY_SESSION)
                .setStartTime(sessionStartTime)
                .setEndTime(sessionEndTime)
                .setEnumValue(
                    IntervalDataTypes.ACTIVITY_SESSION.type, ActivityType.RUNNING
                )
                .setStringValue(
                    IntervalDataTypes.ACTIVITY_SESSION.title,
                    "My running session " + Random.nextInt(50)
                )
                .build()
        )
        .addIntervalData(
            IntervalData.builder(IntervalDataTypes.STEPS)
                .setStartTime(sessionStartTime)
                .setEndTime(sessionEndTime)
                .setLongValue((1000 + 1000 * Random.nextInt(3)).toLong())
                .build()
        )
        .addIntervalData(
            IntervalData.builder(IntervalDataTypes.DISTANCE)
                .setStartTime(sessionStartTime)
                .setEndTime(sessionEndTime)
                .setDoubleValue((1000 + 100 * Random.nextInt(20)).toDouble())
                .build()
        )
        .addSampleData(
            SampleData.builder(SampleDataTypes.SPEED)
                .setTime(sessionStartTime)
                .setDoubleValue(2.5)
                .build()
        )
        .addSampleData(
            SampleData.builder(SampleDataTypes.SPEED)
                .setTime(sessionStartTime.plus(5, ChronoUnit.MINUTES))
                .setDoubleValue(2.7)
                .build()
        )
        .addSampleData(
            SampleData.builder(SampleDataTypes.SPEED)
                .setTime(sessionStartTime.plus(10, ChronoUnit.MINUTES))
                .setDoubleValue(2.9)
                .build()
        )
        .addSeriesData(
            SeriesData.builder(SeriesDataTypes.HEART_RATE)
                .setStartTime(sessionStartTime)
                .setEndTime(sessionEndTime)
                .addValues(heartRateSeries)
                .build()
        )
        .addIntervalData(
            IntervalData.builder(IntervalDataTypes.TOTAL_ENERGY_BURNED)
                .setStartTime(sessionStartTime)
                .setEndTime(sessionEndTime)
                .setDoubleValue((140 + Random.nextInt(20)) * 0.01)
                .build()
        )
        .build()

    /**
     * Checks if permissions are already granted, and if not requests them from the user. If the
     * user does not grant the permissions, throws [IllegalStateException].
     */
    private suspend fun requestAndCheckPermissions(permissions: Set<Permission>) {
        if (hasPermissions(permissions)) {
            return
        }
        val grantedPermissions = healthDataClient.requestPermissions(permissions).await()
        if (!grantedPermissions.containsAll(permissions)) {
            throw IllegalStateException("Required permissions were not granted")
        }
    }

    /**
     * Determines whether the specified permissions have already been granted to the user.
     */
    private suspend fun hasPermissions(permissions: Set<Permission>): Boolean {
        val grantedPermissions = healthDataClient.getGrantedPermissions(permissions).await()
        return grantedPermissions.containsAll(permissions)
    }
}

/**
 * Represents the relevant information from a stored Health Platform session.
 */
data class Session(val start: Instant, val end: Instant, val uid: String, val name: String)

/**
 * Represents the relevant data from the sample sessions, for visualization.
 */
data class SessionDetails(
    val totalSteps: CumulativeData,
    val totalDistance: CumulativeData,
    val totalEnergy: CumulativeData,
    val speedStats: StatisticalData,
    val speedSamples: SampleDataSet,
    val hrStats: StatisticalData,
    val hrSeries: SeriesDataSet
)
