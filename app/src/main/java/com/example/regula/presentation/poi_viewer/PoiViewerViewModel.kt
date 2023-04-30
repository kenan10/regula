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
import com.example.regula.Constants
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

@HiltViewModel
class PoiViewerViewModel @Inject constructor(
    @Named("orientationSensor") private val orientationSensor: OrientationSensor,
    private val appContext: Application,
    private val userPointRepository: PointsRepository
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
    var correctionAzimuth by mutableStateOf(0f)

    private fun setupOrientationSensor() {
        val cl: OrientationSensor.OrientationListener = getOrientationSensorListener()
        orientationSensor.setListener(cl)
    }

    fun onStop() {
        orientationSensor.stop()
    }

    fun onStart() {
        setupOrientationSensor()
        orientationSensor.start()
        updatePOIsInfo()
    }

    private fun getOrientationSensorListener(): OrientationSensor.OrientationListener {
        return object : OrientationSensor.OrientationListener {
            override fun onNewOrientation(azimuth: Float, pitch: Float, roll: Float, geomagnetic: FloatArray, gravity: FloatArray) {
                identifyPoint()
                isReady = abs(gravity[1]) < 0.4f
                var pitchInDegrees = Math.toDegrees(pitch.toDouble()).toFloat()
                pitchInDegrees = (pitchInDegrees + 180) % 180
                var azimuthInDegrees = Math.toDegrees(azimuth.toDouble()).toFloat()
                azimuthInDegrees = (azimuthInDegrees + 360) % 360
                var rollInDegrees = Math.toDegrees(roll.toDouble()).toFloat()
                rollInDegrees = (rollInDegrees + 360) % 360
                currentSpacePoint = SpacePoint(pitch, azimuth)
                indicatorsToDisplay = mapOf(
                    "pitch" to pitchInDegrees,
                    "azimuth" to azimuthInDegrees,
                    "roll" to rollInDegrees
                )

                distance = abs((tan(currentSpacePoint.pitch) * Constants.USER_HEIGHT))
            }
        }
    }

    private fun identifyPoint() {
        if (isReady) {
            val currentPoint = userPoints.find {
                currentSpacePoint.isInCircle(
                    center = it.point, deviation = it.deviation
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

    private fun updatePOIsInfo() {
        viewModelScope.launch {
            userPoints = userPointRepository.getAllPois()
        }.invokeOnCompletion {
            // Toast.makeText(appContext, "UPDATED", Toast.LENGTH_SHORT).show()
            if (correctionDistance > 0) {
                // Toast.makeText(appContext, "CORRECTED", Toast.LENGTH_SHORT).show()
                userPoints.forEach {
                    Toast.makeText(appContext, "Previous ${it.point.azimuth}", Toast.LENGTH_SHORT)
                        .show()
                    val theta = it.point.azimuth
                    val d = correctionDistance
                    val s = it.distance
                    val beta = correctionAzimuth
                    Toast.makeText(appContext, "theta $theta", Toast.LENGTH_SHORT).show()
                    Toast.makeText(appContext, "d $d", Toast.LENGTH_SHORT).show()
                    Toast.makeText(appContext, "s $s", Toast.LENGTH_SHORT).show()
                    Toast.makeText(appContext, "beta $beta", Toast.LENGTH_SHORT).show()

                    // -ArcCos[-((d Cos[\[Beta]] + s Cos[\[Theta]])/Sqrt[d^2 + s^2 + 2 d s Cos[\[Beta] - \[Theta]]])]
                    val newAzimuth = if (beta > 0.2f) {
                        acos((d * cos(beta) + s * cos(theta)) / sqrt(d * d + s * s + 2 * d * s * cos(beta - theta)))
                    } else {
                        // -ArcCos[(d Cos[\[Beta]] + s Cos[\[Theta]])/Sqrt[d^2 + s^2 + 2 d s Cos[\[Beta] - \[Theta]]]]
                        -acos((d * cos(beta) + s * cos(theta)) / sqrt(d * d + s * s + 2 * d * s * cos(beta - theta)))
                    }
                    Toast.makeText(appContext, "newAzimuth $newAzimuth", Toast.LENGTH_SHORT).show()
                    val newPitch =
                        atan((it.distance + correctionDistance) / (it.distance / tan(it.point.pitch)))
                    val newSpacePoint = SpacePoint(
                        newPitch,
                        newAzimuth
                    )
                    it.point = newSpacePoint
//                    viewModelScope.launch {
//                        userPointRepository.setCoordinates(newSpacePoint, it.name)
//                    }
                    Toast.makeText(appContext, "New $newAzimuth", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun resetAdjustment() {
        correctionDistance = 0f
        correctionAzimuth = 0f
        updatePOIsInfo()
    }

    fun recomputeAngles() {
        correctionDistance = distance
        correctionAzimuth = currentSpacePoint.azimuth
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
            deviation = Constants.DEVIATION,
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
