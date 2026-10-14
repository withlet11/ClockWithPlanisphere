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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun CwpScreen(
    modifier: Modifier = Modifier,
    isSouthernSky: Boolean,
    latitude: Double,
    longitude: Double,
    backgroundColor: Int,
) {
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    val isScreenLandScape by remember { derivedStateOf { screenSize.width > screenSize.height } }

    var drawAreaSize by remember { mutableStateOf(IntSize.Zero) }
    val isDrawAreaLandScape by remember { derivedStateOf { drawAreaSize.width > drawAreaSize.height } }
    val longSide by remember { derivedStateOf { if (isDrawAreaLandScape) drawAreaSize.width else drawAreaSize.height } }
    val shortSide by remember { derivedStateOf { if (isDrawAreaLandScape) drawAreaSize.height else drawAreaSize.width } }

    val isZoomed = remember { mutableStateOf(false) }
    val scrollBounds by remember {
        derivedStateOf {
            val totalDifference = (longSide - shortSide).toFloat()
            when {
                isZoomed.value -> {
                    if (isDrawAreaLandScape) {
                        Rect(left = 0f, right = 0f, top = -totalDifference, bottom = 0f)
                    } else {
                        Rect(left = -totalDifference, right = 0f, top = 0f, bottom = 0f)
                    }
                }

                isDrawAreaLandScape ->
                    Rect(left = 0f, right = totalDifference, top = 0f, bottom = 0f)

                else ->
                    Rect(left = 0f, right = 0f, top = 0f, bottom = totalDifference)
            }
        }
    }

    val offsetXY = remember(scrollBounds) { mutableStateOf(scrollBounds.center) }

    val isClockHandsVisible = remember { mutableStateOf(true) }
    val isCurrentTime = remember { mutableStateOf(true) }

    var utc by remember { mutableStateOf(ZonedDateTime.now()) }
    var solarTime by remember { mutableStateOf(LocalTime.now()) }
    var siderealTime by remember { mutableStateOf(LocalTime.now()) }

    val textColor = Color.LightGray
    val shadowColor = Color.Black

    OrientationLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(backgroundColor))
            .onSizeChanged { screenSize = it },
        isLandScape = isScreenLandScape,
        isZoomed = isZoomed,
        isInfoAtTop = false,
        isInfoAtLeft = true,
        first = {
            LocationAndDateInfo(
                isCurrentTime = isCurrentTime.value,
                utc = utc,
                solarTime = solarTime,
                siderealTime = siderealTime,
                latitude = latitude,
                longitude = longitude,
                textColor = textColor,
                shadowColor = shadowColor
            )
        },
        second = {
            ClockScreen(
                modifier = modifier.onSizeChanged { drawAreaSize = it },
                latitude = latitude,
                longitude = longitude,
                isCurrentTime = isCurrentTime,
                isSouthernSky = isSouthernSky,
                isZoomed = isZoomed,
                isClockHandsVisible = isClockHandsVisible,
                isLandScape = isDrawAreaLandScape,
                shortSide = shortSide,
                longSide = longSide,
                scrollBounds = scrollBounds,
                offsetXY = offsetXY,
                onTimeUpdate = { t1, t2, t3 ->
                    utc = t1
                    solarTime = t2
                    siderealTime = t3
                }
            )
        }
    )
}

@Composable
private fun OrientationLayout(
    modifier: Modifier = Modifier,
    isLandScape: Boolean,
    isZoomed: MutableState<Boolean>,
    isInfoAtTop: Boolean,
    isInfoAtLeft: Boolean,
    first: @Composable () -> Unit,
    second: @Composable () -> Unit
) {
    if (isLandScape) {
        Row(modifier = modifier) {
            if (!isZoomed.value && isInfoAtLeft)
                Box(modifier = Modifier.padding(top = 16.dp, start = 16.dp)) {
                    first()
                }

            Box(modifier = Modifier.weight(1f)) {
                second()
            }

            if (!isZoomed.value && !isInfoAtLeft)
                Box(modifier = Modifier.padding(top = 16.dp, end = 16.dp)) {
                    first()
                }
        }
    } else {
        Column(modifier = modifier) {
            if (!isZoomed.value && isInfoAtTop)
                Box(modifier = Modifier.padding(top = 16.dp, start = 16.dp)) {
                    first()
                }

            Box(modifier = Modifier.weight(1f)) {
                second()
            }

            if (!isZoomed.value && !isInfoAtTop)
                Box(modifier = Modifier.padding(bottom = 16.dp, start = 16.dp)) {
                    first()
                }
        }
    }
}

@Composable
private fun LocationAndDateInfo(
    modifier: Modifier = Modifier,
    isCurrentTime: Boolean,
    utc: ZonedDateTime,
    solarTime: LocalTime,
    siderealTime: LocalTime,
    latitude: Double,
    longitude: Double,
    textColor: Color,
    shadowColor: Color,
) {
    var updateTime by remember { mutableLongStateOf(0L) }
    var currentTime by remember { mutableStateOf(ZonedDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            updateTime = System.currentTimeMillis()
            currentTime = ZonedDateTime.now()
            delay(250L.milliseconds)
        }
    }

    Column(modifier = modifier) {
        Text(
            text = "${(if (isCurrentTime) currentTime else utc).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}",
            color = if (isCurrentTime) textColor else textColor.copy(alpha = 0.5f),
            style = MaterialTheme.typography.headlineSmall.copy(
                shadow = Shadow(
                    color = shadowColor,
                    offset = Offset.Zero,
                    blurRadius = 2f
                )
            ),
        )

        Text(
            text = "${(if (isCurrentTime) currentTime else utc).format(DateTimeFormatter.ofPattern("HH:mm:ss"))}",
            color = if (isCurrentTime) textColor else textColor.copy(alpha = 0.5f),
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