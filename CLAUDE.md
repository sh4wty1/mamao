# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A personal Android-only downloader built on **yt-dlp**. Its reason to exist is the **Android
share-sheet flow**: share a link from TikTok/YouTube/Instagram/etc. into the app and pick
Video or Audio with one tap. There is also a normal main screen (paste a link, choose
Video/Audio). No iOS, no Play Store target. Bias toward simplicity over features.

## Build & run

```bash
# First checkout: generate the Gradle wrapper jar (or just open the folder in Android Studio,
# which does this automatically). Needs a local Gradle >= 8.11.
gradle wrapper

./gradlew assembleDebug          # build the debug APK
./gradlew installDebug           # build + install on a connected device
./gradlew lintDebug              # Android lint
```

There are no unit tests yet. **The library only ships native libs for `arm64-v8a`** (see
`abiFilters` in `app/build.gradle.kts`), so it will not run on an x86 emulator — test on a real
arm64 device.

## yt-dlp engine — the one non-obvious thing

yt-dlp is Python. We do not shell out or run a server; we use the
`io.github.junkfood02.youtubedl-android` library, which bundles Python + the yt-dlp binary
(+ ffmpeg) as native libs.

- The engine is initialized **once, off the main thread**, in `DownloaderApp.onCreate`
  (`YoutubeDL.getInstance().init()` + `FFmpeg.getInstance().init()`). First launch extracts the
  Python payload, which is slow. `DownloaderApp.isReady` gates downloads; `Downloader.awaitEngine`
  polls it. Never call the engine before it's ready or off-thread init has finished.
- `android:extractNativeLibs="true"` + `jniLibs.useLegacyPackaging = true` are **required** — the
  Python runtime must be unpacked on install so yt-dlp can `dlopen` it. Don't remove them.
- yt-dlp breaks whenever sites change their pages. The fix is updating the **binary at runtime**
  via `YoutubeDL.getInstance().updateYoutubeDL(context, channel)` — independent of shipping a new
  app version. This is wired up in `download/Updater` (NIGHTLY channel) and triggered by the
  refresh button in the `HomeScreen` top bar.

## Architecture

Modern Android Development: single-activity Compose, thin layers, Kotlin only. Package root
`com.sh4wty.downloader`.

- **Two entry points, one engine.** `MainActivity` hosts the full Compose UI (`ui/HomeScreen`).
  `ShareReceiverActivity` is translucent (theme `Theme.Downloader.Transparent`), listens for
  `ACTION_SEND` `text/plain`, extracts the URL with `util/UrlExtractor`, and floats the shared
  `ui/ChooserButtons` chooser over the source app. Both just call `DownloadService.start(...)`.
- **`download/` is the core.**
  - `Downloader` (object) wraps the engine: builds the `YoutubeDLRequest` (video = `bv*+ba/b`
    merged to mp4; audio = `-x --audio-format mp3`), downloads into an app-private scratch dir,
    then hands the file to `data/StorageHelper`.
  - `DownloadService` is a foreground service (type `dataSync`) — it owns the coroutines so
    downloads survive backgrounding, drives the progress notification, and stops itself when idle.
  - `DownloadManager` (object) is the single source of truth: a `StateFlow<List<DownloadTask>>`
    the UI collects. The service mutates it; the UI only reads it.
- **Storage is scoped.** `StorageHelper` downloads into `getExternalFilesDir` then publishes the
  result into `Download/Downloader` via **MediaStore**. This is why there is **no**
  `WRITE_EXTERNAL_STORAGE` permission — don't add one.
- **State flows one way:** Service → `DownloadManager` StateFlow → Compose. Don't let the UI
  start downloads directly or hold download state; go through `DownloadService.start`.

## Conventions

- Kotlin, Compose Material 3, dynamic color on Android 12+ (`ui/theme/DownloaderTheme`).
- User-facing strings are Portuguese and live in `res/values/strings.xml`.
- Permissions: `INTERNET`, `FOREGROUND_SERVICE(_DATA_SYNC)`, `POST_NOTIFICATIONS` (runtime,
  requested in `MainActivity`). Downloads still work without the notification permission — the
  service just skips notifying.
