package com.example.regula

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.regula.ui.theme.RegulaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RegulaTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {
                    val viewModel = hiltViewModel<MainViewModel>()
                    val activity = LocalContext.current as Activity
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                    PermissionsRequest()
                    CameraPreview()
                    Column(
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(Dp(7f), Dp(5f))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = viewModel.accelerometerShowedValue,
                                    modifier = Modifier.background(Color.White)
                                )
                                Text(
                                    text = viewModel.magnetometerShowedValue,
                                    modifier = Modifier.background(Color.White)
                                )
                                Text(
                                    text = viewModel.angles,
                                    modifier = Modifier.background(Color.White)
                                )
                                Text(
                                    text = viewModel.isInCircleDistance,
                                    modifier = Modifier.background(Color.White)
                                )
                            }
                            ReadinessIndicator(
                                isReady = viewModel.isReady,
                                modifier = Modifier
                                    .height(Dp(25f))
                                    .width(Dp(25f))
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                modifier = Modifier.background(Color.White),
                                text = viewModel.currentPointName
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dp(5f))
                        ) {
                            Button(onClick = { viewModel.deleteAllPoints() }) {
                                Text(text = "Wipe data")
                            }
                            Button(onClick = {
                                if (viewModel.isReady) viewModel.isDialogOpened = true
                            }) {
                                Text(text = "Save current point")
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
                    }
                    if (viewModel.isDialogOpened) {
                        AlertDialog(onDismissRequest = { viewModel.isDialogOpened = false },
                            confirmButton = {
                                Button(onClick = {
                                    viewModel.saveCurrentObject()
                                    viewModel.currentPointName = ""
                                    viewModel.deviation = ""
                                    viewModel.isDialogOpened = false
                                }, content = { Text("Add") })
                            },
                            dismissButton = {
                                Button(onClick = { viewModel.isDialogOpened = false },
                                    content = { Text(text = "Cancel") })
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(Dp(3f))) {
                                    TextField(value = viewModel.newPointName,
                                        onValueChange = { newText ->
                                            viewModel.newPointName = newText
                                        },
                                        placeholder = { Text(text = "Point name") })
                                    TextField(value = viewModel.deviation,
                                        onValueChange = { newText ->
                                            viewModel.deviation = newText
                                        },
                                        placeholder = { Text(text = "Deviation") })
                                }
                            })
                    }
                }
            }
        }
    }
}
