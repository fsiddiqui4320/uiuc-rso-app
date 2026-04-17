package com.fsiddiqui.joinable.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class RSOData
    @JsonCreator
    constructor(
        @JsonProperty("id") val id: String,
        @JsonProperty("title") val title: String,
        @JsonProperty("categories") val categories: String,
        @JsonProperty("website") val website: String,
        @JsonProperty("mission") val mission: String,
    )
