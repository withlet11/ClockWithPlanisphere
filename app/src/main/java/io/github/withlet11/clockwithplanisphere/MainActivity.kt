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
import android.widget.FrameLayout
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.doOnAttach
import androidx.fragment.app.DialogFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.oss.licenses.v2.OssLicensesMenuActivity
import io.github.withlet11.clockwithplanisphere.fragment.*
import io.github.withlet11.clockwithplanisphere.ui.MainScreen

class MainActivity : AppCompatActivity(), LocationSettingFragment.LocationSettingDialogListener,
    ColorSettingFragment.BackgroundColorSettingDialogListener {
    companion object {
        const val AD_DISPLAY_DURATION = 10000L
        const val DEFAULT_LATITUDE = 45.0
        const val DEFAULT_LONGITUDE = 0.0
    }

    var latitude = 0.0
    private var longitude = 0.0
    var isClockHandsVisible = true
    private var backgroundColor = 0
    private var isSouthernSky = false

    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private var adView: AdView? = null
    private var adRunnable: Runnable? = null
    private lateinit var containerFrameLayout: FrameLayout
    // private lateinit var adViewInstance: AdView

    interface ChangeObserver {
        fun onLocationChange(latitude: Double, longitude: Double)
        fun onColorChange(backgroundColor: Int)
    }

    private val observers = mutableListOf<ChangeObserver>()

    fun addObserver(observer: ChangeObserver) {
        observers.add(observer)
    }

    fun removeObserver(observer: ChangeObserver) {
        observers.remove(observer)
    }

    private fun notifyLocationChange() {
        observers.forEach { it.onLocationChange(latitude, longitude) }
    }

    private fun notifyColorChange() {
        observers.forEach { it.onColorChange(backgroundColor) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        loadPreviousSettings()

        containerFrameLayout = FrameLayout(this).apply {
            id = R.id.container
            doOnAttach {
                if (supportFragmentManager.findFragmentById(R.id.container) == null) {
                    replaceCwpFragment(isSouthernSky)
                }
            }
        }

//        adViewInstance = AdView(this).apply {
//            id = R.id.adView
//            setAdSize(com.google.android.gms.ads.AdSize.BANNER)
//            adUnitId = "ca-app-pub-6502278727709781/9103220433"
//        }

        setContent {
            CwpTheme {
                MainScreen(
                    isSouthernSky = isSouthernSky,
                    onSouthernSkyChanged = { b ->
                        isSouthernSky = b
                        getSharedPreferences("observation_position", MODE_PRIVATE).edit {
                            putBoolean("isSouthernSky", isSouthernSky)
                            putInt("backgroundColor", backgroundColor)
                        }
                        replaceCwpFragment(isSouthernSky)
                    },
                    onSettingsClick = {
                        val dialog = LocationSettingFragment()
                        dialog.show(supportFragmentManager, "locationSetting")
                    },
                    onBgColorClick = {
                        val dialog = ColorSettingFragment()
                        dialog.show(supportFragmentManager, "backgroundColor")
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
                        AndroidView(
                            factory = { containerFrameLayout },
                            modifier = modifier
                        )
                    },
//                    adViewContent = {
//                        AndroidView(
//                            factory = { adViewInstance }
//                        )
//                    }
                )
            }
        }

        adRunnable = Runnable {
            adView?.let { view ->
                (view.parent as? ViewGroup)?.removeView(view)
                view.destroy()
            }
            adView = null
        }

        adRunnable?.let { handler.postDelayed(it, AD_DISPLAY_DURATION) }
        setUpAds()
    }

    private fun replaceCwpFragment(isSouthernSky: Boolean) {
        val newFragment =
            (if (isSouthernSky) SouthernCwpFragment() else NorthernCwpFragment()).apply {
                arguments = Bundle().apply {
                    putDouble("LATITUDE", latitude)
                    putDouble("LONGITUDE", longitude)
                    putBoolean("CLOCK_HANDS_VISIBILITY", isClockHandsVisible)
                    putInt("BACKGROUND_COLOR", backgroundColor)
                }
            }

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, newFragment)
            .commit()
    }

    private fun setUpAds() {
        val requestConfiguration = MobileAds.getRequestConfiguration()
            .toBuilder()
            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)

        MobileAds.initialize(this) {}
//        adView = adViewInstance
        val adRequest = AdRequest.Builder().build()
        adView?.loadAd(adRequest)
    }

    override fun onDestroy() {
        adRunnable?.let { handler.removeCallbacks(it) }
        adView?.destroy()
        adView = null
        super.onDestroy()
    }

    override fun onLocationDialogPositiveClick(dialog: DialogFragment) {
        loadPreviousPosition()
    }

    override fun onLocationDialogNegativeClick(dialog: DialogFragment) {
        // Do nothing
    }

    override fun onColorDialogPositiveClick(dialog: DialogFragment) {
        loadColorSettings()
    }

    override fun onColorDialogNegativeClick(dialog: DialogFragment) {
        // Do nothing
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
        } finally {
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
        } finally {
            notifyLocationChange()
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
        } finally {
            notifyColorChange()
        }
    }

    private fun setDefaultColor() {
        backgroundColor = resources.getColor(R.color.defaultBackGround, null)
    }
}
