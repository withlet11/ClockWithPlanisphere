/*
 * SunPanel.kt
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
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import io.github.withlet11.clockwithplanisphere.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign
import androidx.core.graphics.withSave
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import io.github.withlet11.clockwithplanisphere.view.PanelGeometry.toAbsoluteXY
import io.github.withlet11.clockwithplanisphere.view.PanelGeometry.isNear

class SunPanel(context: Context?, attrs: AttributeSet? = null) {
    private var analemma by mutableStateOf(listOf<Offset>())
    private var monthlyPositionList by mutableStateOf(listOf<Offset>())

    private var sunPosition by mutableStateOf(Offset.Zero)
    private val rotateAngle: Float get() = -solarAngle * sign(tenMinuteGridStep)
    var solarAngle by mutableFloatStateOf(0f)
    private var tenMinuteGridStep = 180f / 72f
    private var date by mutableStateOf(LocalDate.now())
    private var secondOfDay by mutableIntStateOf(0)

    var isZoomed by mutableStateOf(false)
    var isLandScape by mutableStateOf(false)
    var shortSide by mutableIntStateOf(0)
    var longSide by mutableIntStateOf(0)
    var offsetXY by mutableStateOf(Offset.Zero)

    private val paint = Paint().apply { isAntiAlias = true }
    private val path = Path()
    private val eclipticColor = context?.getColor(R.color.dandelion) ?: 0
    private val sunColor = context?.getColor(R.color.ripeMango) ?: 0

    private val centerPosition
        get() = ((if (isZoomed) longSide else shortSide) * 0.5f).let {
            Offset(it, it) + offsetXY
        }

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
                        canvas.rotate(rotateAngle, 0f, 0f)
                        drawAnalemma(canvas)
                        drawMonthlyPosition(canvas)
                        drawCurrentPosition(canvas)
                    }
                }
            }
        }
    }

    private fun drawAnalemma(canvas: Canvas) {
        paint.color = eclipticColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        if (analemma.isNotEmpty()) {
            analemma.last().let { (x, y) -> path.moveTo(x.toCanvas(), y.toCanvas()) }
            analemma.forEach { (x, y) -> path.lineTo(x.toCanvas(), y.toCanvas()) }
            canvas.drawPath(path, paint)
            path.reset()
        }
    }

    private fun drawMonthlyPosition(canvas: Canvas) {
        paint.color = eclipticColor
        paint.style = Paint.Style.FILL
        val monthTextList =
            listOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII")
        monthlyPositionList.forEachIndexed { month, pos ->
            val label = monthTextList[month]
            val offset = paint.measureText(label)
            pos.let { (x, y) ->
                when {
                    x < 0f -> canvas.drawText(
                        label,
                        x.toCanvas() - 5f - offset,
                        y.toCanvas() + 5f,
                        paint
                    )

                    else -> canvas.drawText(label, x.toCanvas() + 5f, y.toCanvas() + 5f, paint)
                }
                canvas.drawCircle(x.toCanvas(), y.toCanvas(), 3f, paint)
            }
        }
    }

    private fun drawCurrentPosition(canvas: Canvas) {
        paint.color = sunColor
        paint.style = Paint.Style.FILL
        sunPosition.let { (x, y) ->
            canvas.drawCircle(x.toCanvas(), y.toCanvas(), 5f, paint)
        }
    }

    /**
     * Checks if a position is on analemma.
     * @param posOnFragment a position on the fragment
     * @return true if a position is on analemma
     */
    fun isOnAnalemma(posOnFragment: Offset): Boolean {
        val analemmaPosition =
            sunPosition.toAbsoluteXY(-solarAngle * sign(tenMinuteGridStep), scale, centerPosition)
        return posOnFragment.isNear(analemmaPosition)
    }

    fun set(
        analemma: List<Offset>,
        monthlyPositionList: List<Offset>,
        currentPosition: Offset,
        tenMinuteGridStep: Float
    ) {
        this.analemma = analemma
        this.monthlyPositionList = monthlyPositionList
        this.sunPosition = currentPosition
        this.tenMinuteGridStep = tenMinuteGridStep
    }

    fun setSolarAngle(solarAngle: Float, time: LocalTime) {
        this.solarAngle = solarAngle
        this.secondOfDay = time.toSecondOfDay()
    }

    fun setSolarAngleAndCurrentPosition(
        solarAngle: Float,
        sunPosition: Offset,
        dateTime: LocalDateTime
    ) {
        this.sunPosition = sunPosition
        this.solarAngle = solarAngle
        this.date = dateTime.toLocalDate()
        this.secondOfDay = dateTime.toLocalTime().toSecondOfDay()
    }

    /** @return the difference of an angle with the current solar angle */
    fun getAngleDifference(angle: Float): Float =
        (abs(angle - solarAngle) % 360f).let { min(it, 360f - it) }

    /** @return the square of distance between a position and the current sun position */
    fun getDistance(position: Offset): Float = (sunPosition - position).getDistanceSquared()

    /** Check if a date differs from the date of the view. */
    fun isDifferentDate(date: LocalDate): Boolean = this.date != date

    /** Check if a time differs from the time of the view. */
    fun isDifferentTime(secondOfDay: Int): Boolean = this.secondOfDay != secondOfDay

    fun getAngle(position: Offset): Float = PanelGeometry.getAngle(position, centerPosition)

    private fun Offset.toCanvasXY(): Offset = this + offsetXY
}
