# ARCHIVED REPO
This repo is no longer being worked on, there is now an official fork of Otter by the Funkwhale project. Find it [here](https://dev.funkwhale.audio/funkwhale/funkwhale-android).

## Otter for Funkwhale
This is my personal fork of Otter for Funkwhale. I'm planning to work on several improvements to Otter here, including but not limited to:
* Improvements to the offline experience or the situation when the connected funkwhale instance is down
* Some UI changes
* Extra sorting capabilities
* Easier downloading of favorited tracks
* Embedded lyrics support

I'll see how much of this I can manage to implement, if you have any ideas or suggestions that aren't too large feel free to ask me (: If you're curious about Otter in general read the project readme below!

![](https://img.shields.io/github/license/apognu/otter?style=flat-square)
[![](https://img.shields.io/github/workflow/status/apognu/otter/Continuous%20develop%20build?label=develop&style=flat-square)](https://github.com/apognu/otter/actions?query=workflow%3A%22Continuous+develop+build%22)
[![](https://img.shields.io/badge/Play%20Store-otter-informational?style=flat-square)](https://play.google.com/store/apps/details?id=com.github.apognu.otter)
[![](https://img.shields.io/badge/IzzySoft-otter-informational?style=flat-square)](https://apt.izzysoft.de/fdroid/index/apk/com.github.apognu.otter)
[![](https://img.shields.io/badge/APK-otter-informational?style=flat-square)](https://github.com/apognu/otter/releases) [![](https://translate.funkwhale.audio/widgets/otter/-/android/svg-badge.svg)](https://translate.funkwhale.audio/projects/otter/android/)

Otter is a native Android music player for [Funkwhale](https://funkwhale.audio), native to both Android (developed in Kotlin) and to Funkwhale (uses its native API instead of Subsonic).

You can get help and discuss Otter on Matrix on [#otter:matrix.org](https://matrix.to/#/#otter:matrix.org).

![Otter graphic](https://github.com/apognu/otter/raw/develop/app/src/main/play/listings/en-US/graphics/feature-graphic/1.png)

## State

A beta version of the app can be downloaded on [Google Play](https://play.google.com/store/apps/details?id=com.github.apognu.otter), on [IzzySoft](https://apt.izzysoft.de/fdroid/index/apk/com.github.apognu.otter) (F-Droid-compatible repository) or through [GitHub releases](https://github.com/apognu/otter/releases). Please bear with it, there **will** be bugs, there **will** be crashes and there **will** be performance or UX issues.

Otter's features, as of this writing, are the following:

 * Basic collection browsing (artists, albums and tracks)
 * Playlists listing
 * Favorites management (listing and add/remove)
 * Track search
 * Queue management
 * Caching of played tracks (played tracks work offline)
 * Download tracks for offline playback
 * Radios playback
 * Dark mode! 🎉

Otter will try to behave as you would expect a mobile music player to, meaning integrating with the OS's media controls (including headset controls) or pause on incoming calls. If there is anything you would like it to do, please [open an issue](https://github.com/apognu/otter/issues/new).

## Screenshots

<img src="https://github.com/apognu/otter/raw/develop/app/src/main/play/listings/en-US/graphics/phone-screenshots/1.png" width="200" /> <img src="https://github.com/apognu/otter/raw/develop/app/src/main/play/listings/en-US/graphics/phone-screenshots/2.png" width="200" /> <img src="https://github.com/apognu/otter/raw/develop/app/src/main/play/listings/en-US/graphics/phone-screenshots/3.png" width="200" /> <img src="https://github.com/apognu/otter/raw/develop/app/src/main/play/listings/en-US/graphics/phone-screenshots/4.png" width="200" /> <img src="https://github.com/apognu/otter/raw/develop/app/src/main/play/listings/en-US/graphics/phone-screenshots/5.png" width="200" /> <img src="https://github.com/apognu/otter/raw/develop/app/src/main/play/listings/en-US/graphics/phone-screenshots/6.png" width="200" /> <img src="https://github.com/apognu/otter/raw/develop/app/src/main/play/listings/en-US/graphics/phone-screenshots/7.png" width="200" />

## Translation

Otter is being translated by the community through [Weblate](https://translate.funkwhale.audio/projects/otter/android/). If you would like to contribute to its localization or add a new language, you can help out there.

Thanks to the Funkwhale project for hosting us on their instance.
