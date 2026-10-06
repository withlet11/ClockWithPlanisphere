/*
 * HorizonPanel.kt
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
import androidx.core.graphics.withSave
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import io.github.withlet11.clockwithplanisphere.view.AbstractPanel.toCanvas

class HorizonPanel(context: Context?, attrs: AttributeSet? = null) {
    private var horizon by mutableStateOf(listOf<Pair<Float, Float>>())
    private var altAzimuth by mutableStateOf(listOf<List<Pair<Float, Float>?>>())
    private var directionLetters by mutableStateOf(listOf<Triple<String, Float, Float>>())

    var isZoomed by mutableStateOf(false)
    var isLandScape by mutableStateOf(false)
    var narrowSideLength by mutableStateOf(0)
    var wideSideLength by mutableStateOf(0)
    var offsetX by mutableStateOf(0)
    var offsetY by mutableStateOf(0)

    private val paint = Paint().apply { isAntiAlias = true }
    private val path = Path()
    private val dottedLine = DashPathEffect(floatArrayOf(2f, 2f), 0f)
    private val horizonColor = context?.getColor(R.color.smoke) ?: 0
    private val altAzimuthLineColor = context?.getColor(R.color.veryLightAzure) ?: 0
    private val directionLetterColor = context?.getColor(R.color.silver) ?: 0
    private val siderealTimeIndicatorColor = context?.getColor(R.color.yellow) ?: 0

    private val scale: Float
        get() {
            val drawAreaSize = if (isZoomed) wideSideLength else narrowSideLength
            return if (drawAreaSize > 0) drawAreaSize.toFloat() / AbstractPanel.PREFERRED_SIZE else 1f
        }

    @Composable
    fun Content(modifier: Modifier = Modifier) {
        ComposeCanvas(modifier = modifier) {
            val drawAreaSize = if (isZoomed) wideSideLength else narrowSideLength
            if (drawAreaSize > 0) {
                drawIntoCanvas { composeCanvas ->
                    val canvas = composeCanvas.nativeCanvas
                    canvas.withSave {
                        scale(scale, scale)
                        translate(AbstractPanel.CENTER, AbstractPanel.CENTER)
                        drawHorizon(canvas)
                        drawAltitudeAndAzimuthLines(canvas)
                        drawDirectionLetters(canvas)
                        drawSiderealTimeIndicator(canvas)
                    }
                }
            }
        }
    }

    private fun drawHorizon(canvas: Canvas) {
        paint.color = horizonColor
        paint.style = Paint.Style.FILL
        if (horizon.isNotEmpty()) {
            horizon.first().let { (x, y) -> path.moveTo(x.toCanvas(), y.toCanvas()) }
            horizon.forEach { (x, y) -> path.lineTo(x.toCanvas(), y.toCanvas()) }
            canvas.drawPath(path, paint)
            path.reset()
        }
    }

    private fun drawAltitudeAndAzimuthLines(canvas: Canvas) {
        paint.color = altAzimuthLineColor
        paint.strokeWidth = 1f
        paint.pathEffect = dottedLine
        paint.style = Paint.Style.STROKE
        altAzimuth.forEach { list ->
            var isPenDown = false
            list.forEach { pos ->
                if (isPenDown) {
                    if (pos == null) {
                        isPenDown = false
                    } else {
                        val (x, y) = pos
                        path.lineTo(x.toCanvas(), y.toCanvas())
                    }
                } else {
                    pos?.let { (x, y) ->
                        isPenDown = true
                        path.moveTo(x.toCanvas(), y.toCanvas())
                    }
                }
            }
            canvas.drawPath(path, paint)
            path.reset()
        }
        paint.pathEffect = null
    }

    private fun drawDirectionLetters(canvas: Canvas) {
        paint.color = directionLetterColor
        paint.style = Paint.Style.FILL
        paint.textSize = 18f
        directionLetters.forEach { (letter, x, y) ->
            val textWidth = paint.measureText(letter)
            canvas.drawText(
                letter,
                x.toCanvas() - textWidth * 0.5f,
                y.toCanvas() + textWidth * 0.5f,
                paint
            )
        }
    }

    /**
     * Draws a triangle as an indicator of the sidereal time.
     */
    private fun drawSiderealTimeIndicator(canvas: Canvas) {
        paint.color = siderealTimeIndicatorColor
        paint.style = Paint.Style.FILL
        path.moveTo(0f, -327f)
        path.lineTo(5f, -318f)
        path.lineTo(-5f, -318f)
        path.lineTo(0f, -327f)
        canvas.drawPath(path, paint)
        path.reset()
    }

    fun set(
        horizon: List<Pair<Float, Float>>,
        altAzimuth: List<List<Pair<Float, Float>?>>,
        directionLetters: List<Triple<String, Float, Float>>
    ) {
        this.horizon = horizon
        this.altAzimuth = altAzimuth
        this.directionLetters = directionLetters
    }
}
