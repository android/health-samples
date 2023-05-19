# Health Connect Sample

This repository contains a sample to help you get started writing [Health Connect][health-connect] apps for Android.

## Setup

You will need:

*   An Android device or emulator running API level 27 or greater
*   The [Health Connect APK][health-connect-apk] installed on the device or emulator

## Health Connect Toolbox

The Health Connect Toolbox is a companion developer tool to help you test your app's integration. It can read and write data directly to Health Connect,
allowing you to test your app's CRUD operations.

You can download the toolbox [here][health-connect-toolbox]. 

The first time you open the Health Connect Toolbox app, you're taken to the permission settings under "Apps > Special app access > Display over other apps."
This permission allows the Health Connect Toolbox to show an overlay on top of other apps so that you can test reading and writing data without leaving the app you're developing.

## Screenshots

<img src="screenshots/hc1.png" height="300" alt="Screenshot"/> <img src="screenshots/hc2.png" height="300" alt="Screenshot"/> <img src="screenshots/hc3.png" height="300" alt="Screenshot"/>

## Getting started

This sample uses the Gradle build system. To build this project, use the "gradlew build" command or "File > Open" in Android Studio.

## Support

If you've found an error in this sample, please file an issue:
https://github.com/android/health-samples

Patches are encouraged, and may be submitted by forking this project and
submitting a pull request through GitHub. Please see [CONTRIBUTING][contributing] for more details.

[health-connect]: https://developer.android.com/health-connect
[health-connect-apk]: https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata
[contributing]: ../../CONTRIBUTING.md
[health-connect-toolbox]: https://goo.gle/health-connect-toolbox
