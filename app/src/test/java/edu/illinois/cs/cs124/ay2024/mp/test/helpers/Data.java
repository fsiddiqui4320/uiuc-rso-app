package edu.illinois.cs.cs124.ay2024.mp.test.helpers;

import static edu.illinois.cs.cs124.ay2024.mp.helpers.Helpers.readRSODataFile;

import androidx.annotation.NonNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import edu.illinois.cs.cs124.ay2024.mp.models.RSOData;
import edu.illinois.cs.cs124.ay2024.mp.models.Summary;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/*
 * This file contains helper code used by the test suites.
 * You should not need to modify it.
 * ALL CHANGES TO THIS FILE WILL BE OVERWRITTEN DURING OFFICIAL GRADING.
 *
 * The helper methods in this file assist with loading RSO data for testing.
 */
public class Data {
  /** Object mapper used by the test suites. */
  @NonNull
  public static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper().registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));

  /** Fingerprint of the rsos.json file. */
  @NonNull private static final String RSOS_FINGERPRINT = "77ad622e3a8f0c31c1e9e45fcf5c9e72";

  /** List of RSOData objects for testing. */
  @NonNull public static final List<RSOData> RSODATA = loadRSOData();

  /** List of Summary objects for testing. */
  @NonNull public static final List<Summary> SUMMARIES = loadSummaries();

  /** Number of summaries that we expect. */
  public static final int SUMMARY_COUNT = SUMMARIES.size();

  /** Blue color count. */
  public static final int BLUE_COUNT =
      SUMMARIES.stream()
          .filter(s -> s.getColor() == Summary.Color.BLUE)
          .collect(Collectors.toList())
          .size();

  /** Orange color count. */
  public static final int ORANGE_COUNT =
      SUMMARIES.stream()
          .filter(s -> s.getColor() == Summary.Color.ORANGE)
          .collect(Collectors.toList())
          .size();

  /** Department color count. */
  public static final int DEPARTMENT_COUNT =
      SUMMARIES.stream()
          .filter(s -> s.getColor() == Summary.Color.DEPARTMENT)
          .collect(Collectors.toList())
          .size();

  /**
   * Load JSON and deserialize into RSO data objects.
   *
   * @return list of RSO data objects
   */
  @NonNull
  public static List<RSOData> loadRSOData() {
    // Load the JSON string
    String json = loadAndFingerprintJSON();
    try {
      List<RSOData> summaries = OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
      return Collections.unmodifiableList(summaries);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Load JSON and deserialize into a list of summary objects
   *
   * @return list of summary objects
   */
  @NonNull
  public static List<Summary> loadSummaries() {
    return loadRSOData().stream().map(Summary::new).collect(Collectors.toUnmodifiableList());
  }

  /**
   * Check the fingerprint of the rsos.json file to ensure it hasn't been modified.
   *
   * @return contents of the rsos.json file as a string
   */
  @NonNull
  public static String loadAndFingerprintJSON() {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("MD5");
    } catch (Exception e) {
      throw new IllegalStateException("MD5 algorithm should be available", e);
    }

    String input = readRSODataFile();

    String toFingerprint =
        Arrays.stream(input.split("\n"))
            .map(String::stripTrailing)
            .collect(Collectors.joining("\n"));

    String currentFingerprint =
        String.format(
                "%1$32s",
                new BigInteger(1, digest.digest(toFingerprint.getBytes(StandardCharsets.UTF_8)))
                    .toString(16))
            .replace(' ', '0');

    if (!currentFingerprint.equals(RSOS_FINGERPRINT)) {
      throw new IllegalStateException(
          "rsos.json has been modified. Please restore the original version of the file.");
    }
    return input;
  }

  @NonNull
  public static List<Summary> getShuffledSummaries(int seed, int size) {
    List<Summary> trimmedSummaries = new ArrayList<>(SUMMARIES);
    Collections.shuffle(trimmedSummaries, new Random(seed));
    return trimmedSummaries.subList(0, Math.min(size, SUMMARY_COUNT));
  }

  @NonNull
  public static List<Summary> getShuffledSummaries(int seed) {
    List<Summary> shuffledSummaries = new ArrayList<>(SUMMARIES);
    Collections.shuffle(shuffledSummaries, new Random(seed));
    return shuffledSummaries;
  }
}

// md5: 7927b9fa113f6c1cc36cee96e1bdaca0 // DO NOT REMOVE THIS LINE
