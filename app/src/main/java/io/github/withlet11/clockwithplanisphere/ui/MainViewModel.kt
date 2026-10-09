/*
 * MainViewModel.kt
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

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel

private const val DEFAULT_LATITUDE = 45.0
private const val DEFAULT_LONGITUDE = 0.0

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences =
        application.getSharedPreferences("observation_position", Context.MODE_PRIVATE)

    var latitude by mutableDoubleStateOf(DEFAULT_LATITUDE)
        private set

    var longitude by mutableDoubleStateOf(DEFAULT_LONGITUDE)
        private set

    var backgroundColor by mutableIntStateOf(0)
        private set

    var isSouthernSky by mutableStateOf(false)
        private set

    var isInitialized by mutableStateOf(false)
        private set

    fun initialize(defaultColor: Int) {
        if (isInitialized) return

        try {
            latitude = preferences.getFloat("latitude", DEFAULT_LATITUDE.toFloat()).toDouble()
            longitude = preferences.getFloat("longitude", DEFAULT_LONGITUDE.toFloat()).toDouble()
            isSouthernSky = preferences.getBoolean("isSouthernSky", false)
            backgroundColor = preferences.getInt("backgroundColor", defaultColor)
        } catch (_: ClassCastException) {
            latitude = DEFAULT_LATITUDE
            longitude = DEFAULT_LONGITUDE
            isSouthernSky = false
            backgroundColor = defaultColor
        }

        isInitialized = true
    }

    fun loadPreviousPosition() {
        try {
            latitude = preferences.getFloat("latitude", DEFAULT_LATITUDE.toFloat()).toDouble()
            longitude = preferences.getFloat("longitude", DEFAULT_LONGITUDE.toFloat()).toDouble()
        } catch (_: ClassCastException) {
            latitude = DEFAULT_LATITUDE
            longitude = DEFAULT_LONGITUDE
        }
    }

    fun loadColorSettings(defaultColor: Int) {
        backgroundColor = try {
            preferences.getInt("backgroundColor", defaultColor)
        } catch (_: ClassCastException) {
            defaultColor
        }
    }

    fun updateSouthernSky(value: Boolean) {
        isSouthernSky = value

        preferences.edit {
            putBoolean("isSouthernSky", value)
            putInt("backgroundColor", backgroundColor)
        }
    }
}
