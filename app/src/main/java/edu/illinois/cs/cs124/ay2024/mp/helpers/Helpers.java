package edu.illinois.cs.cs124.ay2024.mp.helpers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import edu.illinois.cs.cs124.ay2024.mp.network.Server;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/** Helper class holding a few broadly-useful items. */
public final class Helpers {
  // Jackson instance for serialization and deserialization
  public static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper()
          // ParameterNamesModule allows serialization without requiring an empty constructor
          .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));

  // Magic server response used by the client to determine that it's properly connected
  public static final String CHECK_SERVER_RESPONSE = "AY2024";

  // Load RSO data from rsos.json
  public static String readRSODataFile() {
    return new Scanner(Server.class.getResourceAsStream("/rsos.json"), StandardCharsets.UTF_8)
        .useDelimiter("\\A")
        .next();
  }
}
