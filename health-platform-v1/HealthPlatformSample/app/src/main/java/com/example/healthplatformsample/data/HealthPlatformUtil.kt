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
import com.example.healthplatformsample.findActivity
import com.google.android.libraries.healthdata.data.AggregatedValue
import com.google.android.libraries.healthdata.data.DataType
import com.google.android.libraries.healthdata.data.HealthDataException
import com.google.android.libraries.healthdata.permission.AccessType
import com.google.android.libraries.healthdata.permission.Permission
import kotlin.math.round

/**
 * Convenience extension functions for working with Health Platform permissions.
 */
fun DataType.readPermission() = Permission.create(this, AccessType.READ)
fun DataType.writePermission() = Permission.create(this, AccessType.WRITE)

/**
 * Aggregate values can either be [Double] or [Long], this function identifies which is the case
 * and creates a rounded (if necessary) string representation.
 */
fun AggregatedValue.formatValue() = when {
    this.longValues.isNotEmpty() -> {
        this.longValue.toString()
    }
    this.doubleValues.isNotEmpty() -> {
        round(this.doubleValue).toString()
    }
    else -> {
        "N/A"
    }
}

/**
 * Some exceptions from Health Platform have resolutions, for example, if the Health Platform
 * itself on the device just needs updating. This function determines if there is a resolution and
 * launches the Intent to start it.
 */
fun Context.tryHealthDataResolution(throwable: Throwable?) {
    if (throwable is HealthDataException && throwable.hasResolution()) {
        throwable.startResolutionForResult(this.findActivity(), 1001)
    }
}
