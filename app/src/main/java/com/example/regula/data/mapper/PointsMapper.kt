package com.example.regula.data.mapper

import com.example.regula.data.local.PoiEntity
import com.example.regula.domain.model.Poi
import com.example.regula.tools.SpacePoint

fun PoiEntity.toPoi(): Poi {
    return Poi(
        name = name,
        deviation = deviation,
        viewingPointId = viewingPointId,
        point = SpacePoint(accelerometerAngle, magnetometerAngle),
        visualSize = visualSize,
        distance = distance
    )
}

fun Poi.toPoiEntity(): PoiEntity {
    return PoiEntity(
        name = name,
        deviation = deviation,
        viewingPointId = viewingPointId,
        accelerometerAngle = point.pitch,
        magnetometerAngle = point.azimuth,
        visualSize = visualSize,
        distance = distance
    )
}