package edu.illinois.cs.cs124.ay2024.mp.models;

import androidx.annotation.NonNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Objects;

/**
 * Model holding summary information about an RSO.
 *
 * <p>This class only holds the information needed by the main activity to render the summary list.
 * That includes each RSOs unique ID, title, and color.
 *
 * <p>The RSO model which you will add in MP2 extends this model and holds the remainder of the
 * provided RSO data.
 */
public class Summary {
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
}
