# Joinable

An Android app for browsing and favoriting Registered Student Organizations (RSOs) at the University of Illinois Urbana-Champaign. Built in Kotlin with Material Design 3.

## Features

- Browse 1,200+ RSOs and university departments
- Filter by organization type (Student Orgs / Departments)
- Full-text search by name
- Tap any RSO to view its mission, website, and categories
- Favorite RSOs with a single tap — state persists across sessions
- Tappable website links open in the browser

## Tech Stack

- **Kotlin** — full codebase, idiomatic null safety and data classes
- **Material Components** — `MaterialToolbar`, `Chip`, `Theme.MaterialComponents`
- **OkHttp / MockWebServer** — local HTTP server bundled with the app; all data access goes through HTTP even on-device
- **Volley** — async HTTP client with callback-based delivery
- **Jackson** — JSON deserialization with `@JsonCreator` constructors
- **Robolectric** — JVM-based unit tests (no emulator required)

## Architecture

The app runs a local `MockWebServer` (OkHttp) as a bundled backend. The Android UI communicates with it over HTTP, keeping the client/server boundary clean and testable.

```
JSON data → MockWebServer → HTTP → Volley client → Activity → RecyclerView
```

- `network/Server.kt` — request dispatcher; parses RSO data and handles `/summary`, `/rso/{id}`, and `/favorite` routes
- `network/Client.kt` — Volley-based HTTP client; delivers results via `ResultMightThrow<T>` callbacks
- `models/` — `Summary` (list item), `RSO` (detail), `RSOData`, `Favorite`
- `activities/` — `MainActivity` (list + search + filter), `RSOActivity` (detail + favorite)
- `adapters/SummaryListAdapter.kt` — `RecyclerView` adapter; sets accent color and subtitle by org type

## Screenshots

_Coming soon_

## Building

```bash
./gradlew assembleDebug
```

Requires Android SDK 34 and JDK 17+.
