package com.example.regula.presentation.pois_manager

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.regula.Constants
import com.example.regula.R
import com.example.regula.domain.model.Poi
import com.example.regula.tools.SpacePoint
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(
    ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun PoiDialog(
    showDialog: Boolean,
    title: String,
    poi: Poi,
    onClose: () -> Unit,
    onSave: (newPoi: Poi) -> Unit
) {
    val poiDialogState = remember { PoiDialogState() }
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA)
    )

    LaunchedEffect(poi) {
        poiDialogState.readPoi(poi)
    }

    if (showDialog) {
        Dialog(
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true
            ),
            onDismissRequest = onClose
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onClose) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_close_24),
                                contentDescription = "Cancel"
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Left,
                        )
                    }
                    TextButton(onClick = { onSave(poiDialogState.toPoi()) }) {
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    OutlinedTextField(
                        value = poiDialogState.name,
                        modifier = Modifier.fillMaxWidth(),
                        onValueChange = { newName: String -> poiDialogState.name = newName },
                        placeholder = { Text(text = "Name") }
                    )
                    Column {
                        Column {
                            Text(text = "Deviation: ${poiDialogState.deviation}")
                            Slider(
                                value = poiDialogState.deviation,
                                onValueChange = { newDeviation ->
                                    poiDialogState.deviation = newDeviation
                                },
                                valueRange = Constants.MIN_DEVIATION..Constants.MAX_DEVIATION
                            )
                        }
                        Column {
                            Text(text = "Visual size: ${poiDialogState.visualSize}")
                            Slider(
                                value = poiDialogState.visualSize,
                                onValueChange = { newVisualSize ->
                                    poiDialogState.visualSize = newVisualSize
                                },
                                valueRange = Constants.MIN_ON_SCREEN_SIZE..Constants.MAX_ON_SCREEN_SIZE
                            )
                        }
                    }
                    Column {
                        Button(
                            onClick = { permissionsState.launchMultiplePermissionRequest() },
                            Modifier.widthIn(min = 100.dp)
                        ) {
                            Text(text = "Bind object")
                        }
                        if (poiDialogState.azimuth != 0f && poiDialogState.pitch != 0f) {
                            Text(text = "Pitch: ${poiDialogState.pitch}")
                            Text(text = "Azimuth: ${poiDialogState.azimuth}")
                            Text(text = "Distance: ${poiDialogState.distance}")
                        }
                    }
                }
            }
        }
    }
}

class PoiDialogState() {
    var name by mutableStateOf("")
    var azimuth by mutableStateOf(0f)
    var pitch by mutableStateOf(0f)
    var visualSize by mutableStateOf(0f)
    var deviation by mutableStateOf(0f)
    var distance by mutableStateOf(0f)

    fun readPoi(poi: Poi) {
        this.name = poi.name
        this.azimuth = poi.point.azimuth
        this.pitch = poi.point.pitch
        this.visualSize = poi.visualSize
        this.deviation = poi.deviation
        this.distance = poi.distance
    }

    fun toPoi(): Poi {
        return Poi(
            name = name,
            viewingPointId = Poi.DEFAULT_VIEWING_POINT_ID,
            point = SpacePoint(azimuth = azimuth, pitch = pitch),
            deviation = deviation,
            visualSize = visualSize
        )
    }
}