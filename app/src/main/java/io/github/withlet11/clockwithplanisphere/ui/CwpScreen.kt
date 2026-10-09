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

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize

@Composable
fun CwpScreen(
    isSouthernSky: Boolean,
    latitude: Double,
    longitude: Double,
    isClockHandsVisible: Boolean,
    onClockHandsVisibilityChanged: (Boolean) -> Unit,
    backgroundColor: Int,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableIntStateOf(0) }
    var offsetY by remember { mutableIntStateOf(0) }

    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    var isZoomed by remember { mutableStateOf(false) }

    val isLandScape = screenSize.width > screenSize.height
    val wideSideLength: Int
    val narrowSideLength: Int
    if (isLandScape) {
        wideSideLength = screenSize.width
        narrowSideLength = screenSize.height
    } else {
        wideSideLength = screenSize.height
        narrowSideLength = screenSize.width
    }
    val totalDifference = wideSideLength - narrowSideLength

    val scrollableHorizonMin: Int
    val scrollableHorizonMax: Int
    val scrollableVerticalMin: Int
    val scrollableVerticalMax: Int
    if (isZoomed) {
        if (isLandScape) {
            scrollableHorizonMin = 0
            scrollableHorizonMax = 0
            scrollableVerticalMin = -totalDifference
            scrollableVerticalMax = 0
        } else {
            scrollableHorizonMin = -totalDifference
            scrollableHorizonMax = 0
            scrollableVerticalMin = 0
            scrollableVerticalMax = 0
        }
    } else if (isLandScape) {
        scrollableHorizonMin = 0
        scrollableHorizonMax = totalDifference
        scrollableVerticalMin = 0
        scrollableVerticalMax = 0
    } else {
        scrollableHorizonMin = 0
        scrollableHorizonMax = 0
        scrollableVerticalMin = 0
        scrollableVerticalMax = totalDifference
    }

    LaunchedEffect(screenSize, isZoomed) {
        if (screenSize != IntSize.Zero) {
            offsetX = (scrollableHorizonMax + scrollableHorizonMin) / 2
            offsetY = (scrollableVerticalMax + scrollableVerticalMin) / 2
        }
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
            onClockHandsVisibilityChanged = onClockHandsVisibilityChanged,
            isLandScape = isLandScape,
            narrowSideLength = narrowSideLength,
            wideSideLength = wideSideLength,
            scrollableHorizonMin = scrollableHorizonMin,
            scrollableHorizonMax = scrollableHorizonMax,
            scrollableVerticalMin = scrollableVerticalMin,
            scrollableVerticalMax = scrollableVerticalMax,
            offsetX = offsetX,
            offsetY = offsetY,
            onChangeOffset = { newX, newY ->
                offsetX = newX
                offsetY = newY
            },
        )
    }
}
