package com.example.regula.presentation.poi_viewer

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.icu.text.SimpleDateFormat
import android.os.Environment
import android.os.Handler
import android.os.Looper
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

const val BUFFER_SIZE = 30
const val DEVIATION = 0.02f
const val UPDATE_RATE: Long = 200

@HiltViewModel
class PoiViewerViewModel @Inject constructor(
    @Named("accelerometer") private val accelerometer: MeasurableSensor,
    @Named("magnetometer") private val magnetometer: MeasurableSensor,
    private val appContext: Application,
    private val userPointRepository: PointsRepository,
) : ViewModel() {
    var radius by mutableStateOf(0f)
    var newRadius by mutableStateOf(0f)
    var currentPointName by mutableStateOf("")
    var userPoints by mutableStateOf(emptyList<Poi>())
    var accelerometerShowedValue by mutableStateOf("")
    var magnetometerShowedValue by mutableStateOf("")
    var isReady by mutableStateOf(false)
    var isDialogOpened by mutableStateOf(false)
    var newPointName by mutableStateOf("")
    var angles by mutableStateOf("")
    var isInCircleDistance by mutableStateOf("")
    var showDetails by mutableStateOf(false)
    var isMenuOpened by mutableStateOf(false)

    private val handler = Handler(Looper.getMainLooper())

    private var accelerometerValue: List<Float> = emptyList()
    private var magnetometerValue: List<Float> = emptyList()
    private var accelerometerRecentValues: MutableList<List<Float>> =
        emptyList<List<Float>>().toMutableList()
    private var magnetometerRecentValues: MutableList<List<Float>> =
        emptyList<List<Float>>().toMutableList()

    init {
        accelerometer.startListening()
        magnetometer.startListening()
        accelerometer.setOnSensorValuesChangedListener { values ->
            if (!isDialogOpened) {
                val runnable = Runnable {
                    accelerometerValue = if (accelerometerRecentValues.size == BUFFER_SIZE)
                        getAverageOf2dFloatList(accelerometerRecentValues)
                    else values

                    accelerometerRecentValues += if (accelerometerRecentValues.size < BUFFER_SIZE) {
                        values
                    } else {
                        accelerometerRecentValues.removeFirst()
                        values
                    }

                    identifyPoint()
                    isReady = abs(0 - accelerometerValue[1]) < 0.4

                    if (accelerometerValue.isNotEmpty() && magnetometerValue.isNotEmpty() && showDetails) {
                        val spacePoint =
                            SpacePoint.fromSensorsValues(accelerometerValue, magnetometerValue)
                        val angle =
                            "a: ${spacePoint.pitch} b: ${spacePoint.azimuth}"
                        angles = angle

                        val sensorValue = appContext.resources.getString(
                            R.string.sensor_value,
                            "Accelerometer",
                            accelerometerValue[0].format(3),
                            accelerometerValue[1].format(3),
                            accelerometerValue[2].format(3)
                        )
                        accelerometerShowedValue = sensorValue
                    }
                }

                handler.postDelayed({
                    runnable.run()
                    handler.removeCallbacks(runnable)
                }, UPDATE_RATE)
            }
        }
        magnetometer.setOnSensorValuesChangedListener { values ->
            if (!isDialogOpened) {
                val runnable = Runnable {
                    magnetometerValue = if (magnetometerRecentValues.size == BUFFER_SIZE)
                        getAverageOf2dFloatList(magnetometerRecentValues)
                    else values

                    magnetometerRecentValues += if (magnetometerRecentValues.size < BUFFER_SIZE) {
                        values
                    } else {
                        magnetometerRecentValues.removeFirst()
                        values
                    }

                    identifyPoint()

                    if (accelerometerValue.isNotEmpty() && magnetometerValue.isNotEmpty() && showDetails) {
                        val spacePoint =
                            SpacePoint.fromSensorsValues(accelerometerValue, magnetometerValue)
                        val angle =
                            "a: ${spacePoint.pitch} b: ${spacePoint.azimuth}"
                        angles = angle
                        val sensorValue = appContext.resources.getString(
                            R.string.sensor_value,
                            "Magnetometer",
                            magnetometerValue[0].format(3),
                            magnetometerValue[1].format(3),
                            magnetometerValue[2].format(3)
                        )
                        magnetometerShowedValue = sensorValue
                    }
                }

                handler.postDelayed({
                    runnable.run()
                    handler.removeCallbacks(runnable)
                }, UPDATE_RATE)
            }
        }
    }

    private fun identifyPoint() {
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
                radius = currentPoint.visualSize
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

    private fun getAverageOf2dFloatList(list: List<List<Float>>): List<Float> {
        val sum = FloatArray(3)
        sum[0] = 0.0f
        sum[1] = 0.0f
        sum[2] = 0.0f

        for (item: List<Float> in list) {
            sum[0] += item[0]
            sum[1] += item[1]
            sum[2] += item[2]
        }

        return listOf(sum[0] / BUFFER_SIZE, sum[1] / BUFFER_SIZE, sum[2] / BUFFER_SIZE)
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
                    deviation = DEVIATION,
                    viewingPointId = 1,
                    visualSize = newRadius
                )
            )
            userPoints = userPointRepository.getAllPois()
            println(userPoints)
        }
    }

    private fun Float.format(digits: Int) = "%.${digits}f".format(this)
}