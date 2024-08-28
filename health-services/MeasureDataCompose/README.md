# Measure Data Sample (Compose)

This sample demonstrates receiving rapid data updates in the foreground using the `MeasureClient`
API.

### Running the sample

You will need a Wear device or emulator with Health Services installed. Open the sample project in
Android Studio and launch the app on your device or emulator.

On startup, the app checks whether heart rate data is available. If it is, you will see a screen
like this:

![heart rate available screenshot](screenshots/whs_measure_data_available.png)

On devices where heart rate data is not available, you will see a screen like this:

![heart rate unavailable screenshot](screenshots/whs_measure_data_not_available.png)

### Try it with synthetic data

With the sample running on an emulator running Wear OS 4 or higher, the emulator will automatically
generate synthetic data.

This sample demonstrates using `MeasureClient` to measure heart rate. With this datatype, the default
behavior of the emulator is to cycle between 60 - 150 bpm, in 5 bpm increments.

To use synthetic data on emulators or physical devices running Wear OS 3,
consult [the documentation](https://developer.android.com/health-and-fitness/guides/health-services/simulated-data#use_synthetic_data_on_wear_os_3)
for synthetic data commands.

