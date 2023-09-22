package com.example.regula.presentation.pois_manager

import android.Manifest
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
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
    ExperimentalPermissionsApi::class, ExperimentalFoundationApi::class
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.Top
            ) {
                stickyHeader {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
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
                }
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        OutlinedTextField(
                            value = poiDialogState.name,
                            modifier = Modifier.fillMaxWidth(),
                            onValueChange = { newName: String -> poiDialogState.name = newName },
                            label = { Text(text = "Name") }
                        )
                        Column {
                            Column {
                                Text(text = "Deviation:")
                                OutlinedTextField(
                                    value = poiDialogState.deviation.toString(),
                                    modifier = Modifier.fillMaxWidth(),
                                    onValueChange = { newDeviation ->
                                        poiDialogState.deviation = newDeviation.toFloat()
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                Slider(
                                    value = poiDialogState.deviation,
                                    onValueChange = { newDeviation ->
                                        poiDialogState.deviation = newDeviation
                                    },
                                    valueRange = Constants.MIN_DEVIATION..Constants.MAX_DEVIATION
                                )
                            }
                            Column {
                                Text(text = "Visual size:")
                                OutlinedTextField(
                                    value = poiDialogState.visualSize.toString(),
                                    modifier = Modifier.fillMaxWidth(),
                                    onValueChange = { newVisualSize ->
                                        poiDialogState.visualSize = newVisualSize.toFloat()
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
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
//                            Button(
//                                onClick = { permissionsState.launchMultiplePermissionRequest() },
//                                Modifier.widthIn(min = 100.dp)
//                            ) {
//                                Text(text = "Bind object")
//                            }
                            if (poiDialogState.azimuth != 0f && poiDialogState.pitch != 0f) {
                                Text(text = "Pitch: ${poiDialogState.pitch}")
                                Text(text = "Azimuth: ${poiDialogState.azimuth}")
                                Text(text = "Distance: ${poiDialogState.distance}")
                            }
                        }
                        OutlinedButton(
                            onClick = { poiDialogState.advanced = poiDialogState.advanced.not() },
                            Modifier.widthIn(min = 100.dp)
                        ) {
                            Text(text = "Advanced")
                        }
                        if (poiDialogState.advanced) {
                            Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                                OutlinedTextField(
                                    value = poiDialogState.azimuth.toString(),
                                    modifier = Modifier.fillMaxWidth(),
                                    onValueChange = { newAzimuth ->
                                        poiDialogState.azimuth =
                                            when (newAzimuth.toFloatOrNull()) {
                                                null -> poiDialogState.azimuth
                                                else -> newAzimuth.toFloat()
                                            }

                                    },
                                    label = { Text(text = "Azimuth") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                OutlinedTextField(
                                    value = poiDialogState.pitch.toString(),
                                    modifier = Modifier.fillMaxWidth(),
                                    onValueChange = { newPitch ->
                                        poiDialogState.pitch = newPitch.toFloat()
                                    },
                                    label = { Text(text = "Pitch") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                OutlinedTextField(
                                    value = poiDialogState.distance.toString(),
                                    modifier = Modifier.fillMaxWidth(),
                                    onValueChange = { newDistance ->
                                        poiDialogState.distance = newDistance.toFloat()
                                    },
                                    label = { Text(text = "Distance") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.padding(150.dp)) }
            }
        }
    }
}

class PoiDialogState {
    var advanced by mutableStateOf(false)
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