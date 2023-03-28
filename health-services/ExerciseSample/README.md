# Exercise Sample

This sample demonstrates managing an exercise experience using the `ExerciseClient` API.

### Running the sample

You will need a Wear device or emulator with Health Services installed. Open the sample project in
Android Studio and launch the app on your device or emulator.

On startup, the app checks the device capabilities. If the necessary exercise capabilities are
available, you will see a screen like this:

![exercise available screenshot](screenshots/exercise_available.png)

When you press start, the app configures a running exercise and starts it. (You may need to use the
[synthetic data provider](#using-synthetic-data) so that Health Services doesn't immediately pause
the exercise due to no user activity.) While the exercise is active, the UI will show the exercise
duration, heart rate bpm, calories burned, distance traveled, and the number of laps. To mark a lap,
press one of the hardware buttons around the watch frame.

![exercise in progress screenshot](screenshots/exercise_in_progress.png)

While an exercise is in progress, if you leave the app, an ongoing notification appears, offering
quick return to the exercise screen.

![ongoing notification screenshot](screenshots/ongoing_notification.png)

On devices where the exercise capability is not available, you will see a screen like this:

![exercise unavailable screenshot](screenshots/exercise_not_available.png)

### Using synthetic data

On some devices, you can get Health Services to generate "fake" activity for testing purposes via
the synthetic data provider. See [Use synthetic data with Health Services](https://developer.android.com/training/wearables/health-services/synthetic-data)
for more information.
