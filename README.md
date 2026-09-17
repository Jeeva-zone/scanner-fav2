# StatusPing By Sivd 🌐

> **BY SIVD**

**StatusPing By Sivd** is a fast, modern Android network diagnostic and HTTP status code checker built with **Kotlin** and **Jetpack Compose (Material 3)**.

---

## 📥 Download the App

You can download and install the latest Android APK directly from this repository:

1. **GitHub Releases (Recommended)**:
   - Go to the [**Releases**](https://github.com/releases) tab on the right side of this repository.
   - Under the latest release (or `latest`), download **`StatusPing-v1.0.apk`**.
   - Open the downloaded file on your Android phone to install. *(If prompted, allow installing apps from your browser or file manager).*

2. **GitHub Actions Artifacts**:
   - Go to the [**Actions**](https://github.com/actions) tab at the top of this repository.
   - Click on the latest workflow run under **Build & Release Android APK**.
   - Scroll to the bottom to the **Artifacts** section and download **`StatusPing-APK`**.

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

## 🛠️ Building from Source

To build the project locally using Gradle:

### Prerequisites
- Android Studio Ladybug (or newer) / Android SDK
- Java Development Kit (JDK 17+)

### Commands
```bash
# Clone the repository
git clone <your-repo-url>
cd StatusPing

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest
```

The compiled APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 👤 Author
Developed and maintained **BY SIVD**.

---

## 📄 License
This project is open source and available under standard open source licensing.
