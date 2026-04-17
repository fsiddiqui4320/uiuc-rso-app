package edu.illinois.cs.cs124.ay2024.mp.models

import com.fasterxml.jackson.annotation.JsonCreator

class RSO : Summary {
    val mission: String
    val website: String
    val categories: List<String>

    @JsonCreator
    constructor(
        setId: String,
        setTitle: String,
        setColor: Color,
        setMission: String,
        setWebsite: String,
        setCategories: List<String>,
    ) : super(setId, setTitle, setColor) {
        mission = setMission
        website = setWebsite
        categories = setCategories
    }

    constructor(rsoData: RSOData) : super(rsoData) {
        val categoryParts = rsoData.categories.split("-")
        categories =
            if (categoryParts.size > 1) {
                categoryParts[1].split(",").map { it.trim() }
            } else {
                emptyList()
            }
        mission = rsoData.mission
        website = rsoData.website
    }

    fun getRelatedRSOs(): List<Summary> = emptyList()
}
