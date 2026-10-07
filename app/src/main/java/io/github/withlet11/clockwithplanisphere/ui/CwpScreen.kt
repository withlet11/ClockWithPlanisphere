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

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.withlet11.clockwithplanisphere.model.*
import io.github.withlet11.clockwithplanisphere.view.*
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

private enum class SwipeStatus { ANYTHING, SUN, SKY_EDGE, DATE }

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
    val context = LocalContext.current

    val skyViewModel = remember(isSouthernSky) {
        SkyViewModel(
            context,
            if (isSouthernSky) SouthernSkyModel() else NorthernSkyModel(),
            latitude,
            longitude
        )
    }

    val skyPanel = remember(skyViewModel) { SkyPanel(context, null) }
    val sunPanel = remember(skyViewModel) { SunPanel(context, null) }
    val sunAndMoonPanel = remember(skyViewModel) { SunAndMoonPanel(context, null) }
    val horizonPanel = remember(skyViewModel) { HorizonPanel(context, null) }
    val clockBasePanel = remember(skyViewModel) { ClockBasePanel(context, null) }
    val clockHandsPanel = remember(skyViewModel) {
        ClockHandsPanel(context, null)
    }

    var isZoomed by remember { mutableStateOf(false) }

    var offsetX by remember { mutableIntStateOf(0) }
    var offsetY by remember { mutableIntStateOf(0) }

    var scrollableHorizonMin by remember { mutableIntStateOf(0) }
    var scrollableHorizonMax by remember { mutableIntStateOf(0) }
    var scrollableVerticalMin by remember { mutableIntStateOf(0) }
    var scrollableVerticalMax by remember { mutableIntStateOf(0) }

    // Touch and swipe management
    var previousActionX by remember { mutableFloatStateOf(0f) }
    var previousActionY by remember { mutableFloatStateOf(0f) }
    var previousRotate by remember { mutableFloatStateOf(0f) }

    var clickCount by remember { mutableIntStateOf(0) }
    var previousClickTime by remember { mutableLongStateOf(0L) }

    var swipeStatus by remember { mutableStateOf(SwipeStatus.ANYTHING) }

    fun updatePanelOffsets() {
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

    fun scrollPanelToCenter() {
        offsetX = (scrollableHorizonMax + scrollableHorizonMin) / 2
        offsetY = (scrollableVerticalMax + scrollableVerticalMin) / 2
        updatePanelOffsets()
    }

    fun adjustFrameLayoutPosition(
        isLandScape: Boolean,
        narrowSideLength: Int,
        wideSideLength: Int
    ) {
        val totalDifference = wideSideLength - narrowSideLength
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
    }

    fun setStarDataList() {
        with(skyViewModel) {
            skyPanel.set(
                starGeometryList,
                constellationLineList,
                milkyWayDotList,
                milkyWayDotSize,
                equatorial,
                ecliptic,
                tenMinuteGridStep
            )
            sunPanel.set(
                analemma,
                monthlySunPositionList,
                currentSunPosition.first,
                tenMinuteGridStep
            )
            sunAndMoonPanel.set(
                currentMoonPosition,
                currentSunPosition.second,
                tenMinuteGridStep
            )
        }
    }

    fun setHorizonPanel() {
        with(skyViewModel) { horizonPanel.set(horizon, altAzimuth, directionLetters) }
    }

    fun setClockBasePanel() {
        with(skyViewModel) { clockBasePanel.set(offset, direction) }
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

    LaunchedEffect(isSouthernSky, latitude, longitude) {
        skyViewModel.changeLocation(latitude, longitude)
        setStarDataList()
        setHorizonPanel()
        setClockBasePanel()
        skyViewModel.setCurrentTime()
        refreshClock()
    }

    LaunchedEffect(isClockHandsVisible) {
        if (isClockHandsVisible) {
            while (true) {
                skyViewModel.setCurrentTime()
                refreshClock()
                delay(250L)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color(backgroundColor))
    ) {
        ClockContent(
            modifier = Modifier.fillMaxSize(),
            clockBasePanel = clockBasePanel,
            skyPanel = skyPanel,
            sunPanel = sunPanel,
            sunAndMoonPanel = sunAndMoonPanel,
            horizonPanel = horizonPanel,
            clockHandsPanel = clockHandsPanel,
            isZoomed = isZoomed,
            isClockHandsVisible = isClockHandsVisible,
            offsetX = offsetX,
            offsetY = offsetY,
            onTap = { x, y ->
                when {
                    clickCount > 0 && SystemClock.elapsedRealtime() - previousClickTime < 200L -> {
                        isZoomed = !isZoomed
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

                        offsetX = newX
                        offsetY = newY

                        previousActionX = x
                        previousActionY = y

                        updatePanelOffsets()
                    }
                }
            },
            onDragEnd = {
                swipeStatus = SwipeStatus.ANYTHING
            },
            onGeometryReady = { geometry ->
                adjustFrameLayoutPosition(
                    geometry.isLandScape,
                    geometry.narrow,
                    geometry.wide
                )
                scrollPanelToCenter()
            }
        )
    }
}
