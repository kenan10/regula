package com.example.regula.tools

import android.hardware.SensorManager
import kotlin.math.pow

class SpacePoint(var pitch: Float, var azimuth: Float) {
    fun isEmpty(): Boolean {
        return azimuth != 0f && pitch != 0f
    }

    companion object Factory {
        fun emptyPoint(): SpacePoint {
            return SpacePoint(0f, 0f)
        }

        /**
         * Factory method to create [SpacePoint] from accelerometer and magnetometer sensors
         * values
         * @return Newly created SpacePoint
         */
        fun fromSensorsValues(
            accelerometerValue: List<Float>, magnetometerValue: List<Float>
        ): SpacePoint {
            val rotationMatrix = FloatArray(9)
            val adjustedRotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                accelerometerValue.toFloatArray(),
                magnetometerValue.toFloatArray()
            )
            SensorManager.remapCoordinateSystem(
                rotationMatrix,
                SensorManager.AXIS_Z, SensorManager.AXIS_X,
                adjustedRotationMatrix
            )
            val orientation = FloatArray(3)
            SensorManager.getOrientation(adjustedRotationMatrix, orientation)
            val azimuth = orientation[0]
            val pitch = orientation[1]
            return SpacePoint(pitch, azimuth)
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
        return deviation > (this.azimuth - center.azimuth).pow(2) +
                (this.pitch - center.pitch).pow(2)
    }
}