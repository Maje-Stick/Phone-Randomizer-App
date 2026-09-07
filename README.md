# Randomizer

A single-purpose Android app: thirteen randomizers, one tap each.

Coin flip · Dice · Number range · Pick from a list · Shuffle order · Split into teams ·
Weighted pick · Password · Draw cards · Yes or no · Random color · Random date · Random letters

Kotlin + Jetpack Compose, Material 3, no third-party libraries, no network permission,
no analytics. Minimum Android 8.0 (API 26).

## Build it from your phone

You don't need a PC. GitHub compiles the APK for you.

1. Create a new **private** repo on github.com (the mobile site works fine).
2. Upload the contents of this folder to it. The GitHub mobile web UI can upload files,
   but it's fussy with folders — the easiest route is an Android git client
   (Termux with `git`, or an app like MGit) to push the whole tree at once.
3. The push triggers `.github/workflows/build.yml` automatically. Watch the **Actions**
   tab; the build takes about 3–5 minutes on a cold cache.
4. When it's green, open the run and download the **randomizer-debug-apk** artifact.
   It arrives as a `.zip` — extract it and install the `.apk` inside.
5. Android will ask you to allow installs from your browser or file manager. That's
   expected for anything not from the Play Store.

You can also trigger a build by hand from Actions → Build APK → **Run workflow**.

## Build it on a PC

Open the folder in Android Studio (Ladybug or newer). It will offer to generate the
Gradle wrapper on first sync. Then `Run`, or `./gradlew assembleDebug` for an APK at
`app/build/outputs/apk/debug/`.

## What's where

| File | What it holds |
| --- | --- |
| `Randomizers.kt` | All the randomization logic as pure functions, no UI |
| `ToolScreens.kt` | One composable per tool |
| `Ui.kt` | Shared pieces: scaffold, result board, stepper, inputs |
| `Theme.kt` | Colors and the type scale |
| `MainActivity.kt` | Tool registry and the home grid |

To add a fourteenth randomizer: write the function in `Randomizers.kt`, write a screen in
`ToolScreens.kt` modeled on `LetterScreen`, then add one line to `TOOLS` and one branch to
the `when` in `MainActivity.kt`.

## Notes on the randomness

Everything uses `kotlin.random.Random` except the password generator, which uses
`SecureRandom`. The password generator also guarantees at least one character from every
enabled set and skips look-alike characters (`I l 1 O 0`) so you can read a password off
the screen without squinting.

The debug APK is signed with GitHub's throwaway debug key. That's fine for your own
device; you'd need a real keystore before distributing it.
