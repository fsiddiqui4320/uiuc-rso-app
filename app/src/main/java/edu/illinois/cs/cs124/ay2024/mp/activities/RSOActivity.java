package edu.illinois.cs.cs124.ay2024.mp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.Nullable;
import java.util.List;
import java.util.Map;
import edu.illinois.cs.cs124.ay2024.mp.R;
import edu.illinois.cs.cs124.ay2024.mp.models.RSO;
import edu.illinois.cs.cs124.ay2024.mp.network.Server;

public class RSOActivity extends Activity {
  /** Tag to identify the RSOActivity in the logs. */
  private static final String TAG = RSOActivity.class.getSimpleName();


  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Load this activity's layout and set the title
    setContentView(R.layout.activity_rso);

    String rsoId = getIntent().getStringExtra("id");
    Map<String, RSO> map = Server.getIdToRSO();
    RSO rso;
    if (!map.containsKey(rsoId)) {
      return;
    } else {
      rso = map.get(rsoId);
    }
    assert rso != null;
    String title = rso.getTitle();
    String website = rso.getWebsite();
    String mission = rso.getMission();
    StringBuilder categories = new StringBuilder();
    List<String> cats = rso.getCategories();

    for (int i = 0; i < cats.size() - 1; i++) {
      String category = cats.get(i);
      categories.append(category).append(", ");
    }
    categories.append(cats.get(cats.size() - 1));
    String finalCats = categories.toString();

    TextView titleText = findViewById(R.id.example);
    titleText.setText(title);
    TextView webText = findViewById(R.id.web);
    webText.setText(website);
    TextView missText = findViewById(R.id.miss);
    missText.setText(mission);
    TextView catText = findViewById(R.id.cats);
    catText.setText(finalCats);

    Log.d(TAG, "RSOActivity launched");
  }
}
