package edu.illinois.cs.cs124.ay2024.mp.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

class RSO : Summary {
    val mission: String
    val website: String
    val categories: List<String>
    private var relatedRSOs: List<Summary> = emptyList()

    @JsonCreator
    constructor(
        @JsonProperty("id") setId: String,
        @JsonProperty("title") setTitle: String,
        @JsonProperty("color") setColor: Color,
        @JsonProperty("mission") setMission: String,
        @JsonProperty("website") setWebsite: String,
        @JsonProperty("categories") setCategories: List<String>,
        @JsonProperty("relatedRSOs") setRelatedRSOs: List<Summary>?,
    ) : super(setId, setTitle, setColor) {
        mission = setMission
        website = setWebsite
        categories = setCategories
        relatedRSOs = setRelatedRSOs ?: emptyList()
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

    fun getRelatedRSOs(): List<Summary> = relatedRSOs

    fun computeRelatedRSOs(allRSOs: Collection<RSO>) {
        if (categories.isEmpty()) {
            relatedRSOs = emptyList()
            return
        }
        val myCats = categories.toSet()
        relatedRSOs =
            allRSOs
                .filter { it.id != id && it.categories.any { cat -> cat in myCats } }
                .sortedWith(
                    compareByDescending<RSO> { rso -> rso.categories.count { it in myCats } }
                        .thenBy { it.title },
                ).take(8)
                // Store as plain Summary objects to avoid circular serialization
                .map { rso -> Summary(rso.id, rso.title, rso.color) }
    }
}
