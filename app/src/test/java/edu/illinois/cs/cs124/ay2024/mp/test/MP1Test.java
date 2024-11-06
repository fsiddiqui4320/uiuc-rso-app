package edu.illinois.cs.cs124.ay2024.mp.test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.collect.Sets.powerSet;
import static com.google.common.truth.Truth.assertWithMessage;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Data.BLUE_COUNT;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Data.DEPARTMENT_COUNT;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Data.ORANGE_COUNT;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Data.SUMMARY_COUNT;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Data.getShuffledSummaries;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.RecyclerViewMatcher.withRecyclerView;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.TestHelpers.pause;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.TestHelpers.startMainActivity;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Views.countRecyclerView;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Views.searchFor;
import static edu.illinois.cs.cs124.ay2024.mp.test.helpers.Views.setChecked;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import edu.illinois.cs.cs124.ay2024.mp.R;
import edu.illinois.cs.cs124.ay2024.mp.models.Summary;
import edu.illinois.cs.cs125.gradlegrader.annotations.Graded;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.experimental.LazyApplication;

/*
 * This is the MP1 test suite.
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
 * The MP1 test suite includes no ungraded tests.
 * Note that test4_testSummarySearch was generated from the MP reference
 * solution, and as such does not represent what a real-world test suite would typically look like.
 * (It would have fewer examples chosen more carefully.)
 */

@RunWith(AndroidJUnit4.class)
@LooperMode(LooperMode.Mode.PAUSED)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class MP1Test {
  // Check that the Summary sort method is implemented properly
  @Test(timeout = 4000)
  @Graded(points = 10, friendlyName = "Test Summary Sort (Unit)")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test0_testSummarySort() {
    // Use fixed seed for reproducible random results
    Random random = new Random(12410);

    for (int repeat = 0; repeat < 128; repeat++) {
      // Get a shuffled subset of the full list of RSO summaries
      List<Summary> trimmedList = getShuffledSummaries(12410, random.nextInt(SUMMARY_COUNT));

      // Skip empty lists
      if (trimmedList.isEmpty()) {
        continue;
      }

      // Sort using the default compareTo method
      Collections.sort(trimmedList);
      trimmedList = Collections.unmodifiableList(trimmedList);

      // Check that the list is sorted by title
      for (int i = 0; i < trimmedList.size() - 1; i++) {
        assertWithMessage("Summary list is not in correct order")
            .that(trimmedList.get(i + 1).getTitle().compareTo(trimmedList.get(i).getTitle()))
            .isAtLeast(0);
      }
    }
  }

  // Check that the main activity sorts the list of RSO summaries properly in the UI
  @Test(timeout = 20000L)
  @Graded(points = 10, friendlyName = "Test Main Activity Summary Sort (Integration)")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test1_testMainActivitySummarySort() {
    // Start the main activity
    startMainActivity(
        activity -> {
          // Double-check that the number of RSO summaries displayed is correct
          onView(withId(R.id.recycler_view)).check(countRecyclerView(SUMMARY_COUNT));

          // Check that the first few RSO summaries are in the expected order
          onView(withRecyclerView(R.id.recycler_view).atPosition(0))
              .check(matches(hasDescendant(withSubstring("4 Paws"))));
          onView(withRecyclerView(R.id.recycler_view).atPosition(1))
              .check(matches(hasDescendant(withSubstring("4-H House"))));
          onView(withRecyclerView(R.id.recycler_view).atPosition(2))
              .check(matches(hasDescendant(withSubstring("8 to CREATE"))));
          onView(withRecyclerView(R.id.recycler_view).atPosition(3))
              .check(matches(hasDescendant(withSubstring("A Space"))));
        });
  }

  // Check that the Summary color filter method works properly
  @Test(timeout = 4000)
  @Graded(points = 10, friendlyName = "Test Summary Color Filter (Unit)")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test2_testSummaryColorFilter() {
    // Expected color counts based on solution set
    Map<Summary.Color, Integer> correctCounts =
        Map.of(
            Summary.Color.BLUE, 190,
            Summary.Color.ORANGE, 1039,
            Summary.Color.DEPARTMENT, 3);

    // Check all subsets of the full set of colors
    for (Set<Summary.Color> colorSet : powerSet(EnumSet.allOf(Summary.Color.class))) {
      // Get a shuffled list of RSO summaries
      // The seed ensures we get the same subset each time
      List<Summary> shuffledList = getShuffledSummaries(12411);

      // Filter the list by color
      List<Summary> filteredList = Summary.filterColor(shuffledList, colorSet);

      // Compute the expected number of summaries based on the color subset
      int colorCount = colorSet.stream().mapToInt(c -> correctCounts.getOrDefault(c, 0)).sum();

      // Check that the filtered list is the correct size
      assertWithMessage("Filtered list has wrong size").that(filteredList).hasSize(colorCount);
      // Check that the filtered list has no incorrectly-colored RSOs
      assertWithMessage("Filtered list includes RSOs with the wrong color")
          .that(
              filteredList.stream()
                  .filter(s -> !colorSet.contains(s.getColor()))
                  .collect(Collectors.toList()))
          .isEmpty();
    }
  }

  // Check that the main activity color filter buttons work correctly
  @Test(timeout = 20000L)
  @Graded(points = 20, friendlyName = "Test Main Activity Color Buttons (Integration)")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test3_testMainActivityColorButtons() {
    // Start the main activity
    startMainActivity(
        activity -> {
          // Double-check that the correct number of RSO summaries is shown
          onView(withId(R.id.recycler_view)).check(countRecyclerView(SUMMARY_COUNT));

          // Both color buttons should initially be checked
          onView(withId(R.id.blueButton)).check(matches(isChecked()));
          onView(withId(R.id.orangeButton)).check(matches(isChecked()));

          // Uncheck the blue color button and check the count
          onView(withId(R.id.blueButton)).perform(setChecked(false));
          onView(withId(R.id.blueButton)).check(matches(isNotChecked()));
          onView(withId(R.id.recycler_view))
              .check(countRecyclerView(ORANGE_COUNT + DEPARTMENT_COUNT));

          // Uncheck the orange color button and check the count
          onView(withId(R.id.orangeButton)).perform(setChecked(false));
          onView(withId(R.id.orangeButton)).check(matches(isNotChecked()));
          onView(withId(R.id.recycler_view)).check(countRecyclerView(DEPARTMENT_COUNT));

          // Recheck the blue color button and check the count
          onView(withId(R.id.blueButton)).perform(setChecked(true));
          onView(withId(R.id.blueButton)).check(matches(isChecked()));
          onView(withId(R.id.recycler_view))
              .check(countRecyclerView(BLUE_COUNT + DEPARTMENT_COUNT));

          // Recheck the orange color button and check the count
          onView(withId(R.id.orangeButton)).perform(setChecked(true));
          onView(withId(R.id.orangeButton)).check(matches(isChecked()));
          onView(withId(R.id.recycler_view)).check(countRecyclerView(SUMMARY_COUNT));
        });
  }

  // Helper method for the Summary search test
  private void searchHelper(
      int seed, @NonNull String search, int expectedSize, @NonNull List<String> expectedIDs) {
    // Get a shuffled list of RSO summaries
    List<Summary> shuffledList = getShuffledSummaries(seed);
    // Run the search using the passed search String
    List<Summary> results = Summary.search(shuffledList, search);

    // The search method should never return the passed list
    assertWithMessage("Search should always return a new list")
        .that(results)
        .isNotSameInstanceAs(shuffledList);
    // Check the size of the search results
    assertWithMessage("Search results has incorrect size").that(results).hasSize(expectedSize);

    // Check the first few search results to make sure that they match
    for (int i = 0; i < expectedIDs.size(); i++) {
      assertWithMessage("Search results mismatch at index " + i)
          .that(results.get(i).getId())
          .startsWith(expectedIDs.get(i));
    }
  }

  // Check that the RSO Summary search method works correctly
  @Test(timeout = 4000)
  @Graded(points = 30, friendlyName = "Test Summary Search (Unit)")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  @SuppressWarnings("SpellCheckingInspection")
  public void test4_testSummarySearch() {
    // These test cases were generated from the reference solution
    searchHelper(
        -1952891732,
        "  ",
        1232,
        List.of("nwxj", "AoMt", "58Lh", "KnL-", "aIk4", "5dwy", "sreY", "kYJB", "MBlU", "cxn3"));
    searchHelper(
        -1952891732,
        "",
        1232,
        List.of("nwxj", "AoMt", "58Lh", "KnL-", "aIk4", "5dwy", "sreY", "kYJB", "MBlU", "cxn3"));
    searchHelper(-165939427, "iMAvINEd   ", 0, Collections.emptyList());
    searchHelper(1212769257, "WntkRnATfoNAn", 0, Collections.emptyList());
    searchHelper(1409063819, "   rIp", 1, List.of("U01V"));
    searchHelper(
        1556972626,
        "scIeNce",
        12,
        List.of("iVc3", "ybnD", "DPXl", "IsFR", "X4BM", "T-Bs", "IiCK", "fYmt", "x2J7", "x8XR"));
    searchHelper(-1806422148, "mOrROW", 1, List.of("nCVa"));
    searchHelper(93440138, "eTA  ", 4, List.of("q-0O", "ZQ_2", "pai4", "k_Mv"));
    searchHelper(1805928485, "UdAAn", 1, List.of("h29P"));
    searchHelper(124013038, "NuMENor", 1, List.of("xl-m"));
    searchHelper(211389951, "laTIn", 2, List.of("TW1a", "zc0y"));
    searchHelper(
        -1322477169,
        "LamBDA",
        10,
        List.of("evMR", "AhFn", "dVrc", "4XwG", "maYt", "TW1a", "zc0y", "RhQ9", "uncP", "nZjK"));
    searchHelper(-1603460054, "lOVERs", 2, List.of("JxIg", "9xJk"));
    searchHelper(537328753, "wRItTEN", 1, List.of("_9lU"));
    searchHelper(-798529819, "AErosol", 1, List.of("RQZp"));
    searchHelper(-868238434, "  uRBANa-", 1, List.of("WbIy"));
    searchHelper(-295039681, "PRe-pOdIAtrY", 1, List.of("o4A1"));
    searchHelper(-1273962125, "FINANIe", 0, Collections.emptyList());
    searchHelper(1740634935, "CearCH  ", 0, Collections.emptyList());
    searchHelper(-185372050, "SWim", 1, List.of("uBmm"));
    searchHelper(1603393153, "eQuamitn", 0, Collections.emptyList());
    searchHelper(244566673, "EGyPXiaN", 0, Collections.emptyList());
    searchHelper(485068595, "CfUNTRY", 0, Collections.emptyList());
    searchHelper(-1993660932, "almA's", 1, List.of("PEeh"));
    searchHelper(
        -283314535,
        "insTItUtE",
        11,
        List.of("QXwc", "IuW8", "1ZbV", "0alN", "ThTR", "Q3GV", "HHzX", "gJ9c", "mhFI", "h2yc"));
    searchHelper(-1775941359, "HEalTHCAre", 4, List.of("5dwy", "u-wW", "9bMm", "UggY"));
    searchHelper(-747804207, "ToWN  ", 1, List.of("4cHS"));
    searchHelper(-158398057, "MIDNiGHt", 1, List.of("CQnI"));
    searchHelper(568410736, "troUpE", 1, List.of("pNAa"));
    searchHelper(1238341293, "sysTAsS", 0, Collections.emptyList());
    searchHelper(-863596268, "SeMICONDUctoL", 0, Collections.emptyList());
    searchHelper(-913904469, "[Iec]", 1, List.of("N3Tf"));
    searchHelper(365523988, "FATaAKr", 0, Collections.emptyList());
    searchHelper(-480234851, "sgI", 1, List.of("jAfQ"));
    searchHelper(608434473, "ShunGROO", 0, Collections.emptyList());
    searchHelper(-201688686, "BEHiND", 1, List.of("GTB8"));
    searchHelper(-1191565188, "lcMA-U  ", 0, Collections.emptyList());
    searchHelper(-1744439999, "@uiuC", 2, List.of("_oGy", "_7sL"));
    searchHelper(911921354, "PaCIfIC", 2, List.of("oFRs", "AjXn"));
    searchHelper(-1722394727, "FIgHTing", 3, List.of("t71A", "qpAJ", "xl-m"));
    searchHelper(1267175130, "gLoVE   ", 1, List.of("qzam"));
    searchHelper(1376415876, "cOmBAT", 1, List.of("ZPmE"));
    searchHelper(1789837026, "projECtS", 1, List.of("WZhX"));
    searchHelper(1999048221, "ChEMPAigN-bmbana", 0, Collections.emptyList());
    searchHelper(1954586708, "sIGN", 1, List.of("QeMO"));
    searchHelper(1849549354, "pre-mED", 1, List.of("dJSU"));
    searchHelper(-1726387383, "KOJObs", 1, List.of("K2rg"));
    searchHelper(1050081819, "tAiWAN", 1, List.of("_oIs"));
    searchHelper(1737481000, "pOLo", 2, List.of("JSKR", "WKuq"));
    searchHelper(-1618988683, "  Ilhio", 0, Collections.emptyList());
    searchHelper(918258485, "BeaUTY", 2, List.of("EmMR", "S8re"));
    searchHelper(-370268409, "aMBasSAdaRs   ", 0, Collections.emptyList());
    searchHelper(-1894605766, "ciRcAssiAN", 1, List.of("REAK"));
    searchHelper(-1561954116, "egypTIan", 1, List.of("QfAH"));
    searchHelper(380281920, "   TeChnOlogy", 3, List.of("5dwy", "ch8k", "ypRV"));
    searchHelper(-2048325493, "sCHOLAR  ", 1, List.of("al-A"));
    searchHelper(1161982334, "quantitAtIve", 1, List.of("BVk8"));
    searchHelper(-1144552010, "INCORPoRAted", 1, List.of("l5U_"));
    searchHelper(639035952, "(GeB)", 1, List.of("_mYO"));
    searchHelper(-1172146015, "sqUAsH", 1, List.of("VLtc"));
    searchHelper(-1236339528, "thmiVE", 0, Collections.emptyList());
    searchHelper(-1393408123, "fOCKetRY", 0, Collections.emptyList());
    searchHelper(-583500765, "aMONG", 1, List.of("pHSW"));
    searchHelper(-2022377362, "   FIgHjond   ", 0, Collections.emptyList());
    searchHelper(1932656840, "teQmS", 0, Collections.emptyList());
    searchHelper(-1859533723, "uNiOn", 3, List.of("KmoR", "LWwr", "2jAz"));
    searchHelper(
        -841649740,
        "nETWOrk",
        11,
        List.of("ecm7", "7DrU", "JtkG", "ZEVD", "ch8k", "ZDZ7", "YrfN", "J65C", "FIYj", "zrqM"));
    searchHelper(-1349900050, "CubErs", 1, List.of("FfHI"));
    searchHelper(2108263940, "LucHA", 1, List.of("DgHQ"));
    searchHelper(-483697629, "eMeRgeNcY", 1, List.of("H0DT"));
    searchHelper(70782308, "sYriA  ", 1, List.of("bqHr"));
    searchHelper(-1966912016, "HoOf", 1, List.of("DLr1"));
    searchHelper(836630567, "iNDIGEnOus", 1, List.of("upBh"));
    searchHelper(825719354, "CoUnVELIng", 0, Collections.emptyList());
    searchHelper(-1380096018, "eqUitY", 4, List.of("dC2Q", "m5yo", "RwYd", "6you"));
    searchHelper(2094366648, "foOD", 4, List.of("2FO0", "J7tl", "5HyU", "T-Bs"));
    searchHelper(37107848, "MedlIFE", 1, List.of("7CtD"));
    searchHelper(-1677747557, "ZIndAA", 1, List.of("nSe5"));
    searchHelper(748036376, "LiNgUiSTS", 1, List.of("qlKq"));
    searchHelper(-1829425849, "AGrOnOmy", 1, List.of("Kvtg"));
    searchHelper(515030204, "ClLioArY", 0, Collections.emptyList());
    searchHelper(-2941008, "ddRIllINi", 1, List.of("Dj5V"));
    searchHelper(-955664152, "CuRLz ", 1, List.of("QJl3"));
    searchHelper(1045431828, "FmmisrAtiOn", 0, Collections.emptyList());
    searchHelper(-1410770895, "dVgEsf", 0, Collections.emptyList());
    searchHelper(-1490795758, "CHICAnOs", 1, List.of("x8XR"));
    searchHelper(-66604091, "bUZZeP", 0, Collections.emptyList());
    searchHelper(-1787358096, " RoTaRAcT", 1, List.of("sFis"));
    searchHelper(-1817065313, "uOciotYP", 0, Collections.emptyList());
    searchHelper(2116722023, " InLIfe", 0, Collections.emptyList());
    searchHelper(269628941, "sCOriNg", 1, List.of("rSQk"));
    searchHelper(2043646527, "POLIzh", 0, Collections.emptyList());
    searchHelper(
        -1132437482,
        "KoreaN",
        9,
        List.of("EtJT", "xSOX", "3mFN", "ouLt", "Qaw-", "AoMt", "7DEO", "S5k_", "aS6i"));
    searchHelper(45435035, "acHIEvERs", 1, List.of("kV0y"));
    searchHelper(-1009851451, "TrionGle", 0, Collections.emptyList());
    searchHelper(381263270, "MAnia", 1, List.of("L-5M"));
    searchHelper(-3752855, "SlILe,", 0, Collections.emptyList());
    searchHelper(-1532280463, "gEoCluB", 1, List.of("V7d0"));
    searchHelper(-1829234711, "bRaNch", 1, List.of("IuW8"));
    searchHelper(-409556808, "aFtER", 1, List.of("B7D3"));
    searchHelper(1572932439, "  black,", 1, List.of("_alK"));
    searchHelper(-1387227661, "FRIeNDsHIpb", 0, Collections.emptyList());
    searchHelper(-677365295, " k-PTOjEct", 0, Collections.emptyList());
    searchHelper(-1050362014, "sOda", 1, List.of("nu5W"));
    searchHelper(-107809662, " kHalleNgm", 0, Collections.emptyList());
    searchHelper(-325070212, "disAbiLiTY", 2, List.of("WGcd", "EjLZ"));
    searchHelper(125002689, "jOURnah", 0, Collections.emptyList());
    searchHelper(976109287, "AmeriwA", 0, Collections.emptyList());
    searchHelper(1594892405, "YZu", 0, Collections.emptyList());
    searchHelper(1382341234, " actGreEn", 1, List.of("rKUE"));
    searchHelper(1958037220, " vOLidaRITY", 0, Collections.emptyList());
    searchHelper(-1800069666, "spUAd", 0, Collections.emptyList());
    searchHelper(-1607666153, "Gwyn", 1, List.of("z-ly"));
    searchHelper(-2060117400, "AibSHoPs", 0, Collections.emptyList());
    searchHelper(-1903723983, "JAsMiNe", 1, List.of("33jT"));
    searchHelper(-920315013, "eaECtronic", 0, Collections.emptyList());
    searchHelper(-2001035501, "QTPoC", 1, List.of("m5yo"));
    searchHelper(-1693080897, "CulInARy", 2, List.of("HcGD", "xSOX"));
    searchHelper(
        1000005945, "PsychOlOgY", 6, List.of("AU0E", "LfmV", "zOyQ", "TAIO", "2OR8", "hXvz"));
    searchHelper(-1044410220, "swIFt", 1, List.of("n7gy"));
    searchHelper(-911089576, "advocaTES", 1, List.of("nxcL"));
    searchHelper(1011456243, "cereal", 1, List.of("wMVV"));
    searchHelper(1844640745, "RESOurce", 1, List.of("jCBn"));
    searchHelper(-1850602689, "NaturaL", 1, List.of("GwoD"));
    searchHelper(-410125013, "iNcluSivE", 1, List.of("41ks"));
    searchHelper(376844043, "CORter  ", 0, Collections.emptyList());
    searchHelper(
        -746307441,
        "ChaMpAiGN",
        8,
        List.of("uEgo", "Lr36", "VPgk", "FIYj", "WbIy", "o-F2", "6jYV", "H6K0"));
    searchHelper(1702024196, "  GrAy", 1, List.of("8Gis"));
    searchHelper(-1375642024, "eLectRIcAl", 3, List.of("ZQ_2", "HHzX", "Uo_A"));
  }

  // Check that the main activity search bar works properly
  @Test(timeout = 20000L)
  @Graded(points = 10, friendlyName = "Test Main Activity Search (Integration)")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test5_testMainActivitySearch() {
    // Start the main activity
    startMainActivity(
        activity -> {
          // Double-check that the number of RSO summaries displayed is correct
          onView(withId(R.id.recycler_view)).check(countRecyclerView(SUMMARY_COUNT));

          // Make sure blank searches work
          onView(withId(R.id.search)).perform(searchFor("  "));
          // Some manual delay is required for these tests to run reliably
          pause();
          onView(withId(R.id.recycler_view)).check(countRecyclerView(SUMMARY_COUNT));

          // Check for various hard-coded search terms
          onView(withId(R.id.search)).perform(searchFor("women in comp"));
          pause();
          onView(withId(R.id.recycler_view)).check(countRecyclerView(1));
          onView(withRecyclerView(R.id.recycler_view).atPosition(0))
              .check(matches(hasDescendant(withText("Women in Computer Science"))));

          onView(withId(R.id.search)).perform(searchFor("Ultimate"));
          pause();
          onView(withId(R.id.recycler_view)).check(countRecyclerView(3));
          onView(withRecyclerView(R.id.recycler_view).atPosition(2))
              .check(matches(hasDescendant(withText("Ultimate CU"))));

          // MuSiC matches several RSOs
          onView(withId(R.id.search)).perform(searchFor("MuSiC"));
          pause();
          onView(withId(R.id.recycler_view)).check(countRecyclerView(9));
          onView(withRecyclerView(R.id.recycler_view).atPosition(1))
              .check(matches(hasDescendant(withText("Electronic Music Club"))));
        });
  }
}

// md5: 8a78b50a7843d5798385c55ad6530c62 // DO NOT REMOVE THIS LINE
