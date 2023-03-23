package com.example.regula.presentation.poi_viewer

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.icu.text.SimpleDateFormat
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
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

const val DEVIATION = 0.0349f
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
    var indicatorsToDisplay by mutableStateOf(emptyMap<String, Float>())
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
    var correctionDistance by mutableStateOf(0f)

    /*
     Create separate function for updating point from db, where applying correction
     dont do update every tick, but only after adding a point, etc.
     */

    init {
        setupOrientationSensor()
        orientationSensor.start()
        updatePOIsInfo()
    }

    private fun setupOrientationSensor() {
        val cl: OrientationSensor.OrientationListener = getOrientationSensorListener()
        orientationSensor.setListener(cl)
    }

    private fun getOrientationSensorListener(): OrientationSensor.OrientationListener {
        return object : OrientationSensor.OrientationListener {
            override fun onNewOrientation(azimuth: Float, pitch: Float, roll: Float) {
                currentSpacePoint = SpacePoint(pitch, azimuth)
                identifyPoint()

                var pitchInDegrees = Math.toDegrees(pitch.toDouble()).toFloat()
                pitchInDegrees = (pitchInDegrees + 180) % 180
                var azimuthInDegrees = Math.toDegrees(pitch.toDouble()).toFloat()
                azimuthInDegrees = (azimuthInDegrees + 360) % 360
                var rollInDegrees = Math.toDegrees(pitch.toDouble()).toFloat()
                rollInDegrees = (rollInDegrees + 360) % 360
                indicatorsToDisplay = mapOf(
                    "pitch" to pitch,
                    "azimuth" to azimuthInDegrees,
                    "roll" to rollInDegrees
                )

                distance = abs((tan(currentSpacePoint.pitch) * USER_HEIGHT))
            }
        }
    }

    private fun identifyPoint() {
        if (isReady) {
            val currentPoint = userPoints.find {
                Log.println(Log.DEBUG, "TAG", "Pitch: ${it.point.pitch}")
                Log.println(Log.DEBUG, "TAG", "Azimuth: ${it.point.azimuth}")
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

    fun updatePOIsInfo() {
        viewModelScope.launch {
            userPoints = userPointRepository.getAllPois()
        }.invokeOnCompletion {
            // Toast.makeText(appContext, "UPDATED", Toast.LENGTH_SHORT).show()
            if (correctionDistance > 0) {
                // Toast.makeText(appContext, "CORRECTED", Toast.LENGTH_SHORT).show()
                userPoints.forEach {
                    // Toast.makeText(appContext, "Previous ${it.point.pitch}", Toast.LENGTH_SHORT).show()
                    /*
                    val a = it.point.azimuth
                    val d = correctionDistance
                    val s = it.distance
                    val b = angle between direction to North and new viewing point
                    val newAzimuth = acos(s*cos(a)+d*cos(b))
                     */
                    val newPitch =
                        atan((it.distance + correctionDistance) / (it.distance / tan(it.point.pitch)))
                    val newSpacePoint = SpacePoint(
                        newPitch,
                        it.point.azimuth
                    )
                    it.point = newSpacePoint
                    // viewModelScope.launch {
                    //    userPointRepository.setCoordinates(newSpacePoint, it.name)
                    // }
                    // Toast.makeText(appContext, "New $newPitch", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun resetAdjustment() {
        correctionDistance = 0f
        updatePOIsInfo()
    }

    fun recomputeAngles() {
        correctionDistance = distance
        updatePOIsInfo()
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
        }
        userPoints = emptyList()
    }

    fun saveCurrentObject() {
        val newPoi = Poi(
            name = newPointName,
            point = newPointSpacePoint,
            deviation = DEVIATION,
            viewingPointId = 1,
            visualSize = newRadius,
            distance = distanceToBase
        )
        userPoints = userPoints + newPoi
        viewModelScope.launch {
            userPointRepository.insertPoi(
                newPoi
            )
        }
    }
}
