package com.example.regula.domain.model

import com.example.regula.util.SpacePoint

data class Poi(
    val name: String,
    val viewingPointId: Int,
    val point: SpacePoint,
    val deviation: Float
)
