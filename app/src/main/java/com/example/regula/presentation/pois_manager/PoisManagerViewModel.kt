package com.example.regula.presentation.pois_manager

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
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.g0dkar.qrcode.QRCode
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PoisManagerViewModel @Inject constructor(private val appContext: Application, private val userPointRepository: PointsRepository) :
    ViewModel() {
    var pois by mutableStateOf(emptyList<Poi>())
    var showPoiDialog by mutableStateOf(false)
    var dialogTitle by mutableStateOf("Create POI")

    var dialogPoi = Poi.emptyPoi()

    fun readQRCodeData(compactString: String) {
        pois = pois.plus(Poi.fromCompactString(compactString))
        viewModelScope.launch {
            pois.forEach {
                userPointRepository.insertPoi(it)
            }
        }
    }

    suspend fun updatePoi(oldPoi: Poi, newPoi: Poi) {
        deletePoi(oldPoi)
        userPointRepository.insertPoi(newPoi)
        updatePois()
    }

    suspend fun updatePois() {
        pois = userPointRepository.getAllPois()
    }

    fun showDialog(title: String) {
        dialogTitle = title
        showPoiDialog = true
    }

    suspend fun deletePoi(poi: Poi) {
        userPointRepository.deletePoi(name = poi.name)
        updatePois()
    }

    suspend fun deleteAll() {
        userPointRepository.deleteAllPois()
    }

    fun editPoi(poi: Poi) {
        dialogPoi = poi
        showDialog("Edit")
    }

    fun exportQRCode() {
        val simpleDateFormat = SimpleDateFormat("yyyymmsshhmmss", Locale.getDefault())
        val date = simpleDateFormat.format(Date())
        var finalCompactString = ""
        pois.forEach {
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
        resolver.openOutputStream(imageUri!!).use {
            QRCode(finalCompactString)
                .render(margin = 25)
                .writeImage(it!!)
        }
        Toast.makeText(appContext, "QR code saved", Toast.LENGTH_SHORT).show()
    }
}