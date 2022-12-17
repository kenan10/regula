package com.example.regula

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp

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