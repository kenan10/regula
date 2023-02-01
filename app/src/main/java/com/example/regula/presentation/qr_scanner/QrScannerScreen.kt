package com.example.regula.presentation.qr_scanner

import android.Manifest
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.regula.presentation.common.PermissionsRequest
import com.example.regula.presentation.destinations.PoiViewerScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode

@Composable
@Destination
@RootNavGraph(start = true)
fun QrScannerScreen(
    navigator: DestinationsNavigator, viewModel: QrScannerViewModel = hiltViewModel()
) {
    PermissionsRequest(listOf(Manifest.permission.CAMERA))
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { navigator.navigate(PoiViewerScreenDestination()) },
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Text(
                    text = "Skip",
                    color = Color.DarkGray
                )
            }
            FloatingActionButton(onClick = {
                if (viewModel.pois.isEmpty()) scanQrCodeLauncher.launch(null)
                else navigator.navigate(PoiViewerScreenDestination())
            }) {
                Icon(
                    if (viewModel.pois.isEmpty()) Icons.Default.Add else Icons.Default.ArrowForward,
                    contentDescription = "Action button"
                )
            }
        }
    }
}

