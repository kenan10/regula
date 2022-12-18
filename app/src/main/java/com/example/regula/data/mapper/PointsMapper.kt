package com.example.regula.data.mapper

import com.example.regula.data.local.PoiEntity
import com.example.regula.domain.model.Poi
import com.example.regula.util.SpacePoint

fun PoiEntity.toPoi(): Poi {
    return Poi(
        name = name,
        deviation = deviation,
        viewingPointId = viewingPointId,
        point = SpacePoint(accelerometerAngle, magnetometerAngle)
    )
}

fun Poi.toPoiEntity(): PoiEntity {
    return PoiEntity(
        name = name,
        deviation = deviation,
        viewingPointId = viewingPointId,
        accelerometerAngle = point.accelerometerAngle,
        magnetometerAngle = point.magnetometerAngle
    )
}