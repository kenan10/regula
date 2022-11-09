package com.example.regula.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserPointEntity(
    @PrimaryKey val id: Int? = null,
    val name: String,
    val accelerometerAngle: Float,
    val magnetometerAngle: Float,
    val deviation: Float
)
