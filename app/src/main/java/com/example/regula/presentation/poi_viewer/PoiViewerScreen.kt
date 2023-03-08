package com.example.regula.presentation.poi_viewer

import android.Manifest
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.regula.R
import com.example.regula.presentation.common.PermissionsRequest
import com.ramcosta.composedestinations.annotation.Destination

const val MIN_VISUAL_SIZE = 7f
const val MAX_VISUAL_SIZE = 20f

@Composable
@Destination
fun PoiViewerScreen(viewModel: PoiViewerViewModel = hiltViewModel()) {
    val activity = LocalContext.current as Activity
    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

    CameraPreview()
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .padding(7.dp, 4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (viewModel.showDetails) {
                Column(modifier = Modifier.widthIn(max = 500.dp)) {
                    DetailsItem(
                        text = "Pitch: ${viewModel.currentSpacePoint.pitch.format(4)} " +
                                "Azim: ${viewModel.currentSpacePoint.azimuth.format(4)} " +
                                "Roll: ${viewModel.rollForDisplay.format(4)}"
                    )
                    DetailsItem(text = "Distance: ${viewModel.distance.format(2)}")
                }
            }
            ReadinessIndicator(
                isReady = viewModel.isReady, modifier = Modifier
                    .height(Dp(25f))
                    .width(Dp(25f))
            )
            Box {
                Button(onClick = { viewModel.isMenuOpened = true }) {
                    Icon(Icons.Default.Menu, contentDescription = "Show menu")
                }
                DropdownMenu(expanded = viewModel.isMenuOpened,
                    onDismissRequest = { viewModel.isMenuOpened = false }) {
                    DropdownMenuItem(onClick = { viewModel.showDetails = !viewModel.showDetails }) {
                        Text(text = "Toggle details")
                    }
                    DropdownMenuItem(onClick = { viewModel.deleteAllPoints() }) {
                        Text(text = "Wipe data")
                    }
                    DropdownMenuItem(onClick = {
                        viewModel.saveQrWithPoisInfo()
                    }) {
                        PermissionsRequest(permissions = listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                        Text(text = "Get QR-code")
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = 70.dp, y = (-40).dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.background(Color.White),
                text = viewModel.currentPointName,
                fontSize = 22.sp
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dp(5f))
        ) {
            if (viewModel.showSetDistanceToBaseBtn) {
                Button(onClick = {
                    viewModel.distanceToBase = viewModel.distance
                    viewModel.freezeSensors = true
                    viewModel.isDialogOpened = true
                    viewModel.showSetDistanceToBaseBtn = false
                }) {
                    Text(text = "Set")
                }
            } else {
                Button(onClick = {
                    if (viewModel.isReady) {
                        viewModel.freezeSensorsValues()
                        viewModel.showSetDistanceToBaseBtn = true
                    }
                }) {
                    Text(text = "Add point")
                }
                Button(onClick = {
                    if (viewModel.isReady) viewModel.recomputeAngles()
                }) {
                    Text(text = "Recompute angles")
                }
            }
        }
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        drawLine(
            start = Offset(x = canvasWidth / 2, y = 0f),
            end = Offset(x = canvasWidth / 2, y = canvasHeight),
            color = Color.Red,
            strokeWidth = 5f
        )
        drawLine(
            start = Offset(x = 0f, y = canvasHeight / 2),
            end = Offset(x = canvasWidth, y = canvasHeight / 2),
            color = Color.Red,
            strokeWidth = 5f
        )

        drawCircle(
            style = Stroke(width = 5f),
            center = Offset(x = canvasWidth / 2, y = canvasHeight / 2),
            radius = viewModel.radius * 10,
            color = Color.Red
        )
    }
    if (viewModel.isDialogOpened) {
        AlertDialog(modifier = Modifier,
            onDismissRequest = { viewModel.closeDialog() },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveCurrentObject()
                    viewModel.currentPointName = ""
                    viewModel.isDialogOpened = false
                    viewModel.freezeSensors = false
                    viewModel.newRadius = 0.0f
                }, content = { Text("Add") })
            },
            dismissButton = {
                Button(onClick = { viewModel.closeDialog() }, content = { Text(text = "Cancel") })
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dp(3f))) {
                    TextField(value = viewModel.newPointName, onValueChange = { newText ->
                        viewModel.newPointName = newText
                    }, placeholder = { Text(text = "Point name") })
                    Slider(
                        value = viewModel.newRadius,
                        onValueChange = { viewModel.newRadius = it },
                        valueRange = MIN_VISUAL_SIZE..MAX_VISUAL_SIZE
                    )
                }
            })
    }
}

@Composable
fun ReadinessIndicator(modifier: Modifier = Modifier, isReady: Boolean) {
    val painterUnready = painterResource(id = R.drawable.red_circle)
    val painterReady = painterResource(id = R.drawable.green_cirle)

    Image(
        painter = if (isReady) painterReady else painterUnready,
        contentDescription = "Readiness Status",
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Composable
fun DetailsItem(text: String) {
    Text(
        text = text, modifier = Modifier.background(Color.White)
    )
}

private fun Float.format(digits: Int): String {
    return String.format("%.${digits}f", this)
}
