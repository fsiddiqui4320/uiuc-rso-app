package edu.illinois.cs.cs124.ay2024.mp.network;

import static edu.illinois.cs.cs124.ay2024.mp.helpers.Helpers.CHECK_SERVER_RESPONSE;
import static edu.illinois.cs.cs124.ay2024.mp.helpers.Helpers.OBJECT_MAPPER;
import static edu.illinois.cs.cs124.ay2024.mp.helpers.Helpers.readRSODataFile;

import androidx.annotation.NonNull;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import edu.illinois.cs.cs124.ay2024.mp.application.JoinableApplication;
import edu.illinois.cs.cs124.ay2024.mp.models.RSOData;
import edu.illinois.cs.cs124.ay2024.mp.models.Summary;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * Development RSO API server.
 *
 * <p>Normally you would run this server on another machine, which the client would contact over the
 * internet. For the sake of development, we're running the server alongside the app on the same
 * device. However, all communication between the RSO API client and RSO API server is still done
 * using the HTTP protocol. Meaning that it would be possible to move this code to a separate
 * server, which could then provide data for all clients.
 */
public final class Server extends Dispatcher {
  /** Used to log errors from the Server class. */
  private final Logger logger = Logger.getLogger(Server.class.getName());

  /** List of RSO summaries. Populated during startup. */
  private final List<Summary> summaries = new ArrayList<>();

  /** Helper method to create a 200 HTTP response with a body. */
  private MockResponse makeOKJSONResponse(@NonNull String body) {
    return new MockResponse()
        .setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(body)
        .setHeader("Content-Type", "application/json; charset=utf-8");
  }

  /** Helper value storing a 404 Not Found response. */
  private static final MockResponse HTTP_NOT_FOUND =
      new MockResponse()
          .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
          .setBody("404: Not Found");

  /** Helper value storing a 400 Bad Request response. */
  private static final MockResponse HTTP_BAD_REQUEST =
      new MockResponse()
          .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
          .setBody("400: Bad Request");

  /** GET the list of RSO summaries. */
  private MockResponse getSummaries() throws JsonProcessingException {
    return makeOKJSONResponse(OBJECT_MAPPER.writeValueAsString(summaries));
  }

  /**
   * HTTP request dispatcher.
   *
   * <p>This method receives HTTP requests from clients and determines how to handle them based on
   * the request path and method.
   */
  @NonNull
  @Override
  public MockResponse dispatch(@NonNull RecordedRequest request) {
    // Reject requests without a path or method
    if (request.getPath() == null || request.getMethod() == null) {
      return HTTP_BAD_REQUEST;
    }

    // Normalize trailing slashes, multiple slashes, and method case
    String path = request.getPath().replaceFirst("/*$", "").replaceAll("/+", "/");
    String method = request.getMethod().toUpperCase();

    // Main dispatcher routing tree
    try {
      if (path.isEmpty() && method.equals("GET")) {
        // Used by API client to validate server after startup
        return makeOKJSONResponse(CHECK_SERVER_RESPONSE);
      } else if (path.equals("/reset") && method.equals("GET")) {
        // Used to reset the server during testing
        return makeOKJSONResponse("200: OK");
      } else if (path.equals("/summary") && method.equals("GET")) {
        return getSummaries();
      } else {
        // Default is not found
        logger.log(Level.WARNING, "Route not found: " + path);
        return HTTP_NOT_FOUND;
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Server internal error for path: " + path, e);
      return new MockResponse()
          .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
          .setBody("500: Internal Error");
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  // YOU SHOULD NOT NEED TO MODIFY THE CODE BELOW
  /////////////////////////////////////////////////////////////////////////////////////////////////

  /** Start the server if has not already been started, and wait for startup to finish. */
  public static void start() {
    if (isRunning(false)) {
      return;
    }
    new Server();
    if (!isRunning(true)) {
      throw new IllegalStateException("Server should be running");
    }
  }

  /** Number of times to check the server before failing. */
  private static final int RETRY_COUNT = 8;

  /** Delay between retries. */
  private static final int RETRY_DELAY = 512;

  /**
   * Determine if the server is currently running.
   *
   * @param wait whether to wait or not
   * @return whether the server is running or not
   * @throws IllegalStateException if something else is running on our port
   */
  public static boolean isRunning(boolean wait) {
    return isRunning(wait, RETRY_COUNT, RETRY_DELAY);
  }

  /**
   * Determine if the server is currently running.
   *
   * @param wait whether to wait or not
   * @param retryCount how many times to retry
   * @param retryDelay how long to wait between retries
   * @return whether the server is running or not
   * @throws IllegalStateException if something else is running on our port
   */
  public static boolean isRunning(boolean wait, int retryCount, long retryDelay) {
    for (int i = 0; i < retryCount; i++) {
      OkHttpClient client = new OkHttpClient();
      Request request = new Request.Builder().url(JoinableApplication.SERVER_URL).get().build();
      try (Response response = client.newCall(request).execute()) {
        if (response.isSuccessful()) {
          if (Objects.requireNonNull(response.body()).string().equals(CHECK_SERVER_RESPONSE)) {
            return true;
          } else {
            throw new IllegalStateException(
                "Another server is running on port " + JoinableApplication.DEFAULT_SERVER_PORT);
          }
        }
      } catch (IOException ignoredIOException) {
        if (!wait) {
          break;
        }
        try {
          Thread.sleep(retryDelay);
        } catch (InterruptedException ignoredInterruptedException) {
        }
      }
    }
    return false;
  }

  /**
   * Reset the server.
   *
   * <p>Used to reset server state between tests.
   */
  @SuppressWarnings("unused")
  public static boolean reset() throws IOException {
    OkHttpClient client = new OkHttpClient();
    Request request =
        new Request.Builder().url(JoinableApplication.SERVER_URL + "/reset/").get().build();
    try (Response response = client.newCall(request).execute()) {
      return response.isSuccessful();
    }
  }

  private Server() {
    // Reduce server logging, since the defaults are fairly verbose
    // noinspection LoggerInitializedWithForeignClass
    Logger.getLogger(MockWebServer.class.getName()).setLevel(Level.SEVERE);

    // Load data used by the server
    loadData();

    try {
      // This resource needs to outlive the try-catch
      // noinspection resource
      MockWebServer server = new MockWebServer();
      server.setDispatcher(this);
      server.start(JoinableApplication.DEFAULT_SERVER_PORT);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Startup failed", e);
      throw new IllegalStateException(e);
    }
  }

  /** Helper method to load data used by the server. */
  private void loadData() {

    // Load the JSON string
    String json = readRSODataFile();

    // Build the list of summaries
    try {
      // Iterate through the list of JsonNodes returned by deserialization
      JsonNode nodes = OBJECT_MAPPER.readTree(json);
      for (JsonNode node : nodes) {
        // Load the RSOData object, use it to initialize the Summary and RSO objects, and then
        // add them to the appropriate collections.
        RSOData rsoData = OBJECT_MAPPER.readValue(node.toString(), RSOData.class);

        Summary summary = new Summary(rsoData);
        summaries.add(summary);
      }
    } catch (JsonProcessingException e) {
      logger.log(Level.SEVERE, "Loading data failed", e);
      throw new IllegalStateException(e);
    }
  }
}
