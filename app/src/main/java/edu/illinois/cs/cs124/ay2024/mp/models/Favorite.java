package edu.illinois.cs.cs124.ay2024.mp.models;

import static edu.illinois.cs.cs124.ay2024.mp.helpers.Helpers.OBJECT_MAPPER;
import androidx.annotation.NonNull;
import java.net.HttpURLConnection;
import java.util.logging.Level;

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
