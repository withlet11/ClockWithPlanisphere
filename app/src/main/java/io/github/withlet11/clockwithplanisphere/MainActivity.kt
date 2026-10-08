/*
 * MainActivity.kt
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

package io.github.withlet11.clockwithplanisphere

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.oss.licenses.v2.OssLicensesMenuActivity
import io.github.withlet11.clockwithplanisphere.fragment.ColorSettingDialog
import io.github.withlet11.clockwithplanisphere.fragment.LocationSettingDialog
import io.github.withlet11.clockwithplanisphere.ui.CwpScreen
import io.github.withlet11.clockwithplanisphere.ui.MainScreen
import kotlin.time.Duration.Companion.seconds

class MainActivity : ComponentActivity() {
    companion object {
        const val DEFAULT_LATITUDE = 45.0
        const val DEFAULT_LONGITUDE = 0.0
    }

    var latitude by mutableDoubleStateOf(DEFAULT_LATITUDE)
    private var longitude by mutableDoubleStateOf(DEFAULT_LONGITUDE)
    private var isClockHandsVisible by mutableStateOf(true)
    private var backgroundColor by mutableIntStateOf(0)
    var isSouthernSky by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        loadPreviousSettings()

        setContent {
            var showLocationDialog by remember { mutableStateOf(false) }
            var showColorDialog by remember { mutableStateOf(false) }
            CwpTheme {
                MainScreen(
                    isSouthernSky = isSouthernSky,
                    onSouthernSkyChanged = { b ->
                        isSouthernSky = b
                        getSharedPreferences("observation_position", MODE_PRIVATE).edit {
                            putBoolean("isSouthernSky", isSouthernSky)
                            putInt("backgroundColor", backgroundColor)
                        }
                    },
                    onSettingsClick = {
                        showLocationDialog = true
                    },
                    onBgColorClick = {
                        showColorDialog = true
                    },
                    onPrivacyPolicyClick = {
                        startActivity(Intent(application, PrivacyPolicyActivity::class.java))
                    },
                    onLicensesClick = {
                        startActivity(Intent(application, LicenseActivity::class.java))
                    },
                    onCreditsClick = {
                        startActivity(Intent(this, OssLicensesMenuActivity::class.java))
                    },
                    content = { modifier ->
                        if (showLocationDialog) {
                            LocationSettingDialog(
                                onDismiss = {
                                    showLocationDialog = false
                                },
                                onLocationSaved = {
                                    showLocationDialog = false
                                    loadPreviousPosition()
                                }
                            )
                        }
                        if (showColorDialog) {
                            ColorSettingDialog(
                                onDismiss = {
                                    showColorDialog = false
                                },
                                onColorSaved = {
                                    showColorDialog = false
                                    loadColorSettings()
                                }
                            )
                        }
                        CwpScreen(
                            isSouthernSky = isSouthernSky,
                            latitude = latitude,
                            longitude = longitude,
                            isClockHandsVisible = isClockHandsVisible,
                            onClockHandsVisibilityChanged = {
                                isClockHandsVisible = it
                            },
                            backgroundColor = backgroundColor,
                            modifier = modifier
                        )
                    },
                    adViewContent = { modifier ->
                        AndroidView(
                            modifier = modifier,
                            factory = { context ->
                                AdView(context).apply {
                                    setAdSize(AdSize.BANNER)
                                    // adUnitId = "ca-app-pub-6502278727709781/9103220433"
                                    adUnitId = "ca-app-pub-3940256099942544/6300978111"
                                    loadAd(AdRequest.Builder().build())
                                }
                            }
                        )
                    },
                )
            }
        }
    }

    private fun loadPreviousSettings() {
        val previous = getSharedPreferences("observation_position", MODE_PRIVATE)

        try {
            latitude = previous.getFloat("latitude", DEFAULT_LATITUDE.toFloat()).toDouble()
            longitude = previous.getFloat("longitude", DEFAULT_LONGITUDE.toFloat()).toDouble()
            isSouthernSky = previous.getBoolean("isSouthernSky", false)
            backgroundColor = previous.getInt(
                "backgroundColor",
                ContextCompat.getColor(this, R.color.defaultBackGround)
            )
        } catch (_: ClassCastException) {
            latitude = DEFAULT_LATITUDE
            longitude = DEFAULT_LONGITUDE
            isSouthernSky = false
            setDefaultColor()
        }
    }

    private fun loadPreviousPosition() {
        val previous = getSharedPreferences("observation_position", MODE_PRIVATE)

        try {
            latitude = previous.getFloat("latitude", DEFAULT_LATITUDE.toFloat()).toDouble()
            longitude = previous.getFloat("longitude", DEFAULT_LONGITUDE.toFloat()).toDouble()
        } catch (_: ClassCastException) {
            latitude = DEFAULT_LATITUDE
            longitude = DEFAULT_LONGITUDE
        }
    }

    private fun loadColorSettings() {
        val previous = getSharedPreferences("observation_position", MODE_PRIVATE)

        try {
            backgroundColor = previous.getInt(
                "backgroundColor",
                resources.getColor(R.color.defaultBackGround, null)
            )
        } catch (_: ClassCastException) {
            setDefaultColor()
        }
    }

    private fun setDefaultColor() {
        backgroundColor = resources.getColor(R.color.defaultBackGround, null)
    }
}
