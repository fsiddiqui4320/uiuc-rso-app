package edu.illinois.cs.cs124.ay2024.mp.test.helpers;

import static com.google.common.truth.Truth.assertWithMessage;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.Permission;

/*
 * This file contains helper code used by the test suites.
 * You should not need to modify it.
 * ALL CHANGES TO THIS FILE WILL BE OVERWRITTEN DURING OFFICIAL GRADING.
 *
 * Used to count the number of times that the API server accesses RSO JSON files, to fail
 * implementations that repeatedly load the file after Server initialization.
 *
 * Here we utilize the fact that Java's (deprecated) security architecture allows us to record
 * file accesses to these files.
 * Normally you'd use this to perform access control, but we can also use it to simply count
 * the number of times a file was accessed.
 */

public class JSONReadCountSecurityManager extends SecurityManager {
  /** Path to the JSON file. */
  private final Path jsonPath;

  /** Number of times the file has been read. */
  private int jsonReadCount = 0;

  public JSONReadCountSecurityManager() {
    try {
      jsonPath = Path.of(JSONReadCountSecurityManager.class.getResource("/rsos.json").toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  /** {@inheritDoc} @noinspection RedundantMethodOverride */
  @Override
  public void checkPermission(Permission perm) {}

  /** {@inheritDoc} @noinspection RedundantMethodOverride */
  @Override
  public void checkPermission(Permission perm, Object context) {}

  /** {@inheritDoc} */
  @Override
  public void checkRead(String file) {
    // This throws on Windows for some strange-looking paths
    try {
      if (Path.of(file).equals(jsonPath)) {
        jsonReadCount++;
      }
    } catch (Exception ignored) {
    }
    super.checkRead(file);
  }

  /** {@inheritDoc} */
  @Override
  public void checkRead(String file, Object context) {
    // This throws on Windows for some strange-looking paths
    try {
      if (Path.of(file).equals(jsonPath)) {
        jsonReadCount++;
      }
    } catch (Exception ignored) {
    }
    super.checkRead(file, context);
  }

  /** Numbers of reads we expect. */
  private static final int EXPECTED_READ_COUNT = 3;

  /**
   * Check that the read count hasn't been exceeded.
   *
   * @param count the maximum read count.
   */
  public void checkCount(int count) {
    assertWithMessage("rsos.json should only be accessed during server start")
        .that(jsonReadCount)
        .isAtMost(count);
  }

  /** Check that the read count hasn't been exceeded. */
  public void checkCount() {
    checkCount(EXPECTED_READ_COUNT);
  }
}
// md5: 6fa329b88da2ff45c29d01f2cb6d34d2 // DO NOT REMOVE THIS LINE
