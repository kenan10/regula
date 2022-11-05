package com.example.regula

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.regula.sensors.MeasurableSensor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.abs

@HiltViewModel
class MainViewModel @Inject constructor(
    @Named("accelerometer") private val accelerometer: MeasurableSensor,
    @Named("magnetometer") private val magnetometer: MeasurableSensor,
    private val appContext: Application
) : ViewModel() {
    var accelerometerValue by mutableStateOf(String())
    var magnetometerValue by mutableStateOf(String())
    var isReady by mutableStateOf(false)

    private fun Float.format(digits: Int) = "%.${digits}f".format(this)

    init {
        accelerometer.startListening()
        magnetometer.startListening()
        accelerometer.setOnSensorValuesChangedListener { values ->
            val sensorValue =
                appContext.resources.getString(
                    R.string.sensor_value,
                    "Accelerometer",
                    values[0].format(3),
                    values[1].format(3),
                    values[2].format(3)
                )
            accelerometerValue = sensorValue

           isReady = abs(0 - values[1]) < 0.4
        }
        magnetometer.setOnSensorValuesChangedListener { values ->
            val sensorValue =
                appContext.resources.getString(
                    R.string.sensor_value,
                    "Magnetometer",
                    values[0].format(3),
                    values[1].format(3),
                    values[2].format(3)
                )
            magnetometerValue = sensorValue
        }
    }
}