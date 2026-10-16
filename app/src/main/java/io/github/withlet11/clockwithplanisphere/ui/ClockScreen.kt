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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import io.github.withlet11.clockwithplanisphere.DEFAULT_LATITUDE
import io.github.withlet11.clockwithplanisphere.DEFAULT_LONGITUDE
import io.github.withlet11.clockwithplanisphere.model.NorthernSkyModel
import io.github.withlet11.clockwithplanisphere.model.SkyViewModel
import io.github.withlet11.clockwithplanisphere.model.SouthernSkyModel
import io.github.withlet11.clockwithplanisphere.view.*
import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.Int

private enum class SwipeStatus { ANYTHING, SUN, SKY_EDGE, DATE }

@Composable
fun ClockScreen(
    modifier: Modifier = Modifier,
    latitude: Double,
    longitude: Double,
    currentTime: MutableState<ZonedDateTime>,
    isCurrentTime: MutableState<Boolean>,
    isSouthernSky: Boolean,
    isZoomed: Boolean,
    toggleZoom: () -> Unit,
    isClockHandsVisible: MutableState<Boolean>,
    isLandScape: Boolean,
    shortSide: Int,
    longSide: Int,
    scrollBounds: Rect,
    offsetXy: Offset,
    onOffsetXyChange: (Offset) -> Unit,
    onTimeUpdate: (ZonedDateTime, LocalTime, LocalTime) -> Unit
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

    val clockBasePanel = remember {
        ClockBasePanel(context, null).apply {
            set(skyViewModel.offset, skyViewModel.direction)
        }
    }

    val clockHandsPanel = remember { ClockHandsPanel(context, null) }

    // Touch and swipe management
    var previousActionXy by remember { mutableStateOf(Offset.Zero) }
    var previousRotate by remember { mutableFloatStateOf(0f) }
    var clickCount by remember { mutableIntStateOf(0) }
    var previousClickTime by remember { mutableLongStateOf(0L) }
    var swipeStatus by remember { mutableStateOf(SwipeStatus.ANYTHING) }

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

        currentTime.value = skyViewModel.getZonedDateTime()

        val solarTime =
            LocalTime.ofSecondOfDay((((solarAngle % 360 + 540) % 360) / 360.0 * 86400).toLong())
        val siderealTime =
            LocalTime.ofSecondOfDay((((siderealAngle % 360 + 360) % 360) / 360.0 * 86400).toLong())

        onTimeUpdate(skyViewModel.utc, solarTime, siderealTime)
    }

    fun refreshClock() {
        if (isClockHandsVisible.value) {
            clockHandsPanel.localTime = skyViewModel.localTime
        }
        refreshPanels()
    }

    LaunchedEffect(offsetXy) {
        clockBasePanel.offsetXy = offsetXy
        sunPanel.offsetXy = offsetXy
        clockHandsPanel.offsetXy = offsetXy
        skyPanel.offsetXy = offsetXy
        sunAndMoonPanel.offsetXy = offsetXy
        horizonPanel.offsetXy = offsetXy
    }

    LaunchedEffect(isSouthernSky, latitude, longitude) {
        skyViewModel.setCurrentTime(currentTime.value)
        refreshPanels()
    }

    if (isCurrentTime.value) {
        skyViewModel.setCurrentTime(currentTime.value)
        refreshClock()
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
        isClockHandsVisible = isClockHandsVisible.value,
        isLandScape = isLandScape,
        shortSide = shortSide,
        longSide = longSide,
        offsetXy = offsetXy,
        onTap = { xy ->
            when {
                clickCount > 0 && SystemClock.elapsedRealtime() - previousClickTime < 300L -> {
                    toggleZoom()
                    clickCount = 0
                    previousClickTime = 0L
                }

                clockHandsPanel.isCenter(xy) -> {
                    clickCount = 0
                    val newVisibility = !isClockHandsVisible.value
                    isClockHandsVisible.value = newVisibility
                    if (newVisibility) {
                        isCurrentTime.value = true
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
        onDragStart = { xy ->
            previousActionXy = xy
            previousRotate = clockBasePanel.getAngle(previousActionXy)

            if (!isClockHandsVisible.value) {
                swipeStatus = when {
                    sunPanel.isOnAnalemma(previousActionXy) -> SwipeStatus.SUN
                    clockBasePanel.isOnTodayGrid(previousActionXy) -> SwipeStatus.DATE
                    clockBasePanel.isOnSkyBackgroundEdge(previousActionXy) -> SwipeStatus.SKY_EDGE
                    else -> SwipeStatus.ANYTHING
                }
            }
        },
        onDrag = { xy ->
            when (swipeStatus) {
                SwipeStatus.SUN -> {
                    val rotate = sunPanel.getAngle(xy)
                    skyViewModel.changeDateWithFixedSiderealTime(rotate)
                    isCurrentTime.value = false
                    refreshPanels()
                }

                SwipeStatus.DATE -> {
                    val rotate = clockBasePanel.getAngleFromJan1(xy)
                    skyViewModel.changeDateWithFixedSolarTime(rotate)
                    isCurrentTime.value = false
                    refreshPanels()
                }

                SwipeStatus.SKY_EDGE -> {
                    val rotate = clockBasePanel.getAngle(xy)
                    skyViewModel.changeSiderealTimeWithFixedDate(rotate - previousRotate)
                    previousRotate = rotate
                    isCurrentTime.value = false
                    refreshPanels()
                }

                else -> {
                    val tempXy = offsetXy + (xy - previousActionXy)

                    onOffsetXyChange(
                        Offset(
                            tempXy.x.coerceIn(scrollBounds.left, scrollBounds.right),
                            tempXy.y.coerceIn(scrollBounds.top, scrollBounds.bottom)
                        )
                    )

                    previousActionXy = xy
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
    shortSide: Int,
    longSide: Int,
    offsetXy: Offset,
    onTap: (Offset) -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    val density = LocalDensity.current

    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

    clockBasePanel.isZoomed = isZoomed
    clockBasePanel.shortSide = shortSide
    clockBasePanel.longSide = longSide

    skyPanel.isZoomed = isZoomed
    skyPanel.shortSide = shortSide
    skyPanel.longSide = longSide

    sunPanel.isZoomed = isZoomed
    sunPanel.shortSide = shortSide
    sunPanel.longSide = longSide

    sunAndMoonPanel.isZoomed = isZoomed
    sunAndMoonPanel.isLandScape = isLandScape
    sunAndMoonPanel.shortSide = shortSide
    sunAndMoonPanel.longSide = longSide

    horizonPanel.isZoomed = isZoomed
    horizonPanel.shortSide = shortSide
    horizonPanel.longSide = longSide

    clockHandsPanel.isZoomed = isZoomed
    clockHandsPanel.shortSide = shortSide
    clockHandsPanel.longSide = longSide

    Box(
        modifier = modifier
            .size(with(density) { longSide.toDp() })
            .pointerInput(isZoomed) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown()
                        val start = down.position

                        currentOnDragStart(start)

                        var isDragging = false

                        val downTime = SystemClock.elapsedRealtime()

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                                ?: break

                            if (!change.pressed) {
                                val end = change.position
                                val elapsed = SystemClock.elapsedRealtime() - downTime
                                val distanceSquared = (start - end).getDistanceSquared()
                                if (!isDragging && elapsed < 200L && distanceSquared < 2500f) {
                                    currentOnTap(end)
                                } else {
                                    currentOnDragEnd()
                                }

                                break
                            }

                            val position = change.position

                            if (!isDragging) {
                                val distanceSquared =
                                    (start - position).getDistanceSquared()
                                isDragging = distanceSquared >= 5f
                            }

                            if (isDragging) {
                                change.consume()
                                currentOnDrag(position)
                            }
                        }
                    }
                }
            }
            .graphicsLayer(
                translationX = offsetXy.x, translationY = offsetXy.y
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
        latitude = DEFAULT_LATITUDE,
        longitude = DEFAULT_LONGITUDE,
        currentTime = remember { mutableStateOf(ZonedDateTime.now()) },
        isCurrentTime = remember { mutableStateOf(false) },
        isSouthernSky = false,
        isZoomed = false,
        toggleZoom = {},
        isClockHandsVisible = remember { mutableStateOf(true) },
        isLandScape = false,
        shortSide = 500,
        longSide = 800,
        offsetXy = Offset.Zero,
        onOffsetXyChange = {},
        scrollBounds = Rect(left = 0f, right = 500f, top = 0f, bottom = 800f),
        onTimeUpdate = { _, _, _ -> }
    )
}
