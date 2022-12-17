package com.example.regula.presentation.poi_viewer

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraPreview() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val cameraProvider = cameraProviderFuture.get()
    val cameraSelector: CameraSelector =
        CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
    preview.setSurfaceProvider(previewView.surfaceProvider)
    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)

    AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
}