# Boilr
[![Get Boilr on F-Droid](https://github.com/drpout/boilr/raw/master/src/main/img/store_badges/f-droid.png)](https://f-droid.org/repository/browse/?fdid=mobi.boilr.boilr)  [![Get Boilr on Aptoide](https://github.com/drpout/boilr/raw/master/src/main/img/store_badges/aptoide.png)](https://drpout.store.aptoide.com/app/market/mobi.boilr.boilr/9/22752431/Boilr)  [![Download Boilr from GitHub](https://github.com/drpout/boilr/raw/master/src/main/img/store_badges/github.png)](https://github.com/drpout/boilr/releases)  [![Get Boilr on Google Play](https://github.com/drpout/boilr/raw/master/src/main/img/store_badges/google_play.png)](https://play.google.com/store/apps/details?id=mobi.boilr.boilr)

[![Translation status](https://hosted.weblate.org/widgets/boilr/-/svg-badge.svg)](https://hosted.weblate.org/engage/boilr/?utm_source=widget)  [![Bountysource](https://api.bountysource.com/badge/tracker?tracker_id=6817098)](https://www.bountysource.com/teams/drpout/issues?tracker_ids=6817098)

Android application which monitors Bitcoin, cryptocurrencies, cryptoassets and derivatives, triggering alarms according to user settings.

![hit alarm creation](https://github.com/drpout/boilr/raw/master/src/main/img/screenshots/hit_creation.png) ![alarm list](https://github.com/drpout/boilr/raw/master/src/main/img/screenshots/alarm_list.png) ![hit alarm firing](https://github.com/drpout/boilr/raw/master/src/main/img/screenshots/hit_firing.png)

Supports 2 alarm types:

* Price Hit - Triggered when price crosses the alarm's upper or lower limit. One limit may be left empty to create an alarm with a single trigger.
* Price Change - Triggered when price changes more than X amount (in currency or percentage) in a specified time frame (e.g. 15 min). It uses a rolling time frame: price is fetched with a given update interval (e.g. 30 s) and compared with the price fetched one time frame ago (e.g. 15 min ago).

Sound and vibration are configurable globally, and individually for each alarm. Lists of exchanges, pairs and alarms are fully searchable, allowing quick setup and configuration. The alarm list behaves as your ticker, where you can reorder your alarms and directly change their limits. Alarms can also be set to snooze automatically on price retrace. Triggered alarms ring, displaying their status, until you turn them off. Just like an alarm clock.

Market data is retrieved directly from the exchanges via Wi-Fi or Mobile Data (if allowed). [libdynticker](https://github.com/drpout/libdynticker) is used to interface with the exchanges. Thus, Boilr supports all [exchanges available on libdynticker](https://github.com/drpout/libdynticker/#supported-exchanges), as all their pairs.

To create alarms Boilr uses [libpricealarm](https://github.com/drpout/libpricealarm).

Other libraries we use: [changeloglib](https://github.com/gabrielemariotti/changeloglib) and [UndoBar](https://github.com/soarcn/UndoBar).

Check Boilr's website to see it in action: http://boilr.mobi Website source-code is available at [boilr-site](https://github.com/drpout/boilr-site).

## Getting help
Bug reports, feature requests and general help questions should be submitted on [Boilr's GitHub issues](https://github.com/drpout/boilr/issues). Try being specific when choosing the right title and label for your issue.

You can browse through a FAQ by checking all [issues labeled as questions](https://github.com/drpout/boilr/issues?q=label%3Aquestion).

## Troubleshooting

Trouble | Solution
------- | --------
Alarm not updating when phone is sleeping. | Set `Settings > Wi-Fi > Advanced > Keep Wi-Fi on during sleep` to `Always`

## Reasons for permissions

Permission | Reason
---------- | ------
Read the contents of your SD card | Used to fetch your custom ringtones.
Disable your screen lock | So you can turn off a ringing alarm without unlocking your device.
Full network access | To fetch data from the exchanges.
View network connections | To know whether you're using Wi-Fi or Mobile Data, allowing Mobile Data restriction. 
Run at startup | To grab your active alarms and start updating them. 
Control vibration | Allows vibration when an alarm is triggered. 
Prevent phone from sleeping | Used to keep showing the alarm triggered activity until you press the turn off button.

## Contributing

### Translating
New translations and reviews are much appreciated. If you have 10 minutes to spare just open [Boilr @ Hosted Weblate](https://hosted.weblate.org/projects/boilr/) and start editing. More info at [CONTRIBUTING.md](/CONTRIBUTING.md).

### Coding
If you know how to program take a look at the [open issues](https://github.com/drpout/boilr/issues). Check for bugs or features you find interesting and feel free to take them into your hands. If you have any doubt just post your questions on the issue's comments. When done, open a pull request. We promise to review it quickly.

If you would like to add more exchanges to Boilr check [libdynticker's contributing guide](https://github.com/drpout/libdynticker/blob/master/CONTRIBUTING.md).

## Building
1. Fill out the [prerequisites for Android Maven Plugin](http://simpligility.github.io/android-maven-plugin/index.html#required_setup).
2. Install Android API 26 SDK and Android Support Repository.
3. `mvn install:install-file -DgroupId=android -DartifactId=android -Dversion=8.0.0 -Dpackaging=jar -Dfile=$ANDROID_HOME/platforms/android-26/android.jar`
4. `mvn install:install-file install:install-file -Dfile=$ANDROID_HOME/extras/android/m2repository/com/android/support/support-annotations/22.1.1/support-annotations-22.1.1.jar -DpomFile=$ANDROID_HOME/extras/android/m2repository/com/android/support/support-annotations/22.1.1/support-annotations-22.1.1.pom`

For now on you can use:

* `mvn package -P debug` to build an apk in debug mode.
* `mvn package -P release` to build an apk in release mode, which will be optimized, signed and aligned.
* `mvn android:deploy` to install the generated apk in any connected phones or emulators.

Note: `mvn package` with no profile associated builds an unsigned release mode apk, unsuitable for deploy. It is meant for further processing by downstream projects like F-Droid.

## Debuging
`adb logcat "ActivityManager:I AndroidRuntime:E Boilr:D *:S"` runs logcat with a nice filter to debug Boilr.

## Importing into Eclipse
If you are no vi or emacs ninja you can use Eclipse to ease your work in Boilr.

1. Install and configure the [ADT plugin](http://android.magicer.xyz/sdk/installing/installing-adt.html).
2. ~~Install [m2e-android](https://rgladwell.github.io/m2e-android).~~ (Eclipse Mars already ships with Maven.)
3. Clone [changeloglib](https://github.com/gabrielemariotti/changeloglib/).  
3.1 Checkout tag `v.1.5.2` with `git checkout v.1.5.2`.  
3.2 Go to `File->Import->Existing Android Code Into Workspace`, import just what's in the `ChangeLogLibrary` directory and name the new project as `ChangeLogLibrary`.  
3.3  In the project's Java Build Path remove source folder `src` and add `java`.
4. Clone [UndoBar](https://github.com/soarcn/UndoBar).  
4.1  `git checkout fcb99913808ee47a3803d3f800cbee6b7678ba21`  
4.2 Go to `File->Import->Existing Android Code Into Workspace`, import just what's in the `library` directory and name the new project as `UndoBar`.  
4.3 Right click in the project, go to `Android Tools` and run `Add Support Library...`
5. Go to `File->Import->Existing Projects into Workspace` and select Boilr's git root directory.

Use Eclipse only for writing code. Always compile in the shell using Maven.

Note: we do plan to migrate to a Gradle build system and Android Studio IDE in the near future.

## Versioning
Boilr follows [Semantic Versioning](http://semver.org) with the API being the user-interface.

## License and authorship
Boilr code licensed under [GNU GPL v3](/LICENSE) or later. Copyright belongs to [André Filipe Santos](https://github.com/andrefbsantos), [David Ludovino](https://github.com/dllud) and other [contributors listed on GitHub](https://github.com/drpout/boilr/graphs/contributors), unless otherwise stated.

Concept, design, [icon](src/main/img/icons/ic_boilr.ai) and [artwork](src/main/img) by [Algazarra](https://www.behance.net/algazarra). Icon and artwork dual licensed under [CC-BY-SA 4.0 International](https://creativecommons.org/licenses/by-sa/4.0) or [GPLv3+](/LICENSE). Third-party graphics' authorship and license information in [AUTHORS.md](src/main/img/AUTHORS.md).

Translators:

* Chinese: [Jan Xie](https://github.com/janx), Song and [YFdyh000](https://hosted.weblate.org/user/yfdyh000)
* Czech: [Jaroslav Lichtblau](https://github.com/svetlemodry), [Michal Čihař](https://github.com/nijel) and [Jirka Daněk](https://hosted.weblate.org/user/jirkadanek)
* French: [Tuux](http://www.rtnp.org), [M for Momo](http://www.rtnp.org) and [therealmarco](https://hosted.weblate.org/user/therealmarco)
* German: Luca Rui
* Italian: Francesca Mautone and [Mario Frasca](https://hosted.weblate.org/user/mfrasca)
* Japanese: [naofum](https://hosted.weblate.org/user/naofum)
* Polish: [Michal Pleban](https://pl.linkedin.com/in/michalpleban) and agilob
* Russian: [ryna Pruitt](https://hosted.weblate.org/user/irynapruitt)
* Spanish: Mariona Martinez and [Luis Garcia](https://hosted.weblate.org/user/luigars)

## Donations and tips
In case you wanna pay us a beer ;)

* BTC: 1PHuSWfuAwR6oz9qV93rTdMVozfM85Qqxx
* XMR: 43KcTwXw5D43YRE8MSG4D7X3S2kjU6ehBABucyKAVNYnKbNxpGWZ8zGbEhCq79N4ZGDtefU5w4AGoeR6eNB5yMbEPhAw3FT
* LTC: LUC5BRtvrJRTcp2LAP1XJe2h718rchQXuR
* ETH: 0xBFD1802F928db59324b67689D1186661a124FBcA
