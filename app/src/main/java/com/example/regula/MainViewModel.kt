package com.example.regula

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.regula.sensors.MeasurableSensor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MainViewModel @Inject constructor(
    @Named("accelerometer") private val accelerometer: MeasurableSensor,
    @Named("magnetometer") private val magnetometer: MeasurableSensor
) : ViewModel() {
    var accelerometerValue by mutableStateOf(listOf<Float>())
    var magnetometerValue by mutableStateOf(listOf<Float>())
    val isReady by mutableStateOf(false)

    init {
        accelerometer.startListening()
        magnetometer.startListening()
        accelerometer.setOnSensorValuesChangedListener { values ->
            accelerometerValue = values
        }
        magnetometer.setOnSensorValuesChangedListener { values ->
            magnetometerValue = values
        }
    }
}