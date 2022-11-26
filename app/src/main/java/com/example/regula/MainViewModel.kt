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
    var radius by mutableStateOf(0f)
    var currentPointName by mutableStateOf("")
    var userPoints by mutableStateOf(emptyList<UserPoint>())
    var accelerometerShowedValue by mutableStateOf("")
    var magnetometerShowedValue by mutableStateOf("")
    var isReady by mutableStateOf(false)
    var isDialogOpened by mutableStateOf(false)
    var newPointName by mutableStateOf("")
    var deviation by mutableStateOf("")
    var angles by mutableStateOf("")
    var isInCircleDistance by mutableStateOf("")

    private var accelerometerValue: List<Float> = emptyList()
    private var magnetometerValue: List<Float> = emptyList()

    init {
        accelerometer.startListening()
        magnetometer.startListening()
        accelerometer.setOnSensorValuesChangedListener { values ->
            if (!isDialogOpened) {
                if (accelerometerValue.isNotEmpty() && magnetometerValue.isNotEmpty()) {
                    val spacePoint =
                        SpacePoint.fromSensorsValues(accelerometerValue, magnetometerValue)
                    val angle =
                        "a: ${spacePoint.accelerometerAngle} b: ${spacePoint.magnetometerAngle}"
                    angles = angle
                    findOutPoint()
                }

                accelerometerValue = values
                val sensorValue = appContext.resources.getString(
                    R.string.sensor_value,
                    "Accelerometer",
                    values[0].format(3),
                    values[1].format(3),
                    values[2].format(3)
                )
                accelerometerShowedValue = sensorValue

                isReady = abs(0 - values[1]) < 0.4
            }
        }
        magnetometer.setOnSensorValuesChangedListener { values ->
            if (!isDialogOpened) {
                magnetometerValue = values
                val sensorValue = appContext.resources.getString(
                    R.string.sensor_value,
                    "Magnetometer",
                    values[0].format(3),
                    values[1].format(3),
                    values[2].format(3)
                )
                magnetometerShowedValue = sensorValue
                if (accelerometerValue.isNotEmpty() && magnetometerValue.isNotEmpty()) {
                    val spacePoint =
                        SpacePoint.fromSensorsValues(accelerometerValue, magnetometerValue)
                    val angle =
                        "a: ${spacePoint.accelerometerAngle} b: ${spacePoint.magnetometerAngle}"
                    angles = angle
                    findOutPoint()
                }
            }
        }
    }

    private fun findOutPoint() {
        if (accelerometerValue.isNotEmpty() && magnetometerValue.isNotEmpty() && isReady) {
            val currentPointSpace =
                SpacePoint.fromSensorsValues(accelerometerValue, magnetometerValue)

            val currentPoint = userPoints.find {
                currentPointSpace.isInCircle(
                    center = it.point, deviation = it.deviation
                )
            }
            if (currentPoint != null) {
                radius = currentPoint.deviation
                currentPointName = currentPoint.name
            } else {
                radius = 0f
                currentPointName = "unknown"
            }

        } else {
            currentPointName = "loading"
            radius = 0f
        }
    }

    fun deleteAllPoints() {
        viewModelScope.launch {
            userPointRepository.deleteAll()
            userPoints = emptyList()
        }
    }

    fun saveCurrentObject() {
        viewModelScope.launch {
            userPointRepository.insertUserPoint(
                AddUserPoint(
                    name = newPointName,
                    accelerometerValue = accelerometerValue,
                    magnetometerValue = magnetometerValue,
                    deviation = deviation.toFloat()
                )
            )
            userPoints = userPointRepository.getAllUserPoints()
            println(userPoints)
        }
    }

    private fun Float.format(digits: Int) = "%.${digits}f".format(this)
}