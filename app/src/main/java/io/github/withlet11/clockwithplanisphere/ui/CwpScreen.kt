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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue
import androidx.compose.ui.tooling.preview.Preview
import io.github.withlet11.clockwithplanisphere.CwpTheme
import java.time.LocalTime
import java.time.ZonedDateTime

@Composable
fun CwpScreen(
    modifier: Modifier = Modifier,
    isSouthernSky: Boolean,
    latitude: Double,
    longitude: Double,
    backgroundColor: Int,
) {
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    var isZoomed by remember { mutableStateOf(false) }
    var isClockHandsVisible by remember { mutableStateOf(true) }

    var utc by remember { mutableStateOf(LocalTime.now()) }
    var solarTime by remember { mutableStateOf(LocalTime.now()) }
    var siderealTime by remember { mutableStateOf(LocalTime.now()) }

    val isLandScape = screenSize.width > screenSize.height
    val longSide: Int
    val shortSide: Int
    if (isLandScape) {
        longSide = screenSize.width
        shortSide = screenSize.height
    } else {
        longSide = screenSize.height
        shortSide = screenSize.width
    }
    val totalDifference = (longSide - shortSide).toFloat()
    val scrollBounds: Rect = if (isZoomed) {
        if (isLandScape) {
            Rect(left = 0f, right = 0f, top = -totalDifference, bottom = 0f)
        } else {
            Rect(left = -totalDifference, right = 0f, top = 0f, bottom = 0f)
        }
    } else if (isLandScape) {
        Rect(left = 0f, right = totalDifference, top = 0f, bottom = 0f)
    } else {
        Rect(left = 0f, right = 0f, top = 0f, bottom = totalDifference)
    }

    var offsetXY by remember(screenSize, isZoomed) {
        mutableStateOf(
            if (isZoomed) scrollBounds.center else (scrollBounds.center + scrollBounds.center / 1.5f)
        )
    }

    val textColor = Color.LightGray // MaterialTheme.colorScheme.onPrimaryContainer
    val shadowColor = Color.Black // MaterialTheme.colorScheme.primaryContainer

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(backgroundColor))
            .onSizeChanged { screenSize = it }
    ) {
        LocationAndDateInfo(
            modifier = Modifier.offset(x = 16.dp, y = 16.dp),
            utc = utc,
            solarTime = solarTime,
            siderealTime = siderealTime,
            latitude = latitude,
            longitude = longitude,
            textColor = textColor,
            shadowColor = shadowColor
        )

        ClockScreen(
            modifier = Modifier.fillMaxSize(),
            latitude = latitude,
            longitude = longitude,
            isSouthernSky = isSouthernSky,
            isZoomed = isZoomed,
            onZoomedChanged = { isZoomed = it },
            isClockHandsVisible = isClockHandsVisible,
            onClockHandsVisibilityChanged = { isClockHandsVisible = it },
            isLandScape = isLandScape,
            shortSide = shortSide,
            longSide = longSide,
            scrollBounds = scrollBounds,
            offsetXY = offsetXY,
            onOffsetChanged = { x, y -> offsetXY = Offset(x, y) },
            onTimeUpdate = { t1, t2, t3 ->
                utc = t1
                solarTime = t2
                siderealTime = t3
            }
        )
    }
}

@Composable
fun LocationAndDateInfo(
    modifier: Modifier = Modifier,
    utc: LocalTime,
    solarTime: LocalTime,
    siderealTime: LocalTime,
    latitude: Double,
    longitude: Double,
    textColor: Color,
    shadowColor: Color,
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "${ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}",
            color = textColor,
            style = MaterialTheme.typography.headlineSmall.copy(
                shadow = Shadow(
                    color = shadowColor,
                    offset = Offset.Zero,
                    blurRadius = 2f
                )
            ),
        )

        Text(
            text = "${ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))}",
            color = textColor,
            style = MaterialTheme.typography.displayLarge.copy(
                shadow = Shadow(
                    color = shadowColor,
                    offset = Offset.Zero,
                    blurRadius = 2f
                )
            ),
            fontWeight = FontWeight.SemiBold

        )

        Text(
            text = "UTC ${
                utc.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            }",
            style = MaterialTheme.typography.headlineSmall.copy(
                shadow = Shadow(
                    color = shadowColor,
                    offset = Offset.Zero,
                    blurRadius = 2f
                )
            ),
            fontFamily = FontFamily.Monospace,
            color = textColor
        )

        Text(
            text = "MST ${
                solarTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            }",
            style = MaterialTheme.typography.headlineSmall.copy(
                shadow = Shadow(
                    color = shadowColor,
                    offset = Offset.Zero,
                    blurRadius = 2f
                )
            ),
            fontFamily = FontFamily.Monospace,
            color = textColor
        )

        Text(
            text = "LST ${
                siderealTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            }",
            style = MaterialTheme.typography.headlineSmall.copy(
                shadow = Shadow(
                    color = shadowColor,
                    offset = Offset.Zero,
                    blurRadius = 2f
                )
            ),
            fontFamily = FontFamily.Monospace,
            color = textColor
        )

        Text(
            text = "${latitude.toLatitudeString()}, ${longitude.toLongitudeString()}",
            style = MaterialTheme.typography.bodyLarge.copy(
                shadow = Shadow(
                    color = shadowColor,
                    offset = Offset.Zero,
                    blurRadius = 2f
                )
            ),
            fontFamily = FontFamily.Monospace,
            color = textColor
        )
    }
}

private fun Double.toLatitudeString(): String {
    val (degrees, minutes, seconds) = this.degreeToDms()
    return "${degrees.absoluteValue}\u00b0${minutes.toString().padStart(2, '0')}\u2032${
        seconds.toString().padStart(2, '0')
    }\u2033${if (this < 0) "S" else "N"}"
}

private fun Double.toLongitudeString(): String {
    val (degrees, minutes, seconds) = this.degreeToDms()
    return "${degrees.absoluteValue}\u00b0${minutes.toString().padStart(2, '0')}\u2032${
        seconds.toString().padStart(2, '0')
    }\u2033${if (this < 0) "W" else "E"}"
}

private fun Double.degreeToDms(): Triple<Int, Int, Int> {
    val absDegree = this.absoluteValue
    val degrees = absDegree.toInt()
    val minutes = ((absDegree - degrees) * 60).toInt()
    val seconds = (((absDegree - degrees) * 3600 - minutes * 60)).toInt()
    return Triple(degrees * (if (this < 0) -1 else 1), minutes, seconds)
}

@Preview(showBackground = true)
@Composable
fun CwpScreenPreview() {
    CwpTheme {
        CwpScreen(
            isSouthernSky = false,
            latitude = 35.6895,
            longitude = 139.6917,
            backgroundColor = android.graphics.Color.BLACK
        )
    }
}