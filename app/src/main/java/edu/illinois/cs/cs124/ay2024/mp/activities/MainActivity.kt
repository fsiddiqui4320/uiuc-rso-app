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
