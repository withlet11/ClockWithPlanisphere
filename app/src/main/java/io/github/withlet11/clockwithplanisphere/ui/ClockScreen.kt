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

import android.os.SystemClock
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import io.github.withlet11.clockwithplanisphere.model.NorthernSkyModel
import io.github.withlet11.clockwithplanisphere.model.SkyViewModel
import io.github.withlet11.clockwithplanisphere.model.SouthernSkyModel
import io.github.withlet11.clockwithplanisphere.view.*
import kotlinx.coroutines.delay
import kotlin.Int
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds

private enum class SwipeStatus { ANYTHING, SUN, SKY_EDGE, DATE }

@Composable
fun ClockScreen(
    modifier: Modifier = Modifier,
    latitude: Double,
    longitude: Double,
    isSouthernSky: Boolean,
    isZoomed: Boolean,
    onZoomedChanged: (Boolean) -> Unit,
    isClockHandsVisible: Boolean,
    onClockHandsVisibilityChanged: (Boolean) -> Unit,
    isLandScape: Boolean,
    narrowSideLength: Int,
    wideSideLength: Int,
    scrollableHorizonMin: Int,
    scrollableHorizonMax: Int,
    scrollableVerticalMin: Int,
    scrollableVerticalMax: Int,
    offsetX: Int,
    offsetY: Int,
    onOffsetChanged: (Int, Int) -> Unit,
) {
    val context = LocalContext.current

    val skyViewModel = remember(isSouthernSky, latitude, longitude) {
        SkyViewModel(
            context,
            if (isSouthernSky) SouthernSkyModel() else NorthernSkyModel(),
            latitude,
            longitude
        )
    }

    val skyPanel = remember(isSouthernSky, latitude, longitude) {
        SkyPanel(context, null).apply {
            set(
                skyViewModel.starGeometryList,
                skyViewModel.constellationLineList,
                skyViewModel.milkyWayDotList,
                skyViewModel.milkyWayDotSize,
                skyViewModel.equatorial,
                skyViewModel.ecliptic,
                skyViewModel.tenMinuteGridStep
            )
        }
    }

    val sunPanel = remember(isSouthernSky, latitude, longitude) {
        SunPanel(context, null).apply {
            set(
                skyViewModel.analemma,
                skyViewModel.monthlySunPositionList,
                skyViewModel.currentSunPosition.first,
                skyViewModel.tenMinuteGridStep
            )
        }
    }
    val sunAndMoonPanel = remember(isSouthernSky, latitude, longitude) {
        SunAndMoonPanel(context, null).apply {
            set(
                skyViewModel.currentMoonPosition,
                skyViewModel.currentSunPosition.second,
                skyViewModel.tenMinuteGridStep
            )
        }
    }

    val horizonPanel = remember(isSouthernSky, latitude, longitude) {
        HorizonPanel(context, null).apply {
            set(skyViewModel.horizon, skyViewModel.altAzimuth, skyViewModel.directionLetters)
        }
    }

    val clockBasePanel = remember(isSouthernSky, latitude, longitude) {
        ClockBasePanel(context, null).apply {
            set(skyViewModel.offset, skyViewModel.direction)
        }
    }

    val clockHandsPanel = remember {
        ClockHandsPanel(context, null)
    }

    // Touch and swipe management
    var previousActionX by remember { mutableFloatStateOf(0f) }
    var previousActionY by remember { mutableFloatStateOf(0f) }
    var previousRotate by remember { mutableFloatStateOf(0f) }
    var clickCount by remember { mutableIntStateOf(0) }
    var previousClickTime by remember { mutableLongStateOf(0L) }
    var swipeStatus by remember { mutableStateOf(SwipeStatus.ANYTHING) }

    LaunchedEffect(offsetX, offsetY) {
        clockBasePanel.offsetX = offsetX
        clockBasePanel.offsetY = offsetY
        sunPanel.offsetX = offsetX
        sunPanel.offsetY = offsetY
        clockHandsPanel.offsetX = offsetX
        clockHandsPanel.offsetY = offsetY
        skyPanel.offsetX = offsetX
        skyPanel.offsetY = offsetY
        sunAndMoonPanel.offsetX = offsetX
        sunAndMoonPanel.offsetY = offsetY
        horizonPanel.offsetX = offsetX
        horizonPanel.offsetY = offsetY
    }

    fun refreshPanels() {
        clockBasePanel.currentDate = skyViewModel.localDate

        // update sky panel
        val currentSidereal = skyViewModel.siderealAngle
        if (skyPanel.getAngleDifference(currentSidereal) > 0.25f) {
            skyPanel.siderealAngle = currentSidereal
        }

        // update sun panel
        val solarDate = skyViewModel.localDate
        val solarAngle = skyViewModel.solarAngle
        val currentSunPos = skyViewModel.currentSunPosition
        if (sunPanel.isDifferentDate(solarDate)) {
            if (sunPanel.getAngleDifference(solarAngle) > 0.25f || sunPanel.getDistance(
                    currentSunPos.first
                ) > 0.005f * 0.005f
            ) {
                sunPanel.setSolarAngle(solarAngle, skyViewModel.localTime)
                if (sunPanel.isDifferentDate(solarDate) || sunPanel.isDifferentTime(skyViewModel.localTime.toSecondOfDay())) {
                    sunPanel.setSolarAngleAndCurrentPosition(
                        solarAngle,
                        currentSunPos.first,
                        skyViewModel.localDateTime
                    )
                }
            }
        } else if (sunPanel.isDifferentTime(skyViewModel.localTime.toSecondOfDay())) {
            val currentSunAngle = skyViewModel.solarAngle
            if (sunPanel.getAngleDifference(currentSunAngle) > 0.25f) {
                sunPanel.setSolarAngle(currentSunAngle, skyViewModel.localTime)
            }
        }

        // update moon panel
        val siderealAngle = skyViewModel.siderealAngle
        if (sunAndMoonPanel.isDifferentDate(solarDate) || sunAndMoonPanel.getAngleDifference(
                siderealAngle
            ) > 0.25f
        ) {
            sunAndMoonPanel.setSolarAngleAndCurrentPosition(
                solarAngle,
                siderealAngle,
                skyViewModel.currentMoonPosition,
                skyViewModel.currentSunPosition.second,
                skyViewModel.localDateTime
            )
        }
    }

    fun refreshClock() {
        clockHandsPanel.localTime = skyViewModel.localTime
        refreshPanels()
    }

    LaunchedEffect(isSouthernSky) {
        refreshPanels()
    }

    LaunchedEffect(isClockHandsVisible) {
        if (isClockHandsVisible) {
            while (true) {
                skyViewModel.setCurrentTime()
                refreshClock()
                delay(250L.milliseconds)
            }
        }
    }

    ClockContent(
        modifier = modifier,
        clockBasePanel = clockBasePanel,
        skyPanel = skyPanel,
        sunPanel = sunPanel,
        sunAndMoonPanel = sunAndMoonPanel,
        horizonPanel = horizonPanel,
        clockHandsPanel = clockHandsPanel,
        isZoomed = isZoomed,
        isClockHandsVisible = isClockHandsVisible,
        isLandScape = isLandScape,
        narrowSideLength = narrowSideLength,
        wideSideLength = wideSideLength,
        offsetX = offsetX,
        offsetY = offsetY,
        onTap = { x, y ->
            when {
                clickCount > 0 && SystemClock.elapsedRealtime() - previousClickTime < 200L -> {
                    onZoomedChanged(!isZoomed)
                    clickCount = 0
                    previousClickTime = 0L
                }

                clockHandsPanel.isCenter(x to y) -> {
                    clickCount = 0
                    val newVisibility = !isClockHandsVisible
                    onClockHandsVisibilityChanged(newVisibility)
                    if (newVisibility) {
                        skyViewModel.setCurrentTime()
                        refreshClock()
                    }
                }

                else -> {
                    clickCount = 1
                    previousClickTime = SystemClock.elapsedRealtime()
                }
            }
        },
        onDragStart = { x, y ->
            previousActionX = x
            previousActionY = y
            previousRotate = clockBasePanel.getAngle(x, y)

            if (!isClockHandsVisible) {
                swipeStatus = when {
                    sunPanel.isOnAnalemma(x to y) -> SwipeStatus.SUN
                    clockBasePanel.isOnTodayGrid(x to y) -> SwipeStatus.DATE
                    clockBasePanel.isOnSkyBackgroundEdge(x to y) -> SwipeStatus.SKY_EDGE
                    else -> SwipeStatus.ANYTHING
                }
            }
        },
        onDrag = { x, y ->
            when (swipeStatus) {
                SwipeStatus.SUN -> {
                    val rotate = sunPanel.getAngle(x, y)
                    skyViewModel.changeDateWithFixedSiderealTime(rotate)
                    refreshPanels()
                }

                SwipeStatus.DATE -> {
                    val rotate = clockBasePanel.getAngleFromJan1(x, y)
                    skyViewModel.changeDateWithFixedSolarTime(rotate)
                    refreshPanels()
                }

                SwipeStatus.SKY_EDGE -> {
                    val rotate = clockBasePanel.getAngle(x, y)
                    skyViewModel.changeSiderealTimeWithFixedDate(
                        rotate - previousRotate
                    )
                    previousRotate = rotate
                    refreshPanels()
                }

                else -> {
                    val newX = max(
                        min(
                            offsetX + (x - previousActionX).toInt(),
                            scrollableHorizonMax
                        ),
                        scrollableHorizonMin
                    )

                    val newY = max(
                        min(
                            offsetY + (y - previousActionY).toInt(),
                            scrollableVerticalMax
                        ),
                        scrollableVerticalMin
                    )

                    onOffsetChanged(newX, newY)

                    previousActionX = x
                    previousActionY = y
                }
            }
        },
        onDragEnd = {
            swipeStatus = SwipeStatus.ANYTHING
        },
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
    isZoomed: Boolean,
    isClockHandsVisible: Boolean,
    isLandScape: Boolean,
    narrowSideLength: Int,
    wideSideLength: Int,
    offsetX: Int,
    offsetY: Int,
    onTap: (Float, Float) -> Unit,
    onDragStart: (Float, Float) -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val density = LocalDensity.current

    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

    clockBasePanel.isZoomed = isZoomed
    clockBasePanel.isLandScape = isLandScape
    clockBasePanel.narrowSideLength = narrowSideLength
    clockBasePanel.wideSideLength = wideSideLength

    skyPanel.isZoomed = isZoomed
    skyPanel.isLandScape = isLandScape
    skyPanel.narrowSideLength = narrowSideLength
    skyPanel.wideSideLength = wideSideLength

    sunPanel.isZoomed = isZoomed
    sunPanel.isLandScape = isLandScape
    sunPanel.narrowSideLength = narrowSideLength
    sunPanel.wideSideLength = wideSideLength

    sunAndMoonPanel.isZoomed = isZoomed
    sunAndMoonPanel.isLandScape = isLandScape
    sunAndMoonPanel.narrowSideLength = narrowSideLength
    sunAndMoonPanel.wideSideLength = wideSideLength

    horizonPanel.isZoomed = isZoomed
    horizonPanel.isLandScape = isLandScape
    horizonPanel.narrowSideLength = narrowSideLength
    horizonPanel.wideSideLength = wideSideLength

    clockHandsPanel.isZoomed = isZoomed
    clockHandsPanel.isLandScape = isLandScape
    clockHandsPanel.narrowSideLength = narrowSideLength
    clockHandsPanel.wideSideLength = wideSideLength

    Box(
        modifier = modifier
            .size(with(density) { wideSideLength.toDp() })
            .pointerInput(isZoomed) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown()
                        val start = down.position

                        currentOnDragStart(start.x, start.y)

                        var isDragging = false

                        val downTime = SystemClock.elapsedRealtime()

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                                ?: break

                            if (!change.pressed) {
                                val end = change.position
                                val elapsed = SystemClock.elapsedRealtime() - downTime
                                val distanceSquared =
                                    (start.x - end.x).pow(2) +
                                            (start.y - end.y).pow(2)

                                if (!isDragging && elapsed < 200L && distanceSquared < 50f) {
                                    currentOnTap(end.x, end.y)
                                } else {
                                    currentOnDragEnd()
                                }

                                break
                            }

                            val position = change.position

                            if (!isDragging) {
                                val distanceSquared =
                                    (start.x - position.x).pow(2) +
                                            (start.y - position.y).pow(2)

                                if (distanceSquared >= 5f) {
                                    isDragging = true
                                }
                            }

                            if (isDragging) {
                                change.consume()
                                currentOnDrag(position.x, position.y)
                            }
                        }
                    }
                }
            }
            .graphicsLayer(
                translationX = offsetX.toFloat(), translationY = offsetY.toFloat()
            ),
        contentAlignment = Alignment.Center
    ) {
        clockBasePanel.Content(Modifier.fillMaxSize())
        skyPanel.Content(Modifier.fillMaxSize())
        sunPanel.Content(Modifier.fillMaxSize())
        sunAndMoonPanel.Content(Modifier.fillMaxSize())
        horizonPanel.Content(Modifier.fillMaxSize())
        if (isClockHandsVisible) {
            clockHandsPanel.Content(Modifier.fillMaxSize())
        }
    }
}

@Preview
@Composable
fun ClockScreenPreview() {
    ClockScreen(
        latitude = 45.0,
        longitude = 0.0,
        isSouthernSky = false,

        isZoomed = false,
        onZoomedChanged = {},
        isClockHandsVisible = true,
        onClockHandsVisibilityChanged = {},
        isLandScape = false,
        narrowSideLength = 500,
        wideSideLength = 800,
        offsetX = 0,
        offsetY = 0,
        scrollableHorizonMin = 0,
        scrollableHorizonMax = 500,
        scrollableVerticalMin = 0,
        scrollableVerticalMax = 800,
        onOffsetChanged = { _, _ -> },
    )
}
