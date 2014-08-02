Boilr
==================

Android application which triggers an alarm when price of a cryptocurrency changes or reaches target value on a given exchange.

Boilr uses [libdynticker](https://github.com/andrefbsantos/libdynticker) to get data from the exchanges. Therefore, it supports all the exchanges available on libdynticker with all their traded pairs.

To create alarms Boilr uses [libpricealarm](https://github.com/andrefbsantos/libpricealarm).


Building
---------------------
You need to fill out the [prerequisites for Android Maven Plugin](https://code.google.com/p/maven-android-plugin/wiki/GettingStarted) and to get the Android SDK for Android 3.0 (API 11). Then you can run `mvn package` to build an APK which can be deployed to a device or emulator.