package edu.illinois.cs.cs124.ay2024.mp.models;

import static edu.illinois.cs.cs124.ay2024.mp.helpers.Helpers.OBJECT_MAPPER;
import androidx.annotation.NonNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.net.CookieHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;
import edu.illinois.cs.cs124.ay2024.mp.application.JoinableApplication;
import edu.illinois.cs.cs124.ay2024.mp.network.Server;


/**
 * Model holding summary information about an RSO.
 *
 * <p>This class only holds the information needed by the main activity to render the summary list.
 * That includes each RSOs unique ID, title, and color.
 *
 * <p>The RSO model which you will add in MP2 extends this model and holds the remainder of the
 * provided RSO data.
 */
public class Summary implements Comparable<Summary> {
  /** Unique ID used to identify each RSO. */
  @NonNull private final String id;

  @NonNull
  public final String getId() {
    return id;
  }

  /** RSO title. */
  @NonNull private final String title;

  @NonNull
  public final String getTitle() {
    return title;
  }





  /**
   * RSOs are categorized by color, based on whether officers serve academic-year (orange) or
   * calendar year (blue) terms. See <a href="https://tinyurl.com/mr2m99rf">this page</a> for more
   * details.
   */
  public enum Color {
    BLUE,
    ORANGE,
    DEPARTMENT,
  }

  @NonNull private final Color color;

  @NonNull
  public final Color getColor() {
    return color;
  }

  /**
   * Create an RSO summary from the JSON data stored by the RSOData object.
   *
   * <p>Some of the RSO data fields map directly on to our Summary object. Others require a bit of
   * processing.
   *
   * @param rsoData the RSOData object to use to initialize this Summary
   */
  public Summary(@NonNull RSOData rsoData) {
    id = rsoData.id();
    title = rsoData.title();


    String[] categoryParts = rsoData.categories().split("-");
    if (categoryParts.length == 0) {
      throw new IllegalStateException("Color not set for RSO");
    }
    String categoryPrefix = categoryParts[0].trim();

    if (categoryPrefix.startsWith("Blue")) {
      color = Color.BLUE;
    } else if (categoryPrefix.startsWith("Orange")) {
      color = Color.ORANGE;
    } else if (categoryPrefix.startsWith("Department")) {
      color = Color.DEPARTMENT;
    } else {
      throw new IllegalStateException("Unknown RSO color: " + categoryPrefix);
    }
  }

  /**
   * Constructor that sets all fields, used for JSON serialization and deserialization.
   *
   * <p>Do not remove this constructor or Jackson serialization operations will fail.
   *
   * @param setId the RSO's unique id
   * @param setTitle the RSO's title
   * @param setColor the the RSO's color category
   */
  @JsonCreator
  @SuppressWarnings("unused")
  public Summary(@NonNull String setId, @NonNull String setTitle, @NonNull Color setColor) {
    id = setId;
    title = setTitle;
    color = setColor;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Summary other)) {
      return false;
    }
    return Objects.equals(id, other.id);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public int compareTo(Summary o) {
    return this.title.compareTo(o.title);
  }

  public static List<Summary> filterColor(List<Summary> inputlist, Set<Color> colors) {
    Collections.sort(inputlist);
    for (int i = inputlist.size() - 1; i >= 0; i--) {
      Summary summary = inputlist.get(i);
      if (!colors.contains(summary.color)) {
        inputlist.remove(summary);
      }
    }
    return inputlist;
  }

/*  public static List<Summary> sortByFavorite(List<Summary> inputlist, Map<String, Boolean> favoriteMap) {
    JoinableApplication application = (JoinableApplication) getApplication();

    Collections.sort(inputlist);
    List<Summary> newSummaries = new ArrayList<>();
    for (String id : favoriteMap.keySet()) {
      RSO rso = ID_TO_RSO.get(id);
      newSummaries.add(rso);
      inputlist.remove(rso);
    }
    Collections.sort(newSummaries);
    Collections.sort(inputlist);
    newSummaries.addAll(inputlist);
    return newSummaries;
  }*/

  public static List<Summary> search(List<Summary> inputlist, String search) {
    List<Summary> result = new ArrayList<>();
    Map<Summary, Integer> map = new HashMap<>();

    String searchTerm = search.trim().toLowerCase();

    if (searchTerm.isEmpty()) {
      result.addAll(inputlist);
      return result;
    }

    for (Summary summary : inputlist) {
      String[] words = summary.getTitle().toLowerCase().split(" ");
      for (String word : words) {
        if (word.equals(searchTerm)) {
          if (map.containsKey(summary)) {
            map.put(summary, map.get(summary) + 1);
          } else {
            result.add(summary);
            map.put(summary, 1);
          }
        }
      }
    }

    if (!map.isEmpty()) {

      Map<Summary, Integer> sortedMap = map.entrySet()
          .stream()
          .sorted(Comparator.comparing(Map.Entry<Summary, Integer>::getValue).reversed()
              .thenComparing(Map.Entry::getKey))
          .collect(Collectors.toMap(
              Map.Entry::getKey,
              Map.Entry::getValue,
              (e1, e2) -> e1,
              LinkedHashMap::new
          ));
      return new ArrayList<>(sortedMap.keySet());
    } else {
      for (Summary summary : inputlist) {
        if (summary.getTitle().toLowerCase().contains(searchTerm)) {
          result.add(summary);
        }
      }
    }
    return result;
  }
}
