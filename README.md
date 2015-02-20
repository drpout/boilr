# Boilr
[![Translation status](https://hosted.weblate.org/widgets/boilr/-/svg-badge.svg)](https://hosted.weblate.org/engage/boilr/?utm_source=widget)

Android application which monitors Bitcoin, cryptocurrencies and bullion prices, and triggers alarms according to user settings.

Supports 2 types of alarms:

* Price Hit - Triggered when price crosses the alarm's upper or lower limit.
* Price Change - Triggered when price changes more than X amount (in currency or percentage) in a specified time frame (e.g. 15 min). It uses a rolling time frame: price is fetched with a given update interval (e.g. 30 s) and compared with the price fetched one time frame ago (e.g. 15 min ago).

Sound and vibration are configurable globally and individually for each alarm. Lists of exchanges, pairs and alarms are fully searchable, allowing a quick alarm setup and configuration. Alarms can be reordered. Disabled alarms keep updating while (and only while) the alarm list is visible, acting as tickers.

Market data is retrieved directly from the exchanges using Wi-Fi or Mobile Data (if allowed). [libdynticker](https://github.com/andrefbsantos/libdynticker) is used to interface with the exchanges. Therefore, Boilr supports all [exchanges available on libdynticker](https://github.com/andrefbsantos/libdynticker/#supported-exchanges), as all their pairs.

To create alarms Boilr uses [libpricealarm](https://github.com/andrefbsantos/libpricealarm).

Other libraries we use: [changeloglib](https://github.com/gabrielemariotti/changeloglib).

Check Boilr's website to see it in action: http://boilr.mobi The website source-code is available at [boilr-site](https://github.com/andrefbsantos/boilr-site).

## Troubleshooting

Trouble | Solution
------- | --------
Alarm not updating when phone is sleeping. | Set `Settings > Wi-Fi > Advanced > Keep Wi-Fi on during sleep` to `Always`

## Building
1. Fill out the [prerequisites for Android Maven Plugin](https://code.google.com/p/maven-android-plugin/wiki/GettingStarted#Prerequisites).
2. Install Android API 21 SDK and Android Support Library. 
3. `mvn install:install-file -DgroupId=android -DartifactId=android -Dversion=5.0_r2 -Dpackaging=jar -Dfile=$ANDROID_HOME/platforms/android-21/android.jar`
4. `mvn install:install-file -DgroupId=com.android.support -DartifactId=support-annotations -Dversion=21.0.0 -Dpackaging=jar -Dfile=$ANDROID_HOME/extras/android/support/annotations/android-support-annotations.jar`
5. Run `mvn validate` to make Maven aware of the plugins used to download and install dependencies which are not in Maven repositories.

For now on you can use:

* `mvn package -P debug` to build an apk in debug mode.
* `mvn package -P release` to build an apk in release mode, which will be optimized, signed and aligned.
* `mvn android:deploy` to install the generated apk in any connected phones or emulators.

Note: `mvn package` with no profile associated builds an unsigned release mode apk, unsuitable for deploy. It is meant for further processing by downstream projects like F-Droid.

## Debuging
On your shell you can use `export ANDROID_LOG_TAGS="ActivityManager:I Boilr:D *:S"` to filter logcat, then run `adb logcat` as usual.

## Importing into Eclipse
If you are no vi or emacs ninja you can use Eclipse to ease your work in Boilr.

1. Install and configure the [ADT plugin](https://developer.android.com/sdk/installing/installing-adt.html).
2. Install [m2e-android](https://rgladwell.github.io/m2e-android).
3. Install Android API 11 SDK and keep it alongside API 21. Eclipse uses API 11 for compatibility checks as it is our minSdkVersion.
4. Clone [changeloglib](https://github.com/gabrielemariotti/changeloglib/). Go to `File->Import->Existing Android Code Into Workspace`, import just what's in the `ChangeLogLibrary` directory and name the new project as `ChangeLogLibrary`.
5. Clone [UndoBar](https://github.com/soarcn/UndoBar). Go to `File->Import->Existing Android Code Into Workspace`, import just what's in the `library` directory and name the new project as `UndoBar`.   
6. Go to `File->Import->Existing Projects into Workspace` and select Boilr's git root directory.

Use Eclipse only for writing code. Always compile in the shell using Maven.

Note: we do plan to migrate to a Gradle build system and Android Studio IDE in the near future.

## Versioning
Boilr follows [Semantic Versioning](http://semver.org) with the API being the user-interface.

## License and authorship
Boilr code licensed under [GNU GPL v3](/LICENSE) or later. Copyright belongs to [André Filipe Santos](https://github.com/andrefbsantos), [David Ludovino](https://github.com/dllud) and other [contributors listed on GitHub](https://github.com/andrefbsantos/boilr/graphs/contributors), unless otherwise stated.

Concept, design, [icon](src/main/img/icon.ai) and [artwork](src/main/img) by [Ricardo Duarte](http://vimeo.com/ricardoduarte). Icon dual licensed under [CC-BY-SA 4.0 International](https://creativecommons.org/licenses/by-sa/4.0) or [GPLv3+](/LICENSE).

Translators:

* Chinese: [Jan Xie](https://github.com/janx) and Song
* Czech: [Jaroslav Lichtblau](https://github.com/svetlemodry) and [Michal Čihař](https://github.com/nijel)
* German: Luca Rui
* Italian: Francesca Mautone
* Polish: [Michal Pleban](https://pl.linkedin.com/in/michalpleban)
* Spanish: Mariona Martinez

## Donations and tips
In case you wanna pay us a beer ;)

* BTC: 1PHuSWfuAwR6oz9qV93rTdMVozfM85Qqxx
* XMR: 43KcTwXw5D43YRE8MSG4D7X3S2kjU6ehBABucyKAVNYnKbNxpGWZ8zGbEhCq79N4ZGDtefU5w4AGoeR6eNB5yMbEPhAw3FT
* DOGE: DSSJyAtJy9xokkbfTUQ5NQov8M1WQ8FaYg
* NXT: NXT-PMQL-5RXJ-QE46-2L4SQ
