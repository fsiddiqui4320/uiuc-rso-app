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

        chipOrange.chipBackgroundColor = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(getColor(R.color.illinoisOrange), Color.parseColor("#F0F0F0")),
        )
        chipOrange.setTextColor(ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(Color.WHITE, Color.parseColor("#888888")),
        ))
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
            if (checked) shownColors.add(Summary.Color.BLUE)
            else shownColors.remove(Summary.Color.BLUE)
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
