/*
 * ClockScreen.kt
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

package io.github.withlet11.clockwithplanisphere.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import io.github.withlet11.clockwithplanisphere.view.*

@Composable
fun ClockScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clockBasePanel = remember {
        ClockBasePanel(context, null).apply {
            currentDate = java.time.LocalDate.now()
        }
    }
    val skyPanel = remember { SkyPanel(context, null) }
    val sunPanel = remember { SunPanel(context, null) }
    val sunAndMoonPanel = remember { SunAndMoonPanel(context, null) }
    val horizonPanel = remember { HorizonPanel(context, null) }
    val clockHandsPanel = remember {
        ClockHandsPanel(context, null).apply {
            localTime = java.time.LocalTime.MIDNIGHT
        }
    }

    ClockContent(
        clockBasePanel = clockBasePanel,
        skyPanel = skyPanel,
        sunPanel = sunPanel,
        sunAndMoonPanel = sunAndMoonPanel,
        horizonPanel = horizonPanel,
        clockHandsPanel = clockHandsPanel
    )
}

@Composable
fun ClockContent(
    modifier: Modifier = Modifier,
    clockBasePanel: ClockBasePanel,
    skyPanel: SkyPanel,
    sunPanel: SunPanel,
    sunAndMoonPanel: SunAndMoonPanel,
    horizonPanel: HorizonPanel,
    clockHandsPanel: ClockHandsPanel,
    isZoomed: Boolean = false,
    offsetX: Int = 0,
    offsetY: Int = 0,
    onTap: (Float, Float) -> Unit = { _, _ -> },
    onDoubleTap: () -> Unit = {},
    onDragStart: (Float, Float) -> Unit = { _, _ -> },
    onDrag: (Float, Float) -> Unit = { _, _ -> },
    onDragEnd: () -> Unit = {}
) {
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val isLandScape = width > height
        val narrow = if (isLandScape) height else width
        val wide = if (isLandScape) width else height

        clockBasePanel.isZoomed = isZoomed
        clockBasePanel.isLandScape = isLandScape
        clockBasePanel.narrowSideLength = narrow
        clockBasePanel.wideSideLength = wide

        skyPanel.isZoomed = isZoomed
        skyPanel.isLandScape = isLandScape
        skyPanel.narrowSideLength = narrow
        skyPanel.wideSideLength = wide

        sunPanel.isZoomed = isZoomed
        sunPanel.isLandScape = isLandScape
        sunPanel.narrowSideLength = narrow
        sunPanel.wideSideLength = wide

        sunAndMoonPanel.isZoomed = isZoomed
        sunAndMoonPanel.isLandScape = isLandScape
        sunAndMoonPanel.narrowSideLength = narrow
        sunAndMoonPanel.wideSideLength = wide

        horizonPanel.isZoomed = isZoomed
        horizonPanel.isLandScape = isLandScape
        horizonPanel.narrowSideLength = narrow
        horizonPanel.wideSideLength = wide

        clockHandsPanel.isZoomed = isZoomed
        clockHandsPanel.isLandScape = isLandScape
        clockHandsPanel.narrowSideLength = narrow
        clockHandsPanel.wideSideLength = wide

        Box(
            modifier = Modifier
                .size(with(density) { wide.toDp() })
                .graphicsLayer(
                    translationX = offsetX.toFloat(), translationY = offsetY.toFloat()
                )
                .pointerInput(isZoomed) {
                    detectTapGestures(onTap = { offset ->
                        onTap(offset.x, offset.y)
                    }, onDoubleTap = {
                        onDoubleTap()
                    })
                }
                .pointerInput(isZoomed) {
                    detectDragGestures(onDragStart = { offset ->
                        onDragStart(offset.x, offset.y)
                    }, onDrag = { change, _ ->
                        change.consume()
                        onDrag(change.position.x, change.position.y)
                    }, onDragEnd = {
                        onDragEnd()
                    })
                }, contentAlignment = Alignment.Center
        ) {
            clockBasePanel.Content(Modifier.fillMaxSize())
            skyPanel.Content(Modifier.fillMaxSize())
            sunPanel.Content(Modifier.fillMaxSize())
            sunAndMoonPanel.Content(Modifier.fillMaxSize())
            horizonPanel.Content(Modifier.fillMaxSize())
            clockHandsPanel.Content(Modifier.fillMaxSize())
        }
    }
}

@Preview
@Composable
fun ClockScreenPreview() {
    ClockScreen()
}
