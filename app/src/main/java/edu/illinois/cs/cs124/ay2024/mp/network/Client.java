package edu.illinois.cs.cs124.ay2024.mp.network;

import static edu.illinois.cs.cs124.ay2024.mp.helpers.Helpers.CHECK_SERVER_RESPONSE;
import static edu.illinois.cs.cs124.ay2024.mp.helpers.Helpers.OBJECT_MAPPER;

import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.ExecutorDelivery;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.NoCache;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import junit.framework.Test;
import edu.illinois.cs.cs124.ay2024.mp.application.JoinableApplication;
import edu.illinois.cs.cs124.ay2024.mp.helpers.ResultMightThrow;
import edu.illinois.cs.cs124.ay2024.mp.models.Favorite;
import edu.illinois.cs.cs124.ay2024.mp.models.RSO;
import edu.illinois.cs.cs124.ay2024.mp.models.Summary;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * RSO API client.
 *
 * <p>You will add functionality to the client to complete the project.
 */
public final class Client {
  private static final String TAG = Client.class.getSimpleName();

  private final Logger logger = Logger.getLogger(Server.class.getName());

  public void getRSO(@NonNull String id, @NonNull final Consumer<ResultMightThrow<RSO>> callback) {
    StringRequest rsoRequest =
        new StringRequest(
            Request.Method.GET,
            JoinableApplication.SERVER_URL + "/rso/" + id,
            response -> {
              try {
                RSO rso =
                    OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
                callback.accept(new ResultMightThrow<>(rso));
              } catch (JsonProcessingException e) {
                callback.accept(new ResultMightThrow<>(e));
              }
            },
            error -> callback.accept(new ResultMightThrow<>(error)));
    requestQueue.add(rsoRequest);
  }
  /**
   * Retrieve the list of RSO summaries.
   *
   * <p>Used by the MainActivity.
   *
   * @param callback the callback that will receive the list of RSO summaries
   */
  public void getSummaries(@NonNull final Consumer<ResultMightThrow<List<Summary>>> callback) {
    StringRequest summariesRequest =
        new StringRequest(
            Request.Method.GET,
            JoinableApplication.SERVER_URL + "/summary/",
            response -> {
              try {
                List<Summary> summaries =
                    OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
                callback.accept(new ResultMightThrow<>(summaries));
              } catch (JsonProcessingException e) {
                callback.accept(new ResultMightThrow<>(e));
              }
            },
            error -> callback.accept(new ResultMightThrow<>(error)));
    requestQueue.add(summariesRequest);
  }

  public void getFavorite(@NonNull final String id,
                          @NonNull final Consumer<ResultMightThrow<Boolean>> callback) {
    StringRequest getFavoriteRequest =
        new StringRequest(
            Request.Method.GET,
            JoinableApplication.SERVER_URL + "/favorite/" + id,
            response -> {
              try {
                Favorite favorite = OBJECT_MAPPER.readValue(response, Favorite.class);
                callback.accept(new ResultMightThrow<>(favorite.getFavorite()));
              } catch (JsonProcessingException e) {
                callback.accept(new ResultMightThrow<>(e));
              }
            },
            error -> callback.accept(new ResultMightThrow<>(error)));
    requestQueue.add(getFavoriteRequest);
  }

  public void setFavorite(@NonNull final String id, final boolean isFavorite,
                          @NonNull final Consumer<ResultMightThrow<Boolean>> callback) {
    // Create the Favorite object
    Favorite favorite = new Favorite(id, isFavorite);
    String favoriteJSON;
    try {
      favoriteJSON = OBJECT_MAPPER.writeValueAsString(favorite);
    } catch (JsonProcessingException e) {
      callback.accept(new ResultMightThrow<>(e));
      return;
    }

    StringRequest setFavoriteRequest =
        new StringRequest(
            Request.Method.POST,
            JoinableApplication.SERVER_URL + "/favorite",
            response -> {
              // The server responds with a redirect to /favorite/RSOID followed by a GET response.
              // Since HttpURLConnection.setFollowRedirects(true) is called, Volley should follow
              // the redirect
              // and 'response' should end up being the final GET response body.

              // At this point, response should contain the JSON of the favorite after redirect.
              try {
                Favorite returnedFavorite = OBJECT_MAPPER.readValue(response, Favorite.class);
                callback.accept(new ResultMightThrow<>(returnedFavorite.getFavorite()));
              } catch (JsonProcessingException e) {
                callback.accept(new ResultMightThrow<>(e));
              }
            },
            error -> callback.accept(new ResultMightThrow<>(error))) {
          @Override
          public String getBodyContentType() {
            return "application/json; charset=utf-8";
          }

          @Override
          public byte[] getBody() throws AuthFailureError {
            return favoriteJSON.getBytes(StandardCharsets.UTF_8);
          }
        };
    requestQueue.add(setFavoriteRequest);
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  // YOU SHOULD NOT NEED TO MODIFY THE CODE BELOW
  /////////////////////////////////////////////////////////////////////////////////////////////////

  /** Client instance to implement the singleton pattern. */
  private static Client instance;

  /**
   * Start the API client.
   *
   * @return an API client instance
   */
  @NonNull
  public static Client start() {
    if (instance == null) {
      instance = new Client();
    }
    return instance;
  }

  /** Initial connection delay. */
  private static final int INITIAL_CONNECTION_RETRY_DELAY = 1000;

  /** Max retries to connect to the server. */
  private static final int MAX_STARTUP_RETRIES = 8;

  /** Queue for our requests. */
  private final RequestQueue requestQueue;

  /** Allow getConnected to wait for startup to complete. */
  private final CompletableFuture<Boolean> connected = new CompletableFuture<>();

  /** Maximum wait during initial connection. */
  private static final int GET_CONNECTED_DELAY_SEC = 2;

  /**
   * Return whether the client is connected or not.
   *
   * @return whether the client is connected
   */
  public boolean getConnected() {
    try {
      return connected.get(GET_CONNECTED_DELAY_SEC, TimeUnit.SECONDS);
    } catch (Exception e) {
      return false;
    }
  }

  /** Private constructor to implement the singleton pattern. */
  private Client() {
    // Whether we're in a testing configuration
    boolean testing = Build.FINGERPRINT.equals("robolectric");
    // Disable debug logging
    VolleyLog.DEBUG = false;
    // Follow redirects so POST works
    HttpURLConnection.setFollowRedirects(true);

    // Configure our request queue
    Cache cache = new NoCache();
    Network network = new BasicNetwork(new HurlStack());

    // The request queue configuration depends on whether we are testing or not.
    if (testing) {
      requestQueue =
          new RequestQueue(
              cache, network, 1,
              new ExecutorDelivery(Executors.newSingleThreadExecutor()));
    } else {
      requestQueue = new RequestQueue(cache, network);
    }

    // Make sure the backend URL is valid
    URL serverURL;
    try {
      serverURL = new URL(JoinableApplication.SERVER_URL);
    } catch (MalformedURLException e) {
      Log.e(TAG, "Bad server URL: " + JoinableApplication.SERVER_URL, e);
      return;
    }

    // Start a background thread to establish the server connection
    new Thread(
            () -> {
              for (int i = 0; i < MAX_STARTUP_RETRIES; i++) {
                try {
                  // Issue a GET request for the root URL
                  HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
                  String body =
                      new BufferedReader(new InputStreamReader(connection.getInputStream()))
                          .lines()
                          .collect(Collectors.joining("\n"));
                  if (!body.equals(CHECK_SERVER_RESPONSE)) {
                    throw new IllegalStateException("Invalid response from server");
                  }
                  connection.disconnect();

                  // Once this succeeds, we're connected and can start the Volley queue
                  connected.complete(true);
                  requestQueue.start();
                  return;
                } catch (Exception ignored) {
                }
                // If the connection fails, delay and then retry
                try {
                  Thread.sleep(INITIAL_CONNECTION_RETRY_DELAY);
                } catch (InterruptedException ignored) {
                }
              }
              Log.e(TAG, "Client couldn't connect");
            })
        .start();
  }
}
