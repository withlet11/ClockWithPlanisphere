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

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import io.github.withlet11.clockwithplanisphere.MainActivity
import io.github.withlet11.clockwithplanisphere.R
import io.github.withlet11.clockwithplanisphere.model.NorthernSkyModel
import io.github.withlet11.clockwithplanisphere.model.SkyViewModel
import io.github.withlet11.clockwithplanisphere.model.SouthernSkyModel
import kotlinx.coroutines.runBlocking

class CwpWidget : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CwpWidgetContent()

    companion object {
        const val ACTION_UPDATE = "io.github.withlet11.clockwithplanisphere.widget.CwpWidget.ACTION_UPDATE"
        const val UPDATE_INTERVAL = 5000L // milliseconds

        fun scheduleUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = getAlarmIntent(context)
            alarmManager.cancel(pendingIntent)
            val triggerAtMillis =
                (System.currentTimeMillis() + 1).let { it + UPDATE_INTERVAL - it % UPDATE_INTERVAL }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.set(AlarmManager.RTC, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC, triggerAtMillis, pendingIntent)
            }
        }

        private fun getAlarmIntent(context: Context): PendingIntent {
            val intent = Intent(context, CwpWidget::class.java)
            intent.action = ACTION_UPDATE
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        fun clearUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(getAlarmIntent(context))
        }

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

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        clearUpdate(context)
        super.onDisabled(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_UPDATE -> {
                scheduleUpdate(context)
                val glanceManager = GlanceAppWidgetManager(context)
                runBlocking {
                    val glanceIds = glanceManager.getGlanceIds(CwpWidgetContent::class.java)
                    glanceIds.forEach { glanceId ->
                        CwpWidgetContent().update(context, glanceId)
                    }
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                scheduleUpdate(context)
            }
        }
        super.onReceive(context, intent)
    }
}

class CwpWidgetContent : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        CwpWidget.scheduleUpdate(context)

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
        clockBasePanel.draw()
        skyPanel.draw()
        sunAndMoonPanel.draw()
        horizonPanel.draw()
        clockHandsPanel.draw()

        val baseBmp = clockBasePanel.bmp
        val skyBmp = skyPanel.bmp
        val sunMoonBmp = sunAndMoonPanel.bmp
        val horizonBmp = horizonPanel.bmp
        val handsBmp = clockHandsPanel.bmp

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(actionStartActivity<MainActivity>())
            ) {
                Image(
                    provider = ImageProvider(baseBmp),
                    contentDescription = context.getString(R.string.clock_base_panel),
                    modifier = GlanceModifier.fillMaxSize()
                )
                Image(
                    provider = ImageProvider(skyBmp),
                    contentDescription = context.getString(R.string.sky_panel),
                    modifier = GlanceModifier.fillMaxSize()
                )
                Image(
                    provider = ImageProvider(sunMoonBmp),
                    contentDescription = context.getString(R.string.sun_and_moon_panel),
                    modifier = GlanceModifier.fillMaxSize()
                )
                Image(
                    provider = ImageProvider(horizonBmp),
                    contentDescription = context.getString(R.string.horizon_panel),
                    modifier = GlanceModifier.fillMaxSize()
                )
                Image(
                    provider = ImageProvider(handsBmp),
                    contentDescription = context.getString(R.string.clock_hands_panel),
                    modifier = GlanceModifier.fillMaxSize()
                )
            }
        }
    }
}
