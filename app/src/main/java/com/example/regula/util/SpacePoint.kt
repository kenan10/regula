package com.example.regula.util

import kotlin.math.asin
import kotlin.math.pow
import kotlin.math.sqrt

class SpacePoint(val accelerometerAngle: Float, val magnetometerAngle: Float) {
    companion object Factory {
        fun fromSensorsValues(
            accelerometerValue: List<Float>, magnetometerValue: List<Float>
        ): SpacePoint {
            val accelerometerAngle = asin(
                accelerometerValue[2] / (sqrt(
                    accelerometerValue[2].pow(2) + accelerometerValue[0].pow(2)
                ))
            )
            val magnetometerAngle = asin(
                magnetometerValue[2] / (sqrt(
                    magnetometerValue[2].pow(2) + magnetometerValue[1].pow(2)
                ))
            )
            return SpacePoint(accelerometerAngle, magnetometerAngle)
        }
    }

    fun isInCircle(deviation: Float, center: SpacePoint): Boolean {
        return deviation > (this.magnetometerAngle - center.magnetometerAngle).pow(2) + (this.accelerometerAngle - center.accelerometerAngle).pow(
            2
        )
    }

    fun distanceTo(center: SpacePoint): Float {
        return (this.magnetometerAngle - center.magnetometerAngle).pow(2) + (this.accelerometerAngle - center.accelerometerAngle).pow(2)
    }
}