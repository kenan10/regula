package com.example.regula.domain.repository

import com.example.regula.domain.model.Poi
import com.example.regula.domain.model.ViewingPoint

interface PointsRepository {
    suspend fun getAllPois(): List<Poi>
    suspend fun insertPoi(userPoint: Poi)
    suspend fun insertViewingPoint(viewingPoint: ViewingPoint)
    suspend fun insertPoiManual(userPoint: Poi)
    suspend fun deleteAllPois()
}