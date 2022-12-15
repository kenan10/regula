package com.example.regula.data.local

import androidx.room.*

@Dao
interface RegulaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoi(savedObjects: PoiEntity)

    @Query("SELECT * FROM PoiEntity")
    suspend fun getAllPois(): List<PoiEntity>

    @Query("DELETE FROM PoiEntity")
    suspend fun deleteAllPois()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertViewingPoint(viewingPoint: ViewingPointEntity)

    @Query("SELECT * FROM ViewingPointEntity")
    suspend fun getAllViewingPoints(): List<ViewingPointEntity>

    @Query("SELECT * FROM ViewingPointEntity WHERE id=:id")
    suspend fun getViewingPoint(id: Int): List<ViewingPointEntity>

    @Query("DELETE FROM ViewingPointEntity")
    suspend fun deleteAllViewingPoints()

    @Transaction
    @Query("SELECT * FROM ViewingPointEntity")
    fun getViewingPointsWithPois(): List<ViewingPointWithPois>
}