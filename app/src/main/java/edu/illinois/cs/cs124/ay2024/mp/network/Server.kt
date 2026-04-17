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
            server.dispatcher = this
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
