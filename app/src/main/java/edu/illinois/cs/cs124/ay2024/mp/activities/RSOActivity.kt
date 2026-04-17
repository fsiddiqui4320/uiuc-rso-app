package edu.illinois.cs.cs124.ay2024.mp.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import edu.illinois.cs.cs124.ay2024.mp.R
import edu.illinois.cs.cs124.ay2024.mp.application.JoinableApplication
import edu.illinois.cs.cs124.ay2024.mp.models.RSO
import edu.illinois.cs.cs124.ay2024.mp.models.Summary

class RSOActivity : Activity() {
    private val tag = RSOActivity::class.java.simpleName
    private lateinit var rsoId: String
    private lateinit var favoriteButton: TextView
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rso)

        rsoId = intent.getStringExtra("id") ?: return

        findViewById<TextView>(R.id.backButton).setOnClickListener { finish() }

        favoriteButton = findViewById(R.id.favoriteButton)
        favoriteButton.setOnClickListener {
            isFavorite = !isFavorite
            updateFavoriteIcon()
            val client = (application as JoinableApplication).getClient()
            client.setFavorite(rsoId, isFavorite) { _ ->
                Log.d(tag, "Favorite set to $isFavorite")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!::rsoId.isInitialized) return
        val client = (application as JoinableApplication).getClient()
        client.getRSO(rsoId) { result ->
            try {
                val rso = result.getValue()
                runOnUiThread { bindRso(rso) }
            } catch (e: Exception) {
                Log.e(tag, "Error loading RSO", e)
            }
        }
        client.getFavorite(rsoId) { result ->
            try {
                isFavorite = result.getValue()
                runOnUiThread { updateFavoriteIcon() }
            } catch (e: Exception) {
                Log.e(tag, "Error fetching favorite status", e)
            }
        }
    }

    private fun bindRso(rso: RSO) {
        findViewById<TextView>(R.id.rsoTitle).text = rso.title
        findViewById<TextView>(R.id.rsoType).text =
            when (rso.color) {
                Summary.Color.DEPARTMENT -> "Department"
                else -> "Student Organization"
            }

        val chipsContainer = findViewById<LinearLayout>(R.id.categoryChips)
        chipsContainer.removeAllViews()
        for (category in rso.categories) {
            chipsContainer.addView(buildCategoryChip(category))
        }

        findViewById<TextView>(R.id.mission).text = rso.mission

        val websiteView = findViewById<TextView>(R.id.website)
        websiteView.text = rso.website
        websiteView.setOnClickListener {
            val url = rso.website.let { if (!it.startsWith("http")) "https://$it" else it }
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    private fun updateFavoriteIcon() {
        favoriteButton.text = if (isFavorite) "★" else "☆"
    }

    private fun buildCategoryChip(text: String): TextView {
        val density = resources.displayMetrics.density
        val chip = TextView(this)
        val params =
            LinearLayout
                .LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { marginEnd = (8 * density).toInt() }
        chip.layoutParams = params
        chip.text = text
        chip.setPadding(
            (12 * density).toInt(),
            (4 * density).toInt(),
            (12 * density).toInt(),
            (4 * density).toInt(),
        )
        chip.setTextColor(Color.WHITE)
        chip.textSize = 12f
        chip.background =
            GradientDrawable().apply {
                cornerRadius = 14 * density
                setColor(Color.parseColor("#59E84A27"))
                setStroke((1 * density).toInt(), Color.parseColor("#B3E84A27"))
            }
        return chip
    }
}
