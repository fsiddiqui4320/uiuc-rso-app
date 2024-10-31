package edu.illinois.cs.cs124.ay2024.mp.helpers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Objects;

/**
 * Helper class for wrapping exceptions thrown by another thread.
 *
 * <p>Allows the main thread to retrieve exceptions thrown by the API client, rather than having
 * them be thrown on another thread. See use in MainActivity.java and Client.java.
 *
 * @param <T> type of the valid result
 */
public class ResultMightThrow<T> {
  /**
   * Value stored when an exception is not thrown. Must be non-null if an exception was not thrown.
   */
  @Nullable private final T value;

  /** Exception stored when an exception is thrown. Must be non-null if the value is null. */
  @Nullable private final Exception exception;

  /**
   * Initialize an instance when a value was generated and no exception thrown.
   *
   * @param setValue value to store, may be null.
   */
  public ResultMightThrow(@NonNull final T setValue) {
    value = Objects.requireNonNull(setValue);
    exception = null;
  }

  /**
   * Initialize an instance when an exception was thrown, implying the value is null.
   *
   * @param setException exception to store, must not be null.
   */
  public ResultMightThrow(@NonNull final Exception setException) {
    value = null;
    exception = Objects.requireNonNull(setException);
  }

  /**
   * Retrieve the saved value, or throw an exception if the instance stores an exception.
   *
   * @return the saved value if no exception was thrown
   * @throws RuntimeException thrown if no value was generated, wraps the saved exception
   */
  @NonNull
  public T getValue() throws RuntimeException {
    if (exception != null) {
      // Wrap the saved exception to avoid it being a checked exception
      throw new RuntimeException(exception);
    } else {
      return Objects.requireNonNull(value);
    }
  }

  /**
   * Retrieve the thrown exception, or null if no exception was thrown.
   *
   * @return the thrown exception, or null if no exception was thrown
   */
  @Nullable
  public Exception getException() {
    return exception;
  }
}
