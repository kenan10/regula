package com.example.regula.tools

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.abs

class OrientationSensor(context: Context) : SensorEventListener {
    interface OrientationListener {
        fun onNewOrientation(azimuth: Float, pitch: Float, roll: Float)
    }

    private var listener: OrientationListener? = null
    private val sensorManager: SensorManager
    private val gsensor: Sensor
    private val msensor: Sensor
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val R = FloatArray(9)
    private val adjustedR = FloatArray(9)
    private val I = FloatArray(9)
    private var azimuth = 0f
    private var pitch = 0f
    private var roll = 0f

    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    fun start() {
        sensorManager.registerListener(
            this, gsensor,
            SensorManager.SENSOR_DELAY_GAME
        )
        sensorManager.registerListener(
            this, msensor,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    fun setListener(l: OrientationListener?) {
        listener = l
    }

    override fun onSensorChanged(event: SensorEvent) {
        val alpha = 0.8f
        synchronized(this) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]
            }
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * event.values[0]
                geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * event.values[1]
                geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * event.values[2]
            }
            val success = SensorManager.getRotationMatrix(
                R, I, gravity,
                geomagnetic
            )
            if (success) {
                val orientation = FloatArray(3)

                SensorManager.remapCoordinateSystem(
                    R,
                    SensorManager.AXIS_X, SensorManager.AXIS_Y,
                    adjustedR
                )
                SensorManager.getOrientation(adjustedR, orientation)
                pitch = abs(orientation[2])

                SensorManager.remapCoordinateSystem(
                    R,
                    SensorManager.AXIS_Z, SensorManager.AXIS_X,
                    adjustedR
                )
                SensorManager.getOrientation(adjustedR, orientation)
                azimuth = orientation[0]
                roll = orientation[2]

                if (listener != null) {
                    listener!!.onNewOrientation(azimuth, pitch, roll)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    companion object {
        private const val TAG = "Compass"
    }
}