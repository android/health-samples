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
package com.example.healthplatformsample.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.res.stringResource
import com.example.healthplatformsample.R
import com.google.android.libraries.healthdata.data.SeriesValue
import java.time.LocalDateTime
import java.time.ZoneId

fun LazyListScope.sessionDetailsSeries(
    @StringRes labelId: Int,
    series: List<SeriesValue>
) {
    item {
        Text(text = stringResource(id = labelId), style = MaterialTheme.typography.h5)
    }
    items(series) {
        val time = LocalDateTime.ofInstant(it.time, ZoneId.systemDefault())
        Text(
            text = stringResource(
                R.string.time_and_value,
                time.toString(),
                it.longValue.toString()
            )
        )
    }
}
