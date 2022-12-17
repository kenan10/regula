package com.example.regula.presentation.qr_scanner

import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import com.example.regula.presentation.common.PermissionsRequest
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode

@Composable
@Destination(start = true)
fun QrScannerScreen(
    navigator: DestinationsNavigator, viewModel: QrScannerViewModel = hiltViewModel()
) {
    PermissionsRequest()
    val scanQrCodeLauncher = rememberLauncherForActivityResult(ScanQRCode()) { result ->
        viewModel.code = when (result) {
            is QRResult.QRSuccess -> result.content.rawValue
            QRResult.QRUserCanceled -> "User canceled"
            QRResult.QRMissingPermission -> "Missing permission"
            is QRResult.QRError -> "${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}"
        }
    }
    LaunchedEffect(key1 = null) {
        scanQrCodeLauncher.launch(null)
    }
    Text(text = viewModel.code)
}

