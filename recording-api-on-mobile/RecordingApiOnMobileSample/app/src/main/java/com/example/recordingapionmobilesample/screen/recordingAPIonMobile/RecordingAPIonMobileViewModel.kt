/*
 * Copyright 2024 The Android Open Source Project
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

package com.example.recordingapionmobilesample.screen.recordingAPIonMobile

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recordingapionmobilesample.data.BucketData
import com.example.recordingapionmobilesample.data.DataPointData
import com.example.recordingapionmobilesample.data.DataSetData
import com.example.recordingapionmobilesample.helper.PermissionHelper
import com.google.android.gms.fitness.LocalRecordingClient
import com.google.android.gms.fitness.data.LocalDataType
import com.google.android.gms.fitness.data.LocalField
import com.google.android.gms.fitness.request.LocalDataReadRequest
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

const val TAG = "Recording API on mobile"

class RecordingAPIonMobileViewModel(
  private val localRecordingClient: LocalRecordingClient,
  private val permissionHelper: PermissionHelper,
) : ViewModel() {
  var hasPermission = mutableStateOf(false)
    private set

  var bucketDataList = mutableStateListOf<BucketData>()
    private set

  var selectedLocalDataType = mutableStateOf(LocalDataType.TYPE_STEP_COUNT_DELTA)

  val localDataTypes = listOf(
    LocalDataType.TYPE_STEP_COUNT_DELTA,
    LocalDataType.TYPE_DISTANCE_DELTA,
    LocalDataType.TYPE_CALORIES_EXPENDED
  )

  private val permission = Manifest.permission.ACTIVITY_RECOGNITION

  init {
    viewModelScope.launch {
      checkPermission()

      if (hasPermission.value) {
        localDataTypes.forEach { localDataType ->
          subscribeData(localDataType)
        }
      }
    }
  }

  /**
   * Sets the permission state to the given value.
   */
  fun setPermission(isGranted: Boolean) {
    hasPermission.value = isGranted
  }

  /**
   * Sets the selected [LocalDataType].
   */
  fun setSelectedLocalDataType(localDataType: LocalDataType) {
    selectedLocalDataType.value = localDataType
  }

  /**
   * Checks if the permission is granted.
   */
  private fun checkPermission() {
    hasPermission.value = permissionHelper.hasPermission(permission)
  }

  /**
   * Subscribes to the given [LocalDataType].
   */
  @SuppressLint("MissingPermission")
  private fun subscribeData(localDataType: LocalDataType) {
    if (!hasPermission.value) {
      Log.e(TAG, "Permission ACTIVITY_RECOGNITION is not granted!")
      return
    }

    localRecordingClient
      .subscribe(localDataType)
      .addOnSuccessListener {
        Log.i(TAG, "Successfully subscribed ${localDataType.name}!")
      }
      .addOnFailureListener { e ->
        Log.e(TAG, "There was a problem of subscribing ${localDataType}.", e)
      }
  }

  private fun getLocalField(localDataType: LocalDataType): LocalField {
    return when (localDataType) {
      LocalDataType.TYPE_STEP_COUNT_DELTA -> LocalField.FIELD_STEPS
      LocalDataType.TYPE_DISTANCE_DELTA -> LocalField.FIELD_DISTANCE
      LocalDataType.TYPE_CALORIES_EXPENDED -> LocalField.FIELD_CALORIES
      else -> throw IllegalArgumentException("Unsupported LocalDataType: $localDataType")
    }
  }

  private fun readData(
    startTime: ZonedDateTime,
    endTime: ZonedDateTime,
    isAggregate: Boolean
  ) {
    val builder = LocalDataReadRequest.Builder()
    val dataType = selectedLocalDataType.value
    if (isAggregate) {
      builder.aggregate(dataType)
    } else {
      builder.read(dataType)
    }
    val readRequest = builder
      .bucketByTime(1, TimeUnit.HOURS)
      .setTimeRange(
        startTime.toEpochSecond(),
        endTime.toEpochSecond(),
        TimeUnit.SECONDS
      )
      .build()

    val localField = getLocalField(dataType)

    localRecordingClient.readData(readRequest)
      .addOnSuccessListener { response ->
        bucketDataList.clear()
        bucketDataList.addAll(response.buckets.mapIndexed { bucketIndex, bucket ->
          BucketData(
            index = bucketIndex,
            startTime = bucket.getStartTime(TimeUnit.SECONDS),
            endTime = bucket.getEndTime(TimeUnit.SECONDS),
            dataSetDataList = bucket.dataSets.mapIndexed { dataSetIndex, dataSet ->
              DataSetData(
                index = dataSetIndex,
                dataPointDataList = dataSet.dataPoints.mapIndexed { dataPointIndex, dataPoint ->
                  DataPointData(
                    index = dataPointIndex,
                    startTime = dataPoint.getStartTime(TimeUnit.SECONDS),
                    endTime = dataPoint.getEndTime(TimeUnit.SECONDS),
                    fieldName = localField.name,
                    fieldValue = dataPoint.getValue(localField).toString()
                  )
                }
              )
            }
          )
        })

        val readType = if (isAggregate) "aggregate" else "raw"
        Log.i(TAG, "Successfully read $readType data")
      }.addOnFailureListener { e ->
        Log.w(TAG, "Error reading data between $startTime and $endTime", e)
      }
  }

  /**
   * Reads raw data of given [LocalDataType] between the given start and end time.
   */
  fun readRawData(
    startTime: ZonedDateTime,
    endTime: ZonedDateTime
  ) {
    readData(startTime, endTime, false)
  }

  /**
   * Reads aggregate data of given [LocalDataType] between the given start and end time.
   */
  fun readAggregateData(
    startTime: ZonedDateTime,
    endTime: ZonedDateTime
  ) {
    readData(startTime, endTime, true)
  }
}
