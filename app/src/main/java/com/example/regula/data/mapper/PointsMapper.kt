package com.example.regula.data.mapper

import com.example.regula.data.local.UserPointEntity
import com.example.regula.domain.model.AddUserPoint
import com.example.regula.domain.model.UserPoint
import com.example.regula.util.SpacePoint

fun UserPointEntity.toUserPoint(): UserPoint {
    return UserPoint(
        name = name,
        deviation = deviation,
        point = SpacePoint(accelerometerAngle, magnetometerAngle)
    )
}

fun AddUserPoint.toUserPointEntity(): UserPointEntity {
    val point = SpacePoint.fromSensorsValues(accelerometerValue, magnetometerValue)

    return UserPointEntity(
        name = name,
        deviation = deviation,
        accelerometerAngle = point.accelerometerAngle,
        magnetometerAngle = point.magnetometerAngle
    )
}

fun UserPoint.toUserPointEntity(): UserPointEntity {
    return UserPointEntity(
        name = name,
        deviation = deviation,
        accelerometerAngle = point.accelerometerAngle,
        magnetometerAngle = point.magnetometerAngle
    )
}