# PID Departures

## To make catching your tram or bus in Prague easier!

[![Build APK](https://github.com/knapejar/pid-departures/actions/workflows/build.yml/badge.svg)](https://github.com/knapejar/pid-departures/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android](https://img.shields.io/badge/Android-24%2B-brightgreen.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple.svg)](https://kotlinlang.org)

<a href="https://github.com/knapejar/pid-departures/releases/latest"><img src="https://img.shields.io/badge/Download-Latest_Release-blue?style=for-the-badge&logo=android" alt="Download Latest Release" height="40"></a>

---

Prague Public Transport departure tracking application.
Built using Kotlin, Jetpack Compose, and Golemio Public Transport API.

1) Choose and save your favorite stops.
2) See upcoming departures at a glance on the home screen.
3) Tap a stop for detailed departure information or to remove it from your list.

![Example1](example1.jpg) ![Example2](example2.jpg)

## Setup

### Prerequisites
- Android Studio (latest stable version)
- Android SDK 24+
- Golemio API Key (free registration)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/knapejar/pid-departures
   cd PIDDepartures
   ```

2. **Get your Golemio API Key**
   - Visit [https://api.golemio.cz/api-keys](https://api.golemio.cz/api-keys)
   - Register for free account
   - Generate an API key

3. **Configure API Key**
   - Copy `local.properties.example` to `local.properties`
   - Open `local.properties` and add your API key:
     ```properties
     GOLEMIO_API_KEY=your_actual_api_key_here
     ```

4. **Build and Run**
   - Open project in Android Studio
   - Sync Gradle files
   - Run the app

## Features

- **Home Screen**: Displays saved stops with the next 3 upcoming departures, drag-and-drop reordering
- **Add Stop**: Search and select stops to track
- **Detail Screen**: Shows up to 10 planned departures with delete and settings options
- **Settings**: Customize stop direction display name
- **Auto-refresh**: Updates departure data every 60 seconds
- **Widget**: Home screen widget with departure information, click to open detail screen

## Technical Implementation

### Data Flow
1. User adds stop → Saved to Room DB
2. ViewModel observes DB → Emits Flow of saved stops
3. For each stop → API call every 60s
4. Parse response → Update UI with departures
5. Detail screen → Fetch more departures (limit 10)
6. Widget → Updates every 15 minutes via WorkManager

### API Integration
- **Endpoint**: `GET /v2/pid/departureboards`
- **Parameters**: 
  - `ids[]`: Stop ID(s)
  - `minutesAfter`: 60
  - `limit`: 3 (home) / 10 (detail/widget)
  - `filter`: routeHeadingOnce
- **Auth**: X-Access-Token header with Golemio API key

### Default Data
- Initial stop: "Anděl" (ASW ID: 1040)

See official Golemio API docs for more details: [https://api.golemio.cz/pid/docs/openapi/](https://api.golemio.cz/pid/docs/openapi/) or [https://api.golemio.cz/docs/public-openapi/](https://api.golemio.cz/docs/public-openapi/) or openapi.json file in the project.

## TODO

- [ ] Better stop search filtering
- [ ] Improve error handling and user feedback
- [ ] Unit and UI tests
- [ ] Consider train and bus support with IDOS or other provider

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.