/*
 * AbstractPanel.kt
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

import java.lang.Math.toDegrees
import kotlin.math.*

object AbstractPanel {
    const val PREFERRED_SIZE = 800f
    const val CENTER = PREFERRED_SIZE * 0.5f
    private const val CIRCLE_RADIUS = PREFERRED_SIZE * 0.4f
    const val MOON_AGE_RING_RADIUS = 40f
    const val MOON_AGE_RING_THICKNESS = 20f
    const val MOON_AGE_HAND_THICKNESS = 5f
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
    fun Pair<Float, Float>.isNear(center: Pair<Float, Float>, radius: Float): Boolean {
        val r2 = (first - center.first).pow(2) + (second - center.second).pow(2)
        val minR2 = (radius - 50).pow(2)
        val maxR2 = (radius + 50).pow(2)
        return r2 > minR2 && r2 < maxR2
    }

    /**
     * Checks if a position and other is near on the canvas.
     * @receiver a position on the canvas
     * @return true if 2 positions are near
     */
    fun Pair<Float, Float>.isNear(other: Pair<Float, Float>): Boolean =
        abs(first - other.first) < 50 && abs(second - other.second) < 50

    /**
     * Converts a relative position to the absolute position on the canvas with a rotate angle.
     * @receiver a relative position on the canvas
     * @param [rotate] the rotate angle
     * @param [scale] the scale
     * @param [centerPosition] the center position
     * @return the absolute position on the canvas
     */
    fun Pair<Float, Float>.toAbsoluteXY(rotate: Float, scale: Float, centerPosition: Pair<Float, Float>): Pair<Float, Float> {
        val x = first.toCanvas()
        val y = second.toCanvas()
        val rad = Math.toRadians(rotate.toDouble())
        val absoluteX = (x * cos(rad) - y * sin(rad)).toFloat() * scale + centerPosition.first
        val absoluteY = (x * sin(rad) + y * cos(rad)).toFloat() * scale + centerPosition.second
        return absoluteX to absoluteY
    }

    /**
     * Converts a relative position to the absolute position on the canvas.
     * @receiver a relative position on the canvas
     * @param [scale] the scale
     * @param [centerPosition] the center position
     * @return the absolute position on the canvas
     */
    fun Pair<Float, Float>.toAbsoluteXY(scale: Float, centerPosition: Pair<Float, Float>): Pair<Float, Float> {
        val absoluteX = first * scale + centerPosition.first
        val absoluteY = second * scale + centerPosition.second
        return absoluteX to absoluteY
    }

    /**
     * Scales a circle.
     * @receiver a radius of the circle
     * @param [scale] the scale
     * @param [centerPosition] the center position
     * @return the absolute position on the canvas
     */
    fun Float.toAbsoluteXY(scale: Float, centerPosition: Pair<Float, Float>): Pair<Pair<Float, Float>, Float> {
        val radius = this * scale
        return centerPosition to radius
    }

    /**
     * Calculates the rotate angle of a position with the center position.
     * @receiver a position on the canvas
     * @param [x] the x position
     * @param [y] the y position
     * @param [centerPosition] the center position
     * @return the rotate angle
     */
    fun getAngle(x: Float, y: Float, centerPosition: Pair<Float, Float>): Float =
        (x to y).let { (absX, absY) ->
            toDegrees(
                atan2(
                    (absX - centerPosition.first).toDouble(),
                    -(absY - centerPosition.second).toDouble()
                )
            ).toFloat()
        }
}
