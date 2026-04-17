# Joinable Portfolio Polish Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert the Joinable Android app from Java to Kotlin, apply Material Design visual polish, and fix three UX rough edges, then push the result to a new GitHub portfolio repo.

**Architecture:** All Java source files are converted to Kotlin one at a time in dependency order, with build verification after each. Visual changes (XML layouts + updated activity code) follow after the full Kotlin conversion is confirmed green. Rough edge fixes (favorite UX, tappable website, empty state) are bundled with the activity layout updates in Phase 2.

**Tech Stack:** Kotlin 2.0.21, Android SDK 34, Material Components 1.12.0, OkHttp MockWebServer, Volley, Jackson, Robolectric (tests)

---

## File Map

**Modified — build:**
- `build.gradle.kts` (root): add `org.jetbrains.kotlin.android` plugin declaration + Kotlin spotless target
- `app/build.gradle.kts`: add Kotlin plugin, `kotlinOptions`, Material Components dependency

**Converted Java → Kotlin (delete .java, create .kt):**
- `app/src/main/java/.../models/RSOData.java` → `RSOData.kt`
- `app/src/main/java/.../models/Favorite.java` → `Favorite.kt`
- `app/src/main/java/.../helpers/ResultMightThrow.java` → `ResultMightThrow.kt`
- `app/src/main/java/.../helpers/Helpers.java` → `Helpers.kt`
- `app/src/main/java/.../models/Summary.java` → `Summary.kt`
- `app/src/main/java/.../models/RSO.java` → `RSO.kt`
- `app/src/main/java/.../application/JoinableApplication.java` → `JoinableApplication.kt`
- `app/src/main/java/.../network/Server.java` → `Server.kt`
- `app/src/main/java/.../network/Client.java` → `Client.kt`
- `app/src/main/java/.../adapters/SummaryListAdapter.java` → `SummaryListAdapter.kt`
- `app/src/main/java/.../activities/MainActivity.java` → `MainActivity.kt`
- `app/src/main/java/.../activities/RSOActivity.java` → `RSOActivity.kt`

**Modified — resources:**
- `app/src/main/res/values/styles.xml`: upgrade to MaterialComponents theme
- `app/src/main/res/layout/item_summary.xml`: accent bar + title + subtitle
- `app/src/main/res/layout/activity_main.xml`: MaterialToolbar + filter chips + empty state
- `app/src/main/res/layout/activity_rso.xml`: blue hero header + white content panel

---

## Task 1: Repo Setup

**Files:** none (git operations only)

- [ ] **Step 1: Tag the current school project state**

```bash
git tag school-original
```

- [ ] **Step 2: Add .superpowers to .gitignore**

Append to `.gitignore` (create it if it doesn't exist):
```
.superpowers/
```

- [ ] **Step 3: Commit**

```bash
git add .gitignore
git commit -m "chore: tag school-original and ignore .superpowers"
```

---

## Task 2: Build Config — Add Kotlin Plugin and Material Dependency

**Files:**
- Modify: `build.gradle.kts` (root)
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Update root `build.gradle.kts` — add Kotlin plugin and spotless Kotlin target**

Replace the `plugins` block and `spotless` block:

```kotlin
@file:Suppress("GradleDependency", "AndroidGradlePluginVersion")

plugins {
    id("com.android.application") version "8.7.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.diffplug.spotless") version "6.25.0"
    java
}
subprojects {
    tasks.withType(JavaCompile::class.java) {
        options.compilerArgs.addAll(listOf("-parameters"))
    }
}
spotless {
    java {
        googleJavaFormat("1.24.0")
        target("app/src/*/java/**/*.java")
    }
    kotlin {
        ktlint("1.3.1")
        target("app/src/*/java/**/*.kt")
    }
    kotlinGradle {
        ktlint("1.3.1")
        target("**/*.gradle.kts")
    }
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
```

- [ ] **Step 2: Update `app/build.gradle.kts` — add Kotlin plugin, kotlinOptions, Material dependency**

Add `id("org.jetbrains.kotlin.android")` to the plugins block:
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.cs124.gradlegrader") version "2024.10.0"
    checkstyle
}
```

Add `kotlinOptions` inside the `android` block (after `compileOptions`):
```kotlin
kotlinOptions {
    jvmTarget = "17"
}
```

Add Material Components to `dependencies`:
```kotlin
implementation("com.google.android.material:material:1.12.0")
```

- [ ] **Step 3: Sync and verify build**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add build.gradle.kts app/build.gradle.kts
git commit -m "build: add Kotlin plugin and Material Components dependency"
```

---

## Task 3: Convert Simple Models (RSOData, Favorite)

**Package:** `edu.illinois.cs.cs124.ay2024.mp.models`

- [ ] **Step 1: Delete `RSOData.java`, create `RSOData.kt`**

```kotlin
package edu.illinois.cs.cs124.ay2024.mp.models

data class RSOData(
    val id: String,
    val title: String,
    val categories: String,
    val website: String,
    val mission: String,
)
```

- [ ] **Step 2: Delete `Favorite.java`, create `Favorite.kt`**

```kotlin
package edu.illinois.cs.cs124.ay2024.mp.models

data class Favorite(val id: String, val favorite: Boolean)
```

- [ ] **Step 3: Verify build**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/models/
git commit -m "refactor: convert RSOData and Favorite to Kotlin data classes"
```

---

## Task 4: Convert Helpers (ResultMightThrow, Helpers)

**Package:** `edu.illinois.cs.cs124.ay2024.mp.helpers`

- [ ] **Step 1: Delete `ResultMightThrow.java`, create `ResultMightThrow.kt`**

```kotlin
package edu.illinois.cs.cs124.ay2024.mp.helpers

class ResultMightThrow<T> {
    private val value: T?
    private val exception: Exception?

    constructor(setValue: T) {
        value = setValue
        exception = null
    }

    constructor(setException: Exception) {
        value = null
        exception = setException
    }

    fun getValue(): T {
        if (exception != null) throw RuntimeException(exception)
        return value!!
    }

    fun getException(): Exception? = exception
}
```

- [ ] **Step 2: Delete `Helpers.java`, create `Helpers.kt`**

Note: `@JvmField` and `@JvmStatic` are required because Java test files reference `Helpers.OBJECT_MAPPER`, `Helpers.CHECK_SERVER_RESPONSE`, and `Helpers.readRSODataFile()` directly.

```kotlin
package edu.illinois.cs.cs124.ay2024.mp.helpers

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import edu.illinois.cs.cs124.ay2024.mp.network.Server
import java.nio.charset.StandardCharsets
import java.util.Scanner

object Helpers {
    @JvmField
    val OBJECT_MAPPER: ObjectMapper =
        ObjectMapper().registerModule(ParameterNamesModule(JsonCreator.Mode.PROPERTIES))

    const val CHECK_SERVER_RESPONSE = "AY2024"

    @JvmStatic
    fun readRSODataFile(): String =
        Scanner(
            Server::class.java.getResourceAsStream("/rsos.json"),
            StandardCharsets.UTF_8,
        ).useDelimiter("\\A").next()
}
```

- [ ] **Step 3: Verify build**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/helpers/
git commit -m "refactor: convert ResultMightThrow and Helpers to Kotlin"
```

---

## Task 5: Convert Summary

**File:** `app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/models/Summary.kt`

Note: `@JvmStatic` on `filterColor` and `search` is required because Java test files call `Summary.filterColor(...)` and `Summary.search(...)` as static methods. The commented-out `sortByFavorite` method is dropped.

- [ ] **Step 1: Delete `Summary.java`, create `Summary.kt`**

```kotlin
package edu.illinois.cs.cs124.ay2024.mp.models

import com.fasterxml.jackson.annotation.JsonCreator

open class Summary : Comparable<Summary> {
    val id: String
    val title: String
    val color: Color

    enum class Color { BLUE, ORANGE, DEPARTMENT }

    constructor(rsoData: RSOData) {
        id = rsoData.id
        title = rsoData.title
        val categoryPrefix = rsoData.categories.split("-")[0].trim()
        color =
            when {
                categoryPrefix.startsWith("Blue") -> Color.BLUE
                categoryPrefix.startsWith("Orange") -> Color.ORANGE
                categoryPrefix.startsWith("Department") -> Color.DEPARTMENT
                else -> throw IllegalStateException("Unknown RSO color: $categoryPrefix")
            }
    }

    @JsonCreator
    constructor(setId: String, setTitle: String, setColor: Color) {
        id = setId
        title = setTitle
        color = setColor
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Summary) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun compareTo(other: Summary): Int = title.compareTo(other.title)

    companion object {
        @JvmStatic
        fun filterColor(inputList: List<Summary>, colors: Set<Color>): List<Summary> =
            inputList.filter { it.color in colors }.sortedBy { it.title }

        @JvmStatic
        fun search(inputList: List<Summary>, search: String): List<Summary> {
            val searchTerm = search.trim().lowercase()
            if (searchTerm.isEmpty()) return inputList.toList()

            val exactMatches = mutableMapOf<Summary, Int>()
            for (summary in inputList) {
                for (word in summary.title.lowercase().split(" ")) {
                    if (word == searchTerm) {
                        exactMatches[summary] = (exactMatches[summary] ?: 0) + 1
                    }
                }
            }

            if (exactMatches.isNotEmpty()) {
                return exactMatches.entries
                    .sortedWith(compareByDescending<Map.Entry<Summary, Int>> { it.value }.thenBy { it.key })
                    .map { it.key }
            }

            return inputList.filter { it.title.lowercase().contains(searchTerm) }
        }
    }
}
```

- [ ] **Step 2: Verify build**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/models/Summary.kt
git commit -m "refactor: convert Summary to Kotlin"
```

---

## Task 6: Convert RSO

**File:** `app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/models/RSO.kt`

- [ ] **Step 1: Delete `RSO.java`, create `RSO.kt`**

```kotlin
package edu.illinois.cs.cs124.ay2024.mp.models

import com.fasterxml.jackson.annotation.JsonCreator

class RSO : Summary {
    val mission: String
    val website: String
    val categories: List<String>

    @JsonCreator
    constructor(
        setId: String,
        setTitle: String,
        setColor: Color,
        setMission: String,
        setWebsite: String,
        setCategories: List<String>,
    ) : super(setId, setTitle, setColor) {
        mission = setMission
        website = setWebsite
        categories = setCategories
    }

    constructor(rsoData: RSOData) : super(rsoData) {
        val categoryParts = rsoData.categories.split("-")
        categories =
            if (categoryParts.size > 1) {
                categoryParts[1].split(",").map { it.trim() }
            } else {
                emptyList()
            }
        mission = rsoData.mission
        website = rsoData.website
    }

    fun getRelatedRSOs(): List<Summary> = emptyList()
}
```

- [ ] **Step 2: Verify build**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/models/RSO.kt
git commit -m "refactor: convert RSO to Kotlin"
```

---

## Task 7: Convert JoinableApplication

**File:** `app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/application/JoinableApplication.kt`

- [ ] **Step 1: Delete `JoinableApplication.java`, create `JoinableApplication.kt`**

```kotlin
package edu.illinois.cs.cs124.ay2024.mp.application

import android.app.Application
import android.os.Build
import edu.illinois.cs.cs124.ay2024.mp.network.Client
import edu.illinois.cs.cs124.ay2024.mp.network.Server

class JoinableApplication : Application() {
    private lateinit var client: Client

    override fun onCreate() {
        super.onCreate()
        if (Build.FINGERPRINT == "robolectric") {
            Server.start()
        } else {
            Thread { Server.start() }.start()
        }
        client = Client.start()
    }

    fun getClient(): Client {
        check(client.getConnected()) { "Client not connected" }
        return client
    }

    companion object {
        const val DEFAULT_SERVER_PORT = 8024
        const val SERVER_URL = "http://localhost:$DEFAULT_SERVER_PORT"
    }
}
```

- [ ] **Step 2: Verify build**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/application/JoinableApplication.kt
git commit -m "refactor: convert JoinableApplication to Kotlin"
```

---

## Task 8: Convert Server

**File:** `app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/network/Server.kt`

Note: `@JvmStatic` on companion methods is required because Java test files call `Server.start()`, `Server.isRunning()`, `Server.reset()`, and `Server.getIdToRSO()`.

- [ ] **Step 1: Delete `Server.java`, create `Server.kt`**

```kotlin
package edu.illinois.cs.cs124.ay2024.mp.network

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import edu.illinois.cs.cs124.ay2024.mp.application.JoinableApplication
import edu.illinois.cs.cs124.ay2024.mp.helpers.Helpers.CHECK_SERVER_RESPONSE
import edu.illinois.cs.cs124.ay2024.mp.helpers.Helpers.OBJECT_MAPPER
import edu.illinois.cs.cs124.ay2024.mp.helpers.Helpers.readRSODataFile
import edu.illinois.cs.cs124.ay2024.mp.models.Favorite
import edu.illinois.cs.cs124.ay2024.mp.models.RSO
import edu.illinois.cs.cs124.ay2024.mp.models.RSOData
import edu.illinois.cs.cs124.ay2024.mp.models.Summary
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.io.IOException
import java.net.HttpURLConnection
import java.util.logging.Level
import java.util.logging.Logger

class Server private constructor() : Dispatcher() {
    private val logger = Logger.getLogger(Server::class.java.name)
    private val summaries = mutableListOf<Summary>()
    private val favoriteMap = mutableMapOf<String, Boolean>()

    private fun makeOKJSONResponse(body: String): MockResponse =
        MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(body)
            .setHeader("Content-Type", "application/json; charset=utf-8")

    private val HTTP_NOT_FOUND =
        MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND).setBody("404: Not Found")

    private val HTTP_BAD_REQUEST =
        MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST).setBody("400: Bad Request")

    @Throws(JsonProcessingException::class)
    private fun getSummaries(): MockResponse {
        val favorites = mutableListOf<Summary>()
        val notFavorites = mutableListOf<Summary>()
        for (summary in summaries) {
            try {
                if (favoriteMap[summary.id] == true) favorites.add(summary)
                else notFavorites.add(summary)
            } catch (e: Exception) {
                return HTTP_BAD_REQUEST
            }
        }
        favorites.sort()
        notFavorites.sort()
        favorites.addAll(notFavorites)
        return makeOKJSONResponse(OBJECT_MAPPER.writeValueAsString(favorites))
    }

    @Throws(JsonProcessingException::class)
    private fun getRSO(path: String): MockResponse {
        if (path.startsWith("/rsos")) return HTTP_NOT_FOUND
        val id = path.split("/")[2]
        val rso = ID_TO_RSO[id] ?: return HTTP_NOT_FOUND
        return makeOKJSONResponse(OBJECT_MAPPER.writeValueAsString(rso))
    }

    @Throws(JsonProcessingException::class)
    private fun getFavoriteResponse(id: String): MockResponse {
        if (!ID_TO_RSO.containsKey(id)) return HTTP_NOT_FOUND
        val isFavorite = favoriteMap.getOrDefault(id, false)
        return makeOKJSONResponse(OBJECT_MAPPER.writeValueAsString(Favorite(id, isFavorite)))
    }

    override fun dispatch(request: RecordedRequest): MockResponse {
        if (request.path == null || request.method == null) return HTTP_BAD_REQUEST
        val path = request.path!!.replace(Regex("/+$"), "").replace(Regex("/+"), "/")
        val method = request.method!!.uppercase()
        return try {
            when {
                path.isEmpty() && method == "GET" -> makeOKJSONResponse(CHECK_SERVER_RESPONSE)
                path == "/reset" -> { favoriteMap.clear(); makeOKJSONResponse("200: OK") }
                path == "/summary" && method == "GET" -> getSummaries()
                path.startsWith("/rso") && method == "GET" -> getRSO(path)
                path == "/favorite" && method == "POST" -> {
                    val body = request.body.readUtf8()
                    if (body.isEmpty()) return HTTP_BAD_REQUEST
                    try {
                        val favorite = OBJECT_MAPPER.readValue(body, Favorite::class.java)
                        favoriteMap[favorite.id] = favorite.favorite
                        MockResponse()
                            .setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP)
                            .setHeader("Location", "/favorite/${favorite.id}")
                    } catch (e: Exception) {
                        HTTP_BAD_REQUEST
                    }
                }
                path.startsWith("/favorite/") && method == "GET" -> {
                    val parts = path.split("/")
                    if (parts.size != 3) return HTTP_BAD_REQUEST
                    getFavoriteResponse(parts[2])
                }
                else -> { logger.log(Level.WARNING, "Route not found: $path"); HTTP_NOT_FOUND }
            }
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Server internal error for path: $path", e)
            MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR).setBody("500: Internal Error")
        }
    }

    private fun loadData() {
        val json = readRSODataFile()
        try {
            val nodes: JsonNode = OBJECT_MAPPER.readTree(json)
            for (node in nodes) {
                val rsoData = OBJECT_MAPPER.readValue(node.toString(), RSOData::class.java)
                ID_TO_RSO[rsoData.id] = RSO(rsoData)
                favoriteMap[rsoData.id] = false
                summaries.add(Summary(rsoData))
            }
        } catch (e: JsonProcessingException) {
            logger.log(Level.SEVERE, "Loading data failed", e)
            throw IllegalStateException(e)
        }
    }

    init {
        Logger.getLogger(MockWebServer::class.java.name).level = Level.SEVERE
        loadData()
        try {
            val server = MockWebServer()
            server.setDispatcher(this)
            server.start(JoinableApplication.DEFAULT_SERVER_PORT)
        } catch (e: IOException) {
            logger.log(Level.SEVERE, "Startup failed", e)
            throw IllegalStateException(e)
        }
    }

    companion object {
        private val ID_TO_RSO = HashMap<String, RSO>()
        private const val RETRY_COUNT = 8
        private const val RETRY_DELAY = 512L

        @JvmStatic fun getIdToRSO(): Map<String, RSO> = ID_TO_RSO

        @JvmStatic
        fun start() {
            if (isRunning(false)) return
            Server()
            if (!isRunning(true)) throw IllegalStateException("Server should be running")
        }

        @JvmStatic fun isRunning(wait: Boolean): Boolean = isRunning(wait, RETRY_COUNT, RETRY_DELAY)

        @JvmStatic
        fun isRunning(wait: Boolean, retryCount: Int, retryDelay: Long): Boolean {
            repeat(retryCount) {
                val client = OkHttpClient()
                val request = Request.Builder().url(JoinableApplication.SERVER_URL).get().build()
                try {
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            if (response.body!!.string() == CHECK_SERVER_RESPONSE) return true
                            else throw IllegalStateException("Another server is running on port ${JoinableApplication.DEFAULT_SERVER_PORT}")
                        }
                    }
                } catch (e: IOException) {
                    if (!wait) return false
                    try { Thread.sleep(retryDelay) } catch (ignored: InterruptedException) {}
                }
            }
            return false
        }

        @JvmStatic
        @Throws(IOException::class)
        fun reset(): Boolean {
            val client = OkHttpClient()
            val request = Request.Builder().url("${JoinableApplication.SERVER_URL}/reset/").get().build()
            return client.newCall(request).execute().use { it.isSuccessful }
        }
    }
}
```

- [ ] **Step 2: Verify build**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/network/Server.kt
git commit -m "refactor: convert Server to Kotlin"
```

---

## Task 9: Convert Client

**File:** `app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/network/Client.kt`

Note: Callback parameters stay as `java.util.function.Consumer<ResultMightThrow<T>>` (not Kotlin lambdas) so Java test files can still pass lambdas to these methods.

- [ ] **Step 1: Delete `Client.java`, create `Client.kt`**

```kotlin
package edu.illinois.cs.cs124.ay2024.mp.network

import android.os.Build
import android.util.Log
import com.android.volley.Cache
import com.android.volley.ExecutorDelivery
import com.android.volley.Network
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.NoCache
import com.android.volley.toolbox.StringRequest
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import edu.illinois.cs.cs124.ay2024.mp.application.JoinableApplication
import edu.illinois.cs.cs124.ay2024.mp.helpers.Helpers.CHECK_SERVER_RESPONSE
import edu.illinois.cs.cs124.ay2024.mp.helpers.Helpers.OBJECT_MAPPER
import edu.illinois.cs.cs124.ay2024.mp.helpers.ResultMightThrow
import edu.illinois.cs.cs124.ay2024.mp.models.Favorite
import edu.illinois.cs.cs124.ay2024.mp.models.RSO
import edu.illinois.cs.cs124.ay2024.mp.models.Summary
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.logging.Level
import java.util.logging.Logger
import java.util.stream.Collectors

class Client private constructor() {
    private val TAG = Client::class.java.simpleName
    private val logger = Logger.getLogger(Server::class.java.name)
    private val requestQueue: RequestQueue
    private val connected = CompletableFuture<Boolean>()

    fun getConnected(): Boolean =
        try { connected.get(GET_CONNECTED_DELAY_SEC, TimeUnit.SECONDS) } catch (e: Exception) { false }

    fun getRSO(id: String, callback: Consumer<ResultMightThrow<RSO>>) {
        val rsoRequest = StringRequest(
            Request.Method.GET,
            JoinableApplication.SERVER_URL + "/rso/" + id,
            { response ->
                try {
                    val rso = OBJECT_MAPPER.readValue(response, object : TypeReference<RSO>() {})
                    callback.accept(ResultMightThrow(rso))
                } catch (e: JsonProcessingException) {
                    callback.accept(ResultMightThrow(e))
                }
            },
            { error -> callback.accept(ResultMightThrow(error)) },
        )
        requestQueue.add(rsoRequest)
    }

    fun getSummaries(callback: Consumer<ResultMightThrow<List<Summary>>>) {
        val summariesRequest = StringRequest(
            Request.Method.GET,
            JoinableApplication.SERVER_URL + "/summary/",
            { response ->
                try {
                    val summaries = OBJECT_MAPPER.readValue(response, object : TypeReference<List<Summary>>() {})
                    callback.accept(ResultMightThrow(summaries))
                } catch (e: JsonProcessingException) {
                    callback.accept(ResultMightThrow(e))
                }
            },
            { error -> callback.accept(ResultMightThrow(error)) },
        )
        requestQueue.add(summariesRequest)
    }

    fun getFavorite(id: String, callback: Consumer<ResultMightThrow<Boolean>>) {
        val request = StringRequest(
            Request.Method.GET,
            JoinableApplication.SERVER_URL + "/favorite/" + id,
            { response ->
                try {
                    val favorite = OBJECT_MAPPER.readValue(response, Favorite::class.java)
                    callback.accept(ResultMightThrow(favorite.favorite))
                } catch (e: JsonProcessingException) {
                    callback.accept(ResultMightThrow(e))
                }
            },
            { error -> callback.accept(ResultMightThrow(error)) },
        )
        requestQueue.add(request)
    }

    fun setFavorite(id: String, isFavorite: Boolean, callback: Consumer<ResultMightThrow<Boolean>>) {
        val favoriteJSON = try {
            OBJECT_MAPPER.writeValueAsString(Favorite(id, isFavorite))
        } catch (e: JsonProcessingException) {
            callback.accept(ResultMightThrow(e))
            return
        }

        val request = object : StringRequest(
            Method.POST,
            JoinableApplication.SERVER_URL + "/favorite",
            { response ->
                try {
                    val returned = OBJECT_MAPPER.readValue(response, Favorite::class.java)
                    callback.accept(ResultMightThrow(returned.favorite))
                } catch (e: JsonProcessingException) {
                    callback.accept(ResultMightThrow(e))
                }
            },
            { error -> callback.accept(ResultMightThrow(error)) },
        ) {
            override fun getBodyContentType() = "application/json; charset=utf-8"
            override fun getBody() = favoriteJSON.toByteArray(StandardCharsets.UTF_8)
        }
        requestQueue.add(request)
    }

    init {
        val testing = Build.FINGERPRINT == "robolectric"
        com.android.volley.VolleyLog.DEBUG = false
        HttpURLConnection.setFollowRedirects(true)

        val cache: Cache = NoCache()
        val network: Network = BasicNetwork(HurlStack())
        requestQueue = if (testing) {
            RequestQueue(cache, network, 1, ExecutorDelivery(Executors.newSingleThreadExecutor()))
        } else {
            RequestQueue(cache, network)
        }

        val serverURL = try {
            URL(JoinableApplication.SERVER_URL)
        } catch (e: MalformedURLException) {
            Log.e(TAG, "Bad server URL: ${JoinableApplication.SERVER_URL}", e)
            return@init
        }

        Thread {
            repeat(MAX_STARTUP_RETRIES) {
                try {
                    val connection = serverURL.openConnection() as HttpURLConnection
                    val body = BufferedReader(InputStreamReader(connection.inputStream))
                        .lines().collect(Collectors.joining("\n"))
                    if (body != CHECK_SERVER_RESPONSE) throw IllegalStateException("Invalid response")
                    connection.disconnect()
                    connected.complete(true)
                    requestQueue.start()
                    return@Thread
                } catch (ignored: Exception) {}
                try { Thread.sleep(INITIAL_CONNECTION_RETRY_DELAY) } catch (ignored: InterruptedException) {}
            }
            Log.e(TAG, "Client couldn't connect")
        }.start()
    }

    companion object {
        private var instance: Client? = null
        private const val INITIAL_CONNECTION_RETRY_DELAY = 1000L
        private const val MAX_STARTUP_RETRIES = 8
        private const val GET_CONNECTED_DELAY_SEC = 2L

        @JvmStatic
        fun start(): Client {
            if (instance == null) instance = Client()
            return instance!!
        }
    }
}
```

- [ ] **Step 2: Verify build**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/network/Client.kt
git commit -m "refactor: convert Client to Kotlin"
```

---

## Task 10: Convert SummaryListAdapter

**File:** `app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/adapters/SummaryListAdapter.kt`

This conversion keeps the current `item_summary.xml` layout (just a `title` TextView) unchanged. The adapter will be updated again in Task 15 when the layout changes.

- [ ] **Step 1: Delete `SummaryListAdapter.java`, create `SummaryListAdapter.kt`**

```kotlin
package edu.illinois.cs.cs124.ay2024.mp.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.illinois.cs.cs124.ay2024.mp.R
import edu.illinois.cs.cs124.ay2024.mp.models.Summary
import java.util.function.Consumer

class SummaryListAdapter(
    private var summaries: List<Summary>,
    private val activity: Activity,
    private val onClickCallback: Consumer<Summary>? = null,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun setSummaries(newSummaries: List<Summary>) {
        summaries = newSummaries
        activity.runOnUiThread { notifyDataSetChanged() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_summary, parent, false)
        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val summary = summaries[position]
        val title = holder.itemView.findViewById<TextView>(R.id.title)
        val layout = holder.itemView.findViewById<LinearLayout>(R.id.layout)

        title.text = summary.title
        title.setTextColor(activity.getColor(R.color.darkTextColor))

        val backgroundColor = when (summary.color) {
            Summary.Color.ORANGE -> activity.getColor(R.color.lighterIllinoisOrange)
            Summary.Color.BLUE -> activity.getColor(R.color.lighterIllinoisBlue)
            Summary.Color.DEPARTMENT -> activity.getColor(R.color.departmentColor)
        }
        layout.setBackgroundColor(backgroundColor)

        onClickCallback?.let { callback ->
            layout.setOnClickListener { callback.accept(summary) }
        }
    }

    override fun getItemCount(): Int = summaries.size
}
```

- [ ] **Step 2: Verify build**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/adapters/SummaryListAdapter.kt
git commit -m "refactor: convert SummaryListAdapter to Kotlin"
```

---

## Task 11: Convert MainActivity

**File:** `app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/activities/MainActivity.kt`

This conversion keeps current behavior (ToggleButtons for filter). The ToggleButtons will be replaced with Chips in Task 16 when the layout changes. The `assertNotNull` call and debug log spam are removed — they were test scaffolding that doesn't belong in production code.

- [ ] **Step 1: Delete `MainActivity.java`, create `MainActivity.kt`**

```kotlin
package edu.illinois.cs.cs124.ay2024.mp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.ToggleButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.illinois.cs.cs124.ay2024.mp.R
import edu.illinois.cs.cs124.ay2024.mp.adapters.SummaryListAdapter
import edu.illinois.cs.cs124.ay2024.mp.application.JoinableApplication
import edu.illinois.cs.cs124.ay2024.mp.models.Summary

class MainActivity : Activity(), SearchView.OnQueryTextListener {
    private val TAG = MainActivity::class.java.simpleName
    private var summaries: List<Summary> = emptyList()
    private lateinit var listAdapter: SummaryListAdapter
    private val shownColors =
        mutableSetOf(Summary.Color.ORANGE, Summary.Color.BLUE, Summary.Color.DEPARTMENT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listAdapter =
            SummaryListAdapter(summaries, this) { summary ->
                Log.d(TAG, "User clicked on ${summary.title}")
                val intent = Intent(this, RSOActivity::class.java)
                intent.putExtra("id", summary.id)
                startActivity(intent)
            }

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = listAdapter

        findViewById<SearchView>(R.id.search).setOnQueryTextListener(this)

        val orangeButton = findViewById<ToggleButton>(R.id.orangeButton)
        orangeButton.setOnCheckedChangeListener { _, checked ->
            if (checked) shownColors.add(Summary.Color.ORANGE)
            else shownColors.remove(Summary.Color.ORANGE)
            val filtered = Summary.filterColor(summaries, shownColors)
            listAdapter.setSummaries(filtered)
        }

        val blueButton = findViewById<ToggleButton>(R.id.blueButton)
        blueButton.setOnCheckedChangeListener { _, checked ->
            if (checked) shownColors.add(Summary.Color.BLUE)
            else shownColors.remove(Summary.Color.BLUE)
            val filtered = Summary.filterColor(summaries, shownColors)
            listAdapter.setSummaries(filtered)
        }
    }

    override fun onStart() {
        super.onStart()
        val client = (application as JoinableApplication).getClient()
        client.getSummaries { result ->
            try {
                summaries = result.getValue().sorted()
                listAdapter.setSummaries(summaries)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating summary list", e)
            }
        }
    }

    override fun onQueryTextChange(query: String): Boolean {
        val filtered = Summary.search(summaries, query)
        listAdapter.setSummaries(filtered)
        return true
    }

    override fun onQueryTextSubmit(unused: String): Boolean = false
}
```

- [ ] **Step 2: Verify build**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/activities/MainActivity.kt
git commit -m "refactor: convert MainActivity to Kotlin"
```

---

## Task 12: Convert RSOActivity

**File:** `app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/activities/RSOActivity.kt`

This conversion keeps current behavior (existing layout IDs). Layout and logic are reworked together in Task 17. The `assertNotNull` import is removed.

- [ ] **Step 1: Delete `RSOActivity.java`, create `RSOActivity.kt`**

```kotlin
package edu.illinois.cs.cs124.ay2024.mp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.ToggleButton
import edu.illinois.cs.cs124.ay2024.mp.R
import edu.illinois.cs.cs124.ay2024.mp.application.JoinableApplication
import edu.illinois.cs.cs124.ay2024.mp.network.Server

class RSOActivity : Activity() {
    private val TAG = RSOActivity::class.java.simpleName
    private lateinit var rsoId: String
    private lateinit var favoriteButton: ToggleButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rso)

        rsoId = intent.getStringExtra("id") ?: return
        val rso = Server.getIdToRSO()[rsoId] ?: return

        findViewById<TextView>(R.id.example).text = rso.title
        findViewById<TextView>(R.id.web).text = rso.website
        findViewById<TextView>(R.id.miss).text = rso.mission
        findViewById<TextView>(R.id.cats).text = rso.categories.joinToString(", ")

        favoriteButton = findViewById(R.id.favoriteButton)
        favoriteButton.setOnCheckedChangeListener { _, checked ->
            Log.d(TAG, "Favorite button checked $checked")
            val client = (application as JoinableApplication).getClient()
            client.setFavorite(rsoId, checked) { result ->
                runOnUiThread { finish() }
                Log.d(TAG, "Favorite status set to: $checked")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val client = (application as JoinableApplication).getClient()
        client.getFavorite(rsoId) { result ->
            if (result != null) {
                val isFavorite = result.getValue()
                runOnUiThread { favoriteButton.isChecked = isFavorite }
            }
        }
    }
}
```

- [ ] **Step 2: Verify build**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/activities/RSOActivity.kt
git commit -m "refactor: convert RSOActivity to Kotlin"
```

---

## Task 13: Verify All Tests Pass

- [ ] **Step 1: Run the full test suite**

```bash
./gradlew test
```
Expected: `BUILD SUCCESSFUL` with all tests passing (MP0Test, MP1Test, MP2Test, MP3Test depending on grade.yaml checkpoint).

If tests fail, do not proceed to Phase 2. Fix any Kotlin conversion issues first.

- [ ] **Step 2: Commit if any fixup was needed**

```bash
git add -p
git commit -m "fix: address Kotlin conversion test failures"
```

---

## Task 14: Update Theme to Material Components

**Files:**
- Modify: `app/src/main/res/values/styles.xml`

- [ ] **Step 1: Replace `styles.xml`**

```xml
<resources>
    <style name="AppTheme" parent="Theme.MaterialComponents.Light.NoActionBar">
        <item name="colorPrimary">@color/illinoisBlue</item>
        <item name="colorPrimaryDark">@color/illinoisBlue</item>
        <item name="colorAccent">@color/illinoisOrange</item>
    </style>
</resources>
```

- [ ] **Step 2: Verify build**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/values/styles.xml
git commit -m "style: upgrade to MaterialComponents theme"
```

---

## Task 15: List Item — New Layout + Updated Adapter

**Files:**
- Modify: `app/src/main/res/layout/item_summary.xml`
- Modify: `app/src/main/java/.../adapters/SummaryListAdapter.kt`

- [ ] **Step 1: Replace `item_summary.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/layout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="?attr/selectableItemBackground"
    android:clickable="true"
    android:focusable="true"
    android:layout_marginBottom="1dp"
    android:orientation="horizontal">

    <View
        android:id="@+id/accent_bar"
        android:layout_width="5dp"
        android:layout_height="match_parent"
        android:background="@color/illinoisOrange" />

    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:orientation="vertical"
        android:paddingHorizontal="14dp"
        android:paddingVertical="12dp">

        <TextView
            android:id="@+id/title"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textColor="#111111"
            android:textSize="15sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/subtitle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="3dp"
            android:textColor="#888888"
            android:textSize="12sp" />

    </LinearLayout>

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:gravity="center_vertical"
        android:paddingEnd="12dp"
        android:text="›"
        android:textColor="#CCCCCC"
        android:textSize="20sp" />

</LinearLayout>
```

- [ ] **Step 2: Update `SummaryListAdapter.kt` — `onBindViewHolder` to use new view IDs**

Replace the `onBindViewHolder` method:

```kotlin
override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val summary = summaries[position]
    val title = holder.itemView.findViewById<TextView>(R.id.title)
    val subtitle = holder.itemView.findViewById<TextView>(R.id.subtitle)
    val accentBar = holder.itemView.findViewById<View>(R.id.accent_bar)
    val layout = holder.itemView.findViewById<LinearLayout>(R.id.layout)

    title.text = summary.title

    when (summary.color) {
        Summary.Color.ORANGE -> {
            accentBar.setBackgroundColor(activity.getColor(R.color.illinoisOrange))
            subtitle.text = "Student Organization"
        }
        Summary.Color.BLUE -> {
            accentBar.setBackgroundColor(activity.getColor(R.color.illinoisBlue))
            subtitle.text = "Student Organization"
        }
        Summary.Color.DEPARTMENT -> {
            accentBar.setBackgroundColor(activity.getColor(R.color.departmentColor))
            subtitle.text = "Department"
        }
    }

    onClickCallback?.let { callback ->
        layout.setOnClickListener { callback.accept(summary) }
    }
}
```

Also add `import android.view.View` to the imports in `SummaryListAdapter.kt`.

- [ ] **Step 3: Verify build**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/item_summary.xml app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/adapters/SummaryListAdapter.kt
git commit -m "style: new list item layout with accent bar, subtitle, and chevron"
```

---

## Task 16: Main Screen — New Layout + Updated MainActivity

**Files:**
- Modify: `app/src/main/res/layout/activity_main.xml`
- Modify: `app/src/main/java/.../activities/MainActivity.kt`

- [ ] **Step 1: Replace `activity_main.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="@color/illinoisBlue"
        android:theme="@style/ThemeOverlay.MaterialComponents.Dark.ActionBar">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:gravity="center_vertical"
            android:orientation="horizontal">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="-8dp"
                android:text="Joinable"
                android:textColor="@android:color/white"
                android:textSize="20sp"
                android:textStyle="bold" />

            <SearchView
                android:id="@+id/search"
                android:layout_width="0dp"
                android:layout_height="match_parent"
                android:layout_marginStart="12dp"
                android:layout_weight="1"
                android:iconifiedByDefault="false"
                android:queryHint="Search RSOs…"
                android:searchIcon="@null" />

        </LinearLayout>

    </com.google.android.material.appbar.MaterialToolbar>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@android:color/white"
        android:elevation="2dp"
        android:orientation="horizontal"
        android:paddingHorizontal="16dp"
        android:paddingVertical="8dp">

        <com.google.android.material.chip.Chip
            android:id="@+id/chipOrange"
            style="@style/Widget.MaterialComponents.Chip.Filter"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginEnd="8dp"
            android:checked="true"
            android:text="Student Orgs" />

        <com.google.android.material.chip.Chip
            android:id="@+id/chipBlue"
            style="@style/Widget.MaterialComponents.Chip.Filter"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:checked="true"
            android:text="Blue Orgs" />

    </LinearLayout>

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recycler_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:clipToPadding="false"
            android:paddingHorizontal="8dp" />

        <TextView
            android:id="@+id/empty_state"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:text="No RSOs found"
            android:textColor="#888888"
            android:textSize="16sp"
            android:visibility="gone" />

    </FrameLayout>

</LinearLayout>
```

- [ ] **Step 2: Replace `MainActivity.kt` with chip-based filter logic + empty state + chip color styling**

```kotlin
package edu.illinois.cs.cs124.ay2024.mp.activities

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import edu.illinois.cs.cs124.ay2024.mp.R
import edu.illinois.cs.cs124.ay2024.mp.adapters.SummaryListAdapter
import edu.illinois.cs.cs124.ay2024.mp.application.JoinableApplication
import edu.illinois.cs.cs124.ay2024.mp.models.Summary

class MainActivity : Activity(), SearchView.OnQueryTextListener {
    private val TAG = MainActivity::class.java.simpleName
    private var summaries: List<Summary> = emptyList()
    private lateinit var listAdapter: SummaryListAdapter
    private lateinit var emptyState: TextView
    private lateinit var searchView: SearchView
    private val shownColors =
        mutableSetOf(Summary.Color.ORANGE, Summary.Color.BLUE, Summary.Color.DEPARTMENT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listAdapter =
            SummaryListAdapter(summaries, this) { summary ->
                Log.d(TAG, "User clicked on ${summary.title}")
                startActivity(Intent(this, RSOActivity::class.java).apply {
                    putExtra("id", summary.id)
                })
            }

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = listAdapter

        emptyState = findViewById(R.id.empty_state)
        searchView = findViewById(R.id.search)
        searchView.setOnQueryTextListener(this)

        val chipOrange = findViewById<Chip>(R.id.chipOrange)
        val chipBlue = findViewById<Chip>(R.id.chipBlue)

        // Style orange chip: filled orange when checked, light grey when unchecked
        chipOrange.chipBackgroundColor = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(getColor(R.color.illinoisOrange), Color.parseColor("#F0F0F0")),
        )
        chipOrange.setTextColor(ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(Color.WHITE, Color.parseColor("#888888")),
        ))

        // Style blue chip: filled Illinois Blue when checked, light grey when unchecked
        chipBlue.chipBackgroundColor = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(getColor(R.color.illinoisBlue), Color.parseColor("#F0F0F0")),
        )
        chipBlue.setTextColor(ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(Color.WHITE, Color.parseColor("#888888")),
        ))

        chipOrange.setOnCheckedChangeListener { _, checked ->
            if (checked) shownColors.add(Summary.Color.ORANGE)
            else shownColors.remove(Summary.Color.ORANGE)
            updateList()
        }

        chipBlue.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                shownColors.add(Summary.Color.BLUE)
            } else {
                shownColors.remove(Summary.Color.BLUE)
            }
            updateList()
        }
    }

    private fun updateList() {
        val query = searchView.query?.toString() ?: ""
        var filtered = Summary.filterColor(summaries, shownColors)
        if (query.isNotBlank()) filtered = Summary.search(filtered, query)
        listAdapter.setSummaries(filtered)
        val isEmpty = filtered.isEmpty()
        emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        findViewById<RecyclerView>(R.id.recycler_view).visibility =
            if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        val client = (application as JoinableApplication).getClient()
        client.getSummaries { result ->
            try {
                summaries = result.getValue().sorted()
                updateList()
            } catch (e: Exception) {
                Log.e(TAG, "Error updating summary list", e)
            }
        }
    }

    override fun onQueryTextChange(query: String): Boolean {
        updateList()
        return true
    }

    override fun onQueryTextSubmit(unused: String): Boolean = false
}
```

- [ ] **Step 3: Verify build**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/activity_main.xml app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/activities/MainActivity.kt
git commit -m "style: new main screen with MaterialToolbar, filter chips, and empty state"
```

---

## Task 17: Detail Screen — New Layout + Updated RSOActivity (Rough Edge Fixes)

**Files:**
- Modify: `app/src/main/res/layout/activity_rso.xml`
- Modify: `app/src/main/java/.../activities/RSOActivity.kt`

This task also implements the three rough edge fixes:
- Favorite no longer closes the activity
- Website is tappable (opens browser)
- Category chips rendered in hero header

- [ ] **Step 1: Replace `activity_rso.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <!-- Illinois Blue hero header -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@color/illinoisBlue"
        android:orientation="vertical"
        android:paddingBottom="20dp">

        <!-- Back + Favorite row -->
        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="56dp"
            android:paddingHorizontal="8dp">

            <TextView
                android:id="@+id/backButton"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_centerVertical="true"
                android:layout_alignParentStart="true"
                android:clickable="true"
                android:focusable="true"
                android:padding="8dp"
                android:text="←"
                android:textColor="@android:color/white"
                android:textSize="22sp" />

            <TextView
                android:id="@+id/favoriteButton"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_centerVertical="true"
                android:layout_alignParentEnd="true"
                android:clickable="true"
                android:focusable="true"
                android:padding="8dp"
                android:text="☆"
                android:textColor="@android:color/white"
                android:textSize="26sp" />

        </RelativeLayout>

        <!-- Org name -->
        <TextView
            android:id="@+id/rsoTitle"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:lineSpacingMultiplier="1.3"
            android:paddingHorizontal="18dp"
            android:textColor="@android:color/white"
            android:textSize="18sp"
            android:textStyle="bold" />

        <!-- Org type -->
        <TextView
            android:id="@+id/rsoType"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp"
            android:paddingHorizontal="18dp"
            android:textColor="#99FFFFFF"
            android:textSize="12sp" />

        <!-- Category chips -->
        <HorizontalScrollView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="10dp"
            android:paddingHorizontal="14dp"
            android:scrollbars="none">

            <LinearLayout
                android:id="@+id/categoryChips"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:padding="4dp" />

        </HorizontalScrollView>

    </LinearLayout>

    <!-- White content panel -->
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="20dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:letterSpacing="0.1"
                android:text="MISSION"
                android:textColor="@color/illinoisOrange"
                android:textSize="10sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/mission"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="6dp"
                android:lineSpacingMultiplier="1.6"
                android:textColor="#222222"
                android:textSize="14sp" />

            <View
                android:layout_width="match_parent"
                android:layout_height="1dp"
                android:layout_marginVertical="16dp"
                android:background="#EBEBEB" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:letterSpacing="0.1"
                android:text="WEBSITE"
                android:textColor="@color/illinoisOrange"
                android:textSize="10sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/website"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="6dp"
                android:clickable="true"
                android:focusable="true"
                android:textColor="@color/illinoisBlue"
                android:textSize="14sp"
                android:textStyle="italic" />

        </LinearLayout>

    </ScrollView>

</LinearLayout>
```

- [ ] **Step 2: Replace `RSOActivity.kt` with new layout wiring + rough edge fixes**

```kotlin
package edu.illinois.cs.cs124.ay2024.mp.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import edu.illinois.cs.cs124.ay2024.mp.R
import edu.illinois.cs.cs124.ay2024.mp.application.JoinableApplication
import edu.illinois.cs.cs124.ay2024.mp.models.Summary
import edu.illinois.cs.cs124.ay2024.mp.network.Server

class RSOActivity : Activity() {
    private val TAG = RSOActivity::class.java.simpleName
    private lateinit var rsoId: String
    private lateinit var favoriteButton: TextView
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rso)

        rsoId = intent.getStringExtra("id") ?: return
        val rso = Server.getIdToRSO()[rsoId] ?: return

        // Back button
        findViewById<TextView>(R.id.backButton).setOnClickListener { finish() }

        // Org name and type
        findViewById<TextView>(R.id.rsoTitle).text = rso.title
        findViewById<TextView>(R.id.rsoType).text =
            when (rso.color) {
                Summary.Color.DEPARTMENT -> "Department"
                else -> "Student Organization"
            }

        // Category chips in hero header
        val chipsContainer = findViewById<LinearLayout>(R.id.categoryChips)
        for (category in rso.categories) {
            chipsContainer.addView(buildCategoryChip(category))
        }

        // Mission
        findViewById<TextView>(R.id.mission).text = rso.mission

        // Website — tappable, opens browser
        val websiteView = findViewById<TextView>(R.id.website)
        websiteView.text = rso.website
        websiteView.setOnClickListener {
            val url = rso.website.let { if (!it.startsWith("http")) "https://$it" else it }
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        // Favorite button — toggle icon only, do NOT close activity
        favoriteButton = findViewById(R.id.favoriteButton)
        favoriteButton.setOnClickListener {
            isFavorite = !isFavorite
            updateFavoriteIcon()
            val client = (application as JoinableApplication).getClient()
            client.setFavorite(rsoId, isFavorite) { result ->
                Log.d(TAG, "Favorite set to $isFavorite")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!::rsoId.isInitialized) return
        val client = (application as JoinableApplication).getClient()
        client.getFavorite(rsoId) { result ->
            try {
                isFavorite = result.getValue()
                runOnUiThread { updateFavoriteIcon() }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching favorite status", e)
            }
        }
    }

    private fun updateFavoriteIcon() {
        favoriteButton.text = if (isFavorite) "★" else "☆"
    }

    private fun buildCategoryChip(text: String): TextView {
        val density = resources.displayMetrics.density
        val chip = TextView(this)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { marginEnd = (8 * density).toInt() }
        chip.layoutParams = params
        chip.text = text
        chip.setPadding(
            (12 * density).toInt(), (4 * density).toInt(),
            (12 * density).toInt(), (4 * density).toInt(),
        )
        chip.setTextColor(Color.WHITE)
        chip.textSize = 12f
        chip.background = GradientDrawable().apply {
            cornerRadius = 14 * density
            setColor(Color.parseColor("#59E84A27"))
            setStroke((1 * density).toInt(), Color.parseColor("#B3E84A27"))
        }
        return chip
    }
}
```

- [ ] **Step 3: Verify build**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/activity_rso.xml app/src/main/java/edu/illinois/cs/cs124/ay2024/mp/activities/RSOActivity.kt
git commit -m "style: new detail screen with blue hero header, tappable website, fixed favorite UX"
```

---

## Task 18: Final Verification

- [ ] **Step 1: Run full test suite**

```bash
./gradlew test
```
Expected: `BUILD SUCCESSFUL` with all tests passing.

- [ ] **Step 2: Run spotless to ensure formatting is clean**

```bash
./gradlew spotlessApply && ./gradlew spotlessCheck
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Fix any formatting issues, commit if needed**

```bash
git add -p
git commit -m "style: apply spotless formatting"
```

---

## Task 19: Push to Portfolio GitHub Repo

- [ ] **Step 1: Add the new remote**

```bash
git remote add origin https://github.com/fsiddiqui4320/uiuc-rso-app.git
```

- [ ] **Step 2: Push main branch and tags**

```bash
git push -u origin main --tags
```
Expected: branch `main` and tag `school-original` appear on GitHub.

- [ ] **Step 3: Verify on GitHub**

Open `https://github.com/fsiddiqui4320/uiuc-rso-app` and confirm:
- `main` branch has all commits
- Tags show `school-original`
- README (if any) is present
