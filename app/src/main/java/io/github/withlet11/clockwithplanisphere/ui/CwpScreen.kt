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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import io.github.withlet11.clockwithplanisphere.fragment.HorizontalPosition
import io.github.withlet11.clockwithplanisphere.fragment.VerticalPosition
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun CwpScreen(
    modifier: Modifier = Modifier,
    isSouthernSky: Boolean,
    latitude: Double,
    longitude: Double,
    backgroundColor: Int,
    horizontalPosition: HorizontalPosition,
    verticalPosition: VerticalPosition,
) {
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    val isScreenLandScape by remember { derivedStateOf { screenSize.width > screenSize.height } }

    var drawAreaSize by remember { mutableStateOf(IntSize.Zero) }
    var isDrawAreaLandScape by remember { mutableStateOf(false) }
    var longSide by remember { mutableIntStateOf(0) }
    var shortSide by remember { mutableIntStateOf(0) }

    var isZoomed by remember { mutableStateOf(false) }

    var scrollBounds by remember { mutableStateOf(Rect.Zero) }
    var offsetXy by remember { mutableStateOf(Offset.Zero) }
    var isOffsetXyReady by remember { mutableStateOf(false) }

    val isClockHandsVisible = remember { mutableStateOf(true) }
    val isCurrentTime = remember { mutableStateOf(true) }

    val currentTime = remember { mutableStateOf(ZonedDateTime.now()) }
    var utc by remember { mutableStateOf(ZonedDateTime.now()) }
    var solarTime by remember { mutableStateOf(LocalTime.now()) }
    var siderealTime by remember { mutableStateOf(LocalTime.now()) }

    val textColor = Color.LightGray
    val shadowColor = Color.Black

    LaunchedEffect(drawAreaSize, isZoomed, isScreenLandScape) {
        isDrawAreaLandScape = drawAreaSize.width > drawAreaSize.height
        longSide = if (isDrawAreaLandScape) drawAreaSize.width else drawAreaSize.height
        shortSide = if (isDrawAreaLandScape) drawAreaSize.height else drawAreaSize.width

        val totalDifference = (longSide - shortSide).toFloat()
        scrollBounds = when {
            isZoomed -> {
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

        offsetXy = scrollBounds.center
        isOffsetXyReady = true
    }

    var updateTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isCurrentTime.value) {
        if (isCurrentTime.value) {
            while (true) {
                updateTime = System.currentTimeMillis()
                currentTime.value = ZonedDateTime.now()
                delay(250L.milliseconds)
            }
        }
    }

    OrientationLayout(
        modifier = modifier
            .fillMaxSize()
            .background(Color(backgroundColor))
            .onSizeChanged {
                isOffsetXyReady = false
                screenSize = it
            },
        isLandScape = isScreenLandScape,
        isZoomed = isZoomed,
        verticalPosition = verticalPosition,
        horizontalPosition = horizontalPosition,
        first = {
            LocationAndDateInfo(
                isCurrentTime = isCurrentTime,
                isClockHandsVisible = isClockHandsVisible,
                currentTime = currentTime,
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged {
                        isOffsetXyReady = false
                        drawAreaSize = it
                    },
            ) {
                if (isOffsetXyReady)
                    ClockScreen(
                        latitude = latitude,
                        longitude = longitude,
                        currentTime = currentTime,
                        isCurrentTime = isCurrentTime,
                        isSouthernSky = isSouthernSky,
                        isZoomed = isZoomed,
                        toggleZoom = {
                            isZoomed = !isZoomed
                            isOffsetXyReady = false
                        },
                        isClockHandsVisible = isClockHandsVisible,
                        isLandScape = isDrawAreaLandScape,
                        shortSide = shortSide,
                        longSide = longSide,
                        scrollBounds = scrollBounds,
                        offsetXy = offsetXy,
                        onOffsetXyChange = { offsetXy = it },
                        onTimeUpdate = { t1, t2, t3 ->
                            utc = t1
                            solarTime = t2
                            siderealTime = t3
                        }
                    )
            }
        }
    )
}

@Composable
private fun OrientationLayout(
    modifier: Modifier = Modifier,
    isLandScape: Boolean,
    isZoomed: Boolean,
    verticalPosition: VerticalPosition,
    horizontalPosition: HorizontalPosition,
    first: @Composable () -> Unit,
    second: @Composable () -> Unit
) {
    if (isLandScape) {
        Row(modifier = modifier) {
            if (!isZoomed && horizontalPosition == HorizontalPosition.LEFT)
                Box(modifier = Modifier.padding(top = 16.dp, start = 16.dp)) {
                    first()
                }

            Box(modifier = Modifier.weight(1f)) {
                second()
            }

            if (!isZoomed && horizontalPosition == HorizontalPosition.RIGHT)
                Box(modifier = Modifier.padding(top = 16.dp, end = 16.dp)) {
                    first()
                }
        }
    } else {
        Column(modifier = modifier) {
            if (!isZoomed && verticalPosition == VerticalPosition.TOP)
                Box(modifier = Modifier.padding(top = 16.dp, start = 16.dp)) {
                    first()
                }

            Box(modifier = Modifier.weight(1f)) {
                second()
            }

            if (!isZoomed && verticalPosition == VerticalPosition.BOTTOM)
                Box(modifier = Modifier.padding(bottom = 16.dp, start = 16.dp)) {
                    first()
                }
        }
    }
}

@Composable
private fun LocationAndDateInfo(
    modifier: Modifier = Modifier,
    isCurrentTime: MutableState<Boolean>,
    isClockHandsVisible: MutableState<Boolean>,
    currentTime: MutableState<ZonedDateTime>,
    utc: ZonedDateTime,
    solarTime: LocalTime,
    siderealTime: LocalTime,
    latitude: Double,
    longitude: Double,
    textColor: Color,
    shadowColor: Color,
) {
    val localDateTime = utc.withZoneSameInstant(ZoneId.systemDefault())

    Column {
        Row(
            modifier = Modifier.padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "${
                    (if (isCurrentTime.value) currentTime.value else localDateTime).format(
                        DateTimeFormatter.ofPattern(
                            "yyyy-MM-dd"
                        )
                    )
                }",
                color = if (isCurrentTime.value) textColor else textColor.copy(alpha = 0.5f),
                style = MaterialTheme.typography.headlineSmall.copy(
                    shadow = Shadow(
                        color = shadowColor,
                        offset = Offset.Zero,
                        blurRadius = 2f
                    )
                ),
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        isCurrentTime.value = !isCurrentTime.value
                        isClockHandsVisible.value = false
                    }
                    .background(
                        color = if (isCurrentTime.value) {
                            textColor
                        } else {
                            textColor.copy(alpha = 0.5f)
                        }
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "LIVE",
                    color = if (isCurrentTime.value) {
                        shadowColor
                    } else {
                        shadowColor.copy(alpha = 0.5f)
                    },
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Text(
            text = "${
                (if (isCurrentTime.value) currentTime.value else localDateTime).format(
                    DateTimeFormatter.ofPattern(
                        "HH:mm:ss"
                    )
                )
            }",
            color = if (isCurrentTime.value) textColor else textColor.copy(alpha = 0.5f),
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
            horizontalPosition = HorizontalPosition.LEFT,
            verticalPosition = VerticalPosition.TOP,
            backgroundColor = android.graphics.Color.BLACK
        )
    }
}