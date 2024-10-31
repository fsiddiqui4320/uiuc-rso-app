package edu.illinois.cs.cs124.ay2024.mp.test.helpers;

import static com.google.common.truth.Truth.assertWithMessage;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import edu.illinois.cs.cs124.ay2024.mp.application.JoinableApplication;
import edu.illinois.cs.cs124.ay2024.mp.helpers.ResultMightThrow;
import edu.illinois.cs.cs124.ay2024.mp.network.Client;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/*
 * This file contains helper code used by the test suites.
 * You should not need to modify it.
 * ALL CHANGES TO THIS FILE WILL BE OVERWRITTEN DURING OFFICIAL GRADING.
 *
 * The helper methods in this file assist with testing the API server and client.
 */
public class HTTP {
  public static class TimedResponse<T> {
    private final T response;

    private final Duration responseTime;

    public TimedResponse(T setResponse, Duration setResponseTime) {
      response = setResponse;
      responseTime = setResponseTime;
    }

    public T getResponse() {
      return response;
    }

    public Duration getResponseTime() {
      return responseTime;
    }
  }

  /** Private HTTP client for testing. */
  private static final OkHttpClient httpClient =
      new OkHttpClient.Builder().callTimeout(1, TimeUnit.SECONDS).build();

  /**
   * Test a GET call to the backend API server.
   *
   * @param route route to test
   * @param responseCode expected response code
   * @param klass expected type of the response body for deserialization
   * @param <T> type of the deserialized response body
   * @return deserialized response body
   * @throws IOException if the HTTP request fails
   */
  @SuppressWarnings("unchecked")
  public static <T> TimedResponse<T> testServerGet(String route, int responseCode, Object klass)
      throws IOException {
    // Create the request
    Request request = new Request.Builder().url(JoinableApplication.SERVER_URL + route).build();
    Instant start = Instant.now();

    // Make the request
    // try-with-resources ensures the response is cleaned up properly
    try (Response response = httpClient.newCall(request).execute()) {
      Duration responseTime = Duration.between(start, Instant.now());

      if (responseCode == HttpURLConnection.HTTP_OK) {
        // The request should have succeeded
        assertWithMessage("GET request for " + route + " should have succeeded")
            .that(response.code())
            .isEqualTo(HttpURLConnection.HTTP_OK);
      } else {
        // The request should have failed the the correct code
        assertWithMessage(
                "GET request for " + route + " should have failed with code " + responseCode)
            .that(response.code())
            .isEqualTo(responseCode);
        return new TimedResponse<>(null, responseTime);
      }

      // The response body should not be null
      ResponseBody body = response.body();
      assertWithMessage("GET response for " + route + " body should not be null")
          .that(body)
          .isNotNull();

      // Deserialize based on type passed to the method
      if (klass == null) {
        String bodyString = body.string();
        try {
          return new TimedResponse<>((T) Data.OBJECT_MAPPER.readTree(bodyString), responseTime);
        } catch (JsonParseException unused) {
          return new TimedResponse<>((T) bodyString, responseTime);
        }
      }

      // All remaining paths expect JSON
      assertWithMessage("Content-Type header not set correctly")
          .that(response.header("Content-Type"))
          .isEqualTo("application/json; charset=utf-8");
      if (klass instanceof Class<?> it) {
        return new TimedResponse<>(
            (T) Data.OBJECT_MAPPER.readValue(body.string(), it), responseTime);
      } else if (klass instanceof TypeReference<?> it) {
        return new TimedResponse<>(
            (T) Data.OBJECT_MAPPER.readValue(body.string(), it), responseTime);
      } else {
        throw new IllegalStateException("Bad deserialization class passed to testServerGet");
      }
    }
  }

  /**
   * Test a GET call to the backend API server that should succeed.
   *
   * @param route route to test
   * @param klass expected type of the response body for deserialization
   * @return deserialized response body
   * @param <T> type of the deserialized response body
   * @throws IOException if the HTTP request fails
   */
  @SuppressWarnings("unchecked")
  public static <T> T testServerGet(String route, Object klass) throws IOException {
    return (T) testServerGet(route, HttpURLConnection.HTTP_OK, klass).getResponse();
  }

  /**
   * Test a GET call to the backend API server with timing information.
   *
   * @param route route to test
   * @param klass expected type of the response body for deserialization
   * @return deserialized response body wrapped with timing information
   * @param <T> type of the deserialized response body
   * @throws IOException if the HTTP request fails
   */
  public static <T> TimedResponse<T> testServerGetTimed(String route, Object klass)
      throws IOException {
    return testServerGet(route, HttpURLConnection.HTTP_OK, klass);
  }

  /**
   * Test a GET call to the backend API server with string output.
   *
   * @param route route to test
   * @param responseCode expected response code
   * @return string response body
   * @param <T> type of the response body
   * @throws IOException if the HTTP request fails
   */
  @SuppressWarnings("unchecked")
  public static <T> T testServerGet(String route, int responseCode) throws IOException {
    return (T) testServerGet(route, responseCode, null).getResponse();
  }

  /**
   * Test a GET call to the backend API server with JsonNode output that should succeed.
   *
   * @param route route to test
   * @return deserialized response body
   * @throws IOException if the HTTP request fails
   */
  public static JsonNode testServerGet(String route) throws IOException {
    return (JsonNode) testServerGet(route, HttpURLConnection.HTTP_OK, null).getResponse();
  }

  /**
   * Test a GET call to the backend API server with string output that should succeed.
   *
   * @param route route to test
   * @return string response body
   * @param <T> type of the response body
   * @throws IOException if the HTTP request fails
   */
  public static <T> TimedResponse<T> testServerGetTimed(String route) throws IOException {
    return testServerGet(route, HttpURLConnection.HTTP_OK, null);
  }

  /**
   * Test a POST to the backend API server.
   *
   * @param route route to test
   * @param responseCode expected response code
   * @param requestBody body to POST
   * @param klass expected type of the response body for deserialization
   * @return deserialized response body
   * @param <T> type of the response body
   * @throws IOException if the HTTP request fails
   */
  @SuppressWarnings("unchecked")
  public static <T> T testServerPost(
      String route, int responseCode, Object requestBody, Object klass) throws IOException {
    // Create the request
    Request request =
        new Request.Builder()
            .url(JoinableApplication.SERVER_URL + route)
            .post(
                RequestBody.create(
                    Data.OBJECT_MAPPER.writeValueAsString(requestBody),
                    MediaType.parse("application/json")))
            .build();

    // Make the request
    // try-with-resources ensures the response is cleaned up properly
    try (Response response = httpClient.newCall(request).execute()) {

      if (responseCode == HttpURLConnection.HTTP_OK) {
        // The request should have succeeded
        assertWithMessage(
                "POST request for " + route + " should have succeeded but was " + response.code())
            .that(response.code())
            .isEqualTo(HttpURLConnection.HTTP_OK);
      } else {
        // The request should have failed the the correct code
        assertWithMessage(
                "POST request for " + route + " should have failed with code " + responseCode)
            .that(response.code())
            .isEqualTo(responseCode);
        return null;
      }

      // The response body should not be null
      ResponseBody responseBody = response.body();
      assertWithMessage("POST response for " + route + " body should not be null")
          .that(responseBody)
          .isNotNull();

      // All POST paths expect JSON
      assertWithMessage("Content-Type header not set correctly")
          .that(response.header("Content-Type"))
          .isEqualTo("application/json; charset=utf-8");

      // Deserialize based on type passed to the method
      if (klass == null) {
        return (T) Data.OBJECT_MAPPER.readTree(responseBody.string());
      } else if (klass instanceof Class<?> it) {
        return (T) Data.OBJECT_MAPPER.readValue(responseBody.string(), it);
      } else if (klass instanceof TypeReference<?> it) {
        return (T) Data.OBJECT_MAPPER.readValue(responseBody.string(), it);
      } else {
        throw new IllegalStateException("Bad deserialization class passed to testServerPost");
      }
    }
  }

  /**
   * Test a POST to the backend API server that should succeed.
   *
   * @param route route to test
   * @param requestBody body to POST
   * @param klass expected type of the response body for deserialization
   * @return deserialized response body
   * @param <T> type of the response body
   * @throws IOException if the HTTP request fails
   */
  public static <T> T testServerPost(String route, Object requestBody, Object klass)
      throws IOException {
    return testServerPost(route, HttpURLConnection.HTTP_OK, requestBody, klass);
  }

  /**
   * Test a POST to the backend API server.
   *
   * @param route route to test
   * @param requestBody body to POST
   * @return deserialized response body
   * @param <T> type of the response body
   * @throws IOException if the HTTP request fails
   */
  public static <T> T testServerPost(String route, Object requestBody, int responseCode)
      throws IOException {
    return testServerPost(route, responseCode, requestBody, null);
  }

  /** Private API client for testing. */
  private static Client apiClient = null;

  /** Retrieve the client, starting if needed. */
  public static Client getAPIClient() {
    if (apiClient == null) {
      apiClient = Client.start();
    }
    return apiClient;
  }

  /** Helper method for API client testing. */
  public static <T> T testClient(Consumer<Consumer<ResultMightThrow<T>>> method) throws Exception {
    // Ensure the client started up properly
    assertWithMessage("Client should be connected").that(apiClient.getConnected()).isTrue();

    // A CompletableFuture allows us to wait for the result of an asynchronous call
    CompletableFuture<ResultMightThrow<T>> completableFuture = new CompletableFuture<>();

    // When the client call returns, it causes the CompletableFuture to complete
    method.accept(completableFuture::complete);

    // Wait for the CompletableFuture to complete
    ResultMightThrow<T> result = completableFuture.get();

    // Throw if the call threw
    if (result.getException() != null) {
      throw result.getException();
    }

    // Shouldn't ever happen, but doesn't hurt to check
    assertWithMessage("Client call expected to succeed returned null")
        .that(result.getValue())
        .isNotNull();

    return result.getValue();
  }
}

// md5: 078507f233e683cf2bc185322c1e0d99 // DO NOT REMOVE THIS LINE
