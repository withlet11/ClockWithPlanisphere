/*
 * SunAndMoonPanel.kt
 *
 * Copyright 2020-2026 Yasuhiro Yamakawa <withlet11@gmail.com>
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

package io.github.withlet11.clockwithplanisphere.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import io.github.withlet11.clockwithplanisphere.R
import java.lang.Math.toRadians
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.*
import androidx.core.graphics.withSave
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import io.github.withlet11.clockwithplanisphere.view.PanelGeometry.toCanvas
import io.github.withlet11.clockwithplanisphere.view.PanelGeometry.MOON_RADIUS

class SunAndMoonPanel(context: Context?, attrs: AttributeSet? = null) {
    private var moonPosition by mutableStateOf(Offset.Zero)
    private var differenceOfLongitude by mutableDoubleStateOf(0.0)
    private val rotateAngleOfSun: Float get() = -solarAngle * sign(tenMinuteGridStep)
    private val rotateAngleOfMoon: Float get() = -siderealAngle * sign(tenMinuteGridStep)
    var solarAngle by mutableFloatStateOf(0f)
    var siderealAngle by mutableFloatStateOf(0f)
    private var tenMinuteGridStep = 180f / 72f
    private var date by mutableStateOf(LocalDate.now())

    var isZoomed by mutableStateOf(false)
    var isLandScape by mutableStateOf(false)
    var shortSide by mutableIntStateOf(0)
    var longSide by mutableIntStateOf(0)
    var offsetXY by mutableStateOf(Offset.Zero)

    private val paint = Paint().apply { isAntiAlias = true }
    private val moonColor = context?.getColor(R.color.pastelYellow) ?: 0
    private val moonDarkSideColor = context?.getColor(R.color.darkBlue) ?: 0

    private val scale: Float
        get() {
            val drawAreaSize = if (isZoomed) longSide else shortSide
            return if (drawAreaSize > 0) drawAreaSize.toFloat() / PanelGeometry.PREFERRED_SIZE else 1f
        }

    @Composable
    fun Content(modifier: Modifier = Modifier) {
        ComposeCanvas(modifier = modifier) {
            val drawAreaSize = if (isZoomed) longSide else shortSide
            if (drawAreaSize > 0) {
                drawIntoCanvas { composeCanvas ->
                    val canvas = composeCanvas.nativeCanvas
                    canvas.withSave {
                        scale(scale, scale)
                        translate(PanelGeometry.CENTER, PanelGeometry.CENTER)
                        drawMoon(canvas)
                    }
                }
            }
        }
    }

    private fun drawMoon(canvas: Canvas) {
        canvas.withSave {
            rotate(rotateAngleOfMoon, 0f, 0f)
            translate(moonPosition.x.toCanvas(), moonPosition.y.toCanvas())
            rotate(
                rotateAngleOfSun - rotateAngleOfMoon -
                        if (tenMinuteGridStep > 0.0) (180 - differenceOfLongitude.toFloat())
                        else differenceOfLongitude.toFloat()
            )
            paint.style = Paint.Style.FILL
            val phase = abs(cos(toRadians(differenceOfLongitude)).toFloat() * MOON_RADIUS)
            val (isFirstHalf, color) = when {
                differenceOfLongitude < 90 -> true to moonDarkSideColor
                differenceOfLongitude < 180 -> true to moonColor
                differenceOfLongitude < 270 -> false to moonColor
                else -> false to moonDarkSideColor
            }
            drawHalfMoon(canvas, isFirstHalf)
            paint.color = color
            canvas.drawOval(-phase, -MOON_RADIUS, phase, MOON_RADIUS, paint)
        }
    }

    private fun drawHalfMoon(canvas: Canvas, isFirstHalf: Boolean) {
        paint.color = moonColor
        canvas.drawCircle(0f, 0f, MOON_RADIUS, paint)
        paint.color = moonDarkSideColor
        canvas.drawArc(
            -MOON_RADIUS, -MOON_RADIUS, MOON_RADIUS, MOON_RADIUS,
            if (isFirstHalf) 90f else -90f,
            180f,
            false,
            paint
        )
    }

    fun set(
        positionOfMoon: Pair<Offset, Double>,
        longitudeOfSun: Double,
        tenMinuteGridStep: Float
    ) {
        this.moonPosition = positionOfMoon.first
        this.differenceOfLongitude =
            ((positionOfMoon.second - longitudeOfSun) % 360.0 + 360.0) % 360.0
        this.tenMinuteGridStep = tenMinuteGridStep
    }

    fun setSolarAngleAndCurrentPosition(
        solarAngle: Float,
        siderealAngle: Float,
        moonPosition: Pair<Offset, Double>,
        longitudeOfSun: Double,
        dateTime: LocalDateTime
    ) {
        this.moonPosition = moonPosition.first
        this.differenceOfLongitude =
            ((moonPosition.second - longitudeOfSun) % 360.0 + 360.0) % 360.0
        this.solarAngle = solarAngle
        this.siderealAngle = siderealAngle
        this.date = dateTime.toLocalDate()
    }

    /** Check if a date differs from the date of the view. */
    fun isDifferentDate(date: LocalDate): Boolean = this.date != date

    /** @return the difference of an angle with the current sidereal angle */
    fun getAngleDifference(angle: Float): Float =
        (abs(angle - siderealAngle) % 360f).let { min(it, 360f - it) }
}
