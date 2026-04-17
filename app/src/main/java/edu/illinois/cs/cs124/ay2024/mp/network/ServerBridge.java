package edu.illinois.cs.cs124.ay2024.mp.network;

/**
 * Java bridge to access Server static methods from Kotlin. Server uses a private companion
 * object; @JvmStatic methods are public static at the JVM level and callable from Java but not
 * directly from Kotlin across packages.
 */
public final class ServerBridge {
  private ServerBridge() {}

  public static void start() {
    Server.start();
  }
}
