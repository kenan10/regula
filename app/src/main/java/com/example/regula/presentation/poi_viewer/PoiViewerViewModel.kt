package com.example.regula.presentation.poi_viewer

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.icu.text.SimpleDateFormat
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.regula.R
import com.example.regula.domain.model.Poi
import com.example.regula.domain.repository.PointsRepository
import com.example.regula.sensors.MeasurableSensor
import com.example.regula.util.SpacePoint
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.g0dkar.qrcode.QRCode
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.abs


@HiltViewModel
class PoiViewerViewModel @Inject constructor(
    @Named("accelerometer") private val accelerometer: MeasurableSensor,
    @Named("magnetometer") private val magnetometer: MeasurableSensor,
    private val appContext: Application,
    private val userPointRepository: PointsRepository,
) : ViewModel() {
    var radius by mutableStateOf(0f)
    var currentPointName by mutableStateOf("")
    var userPoints by mutableStateOf(emptyList<Poi>())
    var accelerometerShowedValue by mutableStateOf("")
    var magnetometerShowedValue by mutableStateOf("")
    var isReady by mutableStateOf(false)
    var isDialogOpened by mutableStateOf(false)
    var newPointName by mutableStateOf("")
    var deviation by mutableStateOf("")
    var angles by mutableStateOf("")
    var isInCircleDistance by mutableStateOf("")
    var showDetails by mutableStateOf(false)
    var isMenuOpened by mutableStateOf(false)

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
                if (accelerometerValue.isNotEmpty() && magnetometerValue.isNotEmpty()) {
                    val spacePoint =
                        SpacePoint.fromSensorsValues(accelerometerValue, magnetometerValue)
                    val angle =
                        "a: ${spacePoint.accelerometerAngle} b: ${spacePoint.magnetometerAngle}"
                    angles = angle
                    findOutPoint()
                }
                magnetometerValue = values
                val sensorValue = appContext.resources.getString(
                    R.string.sensor_value,
                    "Magnetometer",
                    values[0].format(3),
                    values[1].format(3),
                    values[2].format(3)
                )
                magnetometerShowedValue = sensorValue
            }
        }
    }

    private fun findOutPoint() {
        if (accelerometerValue.isNotEmpty() && magnetometerValue.isNotEmpty() && isReady) {
            val currentPointSpace =
                SpacePoint.fromSensorsValues(accelerometerValue, magnetometerValue)

            viewModelScope.launch {
                userPoints = userPointRepository.getAllPois()
            }
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
            currentPointName = "unknown"
            radius = 0f
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun saveQrWithPoisInfo() {
        val simpleDateFormat = SimpleDateFormat("yyyymmsshhmmss")
        val date = simpleDateFormat.format(Date())
        var finalCompactString = ""
        userPoints.forEach {
            finalCompactString += it.toCompactString()
        }

        val resolver: ContentResolver = appContext.contentResolver
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "image_$date.jpg")
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        contentValues.put(
            MediaStore.MediaColumns.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES + File.separator + "TestFolder"
        )
        val imageUri =
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        resolver.openOutputStream(imageUri!!).use {
            QRCode(finalCompactString)
                .render(margin = 25)
                .writeImage(it!!)
        }
        Toast.makeText(appContext, "QR code saved", Toast.LENGTH_SHORT).show()
    }

    fun deleteAllPoints() {
        viewModelScope.launch {
            userPointRepository.deleteAllPois()
            userPoints = emptyList()
        }
    }

    fun saveCurrentObject() {
        viewModelScope.launch {
            userPointRepository.insertPoi(
                Poi(
                    name = newPointName,
                    point = SpacePoint.fromSensorsValues(accelerometerValue, magnetometerValue),
                    deviation = deviation.toFloat(),
                    viewingPointId = 1
                )
            )
            userPoints = userPointRepository.getAllPois()
            println(userPoints)
        }
    }

    private fun Float.format(digits: Int) = "%.${digits}f".format(this)
}