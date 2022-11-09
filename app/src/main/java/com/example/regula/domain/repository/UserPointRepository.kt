package com.example.regula.domain.repository

import com.example.regula.domain.model.AddUserPoint
import com.example.regula.domain.model.UserPoint

interface UserPointRepository {
    suspend fun getAllUserPoints(): List<UserPoint>
    suspend fun insertUserPoint(userPoint: AddUserPoint)
}