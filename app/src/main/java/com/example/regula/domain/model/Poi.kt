package com.example.regula.domain.model

import com.example.regula.tools.SpacePoint

class Poi (
    var name: String,
    var viewingPointId: Int,
    var point: SpacePoint,
    var deviation: Float,
    var visualSize: Float,
    var distance: Float = 0f
) {

    fun toCompactString(): String {
        val accelerometerAngleStr = String.format("%.8f", point.pitch)
        val accelerometerComponent =
            if (point.pitch < 0) "-${
                accelerometerAngleStr
                    .slice(3 until accelerometerAngleStr.length)
            }" else accelerometerAngleStr
                .slice(2 until accelerometerAngleStr.length)

        val magnetometerAngleStr = String.format("%.8f", point.azimuth)

        val magnetometerComponent =
            if (point.azimuth < 0) "-${
                magnetometerAngleStr
                    .slice(3 until magnetometerAngleStr.length)
            }" else magnetometerAngleStr
                .slice(2 until magnetometerAngleStr.length)

        val deviationComponent =
            String.format("%.4f", deviation).slice(3 until String.format("%.4f", deviation).length)

        val visualSize = String.format("%.1f", visualSize)

        val distance = String.format("%.2f", distance)

        return "${name};${accelerometerComponent};${magnetometerComponent};${deviationComponent};${visualSize};${distance};"
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
                val accelerometerValue =
                    if (subss[i + 1][0].toString() == "-") "-" + "0." + subss[i + 1].slice(1 until subss[i + 1].length)
                    else "0." + subss[i + 1].slice(0 until subss[i + 1].length)
                val magnetometerValue =
                    if (subss[i + 2][0].toString() == "-") "-" + "0." + subss[i + 2].slice(1 until subss[i + 2].length)
                    else "0." + subss[i + 2].slice(0 until subss[i + 2].length)
                val deviation = "0.0" + subss[i + 3]
                val visualSize = subss[i + 4]
                val distance = subss[i + 5]
                val spacePoint =
                    SpacePoint(accelerometerValue.toFloat(), magnetometerValue.toFloat())
                pois = pois.plus(
                    Poi(
                        name = label,
                        viewingPointId = 1,
                        point = spacePoint,
                        deviation = deviation.toFloat(),
                        visualSize = visualSize.toFloat(),
                        distance = distance.toFloat()
                    )
                )
            }

            return pois
        }
    }
}
