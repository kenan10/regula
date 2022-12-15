package com.example.regula.domain.model

import com.example.regula.util.SpacePoint

data class AddPoi(
    val name: String,
    val viewingPointId: Int,
    val accelerometerValue: List<Float>,
    val magnetometerValue: List<Float>,
    val deviation: Float
)

data class Poi(
    val name: String,
    val viewingPointId: Int,
    val point: SpacePoint,
    val deviation: Float
)
