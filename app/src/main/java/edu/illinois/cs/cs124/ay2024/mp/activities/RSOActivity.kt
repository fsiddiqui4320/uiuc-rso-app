package edu.illinois.cs.cs124.ay2024.mp.activities

import android.app.Activity
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
            client.setFavorite(rsoId, checked) { _ ->
                runOnUiThread { finish() }
                Log.d(TAG, "Favorite status set to: $checked")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!::rsoId.isInitialized) return
        val client = (application as JoinableApplication).getClient()
        client.getFavorite(rsoId) { result ->
            if (result != null) {
                val isFavorite = result.getValue()
                runOnUiThread { favoriteButton.isChecked = isFavorite }
            }
        }
    }
}
