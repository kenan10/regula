package com.example.regula.presentation.poi_viewer

import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.io.ByteArrayOutputStream
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
    var newPointDeviation by mutableStateOf(Constants.DEVIATION)
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
            override fun onNewOrientation(
                azimuth: Float,
                pitch: Float,
                roll: Float,
                geomagnetic: FloatArray,
                gravity: FloatArray
            ) {
                identifyPoint()
                isReady = abs(gravity[1]) < 0.4f
                var pitchInDegrees = Math.toDegrees(pitch.toDouble()).toFloat()
                pitchInDegrees = (pitchInDegrees + 180) % 180
                var azimuthInDegrees = Math.toDegrees(azimuth.toDouble()).toFloat()
                azimuthInDegrees = (azimuthInDegrees + 360) % 360
                var rollInDegrees = Math.toDegrees(roll.toDouble()).toFloat()
                rollInDegrees = (rollInDegrees + 360) % 360
                currentSpacePoint = SpacePoint(pitchInDegrees, azimuthInDegrees)
                indicatorsToDisplay = mapOf(
                    "pitch" to pitchInDegrees,
                    "azimuth" to azimuthInDegrees,
                    "roll" to rollInDegrees
                )

                distance = abs(
                    (tan(Math.toRadians(currentSpacePoint.pitch.toDouble()))
                            * Constants.USER_HEIGHT)
                ).toFloat()
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
                    val theta = Math.toRadians(it.point.azimuth.toDouble())
                    val d = correctionDistance
                    val s = it.distance
                    val beta = Math.toRadians(correctionAzimuth.toDouble())
                    val phi = Math.toRadians(it.point.pitch.toDouble())
                    val theta1 = acos(
                        (d * cos(beta) + s * cos(theta)) / sqrt(
                            d * d + s * s + 2 * d * s * cos(beta - theta)
                        )
                    )
                    val theta2 = (2 * PI - theta1).toFloat()
                    Toast.makeText(
                        appContext,
                        "theta ${azimuthToDegrees(theta.toFloat())}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Toast.makeText(
                        appContext,
                        "theta1 ${azimuthToDegrees(theta1.toFloat())}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Toast.makeText(
                        appContext,
                        "theta2 ${azimuthToDegrees(theta2)}",
                        Toast.LENGTH_SHORT
                    ).show()
                    val newAzimuth =
                        if (abs(azimuthToDegrees(theta.toFloat()) - azimuthToDegrees(theta1.toFloat())) >
                            abs(azimuthToDegrees(theta.toFloat()) - azimuthToDegrees(theta2))
                        ) {
                            azimuthToDegrees(theta2)
                        } else {
                            azimuthToDegrees(theta1.toFloat())
                        }
                    val newPitch =
                        atan((s + d) / (s / tan(phi)))
                    val newSpacePoint = SpacePoint(
                        Math.toDegrees(newPitch).toFloat(),
                        newAzimuth
                    )
                    it.point = newSpacePoint
                    Toast.makeText(appContext, "newAzimuth ${it.point.azimuth}", Toast.LENGTH_SHORT)
                        .show()
                    // viewModelScope.launch {
                    //     userPointRepository.setCoordinates(newSpacePoint, it.name)
                    // }
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

    fun exportQRCode() {
        val simpleDateFormat = SimpleDateFormat("yyyymmsshhmmss", Locale.getDefault())
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
            Environment.DIRECTORY_PICTURES + File.separator + "RegulaQRs"
        )
        val imageUri =
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val imageBytes = ByteArrayOutputStream()
            .also { QRCode(finalCompactString).render(margin = 50).writeImage(it) }
            .toByteArray()

        var bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val ration = 1.52f
        val newHeight = bitmap.width * ration
        bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, newHeight.toInt(), false)

        resolver.openOutputStream(imageUri!!)
            .use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
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
            deviation = newPointDeviation,
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

    private fun azimuthToDegrees(azimuthRads: Float): Float {
        var azimuthInDegrees = Math.toDegrees(azimuthRads.toDouble()).toFloat()
        azimuthInDegrees = (azimuthInDegrees + 360) % 360
        return azimuthInDegrees
    }
}
