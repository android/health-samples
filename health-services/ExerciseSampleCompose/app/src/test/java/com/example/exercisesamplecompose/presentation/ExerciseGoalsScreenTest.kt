package com.example.exercisesamplecompose.presentation

import ExerciseGoalsScreen
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ResponsiveTimeText
import com.google.android.horologist.screenshots.FixedTimeSource
import com.google.android.horologist.screenshots.rng.WearDevice
import com.google.android.horologist.screenshots.rng.WearDeviceScreenshotTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class ExerciseGoalsScreenTest(override val device: WearDevice) : WearDeviceScreenshotTest(device) {
    @Test
    fun goals() = runTest {
        AppScaffold(
            timeText = { ResponsiveTimeText(timeSource = FixedTimeSource) }
        ) {
            ExerciseGoalsScreen(
                onSet = {},
                setGoals = { _ -> run {} }
            )
        }
    }
}