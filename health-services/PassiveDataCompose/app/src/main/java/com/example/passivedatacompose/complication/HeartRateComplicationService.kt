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
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.wear.protolayout.expression.PlatformHealthSources
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.DynamicComplicationText
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
 * Surfaces heart rate on the watch face.
 *
 * Where possible the complication is backed by a platform binding:
 * [PlatformHealthSources.heartRateBpm] produces a dynamic value that the system re-evaluates
 * roughly once a second while the watch face is in interactive mode. The value therefore stays
 * live without this service being woken up at all, which is both cheaper and far more responsive
 * than returning a static number.
 *
 * On devices that predate dynamic values the complication falls back to the most recent
 * measurement stored by [com.example.passivedatacompose.service.PassiveDataService], which also
 * supplies the fallback text shown whenever the platform cannot evaluate the expression.
 */
class HeartRateComplicationService : SuspendingComplicationDataSourceService() {
    private val repository by lazy { PassiveDataRepository(this) }

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        if (type == ComplicationType.SHORT_TEXT) {
            complicationData(PlainComplicationText.Builder(formatBpm(PREVIEW_HEART_RATE)).build())
        } else {
            null
        }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        if (request.complicationType != ComplicationType.SHORT_TEXT) return null

        // The repository reports 0.0 until the first passive measurement is received.
        val latestHeartRate = repository.latestHeartRate.first()

        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                complicationData(dynamicHeartRateText(latestHeartRate))

            latestHeartRate > 0.0 ->
                complicationData(
                    PlainComplicationText.Builder(formatBpm(latestHeartRate)).build()
                )

            // Letting the watch face draw its own placeholder is better than showing a stale or
            // zero value.
            else -> NoDataComplicationData()
        }
    }

    /**
     * Heart rate as evaluated by the platform, with the last known passive measurement as the
     * fallback for when the expression cannot be evaluated, for example in ambient mode.
     *
     * Requires this app to hold `BODY_SENSORS` (or `READ_HEART_RATE` on API 36 and above), which
     * it already does in order to receive passive data.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun dynamicHeartRateText(latestHeartRate: Double): ComplicationText =
        DynamicComplicationText(
            PlatformHealthSources.heartRateBpm().asInt().format(),
            if (latestHeartRate > 0.0) {
                formatBpm(latestHeartRate)
            } else {
                getString(R.string.heart_rate_complication_placeholder)
            }
        )

    private fun complicationData(text: ComplicationText): ComplicationData =
        ShortTextComplicationData.Builder(
            text = text,
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

    private fun formatBpm(heartRate: Double): String = heartRate.toInt().toString()

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
