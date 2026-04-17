# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CS 124 (AY2024) Machine Project — an Android app called "Joinable" for browsing and favoriting Registered Student Organizations (RSOs) at UIUC. Built in Java, graded across 4 checkpoints (MP0–MP3).

## Common Commands

```bash
# Run all tests for the active checkpoint (set in grade.yaml)
./gradlew test

# Run tests for a specific checkpoint
./gradlew test --tests "MP0Test"
./gradlew test --tests "MP1Test"
./gradlew test --tests "MP2Test"
./gradlew test --tests "MP3Test"

# Format code (Google Java Format + ktlint)
./gradlew spotlessApply

# Check formatting without applying
./gradlew spotlessCheck

# Run checkstyle
./gradlew checkstyle

# Grade locally (runs tests, checkstyle, and posts score)
./gradlew grade
```

**To switch checkpoints:** Edit `grade.yaml` → change `checkpoint: N`, then sync Gradle in Android Studio.

## Architecture

The app runs a local mock HTTP server alongside the Android UI. All client–server communication uses HTTP even though both run on the same device.

### Key source package: `edu.illinois.cs.cs124.ay2024.mp`

- **`network/Server.java`** — `MockWebServer` (OkHttp) dispatcher. Parses RSO JSON data from assets, handles HTTP routes (`/summary`, `/rso/{id}`, `/favorite`). **Primary file students modify for backend logic.**
- **`network/Client.java`** — Volley-based HTTP client. Makes requests to the server and delivers results via callbacks using `ResultMightThrow<T>`. **Students add request methods here.**
- **`activities/MainActivity.java`** — Main screen with `RecyclerView` of RSO summaries, `SearchView` for filtering by name, and a `ToggleButton` for color filtering.
- **`activities/RSOActivity.java`** — Detail screen for a single RSO; shows full info and favorite toggle.
- **`models/Summary.java`** — Lightweight RSO model (id, name, color) used in the list. Contains `filterColor()` static helper.
- **`models/RSO.java`** — Full RSO model with all fields.
- **`models/RSOData.java`** — Wrapper used for deserializing the full JSON asset file.
- **`models/Favorite.java`** — Model for favorite state, used in POST requests to server.
- **`adapters/SummaryListAdapter.java`** — `RecyclerView` adapter for the main list.
- **`helpers/Helpers.java`** — Shared utilities: `OBJECT_MAPPER` (Jackson), `CHECK_SERVER_RESPONSE`, `readRSODataFile()`.
- **`helpers/ResultMightThrow.java`** — Generic wrapper that holds either a result or an exception, used for async callbacks.
- **`application/JoinableApplication.java`** — Application subclass; starts the `MockWebServer` and exposes `SERVER_URL`.

### Data Flow

```
Assets (JSON) → Server (MockWebServer) → HTTP → Client (Volley) → Activity → Adapter → UI
```

### Key Libraries

- **OkHttp / MockWebServer** — server implementation
- **Volley** — HTTP client for Android
- **Jackson** — JSON serialization (`OBJECT_MAPPER` in `Helpers`)
- **Robolectric** — unit tests run on JVM without a device

## Grading

- Tests live in `app/src/test/java/.../mp/test/` and are named `MP0Test`, `MP1Test`, etc.
- `grade.yaml` controls which checkpoint's tests run during `./gradlew grade`
- Checkstyle is worth 10 points; run `spotlessApply` before submitting to auto-fix formatting
- Build files (`build.gradle.kts`) are overwritten during official grading — don't modify them
- `ID.txt` must contain the student's valid 27-character NetID-based identifier
