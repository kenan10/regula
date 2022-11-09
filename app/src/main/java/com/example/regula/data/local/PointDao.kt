package com.example.regula.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PointDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPoint(savedObjects: UserPointEntity)

    @Query("SELECT * FROM UserPointEntity")
    suspend fun getAll(): List<UserPointEntity>
}