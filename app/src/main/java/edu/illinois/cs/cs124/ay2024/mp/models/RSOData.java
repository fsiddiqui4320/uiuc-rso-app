package edu.illinois.cs.cs124.ay2024.mp.models;

import androidx.annotation.NonNull;

/**
 * Model used to deserialize RSO data from the rsos.json file.
 *
 * <p>A Java record is a good fit for this model because the data is only loaded into the model and
 * the values are never updated. Note that the field names must match the JSON data, which has the
 * following shape:
 *
 * <pre>{@code
 * {
 *     "id": "KhnX6tgz-M4dcS4hBPzN_himatu",
 *     "title": "Hiking Club",
 *     "categories": "Blue Student Organization - Athletic & Recreation...",
 *     "website": "https://one.illinois.edu/HikingClub/",
 *     "mission": "The primary mission of the Hiking Club is to foster appreciation of ..."
 * }
 * }</pre>
 *
 * @param id the RSO's unique id
 * @param title the RSO's title
 * @param categories a string containing the color classification and any additional category
 *     information
 * @param website a link to the RSO's website
 * @param mission the RSO's mission statement
 */
public record RSOData(
    @NonNull String id,
    @NonNull String title,
    @NonNull String categories,
    @NonNull String website,
    @NonNull String mission) {}
