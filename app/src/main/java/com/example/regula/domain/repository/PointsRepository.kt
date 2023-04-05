package com.example.regula.domain.repository

import com.example.regula.domain.model.Poi
import com.example.regula.domain.model.ViewingPoint
import com.example.regula.tools.SpacePoint

interface PointsRepository {
    suspend fun getAllPois(): List<Poi>
    suspend fun insertPoi(userPoint: Poi)
    suspend fun setCoordinates(newSpacePoint: SpacePoint, name: String)
    suspend fun insertViewingPoint(viewingPoint: ViewingPoint)
    suspend fun insertPoiManual(userPoint: Poi)
    suspend fun deletePoi(name: String)
    suspend fun deleteAllPois()
}