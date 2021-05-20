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

package com.example.exercise

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.health.services.client.data.DataPoint
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseState
import androidx.health.services.client.data.ExerciseUpdate
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDeepLinkBuilder
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.OngoingActivityStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * Service that listens to the exercise state of the app. Local clients can bind to this service to
 * access the exercise state and associated metrics.
 *
 * This service manages its own lifecycle. Once a client binds to it, it calls [startService] on
 * itself and registers for exercise state. When there are no bound clients, if there isn't an
 * active exercise, this service stops itself. If there is an active exercise, it moves itself to
 * the foreground with an ongoing notification so that the user has an easy way back into the app.
 *
 * (see [Bound Services](https://developer.android.com/guide/components/bound-services))
 */
@AndroidEntryPoint
class ExerciseService : LifecycleService() {

    @Inject
    lateinit var healthServicesManager: HealthServicesManager

    private val localBinder = LocalBinder()
    private var isBound = false
    private var isStarted = false
    private var isForeground = false

    private val _exerciseState = MutableStateFlow(ExerciseState.USER_ENDED)
    val exerciseState: StateFlow<ExerciseState> = _exerciseState

    private val _exerciseMetrics = MutableStateFlow(emptyMap<DataType, List<DataPoint>>())
    val exerciseMetrics: StateFlow<Map<DataType, List<DataPoint>>> = _exerciseMetrics

    private val _exerciseLaps = MutableStateFlow(0)
    val exerciseLaps: StateFlow<Int> = _exerciseLaps

    private val _exerciseDurationUpdate = MutableStateFlow(ActiveDurationUpdate())
    val exerciseDurationUpdate: StateFlow<ActiveDurationUpdate> = _exerciseDurationUpdate

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "onStartCommand")

        if (!isStarted) {
            isStarted = true

            if (!isBound) {
                // We may have been restarted by the system. Manage our lifetime accordingly.
                goForegroundOrStopSelf()
            }
            // Start collecting exercise information. We might stop shortly (see above), in which
            // case launchWhenStarted takes care of canceling this coroutine.
            lifecycleScope.launchWhenStarted {
                healthServicesManager.exerciseUpdateFlow.collect {
                    when (it) {
                        is ExerciseMessage.ExerciseUpdateMessage ->
                            processExerciseUpdate(it.exerciseUpdate)
                        is ExerciseMessage.LapSummaryMessage ->
                            _exerciseLaps.value = it.lapSummary.lapCount
                    }
                }
            }
        }

        // If our process is stopped, we might have an active exercise. We want the system to
        // recreate our service so that we can present the ongoing notification in that case.
        return Service.START_STICKY
    }

    private fun goForegroundOrStopSelf() {
        lifecycleScope.launch {
            // We may have been restarted by the system. Check for an ongoing exercise.
            if (healthServicesManager.isExerciseInProgress()) {
                // Our exercise is running. Give the user a way into the app.
                postOngoingActivityNotification()
            } else {
                // We have nothing to do, so we can stop.
                stopSelf()
            }
        }
    }

    private fun processExerciseUpdate(exerciseUpdate: ExerciseUpdate) {
        val oldState = _exerciseState.value
        if (!oldState.isEnded && exerciseUpdate.state.isEnded) {
            // Our exercise ended. This could be because another app's exercise has superseded ours,
            // so dismiss any ongoing activity notification.
            removeOngoingActivityNotification()
        } else if (oldState.isEnded && exerciseUpdate.state == ExerciseState.ACTIVE) {
            // Reset laps.
            _exerciseLaps.value = 0
        }

        _exerciseState.value = exerciseUpdate.state
        _exerciseMetrics.value = exerciseUpdate.latestMetrics
        _exerciseDurationUpdate.value =
            ActiveDurationUpdate(exerciseUpdate.activeDuration, Instant.now())
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        handleBind()
        return localBinder
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        handleBind()
    }

    private fun handleBind() {
        if (!isBound) {
            isBound = true
            // Start ourself. This will begin collecting exercise state if we aren't already.
            startService(Intent(this, this::class.java))
            // As long as a UI client is bound to us, we can hide the ongoing activity notification.
            removeOngoingActivityNotification()
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isBound = false
        lifecycleScope.launch {
            // Client can unbind because it went through a configuration change, in which case it
            // will be recreated and bind again shortly. Wait a few seconds, and if still not bound,
            // manage our lifetime accordingly.
            delay(UNBIND_DELAY_MILLIS)
            if (!isBound) {
                goForegroundOrStopSelf()
            }
        }
        // Allow clients to re-bind. We will be informed of this in onRebind().
        return true
    }

    private fun removeOngoingActivityNotification() {
        if (isForeground) {
            Log.d(TAG, "Removing ongoing activity notification")
            isForeground = false
            stopForeground(true)
        }
    }

    private fun postOngoingActivityNotification() {
        if (!isForeground) {
            isForeground = true
            Log.d(TAG, "Posting ongoing activity notification")

            createNotificationChannel()
            startForeground(NOTIFICATION_ID, buildNotification())
        }
    }

    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL,
            NOTIFICATION_CHANNEL_DISPLAY,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(notificationChannel)
    }

    private fun buildNotification(): Notification {
        // Make an intent that will take the user straight to the exercise UI.
        val pendingIntent = NavDeepLinkBuilder(this)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.exerciseFragment)
            .createPendingIntent()
        // Build the notification.
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(NOTIFICATION_TEXT)
            .setSmallIcon(R.drawable.ic_run)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Ongoing Activity allows an ongoing Notification to appear on additional surfaces in the
        // Wear OS user interface, so that users can stay more engaged with long running tasks.
        val ongoingActivityStatus = OngoingActivityStatus.Builder()
            .addTemplate(NOTIFICATION_TEXT)
            .build()
        val ongoingActivity =
            OngoingActivity.Builder(applicationContext, NOTIFICATION_ID, notificationBuilder)
                .setAnimatedIcon(R.drawable.ic_run)
                .setStaticIcon(R.drawable.ic_run)
                .setTouchIntent(pendingIntent)
                .setStatus(ongoingActivityStatus)
                .build()
        ongoingActivity.apply(applicationContext)

        return notificationBuilder.build()
    }

    /** Local clients will use this to access the service. */
    inner class LocalBinder : Binder() {
        fun getService() = this@ExerciseService
    }

    private companion object {
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL = "com.example.exercise.ONGOING_EXERCISE"
        const val NOTIFICATION_CHANNEL_DISPLAY = "Ongoing Exercise"
        const val NOTIFICATION_TITLE = "Exercise Sample"
        const val NOTIFICATION_TEXT = "Ongoing Exercise"
        const val UNBIND_DELAY_MILLIS = 3_000L
    }
}

/** Keeps track of the last time we received an update for active exercise duration. */
data class ActiveDurationUpdate(
    /** The last active duration reported. */
    val duration: Duration = Duration.ZERO,
    /** The instant at which the last duration was reported. */
    val timestamp: Instant = Instant.now()
)
