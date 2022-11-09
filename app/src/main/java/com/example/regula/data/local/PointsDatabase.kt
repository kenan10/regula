package com.example.regula.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserPointEntity::class],
    version = 1
)
abstract class PointsDatabase : RoomDatabase() {
    abstract val dao: PointDao
}