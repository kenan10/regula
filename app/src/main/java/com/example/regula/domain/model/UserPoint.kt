package com.example.regula.domain.model

import com.example.regula.util.SpacePoint

data class AddUserPoint(
    val name: String,
    val accelerometerValue: List<Float>,
    val magnetometerValue: List<Float>,
    val deviation: Float
)

data class UserPoint(
    val name: String,
    val point: SpacePoint,
    val deviation: Float
)
