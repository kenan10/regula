package com.example.regula.domain.model

import com.example.regula.util.SpacePoint

data class Poi(
    val name: String,
    val viewingPointId: Int,
    val point: SpacePoint,
    val deviation: Float
) {
    fun toCompactString(): String {
        val accelerometerAngleStr = String.format("%.8f", point.accelerometerAngle)
        val accelerometerComponent =
            if (point.accelerometerAngle < 0) "-${
                accelerometerAngleStr
                    .slice(3 until accelerometerAngleStr.length)
            }" else accelerometerAngleStr
                .slice(2 until accelerometerAngleStr.length)

        val magnetometerAngleStr = String.format("%.8f", point.magnetometerAngle)

        val magnetometerComponent =
            if (point.magnetometerAngle < 0) "-${
                magnetometerAngleStr
                    .slice(3 until magnetometerAngleStr.length)
            }" else magnetometerAngleStr
                .slice(2 until magnetometerAngleStr.length)

        val deviationComponent =
            String.format("%.9f", deviation).slice(3 until String.format("%.9f", deviation).length)

        return "${name};${accelerometerComponent};${magnetometerComponent};${deviationComponent};"
    }

    companion object Factory {
        fun fromCompactString(str: String): List<Poi> {
            var subss = str.split(";")
            subss = subss.slice(0 until subss.size - 1)
            var pois = emptyList<Poi>()

            for (i in subss.indices step 4) {
                val label = subss[i]
                val accelerometerValue =
                    if (subss[i + 1][0].toString() == "-") "-" + "0." + subss[i + 1].slice(1 until subss[i + 1].length)
                    else "0." + subss[i + 1].slice(0 until subss[i + 1].length)
                val magnetometerValue =
                    if (subss[i + 2][0].toString() == "-") "-" + "0." + subss[i + 2].slice(1 until subss[i + 2].length)
                    else "0." + subss[i + 2].slice(0 until subss[i + 2].length)
                val deviation = "0.0" + subss[i + 3]
                val spacePoint =
                    SpacePoint(accelerometerValue.toFloat(), magnetometerValue.toFloat())
                pois = pois.plus(
                    Poi(
                        name = label,
                        viewingPointId = 1,
                        point = spacePoint,
                        deviation = deviation.toFloat()
                    )
                )
            }

            return pois
        }
    }
}
