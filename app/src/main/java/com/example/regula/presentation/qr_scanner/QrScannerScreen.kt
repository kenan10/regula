package com.example.regula.presentation.qr_scanner

import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.regula.presentation.common.PermissionsRequest
import com.example.regula.presentation.destinations.PoiViewerScreenDestination
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
        viewModel.qrCodeText = when (result) {
            is QRResult.QRSuccess -> result.content.rawValue
            QRResult.QRUserCanceled -> "User canceled"
            QRResult.QRMissingPermission -> "Missing permission"
            is QRResult.QRError -> "${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}"
        }
    }

    LaunchedEffect(key1 = viewModel.qrCodeText) {
        if (viewModel.qrCodeText.isNotEmpty()) viewModel.decodeText()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp), verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn {
            items(viewModel.pois) { poi -> PoiRow(poi) }
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            FloatingActionButton(onClick = {
                if (viewModel.pois.isEmpty()) scanQrCodeLauncher.launch(null)
                else navigator.navigate(PoiViewerScreenDestination())
            }) {
                Text(
                    text = if (viewModel.pois.isEmpty()) "+" else "→",
                    style = TextStyle(
                        fontSize = 27.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

