package com.example.regula

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.regula.sensors.MeasurableSensor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class MainUiState(
    var accelerometerValue: List<Float>? = null,
    var magnetometerValue: List<Float>? = null,
    val isReady: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor (
    private val accelerometer: MeasurableSensor,
    private val magnetometer: MeasurableSensor
) : ViewModel() {
    val uiState by mutableStateOf(MainUiState())

    init {
        accelerometer.startListening()
        magnetometer.startListening()
        accelerometer.setOnSensorValuesChangedListener { values ->
            uiState.accelerometerValue = values
        }
        magnetometer.setOnSensorValuesChangedListener { values ->
            uiState.magnetometerValue = values
        }
    }
}