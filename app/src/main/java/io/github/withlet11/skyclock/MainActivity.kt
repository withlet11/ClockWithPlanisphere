/*
 * MainActivity.kt
 *
 * Copyright 2020-2021 Yasuhiro Yamakawa <withlet11@gmail.com>
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

package io.github.withlet11.skyclock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import io.github.withlet11.skyclock.fragment.*


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

    // private val handler = Handler()
    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private var adView: AdView? = null

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
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.my_toolbar)
        toolbar.setLogo(R.drawable.ic_launcher_foreground)
        toolbar.setTitle(R.string.app_name)

        toolbar.inflateMenu(R.menu.menu_main)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_settings -> {
                    val dialog = LocationSettingFragment()
                    dialog.show(supportFragmentManager, "locationSetting")
                }
                R.id.item_bg_color -> {
                    val dialog = ColorSettingFragment()
                    dialog.show(supportFragmentManager, "backgroundColor")
                }
                R.id.item_privacy_policy -> {
                    startActivity(Intent(application, PrivacyPolicyActivity::class.java))
                }
                R.id.item_licenses -> {
                    startActivity(Intent(application, LicenseActivity::class.java))
                }
                R.id.item_credits -> {
                    startActivity(Intent(this, OssLicensesMenuActivity::class.java))
                }
                android.R.id.home -> finish()
            }

            true
        }

        // for ads
        MobileAds.initialize(this) {}
        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView?.loadAd(adRequest)

        loadPreviousSettings()

        val switch: SwitchCompat = findViewById(R.id.view_switch)
        switch.isChecked = isSouthernSky
        switch.setOnCheckedChangeListener { _, b ->
            isSouthernSky = b
            with(getSharedPreferences("observation_position", Context.MODE_PRIVATE).edit()) {
                putBoolean("isSouthernSky", isSouthernSky)
                putInt("backgroundColor", backgroundColor)
                commit()
            }

            val newFragment =
                (if (b) SouthernSkyClockFragment() else NorthernSkyClockFragment()).apply {
                    arguments = Bundle().apply {
                        putDouble("LATITUDE", latitude)
                        putDouble("LONGITUDE", longitude)
                        putBoolean("CLOCK_HANDS_VISIBILITY", isClockHandsVisible)
                        putInt("BACKGROUND_COLOR", backgroundColor)
                    }
                }

            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, newFragment)
            transaction.commit()
        }

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val fragment =
            (if (isSouthernSky) SouthernSkyClockFragment() else NorthernSkyClockFragment()).apply {
                arguments = Bundle().apply {
                    putDouble("LATITUDE", latitude)
                    putDouble("LONGITUDE", longitude)
                    putBoolean("CLOCK_HANDS_VISIBILITY", isClockHandsVisible)
                    putInt("BACKGROUND_COLOR", backgroundColor)
                }
            }

        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()

        val runnable = Runnable {
            val layout: FrameLayout = findViewById(R.id.frameLayoutForAd)
            layout.removeView(adView)
            layout.invalidate()
            adView = null
        }

        handler.postDelayed(runnable, AD_DISPLAY_DURATION)
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
        val previous = getSharedPreferences("observation_position", Context.MODE_PRIVATE)

        try {
            latitude = previous.getFloat("latitude", DEFAULT_LATITUDE.toFloat()).toDouble()
            longitude = previous.getFloat("longitude", DEFAULT_LONGITUDE.toFloat()).toDouble()
            isSouthernSky = previous.getBoolean("isSouthernSky", false)
            backgroundColor = previous.getInt("backgroundColor", resources.getColor(R.color.defaultBackGround, null))
        } catch (e: ClassCastException) {
            latitude = DEFAULT_LATITUDE
            longitude = DEFAULT_LONGITUDE
            isSouthernSky = false
            setDefaultColor()
        } finally {
        }
    }

    private fun loadPreviousPosition() {
        val previous = getSharedPreferences("observation_position", Context.MODE_PRIVATE)

        try {
            latitude = previous.getFloat("latitude", DEFAULT_LATITUDE.toFloat()).toDouble()
            longitude = previous.getFloat("longitude", DEFAULT_LONGITUDE.toFloat()).toDouble()
        } catch (e: ClassCastException) {
            latitude = DEFAULT_LATITUDE
            longitude = DEFAULT_LONGITUDE
        } finally {
            notifyLocationChange()
        }
    }

    private fun loadColorSettings() {
        val previous = getSharedPreferences("observation_position", Context.MODE_PRIVATE)

        try {
            backgroundColor = previous.getInt("backgroundColor", resources.getColor(R.color.defaultBackGround, null))
        } catch (e: ClassCastException) {
            setDefaultColor()
        } finally {
            notifyColorChange()
        }
    }

    private fun setDefaultColor() {
        backgroundColor = resources.getColor(R.color.defaultBackGround, null)
    }
}