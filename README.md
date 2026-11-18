# TicTacToe

A neon Tic-Tac-Toe game for **Android**, **iPhone** and **desktop**. It is one Kotlin codebase built with [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/).

## Features
- **vs AI** with three difficulty levels:
  - **Easy:** random moves
  - **Medium:** takes wins, blocks yours, likes the centre
  - **Hard:** minimax, can't be beaten
- **2 Players:** local mode on one device
- **Scoreboard** for the session
- **Animations:**
  - X and O draw themselves in with a neon glow
  - a gold line sweeps across the winning row
  - the board shakes on a draw
- **Haptic feedback** on phones

## Project layout
| Path | What |
| --- | --- |
| `composeApp/src/commonMain` | Shared game logic (`Board`, `AI`, `GameState`) and the whole UI (`App`, `GameBoard`, `Components`) |
| `composeApp/src/androidMain` | Android entry point, manifest, launcher icon |
| `composeApp/src/iosMain` | `MainViewController()` used by the iOS app |
| `composeApp/src/desktopMain` | Desktop window |
| `iosApp/` | Swift wrapper app plus `project.yml` ([XcodeGen](https://github.com/yonaskolb/XcodeGen)) |

## Requirements
- JDK 17 or newer
- Android SDK (Android Studio installs it). Point to it with `ANDROID_HOME` or a `local.properties` file containing `sdk.dir=...`
- For iOS only: a Mac with Xcode 15+ and XcodeGen

## Android
```
gradlew assembleDebug
```
The APK is written to `composeApp/build/outputs/apk/debug/composeApp-debug.apk`. To install it, use one of these:
- Connect the phone with USB debugging on and run `adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk`.
- Copy the APK to the phone and open it. You'll need to allow "Install unknown apps" for your file manager.

## Desktop
```
gradlew :composeApp:run
```
To build an installer for the current OS (`.msi`, `.dmg` or `.deb`), run `gradlew :composeApp:packageDistributionForCurrentOS`.

## iPhone
iOS apps can only be compiled on macOS. On every push, GitHub Actions ([.github/workflows/build.yml](.github/workflows/build.yml)) does three things:
- builds the app for the **iOS Simulator** (unsigned)
- runs the shared tests on iOS
- uploads `TicTacToe-ios-simulator.zip` as an artifact (the Android APK is uploaded too)

**Run the CI build in the Simulator (on a Mac):**
```
unzip TicTacToe-ios-simulator.zip
xcrun simctl boot "iPhone 15"   # or open Simulator.app
xcrun simctl install booted TicTacToe.app
xcrun simctl launch booted github.shkesar.tictactoe
```

**Run on a real iPhone:** the app has to be signed.
- **With a Mac:** run `cd iosApp && xcodegen generate && open iosApp.xcodeproj`. Select your Apple ID team under *Signing & Capabilities*, plug in the phone and press Run. A free Apple ID works, but the install expires after 7 days.
- **From CI:** you need a paid Apple Developer account. Store a signing certificate and provisioning profile as GitHub secrets, then change the `xcodebuild` step to archive and export an `.ipa`.

## Tests
```
gradlew :composeApp:desktopTest
```
These check the AI: Hard always draws against itself and never loses, and Medium takes wins and blocks yours.
