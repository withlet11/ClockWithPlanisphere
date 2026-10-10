/*
 * PanelGeometry.kt
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

import androidx.compose.ui.geometry.Offset
import java.lang.Math.toDegrees
import kotlin.math.*

object PanelGeometry {
    const val PREFERRED_SIZE = 800f
    const val CENTER = PREFERRED_SIZE * 0.5f
    private const val CIRCLE_RADIUS = PREFERRED_SIZE * 0.4f
    const val MOON_RADIUS = 10f
    const val BEZEL_RADIUS = 400f
    const val DATE_PANEL_RADIUS = 368f
    const val SKY_BACKGROUND_RADIUS = 336f

    /**
     * Converts a length on the canvas.
     * @receiver a length (the radius of the draw area = 1)
     * @return the length on the canvas
     */
    fun Float.toCanvas(): Float = this * CIRCLE_RADIUS

    /**
     * Checks if a position is on the circle.
     * @receiver a position on the canvas
     * @param [center] the center of the canvas
     * @param [radius] the radius of the circle
     * @return true if 2 positions are near
     */
    fun Offset.isNear(center: Offset, radius: Float): Boolean {
        val r2 = (this - center).getDistanceSquared()
        val minR = radius - 50
        val maxR = radius + 50
        return r2 > minR * minR && r2 < maxR * maxR
    }

    /**
     * Checks if a position and other is near on the canvas.
     * @receiver a position on the canvas
     * @return true if 2 positions are near
     */
    fun Offset.isNear(other: Offset): Boolean = (this - other).getDistanceSquared() < 2500

    /**
     * Converts a relative position to the absolute position on the canvas with a rotate angle.
     * @receiver a relative position on the canvas
     * @param [rotate] the rotate angle
     * @param [scale] the scale
     * @param [centerPosition] the center position
     * @return the absolute position on the canvas
     */
    fun Offset.toAbsoluteXY(rotate: Float, scale: Float, centerPosition: Offset): Offset {
        val x = this.x.toCanvas()
        val y = this.y.toCanvas()
        val rad = Math.toRadians(rotate.toDouble())
        return Offset(
            (x * cos(rad) - y * sin(rad)).toFloat(),
            (x * sin(rad) + y * cos(rad)).toFloat()
        ) * scale + centerPosition
    }

    /**
     * Converts a relative position to the absolute position on the canvas.
     * @receiver a relative position on the canvas
     * @param [scale] the scale
     * @param [centerPosition] the center position
     * @return the absolute position on the canvas
     */
    fun Offset.toAbsoluteXY(scale: Float, centerPosition: Offset): Offset {
        return this * scale + centerPosition
    }

    /**
     * Scales a circle.
     * @receiver a radius of the circle
     * @param [scale] the scale
     * @param [centerPosition] the center position
     * @return the absolute position on the canvas
     */
    fun Float.toAbsoluteXY(scale: Float, centerPosition: Offset): Pair<Offset, Float> {
        val radius = this * scale
        return centerPosition to radius
    }

    /**
     * Calculates the rotate angle of a position with the center position.
     * @receiver a position on the canvas
     * @param [position] a position
     * @param [centerPosition] the center position
     * @return the rotate angle
     */
    fun getAngle(position: Offset, centerPosition: Offset): Float =
        position.let { absXY ->
            val pos = absXY - centerPosition
            toDegrees(atan2(pos.x.toDouble(), -pos.y.toDouble())).toFloat()
        }
}
