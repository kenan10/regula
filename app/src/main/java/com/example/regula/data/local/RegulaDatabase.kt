package com.example.regula.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PoiEntity::class, ViewingPointEntity::class],
    version = 2
)
abstract class RegulaDatabase : RoomDatabase() {
    abstract val dao: RegulaDao
}