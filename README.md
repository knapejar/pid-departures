# PID Departures

Prague Public Transport departure tracking application.

## Features

- **Home Screen**: Displays saved stops with the next 3 upcoming departures and time in minutes
- **Add Stop**: Select stop, transport type, and direction to track
- **Detail Screen**: Shows up to 10 planned departures for a selected stop with delete option
- **Auto-refresh**: Updates departure data every 60 seconds

## Technical Implementation

### Architecture
- **Pattern**: MVVM (Model-View-ViewModel)
- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Navigation**: Jetpack Navigation Compose
- **API**: Golemio Public Transport API (v2/pid/departureboards)
- **Storage**: Room Database for persistent stop management
- **Async**: Kotlin Coroutines + Flow

### Project Structure
```
app/
├── data/
│   ├── local/          # Room database, entities, DAOs
│   ├── remote/         # Retrofit API service, DTOs
│   └── repository/     # Repository pattern implementation
├── domain/
│   └── model/          # Domain models
├── ui/
│   ├── home/           # Home screen with saved stops list
│   ├── add/            # Add stop screen
│   ├── detail/         # Stop detail screen
│   ├── components/     # Reusable UI components
│   └── theme/          # App theme
└── util/               # Utilities, extensions
```

### Dependencies
- Jetpack Compose
- Jetpack Navigation Compose
- Room Database
- Retrofit + OkHttp
- Kotlin Coroutines + Flow
- Hilt (Dependency Injection)
- Coil (Image loading for icons)

### Data Flow
1. User adds stop → Saved to Room DB
2. ViewModel observes DB → Emits Flow of saved stops
3. For each stop → API call every 60s
4. Parse response → Update UI with departures
5. Detail screen → Fetch more departures (limit 10)

### API Integration
- **Endpoint**: `GET /v2/pid/departureboards`
- **Parameters**: 
  - `ids[]`: Stop ID(s)
  - `minutesAfter`: 60
  - `limit`: 3 (home) / 10 (detail)
  - `filter`: routeHeadingOnce
- **Auth**: X-Access-Token header with Golemio API key

### Default Data
- Initial stop: "Anděl" (ASW ID: 1040)
