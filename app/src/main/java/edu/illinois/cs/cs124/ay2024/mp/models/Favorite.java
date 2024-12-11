package edu.illinois.cs.cs124.ay2024.mp.models;

import androidx.annotation.NonNull;

public class Favorite {

  @NonNull
  private final String id;

  @NonNull
  public String getId() {
    return id;
  }

  private final boolean favorite;

  public boolean getFavorite() {
    return favorite;
  }

  public Favorite(@NonNull String setID, boolean setFavorite) {
    this.id = setID;
    this.favorite = setFavorite;
  }
}
