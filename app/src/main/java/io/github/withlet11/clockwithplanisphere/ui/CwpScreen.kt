/*
 * CwpScreen.kt
 *
 * Copyright 2026 Yasuhiro Yamakawa <withlet11@gmail.com>
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

package io.github.withlet11.clockwithplanisphere.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize

@Composable
fun CwpScreen(
    modifier: Modifier = Modifier,
    isSouthernSky: Boolean,
    latitude: Double,
    longitude: Double,
    backgroundColor: Int
) {
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    var isZoomed by remember { mutableStateOf(false) }
    var isClockHandsVisible by remember { mutableStateOf(true) }

    val isLandScape = screenSize.width > screenSize.height
    val longSide: Int
    val shortSide: Int
    if (isLandScape) {
        longSide = screenSize.width
        shortSide = screenSize.height
    } else {
        longSide = screenSize.height
        shortSide = screenSize.width
    }
    val totalDifference = (longSide - shortSide).toFloat()
    val scrollBounds: Rect = if (isZoomed) {
        if (isLandScape) {
            Rect(left = 0f, right = 0f, top = -totalDifference, bottom = 0f)
        } else {
            Rect(left = -totalDifference, right = 0f, top = 0f, bottom = 0f)
        }
    } else if (isLandScape) {
        Rect(left = 0f, right = totalDifference, top = 0f, bottom = 0f)
    } else {
        Rect(left = 0f, right = 0f, top = 0f, bottom = totalDifference)
    }

    var offsetXY by remember(screenSize, isZoomed) {
        mutableStateOf(scrollBounds.center)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color(backgroundColor))
            .onSizeChanged { screenSize = it }
    ) {
        ClockScreen(
            modifier = Modifier.fillMaxSize(),
            latitude = latitude,
            longitude = longitude,
            isSouthernSky = isSouthernSky,
            isZoomed = isZoomed,
            onZoomedChanged = { isZoomed = it },
            isClockHandsVisible = isClockHandsVisible,
            onClockHandsVisibilityChanged = { isClockHandsVisible = it },
            isLandScape = isLandScape,
            shortSide = shortSide,
            longSide = longSide,
            scrollBounds = scrollBounds,
            offsetXY = offsetXY,
            onOffsetChanged = { x, y -> offsetXY = Offset(x, y) },
        )
    }
}