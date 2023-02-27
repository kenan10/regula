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
import kotlinx.coroutines.*
import kotlin.math.*

const val BUFFER_SIZE = 15
const val DEVIATION = 0.008f
const val UPDATE_DELAY: Long = 150
const val USER_HEIGHT = 1.48f

@HiltViewModel
class PoiViewerViewModel @Inject constructor(
    @Named("accelerometer") private val accelerometer: MeasurableSensor,
    @Named("magnetometer") private val magnetometer: MeasurableSensor,
    private val appContext: Application,
    private val userPointRepository: PointsRepository,
) : ViewModel() {
    // State for new point creation
    var newRadius by mutableStateOf(0f)
    var distanceToBase by mutableStateOf(0f)
    var newPointName by mutableStateOf("")
    private var newPointSpacePoint = SpacePoint(0f, 0f)

    // State of current point
    var radius by mutableStateOf(0f)
    var currentPointName by mutableStateOf("")

    // Details
    var distance by mutableStateOf(0f)

    // UI state
    var isReady by mutableStateOf(false)
    var freezeSensors by mutableStateOf(false)
    var isDialogOpened by mutableStateOf(false)
    var showSetDistanceToBaseBtn by mutableStateOf(false)
    var showDetails by mutableStateOf(false)
    var isMenuOpened by mutableStateOf(false)

    // Used for computation and point identification
    private var userPoints = emptyList<Poi>()
    var accelerometerValue: List<Float> = emptyList()
    private var accelerometerRecentValues = emptyList<List<Float>>().toMutableList()
    var magnetometerValue: List<Float> = emptyList()
    private var magnetometerRecentValues = emptyList<List<Float>>().toMutableList()
    var currentSpacePoint = SpacePoint(0f, 0f)
    private var correctionDistance: Float = 0f

    private val handler = Handler(Looper.getMainLooper())

    init {
        accelerometer.startListening()
        magnetometer.startListening()
        accelerometer.setOnSensorValuesChangedListener { values ->
            if (!freezeSensors) {
                val runnable = Runnable {
                    accelerometerValue = if (accelerometerRecentValues.size == BUFFER_SIZE)
                        getAverageOf2dFloatList(accelerometerRecentValues)
                    else values

                    accelerometerRecentValues += if (accelerometerRecentValues.size < BUFFER_SIZE)
                        values
                    else {
                        accelerometerRecentValues.removeFirst()
                        values
                    }

                    isReady = abs(0 - accelerometerValue[1]) < 0.4

                    if (accelerometerValue.isNotEmpty() && magnetometerValue.isNotEmpty()) {
                        currentSpacePoint = SpacePoint.fromSensorsValues(accelerometerValue, magnetometerValue)
                        identifyPoint()
                        distance = (tan(currentSpacePoint.pitch + (PI / 2)) * USER_HEIGHT).toFloat()
                    }
                }

                handler.postDelayed({
                    runnable.run()
                    handler.removeCallbacks(runnable)
                }, UPDATE_DELAY)
            }
        }
        magnetometer.setOnSensorValuesChangedListener { values ->
            if (!freezeSensors) {
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

                    if (accelerometerValue.isNotEmpty() && magnetometerValue.isNotEmpty()) {
                        identifyPoint()
                    }
                }

                handler.postDelayed({
                    runnable.run()
                    handler.removeCallbacks(runnable)
                }, UPDATE_DELAY)
            }
        }
    }

    private fun identifyPoint() {
        if (isReady) {
            viewModelScope.launch {
                userPoints = userPointRepository.getAllPois()
            }
            if (correctionDistance > 0f) {
                userPoints.forEach {
                    it.point.pitch =
                        atan((it.distance + correctionDistance) / (it.distance / tan(it.point.pitch)))
                }
            }
            val currentPoint = userPoints.find {
                currentSpacePoint.isInCircle(
                    center = it.point, deviation = DEVIATION
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

    fun recomputeAngles() {
        correctionDistance = distance
    }

    fun closeDialog() {
        freezeSensors = false
        isDialogOpened = false
    }

    fun freezeSensorsValues() {
        newPointSpacePoint = currentSpacePoint
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
                    point = newPointSpacePoint,
                    deviation = DEVIATION,
                    viewingPointId = 1,
                    visualSize = newRadius,
                    distance = distanceToBase
                )
            )
        }
    }
}