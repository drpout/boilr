# Boilr
Android application which triggers an alarm when price of a cryptocurrency or bullion changes or reaches a target value on a given exchange.

Supports 2 types of alarms:

* Price Hit, were you can define an upper bound, a lower bound and an update interval (e.g. 30 s).
* Price Change, were you define a change amount (in currency or percentage) and a time frame (e.g. 1 day).

The alarm sound and vibration are configurable globally and for each alarm in particular. Lists of exchanges, pairs and alarms are fully searchable allowing a quick alarm setup and configuration.

Market data is retrieved directly from the exchanges using Wi-Fi or Mobile Data (if allowed). [libdynticker](https://github.com/andrefbsantos/libdynticker) is used to interface with the exchanges. Therefore, Boilr supports all the [exchanges available on libdynticker](https://github.com/andrefbsantos/libdynticker/#supported-exchanges) with all their traded pairs.

To create alarms Boilr uses [libpricealarm](https://github.com/andrefbsantos/libpricealarm).

## Troubleshooting

Trouble | Solution
------- | --------
Alarm not updating when phone is sleeping. | Set `Settings > Wi-Fi > Advanced > Keep Wi-Fi on during sleep` to `Always`

## Building
You need to fill out the [prerequisites for Android Maven Plugin](https://code.google.com/p/maven-android-plugin/wiki/GettingStarted) and to get the Android SDK for API 20. Then run `mvn validate` once to make Maven aware of the plugins we use to download and install dependencies which are not in Maven repositories. For then on you can use:

* `mvn package` to build an apk in debug mode.
* `mvn package -P release` to build an apk in release mode, which will be optimized, signed and aligned.
* `mvn android:deploy` to install the generated apk through USB in any connected phones.

## Versioning
Boilr follows [Semantic Versioning](http://semver.org) with the API being the user-interface.

## Debuging
On your shell you can use `export ANDROID_LOG_TAGS="ActivityManager:I Boilr:D *:S"` to filter logcat, then run `adb logcat` as usual.

## License
All Boilr code is licensed under [GNU GPL v3](/LICENSE) or later. Copyright belongs to the [contributors listed on GitHub](https://github.com/andrefbsantos/boilr/graphs/contributors).

The Boilr logo and artworks were created by [Ricardo Duarte](http://cargocollective.com/ricardoduarte) and are dual licensed under [CC-BY-SA 3.0 Unported](https://creativecommons.org/licenses/by-sa/3.0) or [GPLv3+](/LICENSE).

## Donations
Donations and tips are welcomed.

* BTC: [1PHuSWfuAwR6oz9qV93rTdMVozfM85Qqxx](bitcoin:1PHuSWfuAwR6oz9qV93rTdMVozfM85Qqxx)
* XMR: 43KcTwXw5D43YRE8MSG4D7X3S2kjU6ehBABucyKAVNYnKbNxpGWZ8zGbEhCq79N4ZGDtefU5w4AGoeR6eNB5yMbEPhAw3FT