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

    override fun getItemCount(): Int = summaries.size
}
