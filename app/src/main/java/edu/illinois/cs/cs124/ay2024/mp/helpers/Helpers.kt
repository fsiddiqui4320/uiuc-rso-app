package edu.illinois.cs.cs124.ay2024.mp.helpers

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import java.nio.charset.StandardCharsets
import java.util.Scanner

object Helpers {
    @JvmField
    val OBJECT_MAPPER: ObjectMapper =
        ObjectMapper().registerModule(ParameterNamesModule(JsonCreator.Mode.PROPERTIES))

    const val CHECK_SERVER_RESPONSE = "AY2024"

    @JvmStatic
    fun readRSODataFile(): String =
        Scanner(
            Helpers::class.java.getResourceAsStream("/rsos.json"),
            StandardCharsets.UTF_8,
        ).useDelimiter("\\A").next()
}
