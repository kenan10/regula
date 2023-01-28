package com.example.regula.util

import kotlin.math.asin
import kotlin.math.pow
import kotlin.math.sqrt

class SpacePoint(val accelerometerAngle: Float, val magnetometerAngle: Float) {
    companion object Factory {
        /**
         * Factory method to create [SpacePoint] from accelerometer and magnetometer sensors
         * values
         * @return Newly created SpacePoint
         */
        fun fromSensorsValues(
            accelerometerValue: List<Float>, magnetometerValue: List<Float>
        ): SpacePoint {
            val accelerometerAngle = asin(
                accelerometerValue[0] / (sqrt(
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

    /**
     * Used to check is instance in circle with another [SpacePoint] as center
     *
     * @param deviation radius of circle
     * @param center point which will be used as a center
     * @return whether true or false if instance is in circle or not
     */
    fun isInCircle(deviation: Float, center: SpacePoint): Boolean {
        return deviation > (this.magnetometerAngle - center.magnetometerAngle).pow(2) +
                (this.accelerometerAngle - center.accelerometerAngle).pow(2)
    }
}