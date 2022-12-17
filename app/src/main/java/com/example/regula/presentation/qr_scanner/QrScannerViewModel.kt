package com.example.regula.presentation.qr_scanner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class QrScannerViewModel : ViewModel() {
    var code by mutableStateOf("")
}