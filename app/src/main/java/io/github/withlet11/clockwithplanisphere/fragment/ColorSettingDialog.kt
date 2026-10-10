/*
 * ColorSettingDialog.kt
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

package io.github.withlet11.clockwithplanisphere.fragment

import android.content.Context
import android.util.Log
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import io.github.withlet11.clockwithplanisphere.CwpTheme
import io.github.withlet11.clockwithplanisphere.R
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red


private const val PREFERENCES_NAME = "observation_position"
private const val BACKGROUND_COLOR_KEY = "backgroundColor"

const val DEFAULT_BACKGROUND_COLOR = 0xffc0e0ffu

@Composable
fun ColorSettingDialog(
    onDismiss: () -> Unit,
    onColorSaved: () -> Unit,
) {
    val context = LocalContext.current

    var backgroundColor by rememberSaveable {
        mutableIntStateOf(DEFAULT_BACKGROUND_COLOR.toInt())
    }

    val hsv = rgbToHsv(backgroundColor)
    var red by rememberSaveable { mutableIntStateOf(backgroundColor.red) }
    var green by rememberSaveable { mutableIntStateOf(backgroundColor.green) }
    var blue by rememberSaveable { mutableIntStateOf(backgroundColor.blue) }

    var hue by rememberSaveable { mutableIntStateOf(hsv[0]) }
    var saturation by rememberSaveable { mutableIntStateOf(hsv[1]) }
    var value by rememberSaveable { mutableIntStateOf(hsv[2]) }

    var isPositiveButtonEnabled by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val preferences = context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

        backgroundColor = preferences.getInt(
            BACKGROUND_COLOR_KEY,
            DEFAULT_BACKGROUND_COLOR.toInt()
        )

        red = backgroundColor.red
        green = backgroundColor.green
        blue = backgroundColor.blue

        val hsv = rgbToHsv(backgroundColor)
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
    }

    fun updateColor(
        newRed: Int = red,
        newGreen: Int = green,
        newBlue: Int = blue,
    ) {
        red = newRed
        green = newGreen
        blue = newBlue
        backgroundColor = Color(red, green, blue).toArgb()

        val hsv = rgbToHsv(backgroundColor)
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]

        isPositiveButtonEnabled = true
    }

    fun updateColor2(
        newHue: Int = hue,
        newSaturation: Int = saturation,
        newValue: Int = value,
    ) {
        hue = newHue
        saturation = newSaturation
        value = newValue
        backgroundColor = hsvToColor(newHue, newSaturation, newValue).toArgb()

        red = backgroundColor.red
        green = backgroundColor.green
        blue = backgroundColor.blue

        isPositiveButtonEnabled = true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.bg_color)) },
        text = {
            ColorSettingContent(
                red = red,
                green = green,
                blue = blue,
                hue = hue,
                saturation = saturation,
                value = value,
                onRedChanged = { value -> updateColor(newRed = value) },
                onGreenChanged = { value -> updateColor(newGreen = value) },
                onBlueChanged = { value -> updateColor(newBlue = value) },
                onHueChanged = { value -> updateColor2(newHue = value) },
                onSaturationChanged = { value -> updateColor2(newSaturation = value) },
                onValueChanged = { value -> updateColor2(newValue = value) },
                onColorSelected = { r, g, b -> updateColor(newRed = r, newGreen = g, newBlue = b) }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    context.getSharedPreferences(
                        PREFERENCES_NAME,
                        Context.MODE_PRIVATE
                    ).edit {
                        putInt(BACKGROUND_COLOR_KEY, backgroundColor)
                        commit()
                    }

                    onColorSaved()
                    onDismiss()
                },
                enabled = isPositiveButtonEnabled
            ) {
                Text(stringResource(R.string.modify))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun ColorSettingContent(
    red: Int,
    green: Int,
    blue: Int,
    hue: Int,
    saturation: Int,
    value: Int,
    onRedChanged: (Int) -> Unit,
    onGreenChanged: (Int) -> Unit,
    onBlueChanged: (Int) -> Unit,
    onHueChanged: (Int) -> Unit,
    onSaturationChanged: (Int) -> Unit,
    onValueChanged: (Int) -> Unit,
    onColorSelected: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = Color(red, green, blue)
    var isRgbMode by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = modifier
            .padding(16.dp)
            .width(320.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .background(backgroundColor)
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { isRgbMode = !isRgbMode },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("RGB", "HSV").forEachIndexed { index, label ->
                        val selected = (index == 0) == isRgbMode

                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isRgbMode) {
                    HorizontalRuler(
                        label = stringResource(R.string.red),
                        value = red,
                        colorAtValue = { red -> Color(red, 0, 0) },
                        onValueChange = onRedChanged
                    )

                    HorizontalRuler(
                        label = stringResource(R.string.green),
                        value = green,
                        colorAtValue = { green -> Color(0, green, 0) },
                        onValueChange = onGreenChanged
                    )

                    HorizontalRuler(
                        label = stringResource(R.string.blue),
                        value = blue,
                        colorAtValue = { blue -> Color(0, 0, blue) },
                        onValueChange = onBlueChanged
                    )
                } else {
                    HueDial(
                        label = "Hue", // stringResource(R.string.red),
                        value = hue,
                        colorAtValue = { hue -> hsvToColor(hue, 100, 100) },
                        onValueChange = onHueChanged
                    )

                    ColorPad(
                        labelX = "Sat.",
                        valueX = saturation,
                        labelY = "Val.",
                        valueY = value,
                        colorAtValues = { saturation, value -> hsvToColor(hue, saturation, value) },
                        onValuesChange = { saturation, value ->
                            onSaturationChanged(saturation)
                            onValueChanged(value)
                        },
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.basic_colors),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        val paletteColors = listOf(
            Triple(R.string.white, R.color.white, R.color.black),
            Triple(R.string.silver, R.color.silver, R.color.black),
            Triple(R.string.gray, R.color.gray, R.color.black),
            Triple(R.string.black, R.color.black, R.color.white),
            Triple(R.string.red, R.color.red, R.color.black),
            Triple(R.string.maroon, R.color.maroon, R.color.white),
            Triple(R.string.yellow, R.color.yellow, R.color.black),
            Triple(R.string.olive, R.color.olive, R.color.black),
            Triple(R.string.lime, R.color.lime, R.color.black),
            Triple(R.string.green, R.color.green, R.color.white),
            Triple(R.string.aqua, R.color.aqua, R.color.black),
            Triple(R.string.teal, R.color.teal, R.color.white),
            Triple(R.string.blue, R.color.blue, R.color.white),
            Triple(R.string.navy, R.color.navy, R.color.white),
            Triple(R.string.fuchsia, R.color.fuchsia, R.color.black),
            Triple(R.string.purple, R.color.purple, R.color.white),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            userScrollEnabled = false
        ) {
            items(paletteColors) { (stringRes, bgColCode, textColCode) ->
                val bgCol = colorResource(bgColCode)
                val textCol = colorResource(textColCode)

                Button(
                    onClick = {
                        onColorSelected(
                            (bgCol.red * 255).toInt(),
                            (bgCol.green * 255).toInt(),
                            (bgCol.blue * 255).toInt()
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = bgCol,
                        contentColor = textCol
                    ),
                    contentPadding = PaddingValues(2.dp),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(stringRes),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalRuler(
    value: Int,
    label: String,
    onValueChange: (Int) -> Unit,
    colorAtValue: (Int) -> Color,
    modifier: Modifier = Modifier,
) {
    val maxValue = 255
    val dpPerUnit = 2.dp
    val tickInterval = 5
    val majorTickInterval = 25

    val density = LocalDensity.current
    val pixelsPerUnit = with(density) { dpPerUnit.toPx() }

    val currentValue by rememberUpdatedState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    var dragRemainder by remember { mutableFloatStateOf(0f) }
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
    val activeColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "$label [$value]",
            style = MaterialTheme.typography.bodyMedium,
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .pointerInput(pixelsPerUnit, 0, maxValue) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragRemainder = 0f },
                        onDragEnd = { dragRemainder = 0f },
                        onDragCancel = { dragRemainder = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            dragRemainder -= dragAmount / pixelsPerUnit

                            val delta = dragRemainder.toInt()

                            if (delta != 0) {
                                val newValue = (currentValue + delta).coerceIn(0, maxValue)

                                if (newValue != currentValue) {
                                    currentOnValueChange(newValue)
                                }

                                dragRemainder -= delta
                            }
                        },
                    )
                },
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val thirdWidth = size.width / 3f

            val visibleUnits = (thirdWidth / pixelsPerUnit).toInt() + tickInterval
            Log.d("CwpScreen", "Ruler visibleUnits: $visibleUnits")

            val firstValue = maxOf(0, value - visibleUnits)
            val lastValue = minOf(maxValue, value + visibleUnits)

            val startX = centerX + (firstValue - value) * pixelsPerUnit
            val endX = centerX + (lastValue - value) * pixelsPerUnit

            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(colorAtValue(firstValue), colorAtValue(lastValue)),
                    startX = startX,
                    endX = endX,
                ),
                topLeft = Offset(startX, 0f),
                size = Size(endX - startX, size.height)
            )

            for (tickValue in firstValue..lastValue) {
                if (tickValue % tickInterval != 0) continue

                val x = centerX + (tickValue - value) * pixelsPerUnit

                val isMajor = tickValue % majorTickInterval == 0
                val tickHeight = when {
                    isMajor -> 28.dp
                    else -> 14.dp
                }

                val halfHeight = with(density) {
                    tickHeight.toPx() / 2f
                }

                drawLine(
                    color = tickColor,
                    start = Offset(x, centerY - halfHeight),
                    end = Offset(x, centerY + halfHeight),
                    strokeWidth = with(density) { 1.dp.toPx() },
                )
            }
            drawLine(
                color = activeColor,
                start = Offset(centerX, 0f),
                end = Offset(centerX, size.height),
                strokeWidth = with(density) { 2.dp.toPx() },
            )
        }
    }
}

@Composable
fun HueDial(
    value: Int,
    label: String,
    onValueChange: (Int) -> Unit,
    colorAtValue: (Int) -> Color,
    modifier: Modifier = Modifier,
) {
    val maxValue = 360
    val dpPerUnit = 2.dp
    val tickInterval = 10
    val majorTickInterval = 30

    val density = LocalDensity.current
    val pixelsPerUnit = with(density) { dpPerUnit.toPx() }

    val currentValue by rememberUpdatedState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    var dragRemainder by remember { mutableFloatStateOf(0f) }
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
    val activeColor = MaterialTheme.colorScheme.primary


    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "$label [${value}]",
            style = MaterialTheme.typography.bodyMedium,
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .pointerInput(pixelsPerUnit, 0, maxValue) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragRemainder = 0f },
                        onDragEnd = { dragRemainder = 0f },
                        onDragCancel = { dragRemainder = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            dragRemainder -= dragAmount / pixelsPerUnit

                            val delta = dragRemainder.toInt()

                            if (delta != 0) {
                                val newValue = (currentValue + delta).mod(maxValue)

                                if (newValue != currentValue) {
                                    currentOnValueChange(newValue)
                                }

                                dragRemainder -= delta
                            }
                        },
                    )
                },
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val thirdWidth = size.width / 3f

            val visibleUnits = (thirdWidth / pixelsPerUnit).toInt() + tickInterval / 2
            Log.d("CwpScreen", "Dial visibleUnits: $visibleUnits")

            val firstValue = value - visibleUnits
            val lastValue = value + visibleUnits

            val startX = centerX - visibleUnits * pixelsPerUnit
            val endX = centerX + visibleUnits * pixelsPerUnit

            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(colorAtValue(firstValue), colorAtValue(lastValue)),
                    startX = startX,
                    endX = endX,
                ),
                topLeft = Offset(startX, 0f),
                size = Size(endX - startX, size.height)
            )

            for (tickValue in firstValue..lastValue) {
                if (tickValue % tickInterval != 0) continue

                val x = centerX + (tickValue - value) * pixelsPerUnit

                val isMajor = tickValue % majorTickInterval == 0
                val tickHeight = when {
                    isMajor -> 28.dp
                    else -> 14.dp
                }

                val halfHeight = with(density) {
                    tickHeight.toPx() / 2f
                }

                drawLine(
                    color = tickColor,
                    start = Offset(x, centerY - halfHeight),
                    end = Offset(x, centerY + halfHeight),
                    strokeWidth = with(density) { 1.dp.toPx() },
                )
            }
            drawLine(
                color = activeColor,
                start = Offset(centerX, 0f),
                end = Offset(centerX, size.height),
                strokeWidth = with(density) { 2.dp.toPx() },
            )
        }
    }
}

@Composable
fun ColorPad(
    valueX: Int,
    labelX: String,
    valueY: Int,
    labelY: String,
    onValuesChange: (Int, Int) -> Unit,
    colorAtValues: (Int, Int) -> Color,
    modifier: Modifier = Modifier,
) {
    val maxValue = 100
    val dpPerUnit = 2.dp
    val tickInterval = 5
    val majorTickInterval = 20

    val density = LocalDensity.current
    val pixelsPerUnit = with(density) { dpPerUnit.toPx() }

    val currentValueX by rememberUpdatedState(valueX)
    val currentValueY by rememberUpdatedState(valueY)
    val currentOnValuesChange by rememberUpdatedState(onValuesChange)
    val currentColorAtValues by rememberUpdatedState(colorAtValues)
    var dragRemainder by remember { mutableStateOf(Offset.Zero) }
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
    val activeColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "$labelX [$valueX] / $labelY [$valueY]",
            style = MaterialTheme.typography.bodyMedium,
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .pointerInput(pixelsPerUnit, 0, maxValue) {
                    detectDragGestures(
                        onDragStart = { dragRemainder = Offset.Zero },
                        onDragEnd = { dragRemainder = Offset.Zero },
                        onDragCancel = { dragRemainder = Offset.Zero },
                        onDrag = { _, dragAmount ->
                            dragRemainder -= dragAmount / pixelsPerUnit

                            if (dragRemainder != Offset.Zero) {
                                val deltaX = dragRemainder.x.toInt()
                                val deltaY = dragRemainder.y.toInt()

                                val newValueX = (currentValueX + deltaX).coerceIn(0, maxValue)
                                val newValueY = (currentValueY - deltaY).coerceIn(0, maxValue)

                                if (newValueX != currentValueX || newValueY != currentValueY) {
                                    currentOnValuesChange(newValueX, newValueY)
                                }

                                dragRemainder -= Offset(deltaX.toFloat(), deltaY.toFloat())
                            }
                        },
                    )
                },
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val thirdWidth = size.width / 3f

            val visibleUnits = (thirdWidth / pixelsPerUnit).toInt() + tickInterval
            Log.d("CwpScreen", "Pad visibleUnits: $visibleUnits")

            val firstValueX = maxOf(0, valueX - visibleUnits)
            val lastValueX = minOf(maxValue, valueX + visibleUnits)
            val startX = centerX + (firstValueX - valueX) * pixelsPerUnit
            val endX = centerX + (lastValueX - valueX) * pixelsPerUnit

            val firstValueY = maxOf(0, valueY - visibleUnits)
            val lastValueY = minOf(maxValue, valueY + visibleUnits)
            val startY = centerY - (firstValueY - valueY) * pixelsPerUnit
            val endY = centerY - (lastValueY - valueY) * pixelsPerUnit

            for (tickValue in firstValueY..lastValueY) {
                val y = centerY - (tickValue - valueY) * pixelsPerUnit

                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            currentColorAtValues(firstValueX, tickValue),
                            currentColorAtValues(lastValueX, tickValue)
                        ),
                        startX = startX,
                        endX = endX,
                    ),
                    topLeft = Offset(startX, y),
                    size = Size(endX - startX, pixelsPerUnit * 1.1f)
                )
            }

            for (tickValue in firstValueX..lastValueX) {
                if (tickValue % tickInterval != 0) continue

                val x = centerX + (tickValue - valueX) * pixelsPerUnit

                val isMajor = tickValue % majorTickInterval == 0
                val strokeWidth = (if (isMajor) 1f else 0.5f).dp.toPx()

                drawLine(
                    color = tickColor,
                    start = Offset(x, startY),
                    end = Offset(x, endY),
                    strokeWidth = strokeWidth,
                )
            }

            for (tickValue in firstValueY..lastValueY) {
                if (tickValue % tickInterval != 0) continue

                val y = centerY - (tickValue - valueY) * pixelsPerUnit

                val isMajor = tickValue % majorTickInterval == 0
                val strokeWidth = (if (isMajor) 1f else 0.5f).dp.toPx()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, y),
                    end = Offset(endX, y),
                    strokeWidth = strokeWidth,
                )
            }
            drawCircle(
                color = activeColor,
                center = Offset(centerX, centerY),
                radius = 3.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = tickColor,
                center = Offset(centerX, centerY),
                radius = 3.dp.toPx(),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

fun rgbToHsv(color: Int): IntArray {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(color, hsv)
    return intArrayOf(hsv[0].toInt(), (hsv[1] * 100f).toInt(), (hsv[2] * 100f).toInt())
}

fun hsvToColor(hue: Int, saturation: Int, value: Int): Color {
    val hsv = floatArrayOf(hue.toFloat(), saturation / 100f, value / 100f)
    return Color(AndroidColor.HSVToColor(hsv))
}

@Preview(showBackground = true)
@Composable
fun ColorSettingContentPreview() {
    CwpTheme {
        ColorSettingContent(
            red = 192,
            green = 224,
            blue = 255,
            hue = 0,
            saturation = 0,
            value = 0,
            onRedChanged = {},
            onGreenChanged = {},
            onBlueChanged = {},
            onHueChanged = {},
            onSaturationChanged = {},
            onValueChanged = {},
            onColorSelected = { _, _, _ -> }
        )
    }
}