package com.fsiddiqui.joinable.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Favorite
    @JsonCreator
    constructor(
        @JsonProperty("id") val id: String,
        @JsonProperty("favorite") val favorite: Boolean,
    )
