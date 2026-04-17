package com.fsiddiqui.joinable.application

import android.app.Application
import android.os.Build
import com.fsiddiqui.joinable.network.Client
import com.fsiddiqui.joinable.network.ServerBridge

class JoinableApplication : Application() {
    private lateinit var client: Client

    override fun onCreate() {
        super.onCreate()
        if (Build.FINGERPRINT == "robolectric") {
            ServerBridge.start()
        } else {
            Thread { ServerBridge.start() }.start()
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
