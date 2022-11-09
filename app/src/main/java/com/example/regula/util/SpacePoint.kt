package com.example.regula.util

import kotlin.math.atan
import kotlin.math.pow

class SpacePoint(val accelerometerAngle: Float, val magnetometerAngle: Float) {
    companion object Factory {
        fun fromSensorsValues(
            accelerometerValue: List<Float>, magnetometerValue: List<Float>
        ): SpacePoint {
            val accelerometerAngle = atan(accelerometerValue[2] / accelerometerValue[0])
            val magnetometerAngle = atan(magnetometerValue[2] / magnetometerValue[1])
            return SpacePoint(accelerometerAngle, magnetometerAngle)
        }
    }

    fun isInCircle(deviation: Float, center: SpacePoint): Boolean {
        return deviation.pow(2) <=
                (this.magnetometerAngle - center.magnetometerAngle).pow(2) +
                (this.accelerometerAngle - center.accelerometerAngle).pow(2)
    }
}