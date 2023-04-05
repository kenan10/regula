package com.example.regula.presentation.pois_manager

import android.Manifest
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.regula.R
import com.example.regula.domain.model.Poi
import com.example.regula.poilistitem.PoiListItem
import com.example.regula.presentation.destinations.PoiViewerScreenDestination
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
@Destination
@RootNavGraph(start = true)
fun PoisManagerScreen(
    navigator: DestinationsNavigator, viewModel: PoisManagerViewModel = hiltViewModel()
) {
    val activity = LocalContext.current as Activity
    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    val scanQrCodeLauncher = rememberLauncherForActivityResult(ScanQRCode()) { result ->
        if (result is QRResult.QRSuccess) {
            viewModel.readQRCodeData(result.content.rawValue)
        }
    }
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA)
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(true) {
        viewModel.updatePois()
    }

    PoiDialog(
        showDialog = viewModel.showPoiDialog,
        onClose = { viewModel.showPoiDialog = false },
        title = viewModel.dialogTitle,
        poi = viewModel.dialogPoi,
        onSave = { newPoi: Poi ->
            coroutineScope.launch {
                viewModel.updatePoi(
                    viewModel.dialogPoi,
                    newPoi
                )
                viewModel.showPoiDialog = false
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.padding(vertical = 20.dp, horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(text = "POIs Manager", style = MaterialTheme.typography.headlineLarge)
            Column {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(viewModel.pois) { poi ->
                        PoiListItem(titleTextContent = poi.name,
                            onDeleteBtnTapped = { coroutineScope.launch { viewModel.deletePoi(poi) } },
                            onEditBtnTapped = { viewModel.editPoi(poi) })
                    }
                }
            }
        }

        BottomAppBar {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    IconButton(onClick = { viewModel.exportQRCode() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_download_24),
                            contentDescription = "Export QR-code"
                        )
                    }
                    IconButton(onClick = {
                        permissionsState.launchMultiplePermissionRequest()
                        scanQrCodeLauncher.launch(null)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_qr_code_scanner_24),
                            contentDescription = "Scan the QR-code"
                        )
                    }
                    IconButton(onClick = { viewModel.showDialog("Create POI") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_add_24),
                            contentDescription = "Add new POI"
                        )
                    }
                }
                Row {
                    FloatingActionButton(
                        onClick = {
                            permissionsState.launchMultiplePermissionRequest()
                            navigator.navigate(PoiViewerScreenDestination())
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraSmall,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(Icons.Default.ArrowForward, "Move to viewing")
                    }
                }
            }
        }
    }
}