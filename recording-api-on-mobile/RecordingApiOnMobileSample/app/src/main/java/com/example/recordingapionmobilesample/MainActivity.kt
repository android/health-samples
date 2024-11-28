package com.example.recordingapionmobilesample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.recordingapionmobilesample.helper.PermissionHelper
import com.example.recordingapionmobilesample.ui.theme.RecordingAPIOnMobileSampleTheme
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