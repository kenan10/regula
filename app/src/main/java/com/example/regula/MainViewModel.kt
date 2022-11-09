package com.example.regula

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.regula.domain.model.AddUserPoint
import com.example.regula.domain.model.UserPoint
import com.example.regula.domain.repository.UserPointRepository
import com.example.regula.sensors.MeasurableSensor
import com.example.regula.util.SpacePoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.abs

@HiltViewModel
class MainViewModel @Inject constructor(
    @Named("accelerometer") private val accelerometer: MeasurableSensor,
    @Named("magnetometer") private val magnetometer: MeasurableSensor,
    private val appContext: Application,
    private val userPointRepository: UserPointRepository,
) : ViewModel() {
    var currentPointName by mutableStateOf("")
    var userPoints by mutableStateOf(emptyList<UserPoint>())
    var accelerometerShowedValue by mutableStateOf("")
    var magnetometerShowedValue by mutableStateOf("")
    var isReady by mutableStateOf(false)
    var isDialogOpened by mutableStateOf(false)
    var newPointName by mutableStateOf("")
    var deviation by mutableStateOf(0f)

    private var accelerometerValue: List<Float> = emptyList()
    private var magnetometerValue: List<Float> = emptyList()

    init {
        accelerometer.startListening()
        magnetometer.startListening()
        accelerometer.setOnSensorValuesChangedListener { values ->
            accelerometerValue = values
            val sensorValue = appContext.resources.getString(
                R.string.sensor_value,
                "Accelerometer",
                values[0].format(3),
                values[1].format(3),
                values[2].format(3)
            )
            accelerometerShowedValue = sensorValue
            findOutPointName()

            isReady = abs(0 - values[1]) < 0.4
        }
        magnetometer.setOnSensorValuesChangedListener { values ->
            magnetometerValue = values
            val sensorValue = appContext.resources.getString(
                R.string.sensor_value,
                "Magnetometer",
                values[0].format(3),
                values[1].format(3),
                values[2].format(3)
            )
            magnetometerShowedValue = sensorValue
            findOutPointName()
        }
    }

    private fun findOutPointName() {
        if (accelerometerValue.isNotEmpty() && magnetometerValue.isNotEmpty() && isReady) {
            val currentPoint = SpacePoint.fromSensorsValues(accelerometerValue, magnetometerValue)

            currentPointName = userPoints.find {
                currentPoint.isInCircle(
                    center = it.point,
                    deviation = it.deviation
                )
            }?.name.toString()
            println(currentPointName)
        } else {
            currentPointName = "null"
        }
    }

    fun saveCurrentObject() {
        viewModelScope.launch {
            userPointRepository.insertUserPoint(
                AddUserPoint(
                    name = newPointName,
                    accelerometerValue = accelerometerValue,
                    magnetometerValue = magnetometerValue,
                    deviation = deviation
                )
            )
            userPoints = userPointRepository.getAllUserPoints()
            println(userPoints)
        }
    }

    private fun Float.format(digits: Int) = "%.${digits}f".format(this)
}