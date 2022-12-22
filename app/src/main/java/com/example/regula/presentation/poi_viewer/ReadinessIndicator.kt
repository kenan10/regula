package com.example.regula.presentation.poi_viewer

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.regula.R

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