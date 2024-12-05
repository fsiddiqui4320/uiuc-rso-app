package edu.illinois.cs.cs124.ay2024.mp.test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.truth.Truth.assertWithMessage;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Data.OBJECT_MAPPER;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Data.RSODATA;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Data.SUMMARIES;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Data.SUMMARY_COUNT;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Data.getShuffledSummaries;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.HTTP.getAPIClient;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.HTTP.testClient;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.HTTP.testServerGet;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.HTTP.testServerGetTimed;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.HTTP.testServerPost;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.RecyclerViewMatcher.withRecyclerView;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.TestHelpers.checkServerDesign;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.TestHelpers.configureLogging;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.TestHelpers.pause;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.TestHelpers.startActivity;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.TestHelpers.startMainActivity;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.TestHelpers.trimmedMean;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Views.countRecyclerView;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Views.isChecked;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Views.setChecked;
import static org.hamcrest.Matchers.containsString;

import android.content.Intent;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.illinois.cs.cs124.ay2024.mp.R;
import edu.illinois.cs.cs124.ay2024.mp.activities.RSOActivity;
import edu.illinois.cs.cs124.ay2024.mp.models.Favorite;
import edu.illinois.cs.cs124.ay2024.mp.models.RSO;
import edu.illinois.cs.cs124.ay2024.mp.models.Summary;
import edu.illinois.cs.cs124.ay2024.mp.network.Client;
import edu.illinois.cs.cs124.ay2024.mp.network.Server;
import edu.illinois.cs.cs124.ay2024.mp.test.helpers.HTTP;
import edu.illinois.cs.cs124.ay2024.mp.test.helpers.JSONReadCountSecurityManager;
import edu.illinois.cs.cs124.ay2024.mp.test.helpers.TestHelpers;
import edu.illinois.cs.cs125.gradlegrader.annotations.Graded;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.experimental.LazyApplication;

/*
 * This is the MP3 test suite.
 * The code below is used to evaluate your app during testing, local grading, and official grading.
 * You may not understand all of the code below, but you'll need to have some understanding of how
 * it works so that you can determine what is wrong with your app and what you need to fix.
 *
 * ALL CHANGES TO THIS FILE WILL BE OVERWRITTEN DURING OFFICIAL GRADING.
 * You can and should modify the code below if it is useful during your own local testing,
 * but any changes you make will be discarded during official grading.
 * The local grader will not run if the test suites have been modified, so you'll need to undo any
 * local changes before you run the grader.
 *
 * Note that this means that you should not fix problems with the app by modifying the test suites.
 * The test suites are always considered to be correct.
 *
 * Our test suites are broken into two parts.
 * The unit tests are tests that we can perform without running your app.
 * They test things like whether a method works properly or the behavior of your API server.
 * Unit tests are usually fairly fast.
 *
 * The integration tests are tests that require simulating your app.
 * This allows us to test things like your API client, and higher-level aspects of your app's
 * behavior, such as whether it displays the right thing on the display.
 * Because integration tests require simulating your app, they run more slowly.
 *
 * The MP3 test suite includes no ungraded tests.
 * These tests are fairly idiomatic, in that they resemble tests you might write for an actual
 * Android programming project.
 * Not entirely though.
 */

@RunWith(AndroidJUnit4.class)
@LooperMode(LooperMode.Mode.PAUSED)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class MP3Test {

  // Test the POST and GET /favorite server routes
  @SuppressWarnings("SpellCheckingInspection")
  @Test(timeout = 60000L)
  @Graded(points = 20, friendlyName = "Server GET and POST /favorite (Unit)")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test0_ServerGETAndPOSTFavorite() throws IOException {
    // Get a shuffled list of summaries
    Random random = new Random(12431);
    List<Summary> trimmedSummaries = getShuffledSummaries(12431, 128);

    // Perform initial GET /favorite requests
    for (Summary summary : trimmedSummaries) {
      // Perform initial GET
      Favorite favorite = testServerGet("/favorite/" + summary.getId(), Favorite.class);
      // No RSOs should be a favorite at this point
      assertWithMessage("Incorrect favorite value for RSO")
          .that(favorite.getFavorite())
          .isEqualTo(false);
    }

    // Perform POST /favorite requests to change favorite status

    // Map to store the favorite status set during the next round
    Map<String, Boolean> favorites = new HashMap<>();
    // Reshuffle the list of summaries
    Collections.shuffle(trimmedSummaries, random);

    for (Summary summary : trimmedSummaries) {
      // POST to change favorite value
      boolean isFavorite = random.nextBoolean();

      // Construct POST favorite body
      ObjectNode newFavorite = OBJECT_MAPPER.createObjectNode();
      newFavorite.set("id", OBJECT_MAPPER.convertValue(summary.getId(), JsonNode.class));
      newFavorite.set("favorite", OBJECT_MAPPER.convertValue(isFavorite, JsonNode.class));

      // POST to change the favorite status accordingly
      Favorite favorite = testServerPost("/favorite", newFavorite, Favorite.class);
      // Ensure the result is what we expect
      assertWithMessage("Incorrect value from favorite POST")
          .that(favorite.getFavorite())
          .isEqualTo(isFavorite);

      // Save favorite value for next stage
      favorites.put(summary.getId(), isFavorite);
    }

    // Save response times for comparison
    List<Long> baseResponseTimes = new ArrayList<>();
    List<Long> getResponseTimes = new ArrayList<>();

    // Second route of GET /favorite requests to ensure favorites are saved
    Collections.shuffle(trimmedSummaries, random);
    for (Summary summary : trimmedSummaries) {
      // Time the index route for comparison
      HTTP.TimedResponse<Summary> baseResult = testServerGetTimed("/");
      baseResponseTimes.add(baseResult.getResponseTime().toNanos());

      // Retrieve saved favorite
      boolean savedFavorite = Objects.requireNonNull(favorites.get(summary.getId()));

      // Final GET
      HTTP.TimedResponse<Favorite> favoriteResult =
          testServerGetTimed("/favorite/" + summary.getId(), Favorite.class);
      // Ensure the result is what we expect
      assertWithMessage("Incorrect favorite value for RSO")
          .that(favoriteResult.getResponse().getFavorite())
          .isEqualTo(savedFavorite);
      getResponseTimes.add(favoriteResult.getResponseTime().toNanos());
    }

    // Check for slow server GETs potentially caused by unnecessary looping or parsing
    double averageBase = trimmedMean(baseResponseTimes, 0.1);
    double averageResponse = trimmedMean(getResponseTimes, 0.1);
    assertWithMessage("Server GET /favorite/ is too slow")
        .that(averageResponse)
        .isLessThan(averageBase * TestHelpers.GET_METHOD_EXTRA_TIME);

    // Test bad requests

    // Non-existent RSO GET
    testServerGet("/favorite/XgRG0-qzi7cNaYPtF8lI1m62tEO/", HttpURLConnection.HTTP_NOT_FOUND);

    // Bad URL GET
    testServerGet("/favorites/8OozGTNQpaETr0sAB4UVt_ee2qk", HttpURLConnection.HTTP_NOT_FOUND);

    // Non-existent RSO POST
    ObjectNode nonexistentRSO = OBJECT_MAPPER.createObjectNode();
    //noinspection SpellCheckingInspection
    nonexistentRSO.set(
        "id", OBJECT_MAPPER.convertValue("ejEQxt9QtbWpjqBIvMt4XaxDDdW", JsonNode.class));
    nonexistentRSO.set("favorite", OBJECT_MAPPER.convertValue(false, JsonNode.class));
    testServerPost("/favorite", nonexistentRSO, HttpURLConnection.HTTP_NOT_FOUND);

    // Bad URL POST
    testServerPost("/favorites/", nonexistentRSO, HttpURLConnection.HTTP_NOT_FOUND);

    // Bad body POST
    ObjectNode badBody = OBJECT_MAPPER.createObjectNode();
    badBody.set("id", OBJECT_MAPPER.convertValue("8OozGTNQpaETr0sAB4UVt_ee2qk", JsonNode.class));
    badBody.set("isFavorite", OBJECT_MAPPER.convertValue(false, JsonNode.class));
    testServerPost("/favorite", badBody, HttpURLConnection.HTTP_BAD_REQUEST);
  }

  // Test the client getFavorite and setFavorite methods
  @Test(timeout = 30000L)
  @Graded(points = 20, friendlyName = "Client getFavorite and setFavorite (Integration)")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test1_ClientGetAndSetFavorite() throws Exception {
    // API client for testing
    Client apiClient = getAPIClient();

    // Get a shuffled list of summaries
    Random random = new Random(12433);
    List<Summary> trimmedSummaries = getShuffledSummaries(12433, 128);
    // Map to store the favorite status set by Client.setFavorite
    Map<String, Boolean> favorites = new HashMap<>();

    // Go through all RSOs twice
    for (int repeat = 0; repeat < 2; repeat++) {
      for (Summary summary : trimmedSummaries) {
        // Randomly either getFavorite or setFavorite
        boolean currentFavorite;
        if (random.nextBoolean()) {
          currentFavorite =
              testClient((callback) -> apiClient.getFavorite(summary.getId(), callback));
        } else {
          boolean isFavorite = random.nextBoolean();
          favorites.put(summary.getId(), isFavorite);
          currentFavorite =
              testClient(
                  (callback) -> apiClient.setFavorite(summary.getId(), isFavorite, callback));
        }

        // Check against the expected value
        boolean expectedFavorite =
            Objects.requireNonNull(favorites.getOrDefault(summary.getId(), false));
        assertWithMessage("Incorrect favorite value")
            .that(currentFavorite)
            .isEqualTo(expectedFavorite);
      }
    }

    // Test bad Client.getFavorite call
    try {
      boolean ignored =
          testClient((callback) -> apiClient.getFavorite("Lr363RRTGsnNHO3EO37VkKOk65a", callback));
      assertWithMessage("Client GET /favorite for non-existent RSO should throw").fail();
    } catch (Exception ignored) {
    }
  }

  // Helper method for the favorite button UI test
  private void favoriteButtonHelper(
      Summary summary, boolean currentFavorite, boolean nextFavorite) {

    // Prepare the Intent to start the RSOActivity
    Intent intent = new Intent(ApplicationProvider.getApplicationContext(), RSOActivity.class);
    intent.putExtra("id", summary.getId());

    // Start the RSOActivity
    startActivity(
        intent,
        activity -> {
          pause();
          // Check that the initial favorite status is correct, change it,
          // and then verify the change
          onView(isAssignableFrom(ToggleButton.class))
              .check(isChecked(currentFavorite))
              .perform(setChecked(nextFavorite))
              .check(isChecked(nextFavorite));
        });
  }

  // Test the favorite button
  @Test(timeout = 30000L)
  @Graded(points = 15, friendlyName = "Favorite Button (Integration)")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test2_FavoriteButton() {
    // Get a shuffled small list of summaries
    Random random = new Random(12434);
    List<Summary> trimmedSummaries = getShuffledSummaries(12434, 2);
    // Map to store our expected favorite results
    Map<String, Boolean> favorites = new HashMap<>();

    // Go through each summary four times, either setting or clearing its favorite status
    for (int repeat = 0; repeat < 4; repeat++) {
      Collections.shuffle(trimmedSummaries, random);
      for (Summary summary : trimmedSummaries) {
        boolean currentFavorite =
            Objects.requireNonNull(favorites.getOrDefault(summary.getId(), false));
        boolean nextFavorite = random.nextBoolean();
        favorites.put(summary.getId(), nextFavorite);
        favoriteButtonHelper(summary, currentFavorite, nextFavorite);
      }
    }
  }

  // Helper method for the favorite sort UI test
  private void favoriteSortHelper(List<Summary> favoriteSummaries) {
    // Start the main activity
    startMainActivity(
        activity -> {
          // Check that the right number of RSOs are displayed initially
          onView(withId(R.id.recycler_view)).check(countRecyclerView(SUMMARY_COUNT));

          // Check that the expected RSOs are at the top of the list
          for (int position = 0; position < favoriteSummaries.size(); position++) {
            String title = favoriteSummaries.get(position).getTitle();
            onView(withRecyclerView(R.id.recycler_view).atPosition(position))
                .check(matches(hasDescendant(withText(title))));
          }
        });
  }

  // Test the favorite sort
  @Test(timeout = 30000L)
  @Graded(points = 15, friendlyName = "Favorite Sort (Integration)")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test3_FavoriteSort() {
    // Get a shuffled small list of summaries
    List<Summary> trimmedSummaries = getShuffledSummaries(12435, 4);
    // Track which summaries are current favorites
    List<Summary> favoriteSummaries = new ArrayList<>();

    // Go through each summary, marking it is a favorite, and then making sure the main activity
    // UI is updated properly
    for (Summary summary : trimmedSummaries) {
      favoriteButtonHelper(summary, false, true);
      favoriteSummaries.add(summary);
      favoriteSummaries.sort(Comparator.comparing(Summary::getTitle));
      favoriteSortHelper(favoriteSummaries);
    }
  }

  // Helper method for the related RSOs test
  private void relatedRSOsHelper(
      @NonNull String id, int expectedSize, @NonNull List<String> expectedIDs) throws IOException {
    RSO rso = testServerGet("/rso/" + id, RSO.class);
    assertWithMessage("Wrong number of related RSOs")
        .that(rso.getRelatedRSOs())
        .hasSize(expectedSize);
    for (int i = 0; i < expectedSize; i++) {
      assertWithMessage("Related results mismatch at index " + i)
          .that(rso.getRelatedRSOs().get(i).getId())
          .startsWith(expectedIDs.get(i));
    }
  }

  // Test that the RSO related RSOs field is set properly
  @SuppressWarnings("SpellCheckingInspection")
  @Test(timeout = 30000L)
  @Graded(points = 5, friendlyName = "Related RSOs (Unit)")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test4_testRelatedRSOs() throws IOException {
    // These test cases were generated from the reference solution
    relatedRSOsHelper(
        "32-4gqW-bwXw-kWCkEOUcRHPUs4",
        8,
        List.of("in6m", "rKUE", "iVc3", "5WQ1", "C75l", "_5cc", "hvLA", "1miu"));
    relatedRSOsHelper(
        "rSTwDZv3pzBZJKaPBYobW4NS9Eg",
        8,
        List.of("eQd5", "ciZG", "1miu", "5gES", "Z9L6", "H0DT", "HmT2", "B0D8"));
    relatedRSOsHelper(
        "HJAfkmSOYPES7L67HtpjUxxCyHw",
        8,
        List.of("wlsU", "Iwu0", "rVWl", "Ply2", "ZEVD", "yZ8P", "Lu9I", "Mtjc"));
    relatedRSOsHelper(
        "W1xasD7GZprE8HXhJHiIo9F5c6I",
        8,
        List.of("9oqb", "KPpO", "0Ehk", "061b", "SX3L", "hdeL", "90WJ", "g27m"));
    relatedRSOsHelper("5HyU4jaDURz5tJkfMk_CTKng6l4", 0, Collections.emptyList());
    relatedRSOsHelper(
        "urdk_DYN0grLi-aPBpqfZcf9c7M",
        8,
        List.of("nSms", "h836", "9oqb", "8nVP", "AhFn", "Bo1L", "FV6P", "Wz-m"));
    relatedRSOsHelper("Ycf2sRS-kbyupsVpszeay7w8DdQ", 0, Collections.emptyList());
    relatedRSOsHelper(
        "gxFlRlrhUjaYy73v-Qpp4N4on58",
        8,
        List.of("qWc7", "nSms", "SvzT", "_fSS", "h836", "oRxw", "9oqb", "l5U_"));
    relatedRSOsHelper(
        "L2QwNEcIdpkEQAlt1BMpbVgUS1M",
        8,
        List.of("h836", "7zCc", "Z0o5", "9oqb", "5WQ1", "lWVa", "oHiC", "ciZG"));
    relatedRSOsHelper("CRyYD8vzu_MomRwOy28FpJF31ms", 0, Collections.emptyList());
    relatedRSOsHelper(
        "AzBhqKVMK-57c02gXru8S4EtBks",
        8,
        List.of("eQd5", "XuEp", "EBpj", "1b-2", "zwzD", "NG7P", "yMXt", "GwoD"));
    relatedRSOsHelper(
        "-cY_Ba4lVngvoj1YtKDGD3HL8Sw",
        8,
        List.of("q-_r", "V4O3", "uUgY", "NAzx", "rVWl", "Ply2", "ZEVD", "6lVH"));
    relatedRSOsHelper("IzASOTLG6b9TlkFvO3kgJw5vxFs", 0, Collections.emptyList());
    relatedRSOsHelper("aJIEVLD9SAq8-CuG7LqTuT7NNrQ", 0, Collections.emptyList());
    relatedRSOsHelper(
        "ck_fqfjhHdRSNFzO3k2EgOYIXZg",
        8,
        List.of("QF0R", "s43p", "9oqb", "ciZG", "1miu", "Z9L6", "HmT2", "-kRi"));
    relatedRSOsHelper("QvrN0Z8ldkoOemOpqSA9azlQEGg", 0, Collections.emptyList());
    relatedRSOsHelper(
        "pDwUif48gNVEKIL_TsuMEtXYKyk",
        8,
        List.of("PPSY", "SvzT", "_5cc", "7U5p", "BUVr", "RRAP", "jMCD", "sHIR"));
    relatedRSOsHelper(
        "Z9L6-YhsCyIdPrJuDzxTmqjK7RA",
        8,
        List.of("ciZG", "HmT2", "8Ooz", "1miu", "YWhd", "H0DT", "QF0R", "Kxk3"));
    relatedRSOsHelper(
        "-kyhx31pYyqtW49P1bySc7pXnbI",
        8,
        List.of("Q8JH", "CZMZ", "FtGp", "VDxJ", "a--m", "1miu", "061b", "Rx-G"));
    relatedRSOsHelper(
        "skMt0nszL5bm_6TKnX8pkYtJeYc",
        8,
        List.of("eQd5", "qWc7", "efE4", "7zCc", "5WQ1", "7Jsx", "lWVa", "8nVP"));
    relatedRSOsHelper(
        "REAKQmiI3MgK1ZaIUgA6fWHVa30",
        8,
        List.of("nSms", "h836", "5sNA", "9oqb", "CZMZ", "osAD", "8nVP", "AhFn"));
    relatedRSOsHelper(
        "1xPnHD0cb-_Wxq2mL1dnPAmQ0UA",
        8,
        List.of("9oqb", "SX3L", "90WJ", "sRzp", "W1x8", "XCf6", "3mFN", "S5k_"));
    relatedRSOsHelper(
        "VDxJbfmKQgbxvw7IyABX3wud5Tg",
        8,
        List.of("a--m", "1miu", "MJPg", "L6Zl", "ATgT", "0Ibj", "b5f4", "eQd5"));
    relatedRSOsHelper(
        "2OR8JRZMn_mqN3Gfi7bPj0AA97k",
        8,
        List.of("eQd5", "Q8JH", "ciZG", "BUVr", "VGdH", "HmT2", "lF3h", "gx81"));
    relatedRSOsHelper(
        "E408COXofsxRMvYQnaGryf82Q5Y",
        8,
        List.of("eQd5", "qWc7", "efE4", "7zCc", "5WQ1", "7Jsx", "lWVa", "8nVP"));
    relatedRSOsHelper(
        "8DKedINfeADfkFLDdendS0U22dU",
        8,
        List.of("nSms", "h836", "5sNA", "9oqb", "CZMZ", "osAD", "8nVP", "AhFn"));
    relatedRSOsHelper("WkI_lm-om7kLYHDS5UKxFKEKwA8", 0, Collections.emptyList());
    relatedRSOsHelper(
        "zOyQ59-nfGOOW1db-BO0TI5E-AI",
        8,
        List.of("ciZG", "VGdH", "HmT2", "LfmV", "GqRf", "FP52", "HTKc", "GQef"));
    relatedRSOsHelper(
        "ECpMsXd2o44YQJeZNwBL9Sbm4FY",
        8,
        List.of("ciZG", "1miu", "Z9L6", "HmT2", "QF0R", "ck_f", "8Ooz", "s43p"));
    relatedRSOsHelper(
        "f87_Me-nb1CAps5zWoLhS1BDZuU",
        8,
        List.of("SvzT", "CnYg", "Q8JH", "iVc3", "Z0o5", "iUwL", "tqO2", "_5cc"));
    relatedRSOsHelper("gtm_LE1sziMGkf5t72DC8uTmy7o", 0, Collections.emptyList());
    relatedRSOsHelper("zeG1dFQgvg05IlZk5nutgok9syI", 0, Collections.emptyList());
    relatedRSOsHelper("ZPmEZWbvsVwOHm72rI3wzTI0CuI", 0, Collections.emptyList());
    relatedRSOsHelper(
        "t0Plky6vzRKCckq5y4hT_8g8j4U",
        8,
        List.of("s43p", "2FO0", "SSws", "HmT2", "yA2n", "7U5p", "k2ZX", "5gES"));
    relatedRSOsHelper(
        "Kxk3Dm7a2D1Jt7JFdrBM66qtJ14",
        8,
        List.of("s43p", "GqRf", "ciZG", "mx9h", "YWhd", "Z9L6", "HmT2", "Y0_A"));
    relatedRSOsHelper(
        "1b-2JmM57W-6XAyCUXk9QGK6NaQ",
        8,
        List.of("eQd5", "XuEp", "EBpj", "AzBh", "zwzD", "NG7P", "yMXt", "GwoD"));
    relatedRSOsHelper(
        "nSLaeZQdVjHT0A5YEljfNUNgo-Y",
        8,
        List.of("8nVP", "4XwG", "pW8m", "RhQ9", "uncP", "nZjK", "9rkE", "HUGz"));
    relatedRSOsHelper(
        "G7SR0Ksn8ExsI-j2lWEc9m_JsJQ",
        8,
        List.of("h836", "7zCc", "Z0o5", "9oqb", "5WQ1", "lWVa", "oHiC", "ciZG"));
    relatedRSOsHelper(
        "5Dkir_p8pApN0hcPbd_tqZ1xMOg",
        8,
        List.of("SvzT", "CnYg", "Q8JH", "iVc3", "Z0o5", "iUwL", "tqO2", "_5cc"));
    relatedRSOsHelper(
        "adqnvii602M5W9N4YrB9a1bPOY8",
        8,
        List.of("yHll", "_5cc", "Lr36", "dvEi", "YPZ6", "dqIx", "MJPg", "TXYP"));
    relatedRSOsHelper("_1e5f8fXw5pO7yrm_1guNimUibo", 0, Collections.emptyList());
    relatedRSOsHelper(
        "VPgk1IbTKAOzyTlHDA0nk1y9iv4",
        8,
        List.of("SvzT", "CnYg", "Q8JH", "iVc3", "Z0o5", "iUwL", "tqO2", "_5cc"));
    relatedRSOsHelper(
        "XSIJ66W_bKsWGf05bXDzL526MHg",
        8,
        List.of("nSms", "h836", "9oqb", "8nVP", "AhFn", "urdk", "Bo1L", "FV6P"));
    relatedRSOsHelper(
        "I47wiWWqg8Uj1qzMVmW4U0giH2w",
        8,
        List.of("SvzT", "CnYg", "Q8JH", "iVc3", "Z0o5", "iUwL", "tqO2", "_5cc"));
    relatedRSOsHelper(
        "Qc1i2riTbh5Lg2kLR-KitLDFpCw",
        8,
        List.of("OGh2", "KFmr", "aGaa", "NW1z", "rdsh", "puxn", "es_D", "6yjJ"));
    relatedRSOsHelper(
        "W1x8_mt1KWZMhHSQ5AisuBLcLY4",
        8,
        List.of("9oqb", "SX3L", "90WJ", "sRzp", "1xPn", "XCf6", "3mFN", "S5k_"));
    relatedRSOsHelper("jAfQcMqe4xCVY7lrLS3iEaPqEtw", 0, Collections.emptyList());
    relatedRSOsHelper("mBLB6Gwr5SMpsHL_QfP6qM_Af0s", 0, Collections.emptyList());
    relatedRSOsHelper(
        "fYmt2yCz0BOl4zd_7thuZSvih7U",
        8,
        List.of("xkz9", "GqRf", "06MU", "eQd5", "Q8JH", "ciZG", "ybnD", "BUVr"));
    relatedRSOsHelper("Vrq6nRMtzrNqiLQnOa6Tn0Qor70", 0, Collections.emptyList());
    relatedRSOsHelper("tn3lDLqhIDNEeVf41sWI0cYhSto", 0, Collections.emptyList());
    relatedRSOsHelper(
        "p6G0sCDUuTcFq161dv3wsgfplo0",
        8,
        List.of("9oqb", "HmT2", "QF0R", "Y0_A", "pDSa", "ck_f", "s43p", "j3pV"));
    relatedRSOsHelper(
        "W95zIllqcBMflWvbpnBWttLd3lc",
        8,
        List.of("CZMZ", "061b", "SkTI", "x4_t", "zaMP", "TTKn", "jE_h", "hC77"));
    relatedRSOsHelper(
        "MJPgAMW6aavHpoQ64xu5nVWUev8",
        8,
        List.of("9oqb", "1miu", "exMc", "VDxJ", "a--m", "7U5p", "mx9h", "j8ph"));
    relatedRSOsHelper(
        "yf3SG9VFM2AEOGIPtnsb0x8TXnY",
        8,
        List.of("nSms", "h836", "9oqb", "8nVP", "AhFn", "urdk", "Bo1L", "FV6P"));
    relatedRSOsHelper(
        "7DEOmnsyi_llJEq8EJHP4AOgT_Y",
        8,
        List.of("q-_r", "V4O3", "uUgY", "NAzx", "rVWl", "Ply2", "ZEVD", "6lVH"));
    relatedRSOsHelper(
        "yxXiFPhKEQeBgBTKBfHrFuuMzDs",
        8,
        List.of("eQd5", "9oqb", "TllZ", "VDxJ", "ciZG", "a--m", "7U5p", "k2ZX"));
    relatedRSOsHelper("ePpWEBsCP9vVSWvVFkdFm3dHRfw", 0, Collections.emptyList());
    relatedRSOsHelper(
        "vkF1bjzByeqZLKZPcpLN4pzS5T0",
        8,
        List.of("in6m", "rKUE", "iVc3", "5WQ1", "C75l", "_5cc", "hvLA", "1miu"));
    relatedRSOsHelper(
        "j3pVDF45lKroVA_0VbOvNyayUTA",
        8,
        List.of("s43p", "9oqb", "exMc", "nfX6", "1miu", "j8ph", "QF0R", "Kxk3"));
    relatedRSOsHelper(
        "gbCVGkF_9Z97XRXPaMrp2YFU8bk",
        8,
        List.of("nULV", "D25G", "OGh2", "KFmr", "Qc1i", "w5ld", "aGaa", "HH-e"));
    relatedRSOsHelper(
        "Q45huzHmfE-a4KqGH2cBqMdYFmU",
        8,
        List.of("h836", "9oqb", "FV6P", "KmoR", "WGcd", "TTKn", "tM2r", "hC77"));
    relatedRSOsHelper(
        "53W1z8tTA-WltES-MzkZRtHgA5U",
        8,
        List.of("s43p", "exMc", "HmT2", "RRAP", "GHDG", "9AmN", "1e5c", "PPSY"));
    relatedRSOsHelper(
        "BW7E7Je-emoPd_NLcD_ZeuKsN3E",
        8,
        List.of("SvzT", "CnYg", "Q8JH", "iVc3", "Z0o5", "iUwL", "tqO2", "_5cc"));
  }

  // Test related RSOs UI
  @Test(timeout = 30000L)
  @Graded(points = 5, friendlyName = "Related RSOs UI (Integration)")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test5_RelatedRSOsUI() throws Exception {
    // Related RSO test must work first
    test4_testRelatedRSOs();

    // API client for testing
    Client apiClient = getAPIClient();

    // Get a shuffled list of RSO summaries
    List<Summary> trimmedSummaries = getShuffledSummaries(12437, 32);

    // Use the random RSOs we picked above
    // Can't use an enhanced for loop here since we maintain two counters
    int count = 0;
    for (int i = 0; i < 32 && count < 4; i++) {
      Summary summary = trimmedSummaries.get(i);

      // Skip RSOs without any relations
      RSO rso = testClient((callback) -> apiClient.getRSO(summary.getId(), callback));
      if (rso.getRelatedRSOs().isEmpty()) {
        continue;
      }
      count++;

      // Create the Intent
      Intent intent = new Intent(ApplicationProvider.getApplicationContext(), RSOActivity.class);
      intent.putExtra("id", summary.getId());

      // Start the RSO activity
      startActivity(
          intent,
          activity -> {
            // Wait for it to load
            pause();
            // Check that the related RSOs are displayed
            for (Summary related : rso.getRelatedRSOs()) {
              onView(withText(containsString(related.getTitle()))).check(matches(isDisplayed()));
            }
          });
    }
  }

  // Security manager used to count access to rsos.json
  private static final JSONReadCountSecurityManager JSON_READ_COUNT_SECURITY_MANAGER =
      new JSONReadCountSecurityManager();

  // Run once before any test in this suite is started
  @BeforeClass
  public static void beforeClass() {
    // Check Server.java for publicly visible members
    checkServerDesign();
    // Set up logging so that you can see log output during testing
    configureLogging();
    // Force loads to perform initialization before we start counting
    RSODATA.size();
    SUMMARIES.size();
    // Install our security manager that allows counting access to rsos.json
    System.setSecurityManager(JSON_READ_COUNT_SECURITY_MANAGER);
    // Start the API server
    Server.start();
  }

  // Run once after all tests in this suite are completed
  @AfterClass
  public static void afterClass() {
    // Remove the custom security manager
    System.setSecurityManager(null);
  }

  // Run before each test in the suite starts
  @Before
  public void beforeTest() {
    // Randomly set several RSOs to favorites before server reset to defend against client-side
    // caching
    Client apiClient = getAPIClient();

    List<Summary> summarySubset = getShuffledSummaries(new Random().nextInt(), 16);
    for (Summary summary : summarySubset) {
      try {
        boolean ignored =
            testClient((callback) -> apiClient.setFavorite(summary.getId(), true, callback));
      } catch (Exception ignored) {
      }
    }

    // Reset the server between tests
    try {
      Server.reset();
    } catch (Exception ignored) {
    }
    // Check for extra reads from rsos.json
    JSON_READ_COUNT_SECURITY_MANAGER.checkCount();
  }

  // Run after each test completes
  @After
  public void afterTest() {
    // Check for extra reads from rsos.json
    JSON_READ_COUNT_SECURITY_MANAGER.checkCount();
  }
}

// md5: 66257fa8aa14d45ac548e66a7b92c276 // DO NOT REMOVE THIS LINE
