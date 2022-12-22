package com.example.regula.presentation.qr_scanner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.regula.domain.model.Poi
import com.example.regula.domain.repository.PointsRepository
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
        pois = Poi.fromCompactString(qrCodeText)
        viewModelScope.launch {
            pois.forEach {
                userPointRepository.insertPoi(it)
            }
        }
    }

}