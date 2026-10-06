/*
 * AbstractCwpFragment.kt
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

package io.github.withlet11.clockwithplanisphere.fragment

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import io.github.withlet11.clockwithplanisphere.CwpTheme
import io.github.withlet11.clockwithplanisphere.ui.ClockContent
import io.github.withlet11.clockwithplanisphere.MainActivity
import io.github.withlet11.clockwithplanisphere.R
import io.github.withlet11.clockwithplanisphere.PeriodicalUpdater
import io.github.withlet11.clockwithplanisphere.model.SkyViewModel
import io.github.withlet11.clockwithplanisphere.view.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

abstract class AbstractCwpFragment : Fragment(), MainActivity.ChangeObserver {
    companion object {
        const val MINIMUM_DEGREE = 0.25f
        const val MINIMUM_SQUARE_DISTANCE = 0.005f * 0.005f
        const val DEFAULT_LATITUDE = 45.0
        const val DEFAULT_LONGITUDE = 0.0
    }

    private enum class SwipeStatus { ANYTHING, SUN, SKY_EDGE, DATE }

    private var locationChangeSubject: MainActivity? = null
    private lateinit var periodicalUpdater: PeriodicalUpdater

    // models
    protected lateinit var skyViewModel: SkyViewModel

    // panels
    lateinit var skyPanel: SkyPanel
        private set
    lateinit var sunPanel: SunPanel
        private set
    lateinit var sunAndMoonPanel: SunAndMoonPanel
        private set
    lateinit var horizonPanel: HorizonPanel
        private set
    lateinit var clockBasePanel: ClockBasePanel
        private set
    lateinit var clockHandsPanel: ClockHandsPanel
        private set

    // compose states
    private var isZoomedState by mutableStateOf(false)
    private var offsetXState by mutableIntStateOf(0)
    private var offsetYState by mutableIntStateOf(0)

    // geometry parameters
    private var isZoomed: Boolean
        get() = isZoomedState
        set(value) {
            isZoomedState = value
            skyPanel.isZoomed = value
            sunPanel.isZoomed = value
            sunAndMoonPanel.isZoomed = value
            horizonPanel.isZoomed = value
            clockBasePanel.isZoomed = value
            clockHandsPanel.isZoomed = value
            adjustFrameLayoutPosition()
            scrollPanelToCenter()
        }

    private var scrollableHorizonMin = 0
    private var scrollableVerticalMin = 0
    private var scrollableHorizonMax = 0
    private var scrollableVerticalMax = 0

    // touch position and time
    private var firstActionX = 0f
    private var firstActionY = 0f
    private var previousActionX = 0f
    private var previousActionY = 0f
    private var previousRotate = 0f
    private var clickCount = 0
    private var previousClickTime = 0L
    private var swipeStatus: SwipeStatus = SwipeStatus.ANYTHING

    // visibility
    private var isClockHandsVisible
        get() = locationChangeSubject?.isClockHandsVisible ?: true
        set(value) {
            clockHandsPanel.isVisible = value
            locationChangeSubject?.isClockHandsVisible = value
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // set this fragment to subject as an observer
        (context as? MainActivity)?.addObserver(this)
        locationChangeSubject = context as? MainActivity
    }

    abstract fun prepareViewModel(
        context: Context,
        latitude: Double,
        longitude: Double
    ): SkyViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        skyPanel = SkyPanel(context, null)
        sunPanel = SunPanel(context, null)
        sunAndMoonPanel = SunAndMoonPanel(context, null)
        horizonPanel = HorizonPanel(context, null)
        clockBasePanel = ClockBasePanel(context, null)
        clockHandsPanel = ClockHandsPanel(context, null)

        return ComposeView(context).apply {
            setContent {
                CwpTheme {
                    ClockContent(
                        clockBasePanel = clockBasePanel,
                        skyPanel = skyPanel,
                        sunPanel = sunPanel,
                        sunAndMoonPanel = sunAndMoonPanel,
                        horizonPanel = horizonPanel,
                        clockHandsPanel = clockHandsPanel,
                        isZoomed = isZoomedState,
                        offsetX = offsetXState,
                        offsetY = offsetYState,
                        onTap = { x, y ->
                            handleTap(x, y)
                        },
                        onDragStart = { x, y ->
                            handleDragStart(x, y)
                        },
                        onDrag = { x, y ->
                            handleDrag(x, y)
                        },
                        onDragEnd = {
                            handleDragEnd()
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get argument
        val args = arguments
        val latitude = args?.getDouble("LATITUDE", DEFAULT_LATITUDE) ?: DEFAULT_LATITUDE
        val longitude = args?.getDouble("LONGITUDE", DEFAULT_LONGITUDE) ?: DEFAULT_LONGITUDE
        clockHandsPanel.isVisible = args?.getBoolean("CLOCK_HANDS_VISIBILITY") ?: true
        val backgroundColor =
            args?.getInt("BACKGROUND_COLOR", resources.getColor(R.color.defaultBackGround, null))
                ?: resources.getColor(R.color.defaultBackGround, null)

        skyViewModel = prepareViewModel(activity?.applicationContext!!, latitude, longitude)

        view.setBackgroundColor(backgroundColor)
        setPeriodicalUpdater()
    }

    override fun onStart() {
        super.onStart()
        setStarDataList()
        setHorizonPanel()
        setClockBasePanel()
        // TODO: 2026-10-06
        adjustFrameLayoutPosition()
        scrollPanelToCenter()
    }

    override fun onResume() {
        super.onResume()
        skyViewModel.changeLocation(skyViewModel.latitude, skyViewModel.longitude)
        updateClock()
        periodicalUpdater.timerSet()
    }

    override fun onPause() {
        periodicalUpdater.stopTimerTask()
        super.onPause()
    }

    override fun onDestroyView() {
        locationChangeSubject?.removeObserver(this)
        super.onDestroyView()
    }

    private fun handleTap(x: Float, y: Float) {
        when {
            clickCount > 0 && SystemClock.elapsedRealtime() - previousClickTime < 200L -> toggleZoom()
            clockHandsPanel.isCenter(x to y) -> showOrHideClockHandsPanel()
            else -> recordFirstClick()
        }
    }

    private fun handleDragStart(x: Float, y: Float) {
        previousActionX = x
        previousActionY = y
        firstActionX = x
        firstActionY = y
        previousRotate = clockBasePanel.getAngle(x, y)

        if (!isClockHandsVisible) {
            swipeStatus = when {
                sunPanel.isOnAnalemma(x to y) -> SwipeStatus.SUN
                clockBasePanel.isOnTodayGrid(x to y) -> SwipeStatus.DATE
                clockBasePanel.isOnSkyBackgroundEdge(x to y) -> SwipeStatus.SKY_EDGE
                else -> SwipeStatus.ANYTHING
            }
        }
    }

    private fun handleDrag(x: Float, y: Float) {
        when (swipeStatus) {
            SwipeStatus.SUN -> {
                val rotate = sunPanel.getAngle(x, y)
                changeDateWithFixedSiderealTime(rotate)
            }

            SwipeStatus.DATE -> {
                val rotate = clockBasePanel.getAngleFromJan1(x, y)
                changeDateWithFixedSolarTime(rotate)
            }

            SwipeStatus.SKY_EDGE -> {
                val rotate = clockBasePanel.getAngle(x, y)
                changeSiderealTimeWithFixedDate(rotate - previousRotate)
                previousRotate = rotate
            }

            else -> {
                scrollPanel(x, y)
                previousActionX = x
                previousActionY = y
                updatePanelOffsets()
            }
        }
    }

    private fun handleDragEnd() {
        swipeStatus = SwipeStatus.ANYTHING
    }

    private fun toggleZoom() {
        isZoomed = !isZoomed
        clickCount = 0
        previousClickTime = 0L
    }

    /** OnClickListener calls this function */
    private fun showOrHideClockHandsPanel() {
        clickCount = 0
        isClockHandsVisible = !isClockHandsVisible
        if (isClockHandsVisible) updateClock() // updates time here
    }

    /** OnClickListener calls this function */
    private fun recordFirstClick() {
        clickCount = 1
        previousClickTime = SystemClock.elapsedRealtime()
    }

    /** Sets [PeriodicalUpdater] as a periodical updater. */
    private fun setPeriodicalUpdater() {
        periodicalUpdater = PeriodicalUpdater(this)
    }

    /** Gets geometries of [clockHandsPanel] and adjust positions of [clockFrame] and panels. */
    private fun adjustFrameLayoutPosition() {
        with(clockHandsPanel) {
            val totalDifference = wideSideLength - narrowSideLength
            val halfOfDifference = totalDifference / 2
            when {
                isZoomed -> {
                    if (clockHandsPanel.isLandScape) {
                        scrollableHorizonMin = 0
                        scrollableHorizonMax = 0
                        scrollableVerticalMin = -totalDifference
                        scrollableVerticalMax = 0
                        0 to -halfOfDifference
                    } else {
                        scrollableHorizonMin = -totalDifference
                        scrollableHorizonMax = 0
                        scrollableVerticalMin = 0
                        scrollableVerticalMax = 0
                        -halfOfDifference to 0
                    }
                }

                clockHandsPanel.isLandScape -> {
                    scrollableHorizonMin = 0
                    scrollableHorizonMax = totalDifference
                    scrollableVerticalMin = 0
                    scrollableVerticalMax = 0
                    halfOfDifference to 0
                }

                else -> {
                    scrollableHorizonMin = 0
                    scrollableHorizonMax = 0
                    scrollableVerticalMin = 0
                    scrollableVerticalMax = totalDifference
                    0 to halfOfDifference
                }
            }
        }.let { (framePositionX, framePositionY) ->
            offsetXState = framePositionX
            offsetYState = framePositionY
            updatePanelOffsets()
        }
    }

    /** Calculates the scroll position from a touch position, and change the positions of panels. */
    private fun scrollPanel(endX: Float, endY: Float) {
        val x = max(
            min(offsetXState + (endX - previousActionX).toInt(), scrollableHorizonMax),
            scrollableHorizonMin
        )
        val y = max(
            min(offsetYState + (endY - previousActionY).toInt(), scrollableVerticalMax),
            scrollableVerticalMin
        )
        offsetXState = x
        offsetYState = y
    }

    private fun scrollPanelToCenter() {
        Log.d("CWP", "scrollPanelToCenter()")
        offsetXState = (scrollableHorizonMax + scrollableHorizonMin) / 2
        offsetYState = (scrollableVerticalMax + scrollableVerticalMin) / 2
        updatePanelOffsets()
    }

    private fun updatePanelOffsets() {
        clockBasePanel.offsetX = offsetXState
        clockBasePanel.offsetY = offsetYState
        sunPanel.offsetX = offsetXState
        sunPanel.offsetY = offsetYState
        clockHandsPanel.offsetX = offsetXState
        clockHandsPanel.offsetY = offsetYState
        skyPanel.offsetX = offsetXState
        skyPanel.offsetY = offsetYState
        sunAndMoonPanel.offsetX = offsetXState
        sunAndMoonPanel.offsetY = offsetYState
        horizonPanel.offsetX = offsetXState
        horizonPanel.offsetY = offsetYState
    }

    private fun setStarDataList() {
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

    private fun setHorizonPanel() {
        with(skyViewModel) { horizonPanel.set(horizon, altAzimuth, directionLetters) }
    }

    private fun setClockBasePanel() {
        with(skyViewModel) { clockBasePanel.set(offset, direction) }
    }

    /** This is called by PeriodicalUpdater only. This shouldn't be called internal */
    fun updateClockIfClockHandsAreVisible() {
        if (isClockHandsVisible) updateClock() // updates time here
    }

    private fun updateClock() {
        skyViewModel.setCurrentTime()
        refreshClock()
    }

    private fun refreshClock() {
        updateClockBasePanel()
        updateClockHandsPanel()
        updateSkyPanel()
        updateSunPanel()
        updateMoonPanel()
    }

    private fun updateClockHandsPanel() {
        clockHandsPanel.localTime = skyViewModel.localTime
    }

    private fun updateClockBasePanel() {
        clockBasePanel.currentDate = skyViewModel.localDate
    }

    private fun updateSkyPanel() {
        val current = skyViewModel.siderealAngle
        val difference = skyPanel.getAngleDifference(current)
        if (difference > MINIMUM_DEGREE)
            skyPanel.siderealAngle = skyViewModel.siderealAngle
    }

    private fun updateSunPanel() {
        if (sunPanel.isDifferentDate(skyViewModel.localDate)) {
            val solarAngle = skyViewModel.solarAngle
            val currentSunPosition = skyViewModel.currentSunPosition
            val angle = sunPanel.getAngleDifference(solarAngle)
            val distance = sunPanel.getDistance(currentSunPosition.first)

            if (angle > MINIMUM_DEGREE || distance > MINIMUM_SQUARE_DISTANCE) {
                sunPanel.setSolarAngle(skyViewModel.solarAngle, skyViewModel.localTime)
                if (sunPanel.isDifferentDate(skyViewModel.localDate) ||
                    sunPanel.isDifferentTime(skyViewModel.localTime.toSecondOfDay())
                ) {
                    with(skyViewModel) {
                        sunPanel.setSolarAngleAndCurrentPosition(
                            solarAngle,
                            currentSunPosition.first,
                            localDateTime
                        )
                    }
                }
            }
        } else if (sunPanel.isDifferentTime(skyViewModel.localTime.toSecondOfDay())) {
            val current = skyViewModel.solarAngle
            val difference = sunPanel.getAngleDifference(current)
            if (difference > MINIMUM_DEGREE) {
                sunPanel.setSolarAngle(current, skyViewModel.localTime)
            }
        }
    }

    private fun updateMoonPanel() {
        val siderealAngle = skyViewModel.siderealAngle
        val difference = sunAndMoonPanel.getAngleDifference(siderealAngle)
        if (sunAndMoonPanel.isDifferentDate(skyViewModel.localDate) || difference > MINIMUM_DEGREE) {
            with(skyViewModel) {
                sunAndMoonPanel.setSolarAngleAndCurrentPosition(
                    solarAngle,
                    siderealAngle,
                    currentMoonPosition,
                    currentSunPosition.second,
                    localDateTime
                )
            }
        }
    }

    /**
     * Receives location change signal from MainActivity.
     * Implementation of [MainActivity.ChangeObserver.onLocationChange]
     */
    override fun onLocationChange(latitude: Double, longitude: Double) {
        changeLocation(latitude, longitude)
    }

    /** Sets location to [skyViewModel] and [horizonPanel]. */
    private fun changeLocation(latitude: Double, longitude: Double) {
        skyViewModel.changeLocation(latitude, longitude)
        setStarDataList()
        setHorizonPanel()
        updateClock()
    }

    /**
     * Receives color change signal from MainActivity.
     * Implementation of [MainActivity.ChangeObserver.onColorChange]
     */
    override fun onColorChange(backgroundColor: Int) {
        view?.setBackgroundColor(backgroundColor)
    }

    /**
     * Changes date by using the rotate angle of the Sun (solar time) with fixed sidereal time
     * and update the sky view.
     * @param rotate the rotate angle of the Sun (degrees)
     */
    private fun changeDateWithFixedSiderealTime(rotate: Float) {
        if (abs(rotate) > MINIMUM_DEGREE) {
            skyViewModel.changeDateWithFixedSiderealTime(rotate)
            updateClockBasePanel()
            updateSkyPanel()
            updateSunPanel()
            updateMoonPanel()
        }
    }

    /**
     * Changes date by using the sidereal time with fixed solar time
     * and update the sky view.
     * @param rotate the sidereal angle (degrees)
     */
    private fun changeDateWithFixedSolarTime(rotate: Float) {
        if (abs(rotate) > MINIMUM_DEGREE) {
            skyViewModel.changeDateWithFixedSolarTime(rotate)
            updateClockBasePanel()
            updateSkyPanel()
            updateSunPanel()
            updateMoonPanel()
        }
    }

    /**
     * Changes sidereal time by setting local time with fixed date and update the sky view.
     * @param rotate the rotate angle of the Sun (degrees)
     */
    private fun changeSiderealTimeWithFixedDate(rotate: Float) {
        if (abs(rotate) > MINIMUM_DEGREE) {
            skyViewModel.changeSiderealTimeWithFixedDate(rotate)
            updateSkyPanel()
            updateSunPanel()
            updateMoonPanel()
        }
    }
}
