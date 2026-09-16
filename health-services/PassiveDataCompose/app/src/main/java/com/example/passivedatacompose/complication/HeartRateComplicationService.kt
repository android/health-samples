/*
 * Copyright 2026 The Android Open Source Project
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
package com.example.passivedatacompose.complication

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.example.passivedatacompose.MainActivity
import com.example.passivedatacompose.R
import com.example.passivedatacompose.data.PassiveDataRepository
import kotlinx.coroutines.flow.first

/**
 * Surfaces the latest passive heart rate measurement on the watch face.
 *
 * A complication data source is short-lived: [onComplicationRequest] must return quickly and must
 * not start a sensor session. Instead, this reads the value most recently stored by
 * [com.example.passivedatacompose.service.PassiveDataService], which owns the passive registration
 * and asks the system to refresh this complication whenever new data arrives.
 */
class HeartRateComplicationService : SuspendingComplicationDataSourceService() {
    private val repository by lazy { PassiveDataRepository(this) }

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        if (type == ComplicationType.SHORT_TEXT) {
            heartRateComplicationData(PREVIEW_HEART_RATE)
        } else {
            null
        }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        if (request.complicationType != ComplicationType.SHORT_TEXT) return null

        val heartRate = repository.latestHeartRate.first()

        // The repository reports 0.0 until the first measurement is received. Returning
        // NoDataComplicationData lets the watch face draw its own placeholder instead of a
        // misleading value.
        return if (heartRate > 0.0) {
            heartRateComplicationData(heartRate)
        } else {
            NoDataComplicationData()
        }
    }

    private fun heartRateComplicationData(heartRate: Double): ComplicationData =
        ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(heartRate.toInt().toString()).build(),
            contentDescription = PlainComplicationText.Builder(
                getString(R.string.heart_rate_complication_description)
            ).build()
        )
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    Icon.createWithResource(this, R.drawable.ic_heart)
                ).build()
            )
            .setTapAction(launchAppPendingIntent())
            .build()

    private fun launchAppPendingIntent(): PendingIntent = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    companion object {
        private const val PREVIEW_HEART_RATE = 72.0
    }
}
