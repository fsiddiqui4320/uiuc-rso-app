package edu.illinois.cs.cs124.ay2024.mp.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

open class Summary : Comparable<Summary> {
    val id: String
    val title: String
    val color: Color

    enum class Color { BLUE, ORANGE, DEPARTMENT }

    constructor(rsoData: RSOData) {
        id = rsoData.id
        title = rsoData.title
        val categoryPrefix = rsoData.categories.split("-")[0].trim()
        color =
            when {
                categoryPrefix.startsWith("Blue") -> Color.BLUE
                categoryPrefix.startsWith("Orange") -> Color.ORANGE
                categoryPrefix.startsWith("Department") -> Color.DEPARTMENT
                else -> throw IllegalStateException("Unknown RSO color: $categoryPrefix")
            }
    }

    @JsonCreator
    constructor(
        @JsonProperty("id") setId: String,
        @JsonProperty("title") setTitle: String,
        @JsonProperty("color") setColor: Color,
    ) {
        id = setId
        title = setTitle
        color = setColor
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Summary) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun compareTo(other: Summary): Int = title.compareTo(other.title)

    companion object {
        @JvmStatic
        fun filterColor(
            inputList: List<Summary>,
            colors: Set<Color>,
        ): List<Summary> = inputList.filter { it.color in colors }.sortedBy { it.title }

        @JvmStatic
        fun search(
            inputList: List<Summary>,
            search: String,
        ): List<Summary> {
            val searchTerm = search.trim().lowercase()
            if (searchTerm.isEmpty()) return inputList.toList()

            val exactMatches = mutableMapOf<Summary, Int>()
            for (summary in inputList) {
                for (word in summary.title.lowercase().split(" ")) {
                    if (word == searchTerm) {
                        exactMatches[summary] = (exactMatches[summary] ?: 0) + 1
                    }
                }
            }

            if (exactMatches.isNotEmpty()) {
                return exactMatches.entries
                    .sortedWith(compareByDescending<Map.Entry<Summary, Int>> { it.value }.thenBy { it.key })
                    .map { it.key }
            }

            return inputList.filter { it.title.lowercase().contains(searchTerm) }
        }
    }
}
