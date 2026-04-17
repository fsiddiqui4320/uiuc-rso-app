# Joinable — Portfolio Polish Design Spec
Date: 2026-04-16

## Overview

Transform the CS 124 school project "Joinable" into a portfolio-ready Android app by converting to Kotlin, applying Material Design visuals, and fixing glaring UX rough edges. The app will be published to a new GitHub repo (`https://github.com/fsiddiqui4320/uiuc-rso-app.git`). The original school project state is preserved via a `school-original` git tag.

## Section 1 — Repo & Branch Strategy

- Tag the current commit: `git tag school-original`
- All work proceeds on `main`
- Add new remote and push when complete:
  ```bash
  git remote add origin https://github.com/fsiddiqui4320/uiuc-rso-app.git
  git push -u origin main --tags
  ```

## Section 2 — Kotlin Conversion

Convert all `.java` source files to Kotlin using Android Studio's built-in converter (**Code → Convert Java File to Kotlin File**), one file at a time. Order:

1. Models: `Summary.java`, `RSO.java`, `RSOData.java`, `Favorite.java`
2. Helpers: `Helpers.java`, `ResultMightThrow.java`
3. Application: `JoinableApplication.java`
4. Network: `Server.java`, `Client.java`
5. Adapters: `SummaryListAdapter.java`
6. Activities: `MainActivity.java`, `RSOActivity.java`

After each file conversion:
- Fix any `!!` force-unwraps where null safety can be expressed more cleanly
- Use `val` for immutable references, `var` only where mutation is required
- Remove redundant explicit types that Kotlin infers

**Gate:** The project must build and all existing tests must pass before proceeding to visual changes.

## Section 3 — Visual Changes

### 3a. Theme & Dependencies

**`app/build.gradle.kts`** — add:
```
implementation("com.google.android.material:material:1.12.0")
```

**`res/values/styles.xml`** — change parent theme:
```xml
<style name="AppTheme" parent="Theme.MaterialComponents.Light.NoActionBar">
```

### 3b. Main Screen (`activity_main.xml`)

Replace current layout with:
- `MaterialToolbar` (56dp height, `colorPrimary` background = Illinois Blue `#13294b`)
  - App title "Joinable" as white title text
  - `SearchView` embedded in toolbar (full-width, rounded background, "Search RSOs…" hint)
- Filter chips row (white background, 8dp vertical padding, 16dp horizontal padding):
  - Orange chip: label "Student Orgs", filled orange (`#E84A27`) when active, outlined when inactive
  - Blue chip: label "Departments", filled Illinois Blue (`#13294b`) when active, outlined when inactive
- `RecyclerView` with 8dp side padding, takes remaining height

### 3c. List Item (`item_summary.xml`)

Replace current single-TextView row with:
- `LinearLayout` (horizontal, `wrap_content` height, white background, 1dp elevation/shadow)
  - Left accent `View`: 5dp wide, full item height, color = RSO color (orange or blue)
  - `LinearLayout` (vertical, `0dp` width, `weight=1`, 12dp vertical padding, 14dp horizontal padding):
    - Org name: `bold`, `15sp`, `#111111`
    - Type subtitle: `12sp`, `#888888`, `4dp` top margin ("Student Organization" or "Department")
  - Right chevron `TextView`: `›`, `18sp`, `#CCCCCC`, 12dp end margin

Type subtitle text and accent color are set in `SummaryListAdapter` based on `Summary.Color`.

### 3d. Detail Screen (`activity_rso.xml`)

Replace current layout with a `NestedScrollView` containing:

**Hero header** (`LinearLayout`, `colorPrimary` background):
- Top row (`RelativeLayout`, 56dp): back arrow (`←`) on left, favorite star (`☆`/`★`) on right — both white
- Org name: `bold`, `18sp`, white, 18dp horizontal padding
- Type subtitle: `12sp`, `rgba(255,255,255,0.6)`, 4dp top margin
- Category chips row: `HorizontalScrollView` wrapping a `LinearLayout` of chips with semi-transparent orange background and white text, 12dp top margin, 18dp bottom padding

**White content panel** (`LinearLayout`, vertical, white background, 20dp padding):
- MISSION section: orange uppercase label (`10sp`, `#E84A27`, 1.2px letter spacing) + body text (`14sp`, `#222222`, 1.6 line height)
- Divider
- WEBSITE section: orange uppercase label + tappable link text (`14sp`, `#13294b`, underline)
- Divider
- CATEGORIES section: orange uppercase label + chips (orange-tinted: `#fadbd4` background, `#c0392b` text)

## Section 4 — Rough Edge Fixes

### 4a. Favorite UX
**File:** `RSOActivity.kt`
- Remove `finish()` from the `favoriteButtonListener` callback
- After `setFavorite` completes, update the star icon state only — do not close the screen
- Star shows `★` (filled) when favorited, `☆` (outline) when not

### 4b. Tappable Website
**File:** `RSOActivity.kt`
- Set `android:clickable="true"` and `android:autoLink="web"` on the website TextView, or attach an `OnClickListener` that fires `Intent(Intent.ACTION_VIEW, Uri.parse(website))`

### 4c. Empty State
**File:** `activity_main.xml` + `MainActivity.kt`
- Add a `TextView` ("No RSOs found") centered in the `FrameLayout` alongside the `RecyclerView`, `visibility="gone"` by default
- In `MainActivity.kt`, after filtering: if the filtered list is empty, show the empty state view and hide the `RecyclerView`; otherwise hide the empty state and show the `RecyclerView`

## Out of Scope

- No new features beyond the four fixes above
- No changes to `Server.kt` routing logic or `Client.kt` request methods beyond Kotlin conversion
- No changes to test files or build files (except adding the Material dependency)
