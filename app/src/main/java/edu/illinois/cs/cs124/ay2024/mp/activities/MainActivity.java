package edu.illinois.cs.cs124.ay2024.mp.activities;

import static org.junit.Assert.assertNotNull;
import static edu.illinois.cs.cs124.ay2024.mp.models.Summary.filterColor;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SearchView;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import edu.illinois.cs.cs124.ay2024.mp.R;
import edu.illinois.cs.cs124.ay2024.mp.adapters.SummaryListAdapter;
import edu.illinois.cs.cs124.ay2024.mp.application.JoinableApplication;
import edu.illinois.cs.cs124.ay2024.mp.models.Summary;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Main activity showing RSO summaries and enabling filtering and searching.
 *
 * <p>This creates the first screen shown to the user when the app launches.
 */
public final class MainActivity extends Activity implements SearchView.OnQueryTextListener {
  /** Tag to identify the MainActivity in the logs. */
  private static final String TAG = MainActivity.class.getSimpleName();


  /** List of RSO summaries received from the server, initially empty. */
  private List<Summary> summaries = Collections.emptyList();

  public void setSummaries(List<Summary> summaries) {
    this.summaries = summaries;
  }

  /** Adapter that connects our list of summaries with the UI displayed to the user. */
  private SummaryListAdapter listAdapter;

  /** {@inheritDoc} */
  @Override
  protected void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);

    // Load this activity's layout and set the title
    setContentView(R.layout.activity_main);
    setTitle("Discover RSOs");

    // Set up the list adapter for the list of RSO summaries
    listAdapter = new SummaryListAdapter(summaries, this, summary -> {
      Log.d(TAG, "User clicked on " + summary.getTitle());
      Intent rsoActivity = new Intent(this, RSOActivity.class);
      assertNotNull("Intent component is null", rsoActivity.getComponent());
      rsoActivity.putExtra("id", summary.getId());
      Log.d("TEST", "Intent Action: " + rsoActivity.getAction());
      Log.d("TEST", "Intent Component: " + rsoActivity.getComponent());
      Log.d("TEST", "Intent Extras: " + rsoActivity.getExtras());
      startActivity(rsoActivity);
    });

    // Add the list to the layout
    RecyclerView recyclerView = findViewById(R.id.recycler_view);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setAdapter(listAdapter);

    // Register this component as a callback for changes to the search view component.
    // We'll eventually use these events to perform interactive searching.
    SearchView searchView = findViewById(R.id.search);
    searchView.setOnQueryTextListener(this);

    ToggleButton orangeButton = findViewById(R.id.orangeButton);
    orangeButton.setOnCheckedChangeListener(orangeButtonListener);

    ToggleButton blueButton = findViewById(R.id.blueButton);
    blueButton.setOnCheckedChangeListener(blueButtonListener);
  }

  private final Set<Summary.Color> shownColors = new HashSet<>(Arrays.asList(
      Summary.Color.ORANGE, Summary.Color.BLUE, Summary.Color.DEPARTMENT
  ));

  private final CompoundButton.OnCheckedChangeListener orangeButtonListener = (unused, checked) -> {
    Log.d(TAG, "Orange button checked " + checked);
    if (checked) {
      shownColors.add(Summary.Color.ORANGE);
      onStart();
    } else {
      shownColors.remove(Summary.Color.ORANGE);
    }
    List<Summary> filteredSummaries = filterColor(summaries, shownColors);
    listAdapter.setSummaries(filteredSummaries);
    listAdapter.notifyDataSetChanged();
    onStart();
    Log.d(TAG, "Currently shown colors: " + shownColors);
  };

  private final CompoundButton.OnCheckedChangeListener blueButtonListener = (unused, checked) -> {
    Log.d(TAG, "Blue button checked " + checked);
    if (checked) {
      shownColors.add(Summary.Color.BLUE);
      onStart();
    } else {
      shownColors.remove(Summary.Color.BLUE);
    }
    List<Summary> filteredSummaries = filterColor(summaries, shownColors);
    listAdapter.setSummaries(filteredSummaries);
    listAdapter.notifyDataSetChanged();
    onStart();
    Log.d(TAG, "Currently shown colors: " + shownColors);
  };

  /** {@inheritDoc} */
  @Override
  protected void onStart() {
    super.onStart();

    // Initiate a request for the summary list
    JoinableApplication application = (JoinableApplication) getApplication();
    application
        .getClient()
        .getSummaries(
            (result) -> {
              // Update the list shown to the user in a callback
              try {
                Log.d(TAG, "Initial size: " + result.getValue().size());
                summaries = filterColor(result.getValue(), shownColors);
                Log.d(TAG, "Filtered size: " + summaries.size());
                listAdapter.setSummaries(summaries);
              } catch (Exception e) {
                Log.e(TAG, "Error updating summary list", e);
              }
            });
  }

  /**
   * This fires every time the text in the search bar changes. We'll eventually handle this by
   * updating the UI summary list.
   *
   * <p>{@inheritDoc}
   *
   * @param query the text to use to filter the summary list
   * @return true because we handled the action
   */
  @Override
  public boolean onQueryTextChange(@NonNull String query) {
    List<Summary> filteredSummaries = Summary.search(summaries, query);
    List<String> filteredIDs = new ArrayList<>();
    List<String> filteredTitles = new ArrayList<>();

    for (Summary summary : filteredSummaries) {
      filteredIDs.add(summary.getId());
      filteredTitles.add(summary.getId());
    }
    Log.d(TAG, "Filtered IDs: " + filteredIDs + "\n Filtered IDs: " + filteredIDs);
    listAdapter.setSummaries(filteredSummaries);
    listAdapter.notifyDataSetChanged();
    return true;
  }

  /**
   * This would correspond to the user hitting enter or a submit button next to the search bar.
   * Because we update the list on each change to the search value, we do not handle this event.
   *
   * <p>{@inheritDoc}
   *
   * @param unused current query text
   * @return false because we did not handle this action
   */
  @Override
  public boolean onQueryTextSubmit(@NonNull String unused) {
    return false;
  }
}
