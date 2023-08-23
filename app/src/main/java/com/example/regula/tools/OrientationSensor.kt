package com.example.regula.tools

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class OrientationSensor(context: Context) : SensorEventListener {
    interface OrientationListener {
        fun onNewOrientation(azimuth: Float, pitch: Float, roll: Float, geomagnetic: FloatArray, gravity: FloatArray, sensorsAccuracy: String)
    }

    private var listener: OrientationListener? = null
    private val sensorManager: SensorManager

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var azimuthReadings = listOf<Float>()
    private var pitch = 0f
    private var roll = 0f
    private var accuracyStatusStr = ""

    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    fun start() {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magnetometer ->
            sensorManager.registerListener(
                this,
                magnetometer,
                SensorManager.SENSOR_DELAY_GAME,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    fun setListener(l: OrientationListener?) {
        listener = l
    }

    override fun onSensorChanged(event: SensorEvent) {
        val alpha = 0.9f
        synchronized(this) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]
            }
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic[0] = event.values[0]
                geomagnetic[1] = event.values[1]
                geomagnetic[2] = event.values[2]
            }

            val R = FloatArray(9)
            val I = FloatArray(9)
            val adjustedR = FloatArray(9)
            val success = SensorManager.getRotationMatrix(
                R, I, gravity,
                geomagnetic
            )
            if (success) {
                val orientation = FloatArray(3)

                SensorManager.remapCoordinateSystem(
                    R,
                    SensorManager.AXIS_Z, SensorManager.AXIS_X,
                    adjustedR
                )
                SensorManager.getOrientation(adjustedR, orientation)
                if (azimuthReadings.size == READINGS_MEMORY_SIZE) {
                    azimuthReadings = azimuthReadings.drop(1)
                }
                azimuthReadings = azimuthReadings.plus(orientation[0])
                pitch = (orientation[1] + PI/2).toFloat()
                roll = orientation[2]

                listener?.onNewOrientation(averageAngle(azimuthReadings), pitch, roll, geomagnetic, gravity, accuracyStatusStr)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> accuracyStatusStr = "ACCURACY_HIGH"
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> accuracyStatusStr = "ACCURACY_LOW"
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> accuracyStatusStr = "ACCURACY_MEDIUM"
            SensorManager.SENSOR_STATUS_UNRELIABLE -> accuracyStatusStr = "UNRELIABLE"
        }
    }

    private fun averageAngle(terms: List<Float>): Float {
        val totalTerm = terms.size
        var sumSin = 0f
        var sumCos = 0f
        for (i in 0 until totalTerm) {
            sumSin += sin(terms[i].toDouble()).toFloat()
            sumCos += cos(terms[i].toDouble()).toFloat()
        }
        return atan2((sumSin / totalTerm).toDouble(), (sumCos / totalTerm).toDouble())
            .toFloat()
    }

    companion object {
        private const val TAG = "Compass"
        private const val READINGS_MEMORY_SIZE = 100
    }
}