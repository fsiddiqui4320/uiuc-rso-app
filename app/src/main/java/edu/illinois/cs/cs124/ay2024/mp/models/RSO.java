package edu.illinois.cs.cs124.ay2024.mp.models;

import androidx.annotation.NonNull;
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
    return categories;
  }

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
  }
}
