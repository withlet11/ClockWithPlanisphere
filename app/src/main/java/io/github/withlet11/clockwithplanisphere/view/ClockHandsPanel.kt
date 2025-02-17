/*
 * ClockHandsPanel.kt
 *
 * Copyright 2020-2024 Yasuhiro Yamakawa <withlet11@gmail.com>
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
import java.time.LocalDate
import java.time.LocalTime

class ClockHandsPanel(context: Context?, attrs: AttributeSet?) : AbstractPanel(context, attrs) {
    private var localDate = LocalDate.now()

    var localTime: LocalTime = LocalTime.MIDNIGHT
        set(value) {
            if (field.toSecondOfDay() != value.toSecondOfDay()) {
                field = value
                invalidate()
            }
        }

    var isVisible = true
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint().apply { isAntiAlias = true }
    private val path = Path()
    private val moonAgeRingColor = context?.getColor(R.color.transparentBlue3) ?: 0
    private val moonAgeDirectionColor = context?.getColor(R.color.transparentBlue1) ?: 0
    private val moonAgeGridColor = context?.getColor(R.color.silver) ?: 0
    private val moonAgeHandColor = context?.getColor(R.color.ripeMango) ?: 0
    private val hourHandsColor = context?.getColor(R.color.transparentBlue2) ?: 0
    private val minuteHandsColor = context?.getColor(R.color.transparentBlue1) ?: 0
    private val secondHandsColor = context?.getColor(R.color.transparentWhite) ?: 0
    private val shadow = context?.getColor(R.color.smoke) ?: 0

    private val hourHandGeometries = listOf(
        0.0f to -20.0f,
        -5.2f to -19.3f,
        -10.0f to -17.3f,
        -14.1f to -14.1f,
        -17.3f to -10.0f,
        -19.3f to -5.2f,
        -20.0f to 0.0f,
        -19.3f to 5.2f,
        -17.3f to 10.0f,
        -14.1f to 14.1f,
        -10.0f to 17.3f,
        -10.0f to 236.0f,
        -9.5f to 238.0f,
        -8.0f to 239.5f,
        -6.0f to 240.0f,
        6.0f to 240.0f,
        8.0f to 239.5f,
        9.5f to 238.0f,
        10.0f to 236.0f,
        10.0f to 17.3f,
        14.1f to 14.1f,
        17.3f to 10.0f,
        19.3f to 5.2f,
        20.0f to 0.0f,
        19.3f to -5.2f,
        17.3f to -10.0f,
        14.1f to -14.1f,
        10.0f to -17.3f,
        5.2f to -19.3f
    )

    private val minuteHandGeometries = listOf(
        0.0f to -16.0f,
        -4.1f to -15.5f,
        -8.0f to -13.9f,
        -11.3f to -11.3f,
        -13.9f to -8.0f,
        -15.5f to -4.1f,
        -16.0f to 0.0f,
        -15.5f to 4.1f,
        -13.9f to 8.0f,
        -11.3f to 11.3f,
        -8.0f to 13.9f,
        -8.0f to 350.0f,
        -7.5f to 352.0f,
        -6.0f to 353.5f,
        -4.0f to 354.0f,
        4.0f to 354.0f,
        6.0f to 353.5f,
        7.5f to 352.0f,
        8.0f to 350.0f,
        8.0f to 13.9f,
        11.3f to 11.3f,
        13.9f to 8.0f,
        15.5f to 4.1f,
        16.0f to 0.0f,
        15.5f to -4.1f,
        13.9f to -8.0f,
        11.3f to -11.3f,
        8.0f to -13.9f,
        4.1f to -15.5f
    )

    private val secondHandGeometries = listOf(
        0.0f to -12.0f,
        -3.1f to -11.6f,
        -6.0f to -10.4f,
        -8.5f to -8.5f,
        -10.4f to -6.0f,
        -11.6f to -3.1f,
        -12.0f to 0.0f,
        -11.6f to 3.1f,
        -10.4f to 6.0f,
        -8.5f to 8.5f,
        -6.0f to 10.4f,
        -3.1f to 11.6f,
        -2.0f to 382.5f,
        -1.8f to 383.3f,
        -1.3f to 383.8f,
        -0.5f to 384.0f,
        0.5f to 384.0f,
        1.3f to 383.8f,
        1.8f to 383.3f,
        2.0f to 382.5f,
        3.1f to 11.6f,
        6.0f to 10.4f,
        8.5f to 8.5f,
        10.4f to 6.0f,
        11.6f to 3.1f,
        12.0f to 0.0f,
        11.6f to -3.1f,
        10.4f to -6.0f,
        8.5f to -8.5f,
        6.0f to -10.4f,
        3.1f to -11.6f
    )

    @Suppress("RedundantOverride")
    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isVisible) {
            canvas.run {
                // drawMoonAgeRing()
                drawHourHand()
                drawMinuteHand()
                drawSecondHand()
            }
        }
    }

    private fun Canvas.drawMoonAgeRing() {
        save()
        rotate(localDate.dayOfYear / 29.530589f * 360f)
        paint.color = moonAgeRingColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = MOON_AGE_RING_THICKNESS
        // drawCircle(0f, 0f, MOON_AGE_RING_RADIUS, paint)
        paint.strokeCap = Paint.Cap.BUTT
        drawArc(
            -MOON_AGE_RING_RADIUS,
            -MOON_AGE_RING_RADIUS,
            MOON_AGE_RING_RADIUS,
            MOON_AGE_RING_RADIUS,
            18f - 90f,
            324f,
            false,
            paint
        )

        paint.color = moonAgeDirectionColor
        paint.strokeWidth = MOON_AGE_RING_THICKNESS
        drawArc(
            -MOON_AGE_RING_RADIUS,
            -MOON_AGE_RING_RADIUS,
            MOON_AGE_RING_RADIUS,
            MOON_AGE_RING_RADIUS,
            -18f - 90f,
            36f,
            false,
            paint
        )

        paint.textSize = 12f
        paint.color = moonAgeGridColor
        paint.style = Paint.Style.FILL
        val fontMetrics = paint.fontMetrics

        for (i in 0..29) {
            when (i) {
                5, 10, 15, 20, 25 -> {
                    val text = i.toString()
                    val textWidth = paint.measureText(text)
                    drawText(
                        text,
                        -textWidth * 0.5f,
                        fontMetrics.descent - MOON_AGE_RING_RADIUS,
                        paint
                    )
                }
                else -> drawCircle(0f, -MOON_AGE_RING_RADIUS, 1.5f, paint)
            }
            rotate(360f / 29.530589f)
        }
        restore()
        save()
        paint.color = moonAgeHandColor
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = MOON_AGE_HAND_THICKNESS
        rotate(180 - localTime.toSecondOfDay() / 86400f * 360f)
        drawLine(0f, 0f, 0f, -MOON_AGE_RING_RADIUS - MOON_AGE_RING_THICKNESS * 0.4f, paint)
        paint.strokeCap = Paint.Cap.SQUARE
        restore()
    }

    private fun Canvas.drawHourHand() {
        save()
        translate(5f, 5f)
        rotate(180f / 6f * (localTime.toSecondOfDay() / 3600f + 6f))
        paint.maskFilter = BlurMaskFilter(2f, BlurMaskFilter.Blur.NORMAL)
        paint.color = shadow
        paint.style = Paint.Style.FILL
        hourHandGeometries.last().let { (x, y) -> path.moveTo(x, y) }
        hourHandGeometries.forEach { (x, y) -> path.lineTo(x, y) }
        drawPath(path, paint)
        path.reset()
        restore()
        save()
        rotate(180f / 6f * (localTime.toSecondOfDay() / 3600f + 6f))
        paint.maskFilter = null
        paint.color = hourHandsColor
        paint.style = Paint.Style.FILL
        hourHandGeometries.last().let { (x, y) -> path.moveTo(x, y) }
        hourHandGeometries.forEach { (x, y) -> path.lineTo(x, y) }
        drawPath(path, paint)
        path.reset()
        restore()
    }

    private fun Canvas.drawMinuteHand() {
        save()
        translate(5f, 5f)
        rotate(180f / 30f * (localTime.minute + localTime.second / 60f + 30f))
        paint.maskFilter = BlurMaskFilter(2f, BlurMaskFilter.Blur.NORMAL)
        paint.color = shadow
        paint.style = Paint.Style.FILL
        minuteHandGeometries.last().let { (x, y) -> path.moveTo(x, y) }
        minuteHandGeometries.forEach { (x, y) -> path.lineTo(x, y) }
        drawPath(path, paint)
        path.reset()
        restore()
        save()
        rotate(180f / 30f * (localTime.minute + localTime.second / 60f + 30f))
        paint.maskFilter = null
        paint.color = minuteHandsColor
        paint.style = Paint.Style.FILL
        minuteHandGeometries.last().let { (x, y) -> path.moveTo(x, y) }
        minuteHandGeometries.forEach { (x, y) -> path.lineTo(x, y) }
        drawPath(path, paint)
        path.reset()
        restore()
    }

    private fun Canvas.drawSecondHand() {
        save()
        translate(5f, 5f)
        rotate(180f / 30f * (localTime.second + 30f))
        paint.maskFilter = BlurMaskFilter(2f, BlurMaskFilter.Blur.NORMAL)
        paint.color = shadow
        paint.style = Paint.Style.FILL
        secondHandGeometries.last().let { (x, y) -> path.moveTo(x, y) }
        secondHandGeometries.forEach { (x, y) -> path.lineTo(x, y) }
        drawPath(path, paint)
        path.reset()
        restore()
        save()
        rotate(180f / 30f * (localTime.second + 30f))
        paint.maskFilter = null
        paint.color = secondHandsColor
        paint.style = Paint.Style.FILL
        secondHandGeometries.last().let { (x, y) -> path.moveTo(x, y) }
        secondHandGeometries.forEach { (x, y) -> path.lineTo(x, y) }
        drawPath(path, paint)
        path.reset()
        restore()
    }

    /** Checks if a position is on the center of the canvas. */
    fun isCenter(position: Pair<Float, Float>): Boolean =
        position.toCanvasXY().isNear(centerPosition)
}
