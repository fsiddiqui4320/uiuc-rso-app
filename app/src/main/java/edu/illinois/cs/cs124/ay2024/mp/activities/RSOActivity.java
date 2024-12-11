package edu.illinois.cs.cs124.ay2024.mp.activities;

import static org.junit.Assert.assertNotNull;
import static edu.illinois.cs.cs124.ay2024.mp.models.Summary.filterColor;
//import static edu.illinois.cs.cs124.ay2024.mp.models.Summary.sortByFavorite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cs124.ay2024.mp.R;
import edu.illinois.cs.cs124.ay2024.mp.adapters.SummaryListAdapter;
import edu.illinois.cs.cs124.ay2024.mp.application.JoinableApplication;
import edu.illinois.cs.cs124.ay2024.mp.models.RSO;
import edu.illinois.cs.cs124.ay2024.mp.models.Summary;
import edu.illinois.cs.cs124.ay2024.mp.network.Client;
import edu.illinois.cs.cs124.ay2024.mp.network.Server;

public class RSOActivity extends Activity {
  /** Tag to identify the RSOActivity in the logs. */
  private static final String TAG = RSOActivity.class.getSimpleName();

  private List<Summary> summaries = Collections.emptyList();
  private final Set<Summary.Color> shownColors = new HashSet<>(Arrays.asList(
      Summary.Color.ORANGE, Summary.Color.BLUE, Summary.Color.DEPARTMENT
  ));

  private SummaryListAdapter listAdapter;
  private String rsoId;
  private ToggleButton favoriteButton;

  private final Map<String, Boolean> favoriteMap = new HashMap<>();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Load this activity's layout and set the title
    setContentView(R.layout.activity_rso);

    rsoId = getIntent().getStringExtra("id");
    Map<String, RSO> map = Server.getIdToRSO();
    RSO rso;
    if (!map.containsKey(rsoId)) {
      // If no RSO found with that ID, just return
      return;
    } else {
      rso = map.get(rsoId);
    }
    assert rso != null;
    String title = rso.getTitle();
    String website = rso.getWebsite();
    String mission = rso.getMission();
    String finalCats = String.join(", ", rso.getCategories());

    TextView titleText = findViewById(R.id.example);
    titleText.setText(title);
    TextView webText = findViewById(R.id.web);
    webText.setText(website);
    TextView missText = findViewById(R.id.miss);
    missText.setText(mission);
    TextView catText = findViewById(R.id.cats);
    catText.setText(finalCats);

    // Initially, create the list adapter with an empty list
    listAdapter = new SummaryListAdapter(summaries, this, summary -> {
      Log.d(TAG, "User clicked on " + summary.getTitle());
      Intent rsoActivity = new Intent(this, RSOActivity.class);
      assertNotNull("Intent component is null", rsoActivity.getComponent());
      rsoActivity.putExtra("id", summary.getId());
      startActivity(rsoActivity);
    });

    favoriteButton = findViewById(R.id.favoriteButton);
    favoriteButton.setOnCheckedChangeListener(favoriteButtonListener);
    Log.d(TAG, "RSOActivity launched");
  }

  @Override
  protected void onStart() {
    super.onStart();

    // Access the application and retrieve the client to get summaries
    JoinableApplication application = (JoinableApplication) getApplication();
    Client client = application.getClient();

    // Retrieve summaries from the server
    client.getSummaries(result -> {
      try {
        List<Summary> allSummaries = result.getValue();
        // Filter by shownColors if needed
        summaries = filterColor(allSummaries, shownColors);
        Collections.sort(summaries);
        runOnUiThread(() -> {
          listAdapter.setSummaries(summaries);
          listAdapter.notifyDataSetChanged();
        });
      } catch (Exception e) {
        Log.e(TAG, "Error updating summary list", e);
      }
    });

    // Retrieve the current favorite status of this RSO
    client.getFavorite(rsoId, favoriteResult -> {
      if (!(favoriteResult == null)) {
        boolean isFavorite = favoriteResult.getValue();
        runOnUiThread(() -> favoriteButton.setChecked(isFavorite));
      }
    });
  }

  private final CompoundButton.OnCheckedChangeListener favoriteButtonListener = (unused, checked) -> {
    Log.d(TAG, "Favorite button checked " + checked);
    JoinableApplication application = (JoinableApplication) getApplication();
    Client client = application.getClient();
    // Set the favorite status for the current RSO based on the button state
    client.setFavorite(rsoId, checked, result -> {
      runOnUiThread(() -> {
        listAdapter.notifyDataSetChanged();
      });
      Log.d(TAG, "Favorite status set to: " + checked);
    });
  };
}
