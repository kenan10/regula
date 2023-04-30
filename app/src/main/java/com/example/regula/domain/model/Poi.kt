package com.example.regula.domain.model

import com.example.regula.tools.SpacePoint

class Poi(
    var name: String,
    var viewingPointId: Int,
    var point: SpacePoint,
    var deviation: Float,
    var visualSize: Float,
    var distance: Float = 0f
) {

    fun toCompactString(): String {
        val pitchStr = point.pitch.format(5)
        val azimuthStr = point.azimuth.format(5)
        val deviationStr = deviation.format(3)
        val visualSizeStr = visualSize.format(3)
        val distanceStr = distance.format(2)

        return "${name};${pitchStr};${azimuthStr};${deviationStr};${visualSizeStr};${distanceStr};"
    }

    fun clearPoint() {
        point = SpacePoint.emptyPoint()
    }

    companion object Factory {
        const val DEFAULT_VIEWING_POINT_ID = 1

        fun emptyPoi(): Poi {
            return Poi(
                name = "",
                viewingPointId = DEFAULT_VIEWING_POINT_ID,
                point = SpacePoint.emptyPoint(),
                deviation = 0f,
                visualSize = 0f
            )
        }

        fun fromCompactString(str: String): List<Poi> {
            var subss = str.split(";")
            subss = subss.slice(0 until subss.size - 1)
            var pois = emptyList<Poi>()

            for (i in subss.indices step 6) {
                val label = subss[i]
                val accelerometerValue = subss[i + 1].toFloat()
                val magnetometerValue = subss[i + 2].toFloat()
                val deviation = subss[i + 3].toFloat()
                val visualSize = subss[i + 4].toFloat()
                val distance = subss[i + 5].toFloat()
                val spacePoint =
                    SpacePoint(accelerometerValue, magnetometerValue)
                pois = pois.plus(
                    Poi(
                        name = label,
                        viewingPointId = 1,
                        point = spacePoint,
                        deviation = deviation,
                        visualSize = visualSize,
                        distance = distance
                    )
                )
            }

            return pois
        }
    }
}

private fun Float.format(digits: Int): String {
    return String.format("%.${digits}f", this)
}

