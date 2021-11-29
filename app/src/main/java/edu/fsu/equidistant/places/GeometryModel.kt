package edu.fsu.equidistant.places

import com.squareup.moshi.Json

data class GeometryModel(
    @field:Json(name = "location")
    val location: LocationModel?
)