/*
 * SunAndMoonPanel.kt
 *
 * Copyright 2020-2023 Yasuhiro Yamakawa <withlet11@gmail.com>
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

/** This class is a view that show the Moon. */
class SunAndMoonPanel(context: Context?, attrs: AttributeSet?) : AbstractPanel(context, attrs) {
    private var moonPosition = 0f to 0f
    private var differenceOfLongitude = 0.0
    private val rotateAngleOfSun: Float get() = -solarAngle * sign(tenMinuteGridStep)
    private val rotateAngleOfMoon: Float get() = -siderealAngle * sign(tenMinuteGridStep)
    private var solarAngle = 0f
    private var siderealAngle = 0f
    private var tenMinuteGridStep = 180f / 72f
    private var date: LocalDate = LocalDate.now()

    private val paint = Paint().apply { isAntiAlias = true }
    private val moonColor = context?.getColor(R.color.pastelYellow) ?: 0
    private val moonDarkSideColor = context?.getColor(R.color.darkBlue) ?: 0

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawMoon()
    }

    private fun Canvas.drawMoon() {
        rotate(rotateAngleOfMoon, 0f, 0f)
        translate(moonPosition.first.toCanvas(), moonPosition.second.toCanvas())
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
        drawHalfMoon(isFirstHalf)
        paint.color = color
        drawOval(-phase, -MOON_RADIUS, phase, MOON_RADIUS, paint)
    }

    private fun Canvas.drawHalfMoon(isFirstHalf: Boolean) {
        paint.color = moonColor
        drawCircle(0f, 0f, MOON_RADIUS, paint)
        paint.color = moonDarkSideColor
        drawArc(
            -MOON_RADIUS, -MOON_RADIUS, MOON_RADIUS, MOON_RADIUS,
            if (isFirstHalf) 90f else -90f,
            180f,
            false,
            paint
        )
    }

    fun set(
        positionOfMoon: Pair<Pair<Float, Float>, Double>,
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
        moonPosition: Pair<Pair<Float, Float>, Double>,
        longitudeOfSun: Double,
        dateTime: LocalDateTime
    ) {
        this.moonPosition = moonPosition.first
        this.differenceOfLongitude =
            ((moonPosition.second - longitudeOfSun) % 360.0 + 360.0) % 360.0
        this.solarAngle = solarAngle
        this.siderealAngle = siderealAngle
        this.date = dateTime.toLocalDate()
        invalidate()
    }

    /** Check if a date differs from the date of the view. */
    fun isDifferentDate(date: LocalDate): Boolean = this.date != date

    /** @return the difference of an angle with the current sidereal angle */
    fun getAngleDifference(angle: Float): Float =
        (abs(angle - siderealAngle) % 360f).let { min(it, 360f - it) }
}
