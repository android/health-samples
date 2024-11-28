package com.example.recordingapionmobilesample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.recordingapionmobilesample.helper.PermissionHelper
import com.google.android.gms.fitness.FitnessLocal

/**
 * The entry point into the sample.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val localRecordingClient by lazy { FitnessLocal.getLocalRecordingClient(this) }
        val permissionHelper by lazy { PermissionHelper(this) }

        setContent {
            RecordingApiOnMobileApp(
                localRecordingClient,
                permissionHelper
            )
        }
    }
}