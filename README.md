# StatusPing By Sivd 🌐

> **BY SIVD**

[![Build & Release APK](https://github.com/Jeeva-zone/scanner-fav2/actions/workflows/build-apk.yml/badge.svg)](https://github.com/Jeeva-zone/scanner-fav2/actions/workflows/build-apk.yml)
[![Release](https://img.shields.io/github/v/release/Jeeva-zone/scanner-fav2?label=release&color=2ea043)](https://github.com/Jeeva-zone/scanner-fav2/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/Jeeva-zone/scanner-fav2/latest/total?label=downloads&color=0969da)](https://github.com/Jeeva-zone/scanner-fav2/releases/latest)
[![Platform](https://img.shields.io/badge/platform-Android%207.0%2B-brightgreen)](https://developer.android.com/about/versions/nougat)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose)](https://m3.material.io)

**StatusPing By Sivd** is a fast, modern Android network diagnostic and HTTP status
code checker built with **Kotlin** and **Jetpack Compose (Material 3)**.

---

## 📥 Download the App

Every push to `main` is built by the **Build & Release Android APK** workflow, which
publishes both APKs to the [`latest`](https://github.com/Jeeva-zone/scanner-fav2/releases/latest) release.

| APK | Build type | Download |
| --- | ---------- | -------- |
| `StatusPing-v1.0.apk` | release | [Download](https://github.com/Jeeva-zone/scanner-fav2/releases/download/latest/StatusPing-v1.0.apk) |
| `StatusPing-debug.apk` | debug | [Download](https://github.com/Jeeva-zone/scanner-fav2/releases/download/latest/StatusPing-debug.apk) |

### Install from a phone

1. Open the download link above, or go to the [Releases](https://github.com/Jeeva-zone/scanner-fav2/releases) tab.
2. Download **`StatusPing-v1.0.apk`**.
3. Open the file on your Android device. *(If prompted, allow installing apps from your browser or file manager.)*

### Install from CI artifacts

1. Open the [Actions](https://github.com/Jeeva-zone/scanner-fav2/actions) tab.
2. Click the latest **Build & Release Android APK** run.
3. Scroll to **Artifacts** and download **`StatusPing-APK`** (contains both APKs, retained 30 days).

---

## ✨ Features

- ⚡ **Instant Status Ping**: Check any HTTP/HTTPS domain, IP address, or port (e.g. `example.com`, `192.168.1.1:8080`, `[2001:db8::1]`).
- 🎯 **HTTP Status Badges**: Clean Material 3 status badges for 1xx, 2xx, 3xx, 4xx, and 5xx responses with latency in milliseconds.
- 🔄 **Redirect Tracking**: Detects when URLs redirect (301, 302, 307, 308) and shows final landing destination and hop counts.
- 🖼️ **Secondary Favicon Test**: Automatically tests `http://IP:80/favicon.ico` and reports status code and response time.
- ☁️ **Cloud Search Scanner**:
  - Automatically loads and synchronizes IP lists with secure local caching.
  - One-tap batch scanning with real-time progress indicators.
  - Multi-threaded scanning with filter chips (All, Online 200, Other).
  - Toggles for HTTP and Favicon scan targets.
  - Manual update button to refresh the cached list on demand.
- 🎨 **Modern Design**: Material 3 theming, dark mode support, and edge-to-edge rendering.

---

## Result card colours

The result card colour-codes every outcome so a list of hosts can be scanned at a glance.

| Colour | Meaning | Applies to |
| --- | --- | --- |
| Green | The host answered and returned one of the expected codes | `200`, `301`, `400`, `403` |
| Orange | The host answered, but with something else | every other code — `404`, `500`, `302`, `429`, … |
| Red | No response at all — the host is unreachable, timed out, or dead | timeout, DNS failure, connection refused, reset, SSL error |

The same rule drives the status pill, the large status code, the latency readout, the
favicon badge and the cloud scan results list.

---

## 🛠️ Building from Source

### Prerequisites

- Android Studio Ladybug (or newer) with Android SDK 36
- Java Development Kit **JDK 17** (AGP 9 requires 17 or newer)
- `ANDROID_HOME` set, or a `local.properties` file pointing at your SDK

### Commands

```bash
# Clone the repository
git clone https://github.com/Jeeva-zone/scanner-fav2.git
cd scanner-fav2

# Build a debug APK
./gradlew assembleDebug      # -> app/build/outputs/apk/debug/app-debug.apk

# Build a release APK
./gradlew assembleRelease    # -> app/build/outputs/apk/release/app-release.apk

# Run unit tests
./gradlew testDebugUnitTest
```

### Signing a release

Out of the box, release builds are signed with the default debug keystore so the build
never fails. To sign with your own upload key, set three environment variables before
building:

```bash
export KEYSTORE_PATH=/path/to/my-upload-key.jks
export STORE_PASSWORD=<store password>
export KEY_PASSWORD=<key password>   # key alias must be "upload"
```

When all three are present and the keystore file exists, `assembleRelease` signs with it
automatically. In CI, add them as repository secrets.

---

## 🧱 Tech Stack

| Layer | Choice |
| --- | --- |
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose, Material 3 (BOM 2025.08.00) |
| Networking | OkHttp 4.12 |
| Concurrency | Kotlin Coroutines |
| Architecture | `AndroidViewModel` + `StateFlow` |
| Build | Gradle 9.3.1, Android Gradle Plugin 9.1.1 |
| SDK | minSdk 24 (Android 7.0), targetSdk 36 |

---

## 👤 Author

Developed and maintained **BY SIVD**.

---

## 📄 License

No `LICENSE` file is included in this repository, so the default copyright applies —
all rights reserved. Add a `LICENSE` file (MIT and Apache-2.0 are the usual choices
for a project like this) to make the terms explicit.
