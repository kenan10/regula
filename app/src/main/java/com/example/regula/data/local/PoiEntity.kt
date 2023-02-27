package com.example.regula.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PoiEntity(
    @PrimaryKey val id: Int? = null,
    val name: String,
    val viewingPointId: Int,
    val accelerometerAngle: Float,
    val magnetometerAngle: Float,
    val deviation: Float,
    val visualSize: Float,
    val distance: Float
)
