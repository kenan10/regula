package com.example.regula.presentation.qr_scanner

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.example.regula.domain.model.Poi

@Composable
fun PoiRow(poi: Poi) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = poi.name, style = TextStyle(
                fontSize = 20.sp
            )
        )
    }
}