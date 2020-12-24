/*
 * AbstractSkyModel.kt
 *
 * Copyright 2020 Yasuhiro Yamakawa <withlet11@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.withlet11.skyclock.model

import android.content.Context
import android.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.*

abstract class AbstractSkyModel {
    data class StarGeometry(val x: Float, val y: Float, val r: Float)
    data class ConstellationLineGeometry(val x1: Float, val y1: Float, val x2: Float, val y2: Float)
    data class MilkyWayDot(val x1: Float, val y1: Float, val magnitude: Int)

    @Entity(tableName = "hip_list")
    data class HipEntry(
        @PrimaryKey(autoGenerate = false) val hip: Int,
        @ColumnInfo(name = "ra") val ra: Double,
        @ColumnInfo(name = "dec") val dec: Double,
        @ColumnInfo(name = "mag") val mag: Double,
    )

    @Entity(tableName = "constellation_lines")
    data class ConstellationLineEntry(
        @PrimaryKey(autoGenerate = false) val id: Int,
        @ColumnInfo(name = "ra1") val ra1: Double,
        @ColumnInfo(name = "dec1") val dec1: Double,
        @ColumnInfo(name = "ra2") val ra2: Double,
        @ColumnInfo(name = "dec2") val dec2: Double
    )

    @Entity(tableName = "milkyway_north")
    data class NorthMilkyWayDotEntry(
        @PrimaryKey(autoGenerate = false) val id: Int,
        @ColumnInfo(name = "x_position") val x: Int,
        @ColumnInfo(name = "y_position") val y: Int,
        @ColumnInfo(name = "magnitude") val magnitude: Int
    )

    @Entity(tableName = "milkyway_south")
    data class SouthMilkyWayDotEntry(
        @PrimaryKey(autoGenerate = false) val id: Int,
        @ColumnInfo(name = "x_position") val x: Int,
        @ColumnInfo(name = "y_position") val y: Int,
        @ColumnInfo(name = "magnitude") val magnitude: Int
    )

    companion object {
        const val ANGLE_LIMIT = 155.0
    }

    abstract fun toAngle(declination: Double): Double
    abstract fun toDeclinationFromPole(angle: Int): Int
    protected abstract fun toRadius(declination: Double): Double
    protected abstract fun toRadiansFromHours(hour: Double): Double
    abstract fun toRadiansFromDegrees(degree: Double): Double

    var latitude = 0.0
        set(value) {
            field = value
            maxAngle = min(toAngle(value) + 10.0, ANGLE_LIMIT)
            equatorial = List(5) {
                val angle = (it + 1) * 30
                val dec = toDeclinationFromPole(angle)
                if (angle < toAngle(value)) dec to (angle / maxAngle).toFloat() else null
            }.filterNotNull()
            ecliptic = List(360) {
                val (dec, ra) = convertToEquatorialFromEcliptic(it.toDouble())
                val radius = toRadius(dec)
                val angle = toRadiansFromDegrees(-ra) // need hour angle
                (-radius * sin(angle)).toFloat() to (-radius * cos(angle)).toFloat()
            }
        }

    protected var maxAngle = 0.0
    abstract val tenMinuteGridStep: Float

    var equatorial = listOf<Pair<Int, Float>>()
    var ecliptic = listOf<Pair<Float, Float>>()

    var starGeometryList = listOf<StarGeometry>()
        protected set

    var constellationLineList = listOf<ConstellationLineGeometry>()
        protected set

    var northMilkyWayDotList = listOf<MilkyWayDot>()
        protected set

    var southMilkyWayDotList = listOf<MilkyWayDot>()
        protected set

    private lateinit var skyClockDao: SkyClockDao
    private lateinit var db: SkyClockDataBase


    fun loadDatabase(context: Context) {
        db = SkyClockDataBase.getInstance(context)
        skyClockDao = db.skyClockDao()
    }

    fun updatePositionList() {
        starGeometryList = skyClockDao.getAllHip().mapNotNull { (_, ra, dec, mag) ->
            calculateStarPosition(dec, ra, mag)
        }

        constellationLineList = skyClockDao.getAllConstellationLines().mapNotNull { (_, ra1, dec1, ra2, dec2) ->
            val xy1 = convertToXYPositionWithNull(dec1, -ra1)
            val xy2 = convertToXYPositionWithNull(dec2, -ra2)
            if (xy1 != null && xy2 != null) ConstellationLineGeometry(
                xy1.first,
                xy1.second,
                xy2.first,
                xy2.second
            ) else null
        }

        northMilkyWayDotList = skyClockDao.getNorthMilkyWay().map { (_, x, y, v) ->
            makeMilkyWayDot(x, y, v)
        }

        southMilkyWayDotList = skyClockDao.getSouthMilkyWay().map { (_, x, y, v) ->
            makeMilkyWayDot(x, y, v)
        }
    }

    private fun makeMilkyWayDot(x: Int, y: Int, v: Int): MilkyWayDot {
        val alpha = min(128, v / 8 + 32)
        val color = Color.argb(alpha, 192, 192, 192)
        return MilkyWayDot(x / 150f, y / 150f, color)
    }

    private fun calculateStarPosition(dec: Double, ra: Double, magnitude: Double): StarGeometry? =
        when {
            magnitude < 6.0 -> {
                convertToXYPositionWithNull(dec, -ra)?.run {
                    val r = min(4.5, 6.0 * 0.65.pow(magnitude)).toFloat()
                    StarGeometry(first, second, r)
                }
            }
            else -> null
        }

    fun convertToXYPositionWithNull(dec: Double, ha: Double): Pair<Float, Float>? {
        val radius = toRadius(dec)
        return when {
            radius < 1.0 -> {
                val angle = toRadiansFromHours(ha)
                (-radius * sin(angle)).toFloat() to (-radius * cos(angle)).toFloat()
            }
            else -> null
        }
    }

    fun convertToXYPosition(dec: Double, ha: Double): Pair<Float, Float> {
        val radius = min(toRadius(dec), 1.0)
        val angle = toRadiansFromHours(ha)
        return (-radius * sin(angle)).toFloat() to (-radius * cos(angle)).toFloat()
    }

    fun getXYPositionOnOuterCircle(degree: Double): Pair<Float, Float> {
        val radian = toRadiansFromDegrees(degree)
        return cos(radian).toFloat() to sin(radian.toFloat())
    }

    private fun convertToEquatorialFromEcliptic(eclipticLongitude: Double): Pair<Double, Double> {
        val obliquity = 23.44
        val eLong = Math.toRadians(eclipticLongitude)
        val dec = asin(sin(Math.toRadians(obliquity)) * sin(eLong))
        val ra = acos(cos(eLong) / cos(dec)).let { if (eLong > PI) 2.0 * PI - it else it }
        return Math.toDegrees(dec) to Math.toDegrees(ra)
    }
}