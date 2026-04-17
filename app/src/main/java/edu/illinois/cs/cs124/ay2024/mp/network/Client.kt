package edu.illinois.cs.cs124.ay2024.mp.network

import android.os.Build
import android.util.Log
import com.android.volley.Cache
import com.android.volley.ExecutorDelivery
import com.android.volley.Network
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyLog
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
        VolleyLog.DEBUG = false
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
            null
        }

        if (serverURL != null) {
            Thread {
                var connected = false
                for (i in 0 until MAX_STARTUP_RETRIES) {
                    try {
                        val connection = serverURL.openConnection() as HttpURLConnection
                        val body = BufferedReader(InputStreamReader(connection.inputStream))
                            .lines().collect(Collectors.joining("\n"))
                        if (body != CHECK_SERVER_RESPONSE) throw IllegalStateException("Invalid response")
                        connection.disconnect()
                        this.connected.complete(true)
                        requestQueue.start()
                        connected = true
                        break
                    } catch (ignored: Exception) {}
                    try { Thread.sleep(INITIAL_CONNECTION_RETRY_DELAY) } catch (ignored: InterruptedException) {}
                }
                if (!connected) {
                    Log.e(TAG, "Client couldn't connect")
                }
            }.start()
        }
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
