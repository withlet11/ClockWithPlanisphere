/*
 * CwpWidget.kt
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

package io.github.withlet11.clockwithplanisphere.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withSave
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.github.withlet11.clockwithplanisphere.MainActivity
import io.github.withlet11.clockwithplanisphere.R
import io.github.withlet11.clockwithplanisphere.model.NorthernSkyModel
import io.github.withlet11.clockwithplanisphere.model.SkyViewModel
import io.github.withlet11.clockwithplanisphere.model.SouthernSkyModel
import io.github.withlet11.clockwithplanisphere.widget.AbstractPanel.Companion.CENTER
import io.github.withlet11.clockwithplanisphere.widget.AbstractPanel.Companion.PREFERRED_SIZE

class CwpWidget : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CwpWidgetContent()

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WorkManager.getInstance(context).cancelUniqueWork("CwpWidgetClockUpdate")
    }

    companion object {
        fun loadPreviousPosition(context: Context): Triple<Double, Double, Boolean> {
            var latitude: Double
            var longitude: Double
            var isSouthernSky: Boolean
            val previous =
                context.getSharedPreferences("observation_position", Context.MODE_PRIVATE)

            try {
                latitude = previous.getFloat("latitude", 0f).toDouble()
                longitude = previous.getFloat("longitude", 0f).toDouble()
                isSouthernSky = previous.getBoolean("isSouthernSky", false)
            } catch (_: ClassCastException) {
                latitude = 0.0
                longitude = 0.0
                isSouthernSky = false
            }
            return Triple(latitude, longitude, isSouthernSky)
        }
    }
}

class CwpWidgetContent : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        scheduleNextUpdate(context)

        val bitmap = withContext(Dispatchers.Default) {
            generateClockBitmap(context)
        }

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(actionStartActivity<MainActivity>())
            ) {
                Image(
                    provider = ImageProvider(bitmap),
                    contentDescription = context.getString(R.string.app_name),
                    modifier = GlanceModifier.fillMaxSize()
                )
            }
        }
    }

    private fun generateClockBitmap(context: Context): Bitmap {
        val widthPx = PREFERRED_SIZE.toInt()
        val heightPx = PREFERRED_SIZE.toInt()
        val bitmap = createBitmap(widthPx, heightPx)
        val canvas = Canvas(bitmap)

        val (latitude, longitude, isSouthernSky) = CwpWidget.loadPreviousPosition(context)
        val skyViewModel =
            SkyViewModel(
                context,
                if (isSouthernSky) SouthernSkyModel() else NorthernSkyModel(),
                latitude,
                longitude
            )
        val clockBasePanel = ClockBasePanel(context)
        val skyPanel = SkyPanel(context)
        val sunAndMoonPanel = SunAndMoonPanel(context)
        val horizonPanel = HorizonPanel(context)
        val clockHandsPanel = ClockHandsPanel(context)

        with(skyViewModel) {
            clockBasePanel.set(offset, direction)
            skyPanel.set(
                starGeometryList,
                constellationLineList,
                milkyWayDotList,
                milkyWayDotSize,
                equatorial,
                ecliptic,
                tenMinuteGridStep
            )
            sunAndMoonPanel.set(
                analemma,
                monthlySunPositionList,
                currentSunPosition,
                currentMoonPosition,
                tenMinuteGridStep
            )
            horizonPanel.set(horizon, altAzimuth, directionLetters)
        }

        skyViewModel.setCurrentTime()
        clockBasePanel.currentDate = skyViewModel.localDate
        clockHandsPanel.localTime = skyViewModel.localTime
        skyPanel.siderealAngle = skyViewModel.siderealAngle
        sunAndMoonPanel.solarAngle = skyViewModel.solarAngle
        sunAndMoonPanel.siderealAngle = skyViewModel.siderealAngle

        // Draw panels
        canvas.withSave {
            translate(CENTER, CENTER)
            clockBasePanel.draw(canvas)
            skyPanel.draw(canvas)
            sunAndMoonPanel.draw(canvas)
            horizonPanel.draw(canvas)
            clockHandsPanel.draw(canvas)
        }

        return bitmap
    }

    private fun scheduleNextUpdate(context: Context) {
        val calendar = Calendar.getInstance()
        val secondsUntilNextMinute = 60 - calendar.get(Calendar.SECOND)

        val request = OneTimeWorkRequestBuilder<ClockUpdateWorker>()
            .setInitialDelay(secondsUntilNextMinute.toLong(), TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "CwpWidgetClockUpdate",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}

class ClockUpdateWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(CwpWidgetContent::class.java)

        glanceIds.forEach { glanceId ->
            CwpWidgetContent().update(context, glanceId)
        }
        return Result.success()
    }
}
