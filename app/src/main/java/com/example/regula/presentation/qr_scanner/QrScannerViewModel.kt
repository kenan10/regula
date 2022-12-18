package com.example.regula.presentation.qr_scanner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.regula.domain.model.Poi
import com.example.regula.domain.repository.PointsRepository
import com.example.regula.util.SpacePoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrScannerViewModel @Inject constructor(private val userPointRepository: PointsRepository) :
    ViewModel() {
    var qrCodeText by mutableStateOf("")
    var pois by mutableStateOf(emptyList<Poi>())

    init {
        viewModelScope.launch {
            userPointRepository.deleteAllPois()
        }
    }

    fun decodeText() {
        val subss = qrCodeText.split(";")

        for (i in subss.indices step 4) {
            val label = subss[i]
            val accelerometerValue =
                if (subss[i + 1][0].toString() == "-") "-" + "0." + subss[i + 1].slice(1 until subss[i + 1].length)
                else "0." + subss[i + 1].slice(0 until subss[i + 1].length)
            val magnetometerValue =
                if (subss[i + 2][0].toString() == "-") "-" + "0." + subss[i + 2].slice(1 until subss[i + 2].length)
                else "0." + subss[i + 2].slice(0 until subss[i + 2].length)
            val deviation = "0.0" + subss[i + 3]
            val spacePoint = SpacePoint(accelerometerValue.toFloat(), magnetometerValue.toFloat())
            val newPoi = Poi(
                name = label,
                viewingPointId = 1,
                point = spacePoint,
                deviation = deviation.toFloat()
            )

            pois = pois + newPoi
            viewModelScope.launch {
                userPointRepository.insertPoi(newPoi)
            }
        }
    }

}