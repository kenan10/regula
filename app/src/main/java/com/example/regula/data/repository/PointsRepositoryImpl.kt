package com.example.regula.data.repository

import com.example.regula.data.local.RegulaDatabase
import com.example.regula.data.mapper.toPoi
import com.example.regula.data.mapper.toPoiEntity
import com.example.regula.domain.model.Poi
import com.example.regula.domain.model.ViewingPoint
import com.example.regula.domain.repository.PointsRepository
import com.example.regula.tools.SpacePoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PointsRepositoryImpl @Inject constructor(db: RegulaDatabase) :
    PointsRepository {
    private val dao = db.dao

    override suspend fun getAllPois(): List<Poi> {
        return dao.getAllPois().map { it.toPoi() }
    }

    override suspend fun insertPoi(userPoint: Poi) {
        dao.insertPoi(userPoint.toPoiEntity())
    }

    override suspend fun setCoordinates(newSpacePoint: SpacePoint, name: String) {
        dao.updateCoordinatesByName(name, newSpacePoint.pitch, newSpacePoint.azimuth)
    }

    override suspend fun insertViewingPoint(viewingPoint: ViewingPoint) {
        TODO("Not yet implemented")
    }

    override suspend fun insertPoiManual(userPoint: Poi) {
        dao.insertPoi(userPoint.toPoiEntity())
    }

    override suspend fun deletePoi(name: String) {
        dao.deletePoi(name = name)
    }

    override suspend fun deleteAllPois() {
        dao.deleteAllPois()
    }
}