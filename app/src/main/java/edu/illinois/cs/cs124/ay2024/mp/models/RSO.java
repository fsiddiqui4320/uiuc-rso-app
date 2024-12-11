package edu.illinois.cs.cs124.ay2024.mp.models;

import androidx.annotation.NonNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RSO extends Summary {
  @NonNull
  private String mission;

  @NonNull
  public String getMission() {
    return mission;
  }

  @NonNull
  private String website;

  @NonNull
  public String getWebsite() {
    return website;
  }

  @NonNull
  private List<String> categories;

  @NonNull
  public List<String> getCategories() {
    if (categories == null) {
      throw new IllegalArgumentException();
    }
    return categories;
  }

  @JsonCreator
  public RSO(
      @NonNull String setId,
      @NonNull String setTitle,
      @NonNull Color setColor,
      @NonNull String setMission,
      @NonNull String setWebsite,
      @NonNull List<String> setCategories) {
    super(setId, setTitle, setColor);
    mission = setMission;
    website = setWebsite;
    categories = setCategories;
  }

  public RSO(@NonNull RSOData rsoData) {
    super(rsoData);
    String[] categoryParts = rsoData.categories().split("-");
    List<String> setCategories = new ArrayList<>();
    if (categoryParts.length > 1) {
      String[] cats = categoryParts[1].split(",");
      setCategories = new ArrayList<>(Arrays.asList(cats));
      setCategories.replaceAll(String::trim);
    }
    mission = rsoData.mission();
    website = rsoData.website();
    categories = setCategories;
  }

  @NonNull
  public List<Summary> getRelatedRSOs() {
    return Collections.emptyList();
  }
}
