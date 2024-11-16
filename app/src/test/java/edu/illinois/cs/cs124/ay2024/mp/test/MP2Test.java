package edu.illinois.cs.cs124.ay2024.mp.test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.truth.Truth.assertWithMessage;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Data.RSODATA;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Data.SUMMARIES;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Data.SUMMARY_COUNT;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Data.getShuffledSummaries;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.HTTP.getAPIClient;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.HTTP.testClient;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.HTTP.testServerGet;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.HTTP.testServerGetTimed;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.RecyclerViewMatcher.withRecyclerView;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.TestHelpers.GET_METHOD_EXTRA_TIME;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.TestHelpers.checkServerDesign;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.TestHelpers.configureLogging;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.TestHelpers.pause;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.TestHelpers.startActivity;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.TestHelpers.startMainActivity;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.TestHelpers.testSummaryRoute;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.TestHelpers.trimmedMean;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Views.countRecyclerView;
import static org.hamcrest.Matchers.containsString;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.fasterxml.jackson.databind.JsonNode;
import edu.illinois.cs.cs124.ay2024.mp.R;
import edu.illinois.cs.cs124.ay2024.mp.activities.RSOActivity;
import edu.illinois.cs.cs124.ay2024.mp.models.RSO;
import edu.illinois.cs.cs124.ay2024.mp.models.RSOData;
import edu.illinois.cs.cs124.ay2024.mp.models.Summary;
import edu.illinois.cs.cs124.ay2024.mp.network.Client;
import edu.illinois.cs.cs124.ay2024.mp.network.Server;
import edu.illinois.cs.cs124.ay2024.mp.test.helpers.HTTP;
import edu.illinois.cs.cs124.ay2024.mp.test.helpers.JSONReadCountSecurityManager;
import edu.illinois.cs.cs125.gradlegrader.annotations.Graded;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
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
 * This is the MP2 test suite.
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
 * The MP2 test suite includes no ungraded tests.
 * These tests are fairly idiomatic, in that they resemble tests you might write for an actual
 * Android programming project.
 */

@RunWith(AndroidJUnit4.class)
@LooperMode(LooperMode.Mode.PAUSED)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class MP2Test {
  // Test the RSO class design
  @Test(timeout = 4000L)
  @Graded(points = 10, friendlyName = "RSO Class Design (Unit)")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test0_RSOClassDesign() {
    // Allows us to count RSO categories, non-empty mission statements, and external websites,
    // ensuring that the RSO class is designed properly and parsing the input data correctly
    RSOCounts counts = new RSOCounts();

    // Initialize all RSO classes using RSOData objects
    for (RSOData rsoData : RSODATA) {
      RSO rso = new RSO(rsoData);
      // Add the new RSO to our counts
      counts.countRSO(rso);
    }

    // Check that the counts match what we expect
    counts.checkCounts(2059, 1152, 53);
  }

  // Test GET /rso server route
  @Test(timeout = 60000L)
  @Graded(points = 20, friendlyName = "Server GET /rso (Unit)")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test1_ServerRSORoute() throws IOException {
    // Test the /summary route first as a sanity check
    testSummaryRoute();

    // Save response times for comparison
    List<Long> baseResponseTimes = new ArrayList<>();
    List<Long> getResponseTimes = new ArrayList<>();

    // Get a random subset of all RSO summaries
    List<Summary> trimmedSummaries = getShuffledSummaries(12421, 128);

    // Allows us to count RSO categories, non-empty mission statements, and external websites,
    // ensuring that the RSO results returned by the server are correct
    RSOCounts counts = new RSOCounts();

    // Round 0: Test good GET /rso requests for all RSOs
    // Untimed to warm up the server and not fail after MP3
    for (Summary rsoSummary : trimmedSummaries) {
      // Perform the /rso GET
      RSO rso = testServerGet("/rso/" + rsoSummary.getId(), RSO.class);

      // RSO returned should not be null
      assertWithMessage("RSO returned from server GET /rso should not be null")
          .that(rso)
          .isNotNull();

      // Count the returned RSO object
      counts.countRSO(rso);
    }

    // Check that the counts match what we expect
    counts.checkCounts(222, 120, 3);

    // Clear the counts for the timed round
    counts.clear();

    // Round 1: Test good GET /rso requests for all RSOs
    // Timed to detect slow routes
    for (Summary rsoSummary : trimmedSummaries) {
      // Time the index route for comparison
      HTTP.TimedResponse<JsonNode> baseResult = testServerGetTimed("/");
      baseResponseTimes.add(baseResult.getResponseTime().toNanos());

      // Perform the main /rso GET
      HTTP.TimedResponse<RSO> rsoResult =
          testServerGetTimed("/rso/" + rsoSummary.getId(), RSO.class);
      getResponseTimes.add(rsoResult.getResponseTime().toNanos());

      // RSO returned should not be null
      assertWithMessage("RSO returned from server GET /rso should not be null")
          .that(rsoResult.getResponse())
          .isNotNull();

      // Count the returned RSO object
      counts.countRSO(rsoResult.getResponse());
    }

    // Check that the counts match what we expect
    counts.checkCounts(222, 120, 3);

    // Check for slow server GETs potentially caused by unnecessary parsing or looping
    double averageBase = trimmedMean(baseResponseTimes, 0.1);
    double averageResponse = trimmedMean(getResponseTimes, 0.1);
    assertWithMessage("Server GET /rso is too slow")
        .that(averageResponse)
        .isLessThan(averageBase * GET_METHOD_EXTRA_TIME);

    // Test bad requests
    testServerGet("/rsos/KUv_oPtSyVms6QUnd2VzZu_3CVI/", HttpURLConnection.HTTP_NOT_FOUND);
    // Non-existent RSO
    testServerGet("/rso/KUv_oPtSyVms6QUnd2VZZu_3CVI", HttpURLConnection.HTTP_NOT_FOUND);
    // Non-existent URL
    testServerGet("/rsos/KUv_oPtSyVms6QUnd2VzZu_3CVI/", HttpURLConnection.HTTP_NOT_FOUND);
  }

  // Test the Client getRSO method
  @Test(timeout = 40000L)
  @Graded(points = 20, friendlyName = "Client getRSO (Integration)")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test2_ClientGetRSO() throws Exception {
    // Create a Client for testing
    Client apiClient = getAPIClient();

    // Get a random subset of all RSO summaries
    List<Summary> trimmedSummaries = getShuffledSummaries(12422, 128);

    // Allows us to count RSO categories, non-empty mission statements, and external websites,
    // ensuring that the RSO results returned by the client are correct
    RSOCounts counts = new RSOCounts();

    // Use the Client to make RSO requests for all RSO summaries in our subset
    for (Summary summary : trimmedSummaries) {
      RSO rso = testClient((callback) -> apiClient.getRSO(summary.getId(), callback));
      // Check that the returned RSO id is correct
      assertWithMessage("Incorrect id on returned RSO")
          .that(rso.getId())
          .isEqualTo(summary.getId());
      // Count the returned RSO object
      counts.countRSO(rso);
    }

    // Check that the counts match what we expect
    counts.checkCounts(217, 124, 3);

    // Test bad getRSO request
    try {
      RSO ignored =
          testClient((callback) -> apiClient.getRSO("_6oRAM9nit1fntTdDkIOk_Q2nVS", callback));
      assertWithMessage("Client GET /rso/ for non-existent RSO should throw").fail();
    } catch (Exception ignored) {
    }
  }

  // Test onClick intent generation in the main activity
  @SuppressWarnings("SpellCheckingInspection")
  @Test(timeout = 20000L)
  @Graded(points = 10, friendlyName = "Summary Click Launch (Integration)")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test3_SummaryClickLaunch() {
    startMainActivity(
        activity -> {
          // Double-check that the number of RSO summaries displayed is correct
          onView(withId(R.id.recycler_view)).check(countRecyclerView(SUMMARY_COUNT));
          // Double-check that the RSO summaries are sorted as we expect
          onView(withRecyclerView(R.id.recycler_view).atPosition(2))
              .check(matches(hasDescendant(withText("8 to CREATE"))));

          // Perform the click
          onView(withRecyclerView(R.id.recycler_view).atPosition(2)).perform(click());

          // Make sure the intent generated is correct
          String id = shadowOf(activity).getNextStartedActivity().getStringExtra("id");
          assertWithMessage("Incorrect id in intent")
              .that(id)
              .isEqualTo("eIm2apBazz95gGylU3a8Mbd0vFY");
        });
  }

  // Test RSOActivity UI launched via intent
  @Test(timeout = 20000L)
  @Graded(points = 20, friendlyName = "RSO View (Integration)")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test4_RSOView() throws Exception {
    // Create a Client for testing, used to retrieve full RSO details
    Client apiClient = getAPIClient();

    // Get a random subset of all RSO summaries
    List<Summary> trimmedSummaries = getShuffledSummaries(12424, 32);

    // Use the random RSOs we picked above
    // Can't use an enhanced for loop here since we maintain two counters
    int count = 0;
    for (int i = 0; i < 32 && count < 4; i++) {
      Summary summary = trimmedSummaries.get(i);

      // Create the Intent
      Intent intent = new Intent(ApplicationProvider.getApplicationContext(), RSOActivity.class);
      intent.putExtra("id", summary.getId());

      // Use the Client to retrieve the RSO details
      RSO rso = testClient((callback) -> apiClient.getRSO(summary.getId(), callback));
      // Skip RSOs without a website, mission statement, or categories
      if (rso.getWebsite().isBlank()
          || rso.getMission().isBlank()
          || rso.getCategories().isEmpty()) {
        continue;
      }
      count++;

      // Start the RSO activity
      startActivity(
          intent,
          activity -> {
            // Wait for it to load...
            pause();

            // Check for the title, website, mission statement, and categories
            // Title, website, and mission should appear in their own view
            onView(withText(rso.getTitle())).check(matches(isDisplayed()));
            onView(withText(rso.getWebsite())).check(matches(isDisplayed()));
            onView(withText(rso.getMission())).check(matches(isDisplayed()));

            // Category strings may eventually appear multiple times, and formatting may vary.
            // So we'll just check that all the categories for this RSO are shown somewhere
            for (String categoryString : rso.getCategories()) {
              onView(first(withText(containsString(categoryString)))).check(matches(isDisplayed()));
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
    // Check for extra reads from rsos.json
    JSON_READ_COUNT_SECURITY_MANAGER.checkCount();
  }

  // Run after each test completes
  @After
  public void afterTest() {
    // Check for extra reads from rsos.json
    JSON_READ_COUNT_SECURITY_MANAGER.checkCount();
  }

  /**
   * Used to count RSO attributes for testing verification.
   *
   * @noinspection NewClassNamingConvention
   */
  static class RSOCounts {
    public int totalCategories = 0;
    public int totalNonEmptyMissionStatements = 0;
    public int totalNonOneIllinoisWebsites = 0;

    public void countRSO(@NonNull RSO rso) {
      totalCategories += rso.getCategories().size();
      if (!rso.getMission().isEmpty()) {
        totalNonEmptyMissionStatements += 1;
      }
      // Count RSO if it has an external website and not a default one provided by one.illinois.edu
      if (!rso.getWebsite().startsWith("https://one.illinois.edu")) {
        totalNonOneIllinoisWebsites += 1;
      }
    }

    public void checkCounts(
        int expectedTotalCategories,
        int expectedTotalNonEmptyMissionStatements,
        int expectedTotalNonOneIllinoisWebsites) {
      assertWithMessage("Incorrect total number of RSO categories")
          .that(totalCategories)
          .isEqualTo(expectedTotalCategories);
      assertWithMessage("Incorrect total number of non-empty mission statements")
          .that(totalNonEmptyMissionStatements)
          .isEqualTo(expectedTotalNonEmptyMissionStatements);
      assertWithMessage("Incorrect total number non-one.illinois.edu websites")
          .that(totalNonOneIllinoisWebsites)
          .isEqualTo(expectedTotalNonOneIllinoisWebsites);
    }

    public void clear() {
      totalCategories = 0;
      totalNonEmptyMissionStatements = 0;
      totalNonOneIllinoisWebsites = 0;
    }
  }

  // View matcher to select first matching view when multiple match our criteria
  private <T> Matcher<T> first(final Matcher<T> matcher) {
    return new BaseMatcher<>() {
      boolean isFirst = true;

      @Override
      public boolean matches(final Object item) {
        if (isFirst && matcher.matches(item)) {
          isFirst = false;
          return true;
        }
        return false;
      }

      @Override
      public void describeTo(final Description description) {
        description.appendText("should return first matching item");
      }
    };
  }
}

// md5: 898b9a5c35129adea274449c1c732bbf // DO NOT REMOVE THIS LINE
