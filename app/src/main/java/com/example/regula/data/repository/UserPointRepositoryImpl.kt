package com.example.regula.data.repository

import com.example.regula.data.local.PointsDatabase
import com.example.regula.data.mapper.toUserPoint
import com.example.regula.data.mapper.toUserPointEntity
import com.example.regula.domain.model.AddUserPoint
import com.example.regula.domain.model.UserPoint
import com.example.regula.domain.repository.UserPointRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPointRepositoryImpl @Inject constructor(db: PointsDatabase) :
    UserPointRepository {
    private val dao = db.dao

    override suspend fun getAllUserPoints(): List<UserPoint> {
        return dao.getAll().map { it.toUserPoint() }
    }

    override suspend fun insertUserPoint(userPoint: AddUserPoint) {
        dao.insertUserPoint(userPoint.toUserPointEntity())
    }

    override suspend fun insertUserPointManual(userPoint: UserPoint) {
        dao.insertUserPoint(userPoint.toUserPointEntity())
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }
}