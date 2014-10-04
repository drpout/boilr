# Boilr
Android application which triggers an alarm when price of a cryptocurrency or bullion changes or reaches target value on a given exchange.

Boilr uses [libdynticker](https://github.com/andrefbsantos/libdynticker) to get data from the exchanges. Therefore, it supports all the exchanges available on libdynticker with all their traded pairs.

To create alarms Boilr uses [libpricealarm](https://github.com/andrefbsantos/libpricealarm).

## Building
You need to fill out the [prerequisites for Android Maven Plugin](https://code.google.com/p/maven-android-plugin/wiki/GettingStarted) and to get the Android SDK for Android 3.0 (API 11). Then run `mvn validate` once to make Maven aware of the plugins we use to download and install dependencies which are not in Maven repositories. For then on you can run `mvn package` to build an APK which can be deployed to a device or emulator.

## Versioning
Boilr follows [Semantic Versioning](http://semver.org) with the API being the user-interface.

## Debuging
On your shell you can use `export ANDROID_LOG_TAGS="ActivityManager:I Boilr:D *:S"` to filter logcat, then run `adb logcat` as usual.

## Donations
Donations and tips are welcomed.

* BTC: [1PHuSWfuAwR6oz9qV93rTdMVozfM85Qqxx](bitcoin:1PHuSWfuAwR6oz9qV93rTdMVozfM85Qqxx)
* XMR: 43KcTwXw5D43YRE8MSG4D7X3S2kjU6ehBABucyKAVNYnKbNxpGWZ8zGbEhCq79N4ZGDtefU5w4AGoeR6eNB5yMbEPhAw3FT