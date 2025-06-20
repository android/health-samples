/*
 * Copyright 2025 The Android Open Source Project
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
package com.example.exercisesamplecompose.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.example.exercisesamplecompose.R
import com.example.exercisesamplecompose.app.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import javax.inject.Inject

class ExerciseNotificationManager
@Inject
constructor(
    @ApplicationContext val applicationContext: Context,
    val manager: NotificationManager
) {
    fun createNotificationChannel() {
        val notificationChannel =
            NotificationChannel(
                NOTIFICATION_CHANNEL,
                NOTIFICATION_CHANNEL_DISPLAY,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        manager.createNotificationChannel(notificationChannel)
    }

    fun buildNotification(duration: Duration): Notification {
        // Make an intent that will take the user straight to the exercise UI.
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        // Build the notification.
        val notificationBuilder =
            NotificationCompat
                .Builder(applicationContext, NOTIFICATION_CHANNEL)
                .setContentTitle(NOTIFICATION_TITLE)
                .setContentText(NOTIFICATION_TEXT)
                .setSmallIcon(R.drawable.ic_baseline_directions_run_24)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_WORKOUT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val startMillis = SystemClock.elapsedRealtime() - duration.toMillis()
        val ongoingActivityStatus =
            Status
                .Builder()
                .addTemplate(ONGOING_STATUS_TEMPLATE)
                .addPart("duration", Status.StopwatchPart(startMillis))
                .build()
        val ongoingActivity =
            OngoingActivity
                .Builder(applicationContext, NOTIFICATION_ID, notificationBuilder)
                .setAnimatedIcon(R.drawable.ic_baseline_directions_run_24)
                .setStaticIcon(R.drawable.ic_baseline_directions_run_24)
                .setTouchIntent(pendingIntent)
                .setStatus(ongoingActivityStatus)
                .build()

        ongoingActivity.apply(applicationContext)

        return notificationBuilder.build()
    }

    companion object {
        const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL =
            "com.example.exercisesamplecompose.ONGOING_EXERCISE"
        private const val NOTIFICATION_CHANNEL_DISPLAY = "Ongoing Exercise"
        private const val NOTIFICATION_TITLE = "Exercise Sample"
        private const val NOTIFICATION_TEXT = "Ongoing Exercise"
        private const val ONGOING_STATUS_TEMPLATE = "Ongoing Exercise #duration#"
    }
}
