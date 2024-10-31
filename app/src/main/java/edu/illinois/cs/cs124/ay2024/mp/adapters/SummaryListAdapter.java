package edu.illinois.cs.cs124.ay2024.mp.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import edu.illinois.cs.cs124.ay2024.mp.R;
import edu.illinois.cs.cs124.ay2024.mp.models.Summary;
import java.util.List;
import java.util.function.Consumer;

/**
 * Adapter to display a list of summaries using Android's RecyclerView.
 *
 * <p>You should not need to modify this code, although you may want to.
 */
public class SummaryListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  /** Holds the list of summaries shown by the recycler view. */
  @NonNull private List<Summary> summaries;

  /** Callback method invoked when an item in the list is clicked. */
  @Nullable private final Consumer<Summary> onClickCallback;

  /** Activity using this list adapter, to allow UI actions to run on the UI thread. */
  @NonNull private final Activity activity;

  /** Create a list adapter holding a list of RSO summaries and register a callback method. */
  public SummaryListAdapter(
      @NonNull List<Summary> setSummaries,
      @NonNull Activity setActivity,
      @Nullable Consumer<Summary> setOnClickCallback) {
    summaries = setSummaries;
    activity = setActivity;
    onClickCallback = setOnClickCallback;
  }

  /** Create a list adapter holding a list of RSO summaries without a callback method. */
  public SummaryListAdapter(@NonNull List<Summary> setSummaries, @NonNull Activity setActivity) {
    this(setSummaries, setActivity, null);
  }

  /** Update the list of RSO summaries displayed by the list adapter. */
  @SuppressLint("NotifyDataSetChanged")
  public void setSummaries(@NonNull List<Summary> setSummaries) {
    summaries = setSummaries;
    activity.runOnUiThread(this::notifyDataSetChanged);
  }

  /** {@inheritDoc} */
  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_summary, parent, false);
    return new RecyclerView.ViewHolder(view) {};
  }

  /** {@inheritDoc} */
  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    Summary summary = summaries.get(position);

    // Load layout component references
    TextView title = holder.itemView.findViewById(R.id.title);
    LinearLayout layout = holder.itemView.findViewById(R.id.layout);

    // Set the RSO item title text and text color appropriately
    title.setText(summary.getTitle());
    title.setTextColor(activity.getColor(R.color.darkTextColor));

    // Set the RSO item background color appropriately
    int backgroundColor = activity.getColor(R.color.departmentColor);
    Summary.Color color = summary.getColor();
    if (color.equals(Summary.Color.ORANGE)) {
      backgroundColor = activity.getColor(R.color.lighterIllinoisOrange);
    } else if (color.equals(Summary.Color.BLUE)) {
      backgroundColor = activity.getColor(R.color.lighterIllinoisBlue);
    }
    layout.setBackgroundColor(backgroundColor);

    if (onClickCallback != null) {
      // Set the onClick callback to launch the RSO detail activity
      layout.setOnClickListener(view -> onClickCallback.accept(summary));
    }
  }

  /** {@inheritDoc} */
  @Override
  public int getItemCount() {
    return summaries.size();
  }
}
