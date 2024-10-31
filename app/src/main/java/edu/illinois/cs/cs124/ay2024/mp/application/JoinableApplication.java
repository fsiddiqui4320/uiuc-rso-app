package edu.illinois.cs.cs124.ay2024.mp.application;

import android.app.Application;
import android.os.Build;
import edu.illinois.cs.cs124.ay2024.mp.network.Client;
import edu.illinois.cs.cs124.ay2024.mp.network.Server;

/**
 * Application class for the Joinable app.
 *
 * <p>Starts the development server and creates the RSO API client.
 */
public final class JoinableApplication extends Application {
  /**
   * RSO API server port. You can change this if needed, for example if it conflicts with something
   * else running on your machine.
   */
  public static final int DEFAULT_SERVER_PORT = 8024;

  /** RSO API server URL. */
  public static final String SERVER_URL = "http://localhost:" + DEFAULT_SERVER_PORT;

  /** RSO API client created during app startup. */
  private Client client;

  /** {@inheritDoc} */
  @Override
  public void onCreate() {
    super.onCreate();

    // Start the API server
    if (Build.FINGERPRINT.equals("robolectric")) { // Flag that indicates we're testing the app
      Server.start();
    } else {
      // In a new thread if we're not testing
      new Thread() {
        @Override
        public void run() {
          Server.start();
        }
      }.start();
    }

    // Start the API client
    client = Client.start();
  }

  /**
   * Retrieve the RSO API client instance for this app.
   *
   * @return the RSO API client instance.
   */
  public Client getClient() {
    if (!client.getConnected()) {
      throw new IllegalStateException("Client not connected");
    }
    return client;
  }
}
