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
import com.example.regula.domain.model.Poi
import com.example.regula.domain.repository.PointsRepository
import com.example.regula.tools.OrientationSensor
import com.example.regula.tools.SpacePoint
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.g0dkar.qrcode.QRCode
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.*

const val DEVIATION = 2.0f
const val USER_HEIGHT = 1.48f

@HiltViewModel
class PoiViewerViewModel @Inject constructor(
    @Named("orientationSensor") private val orientationSensor: OrientationSensor,
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
    var rollForDisplay by mutableStateOf(0f)
    var distance by mutableStateOf(0f)

    // UI state
    var isReady by mutableStateOf(true)
    var freezeSensors by mutableStateOf(false)
    var isDialogOpened by mutableStateOf(false)
    var showSetDistanceToBaseBtn by mutableStateOf(false)
    var showDetails by mutableStateOf(false)
    var isMenuOpened by mutableStateOf(false)

    // Used for computation and point identification
    private var userPoints = emptyList<Poi>()
    var currentSpacePoint = SpacePoint(0f, 0f)
    private var correctionDistance: Float = 0f

    init {
        setupOrientationSensor()
        orientationSensor.start()
    }

    private fun setupOrientationSensor() {
        val cl: OrientationSensor.OrientationListener = getOrientationSensorListener()
        orientationSensor.setListener(cl)
    }

    private fun getOrientationSensorListener(): OrientationSensor.OrientationListener {
        return object : OrientationSensor.OrientationListener {
            override fun onNewOrientation(azimuth: Float, pitch: Float, roll: Float) {
                currentSpacePoint = SpacePoint(pitch, azimuth)
                rollForDisplay = roll
                identifyPoint()
                distance = (tan(currentSpacePoint.pitch + (PI / 2)) * USER_HEIGHT).toFloat()
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
                currentPointName = ""
            }

        } else {
            currentPointName = ""
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