/*
 * PeriodicalUpdater.kt
 *
 * Copyright 2020-2023 Yasuhiro Yamakawa <withlet11@gmail.com>
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

import android.os.Handler
import android.os.Looper
import io.github.withlet11.clockwithplanisphere.fragment.AbstractCwpFragment


class PeriodicalUpdater(private val cwpFragment: AbstractCwpFragment) {
    companion object {
        const val PERIOD = 250L
    }

    private lateinit var runnable: Runnable
    // private val handler = Handler()
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    fun timerSet() {
        runnable = object : Runnable {
            override fun run() {
                cwpFragment.updateClockIfClockHandsAreVisible()
                handler.postDelayed(this, PERIOD)
            }
        }
        handler.post(runnable)
    }

    fun stopTimerTask() {
        handler.removeCallbacks(runnable)
    }
}