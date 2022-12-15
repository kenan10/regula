package com.example.regula.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class ViewingPointEntity(
    @PrimaryKey val id: Int? = null,
    val name: String
)

data class ViewingPointWithPois(
    @Embedded val viewingPoint: ViewingPointEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "viewingPointId"
    )
    val pois: List<PoiEntity>
)