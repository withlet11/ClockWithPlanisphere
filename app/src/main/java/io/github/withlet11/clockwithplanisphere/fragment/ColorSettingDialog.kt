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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.content.edit
import io.github.withlet11.clockwithplanisphere.BACKGROUND_COLOR_KEY
import io.github.withlet11.clockwithplanisphere.CwpTheme
import io.github.withlet11.clockwithplanisphere.PREFERENCES_NAME
import io.github.withlet11.clockwithplanisphere.R


const val DEFAULT_BACKGROUND_COLOR = 0xffc0e0ffu

enum class ColorMode { RGB, HSV, PALETTE }

@Composable
fun ColorSettingDialog(
    onDismiss: () -> Unit,
    onColorSaved: () -> Unit,
) {
    val context = LocalContext.current

    var backgroundColor by rememberSaveable {
        mutableIntStateOf(DEFAULT_BACKGROUND_COLOR.toInt())
    }

    var red by rememberSaveable { mutableIntStateOf(backgroundColor.red) }
    var green by rememberSaveable { mutableIntStateOf(backgroundColor.green) }
    var blue by rememberSaveable { mutableIntStateOf(backgroundColor.blue) }

    val hsv = rgbToHsv(backgroundColor)
    var hue by rememberSaveable { mutableIntStateOf(hsv[0]) }
    var saturation by rememberSaveable { mutableIntStateOf(hsv[1]) }
    var value by rememberSaveable { mutableIntStateOf(hsv[2]) }

    var isPositiveButtonEnabled by rememberSaveable { mutableStateOf(false) }

    fun updateRgbColor(
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

    fun updateHsvColor(
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

    val colorMode = rememberSaveable { mutableStateOf(ColorMode.RGB) }
    val scrollState = rememberScrollState()
    val fadeHeight = 24.dp
    val dialogColor = MaterialTheme.colorScheme.surfaceContainer

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = dialogColor,
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 500.dp)
                    .drawWithContent {
                        drawContent()

                        val fadePx = fadeHeight.toPx()
                        val fadeColor = dialogColor.copy(alpha = 0.9f)

                        if (scrollState.value > 0) {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(fadeColor, Color.Transparent),
                                    startY = 0f,
                                    endY = fadePx,
                                ),
                                size = Size(size.width, fadePx),
                            )
                        }

                        if (scrollState.value < scrollState.maxValue) {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, fadeColor),
                                    startY = size.height - fadePx,
                                    endY = size.height,
                                ),
                                topLeft = Offset(0f, size.height - fadePx),
                                size = Size(size.width, fadePx),
                            )
                        }
                    }
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(intrinsicSize = IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    ) {
                    Text(
                        modifier = Modifier.weight(0.6f),
                        text = stringResource(R.string.bg_color),
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                    )
                    ColorModeSelector(
                        modifier = Modifier.weight(0.5f),
                        colorMode = colorMode)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f)
                ) {
                    ColorSettingContent(
                        colorMode = colorMode,
                        red = red,
                        green = green,
                        blue = blue,
                        hue = hue,
                        saturation = saturation,
                        value = value,
                        onRedChanged = { value -> updateRgbColor(newRed = value) },
                        onGreenChanged = { value -> updateRgbColor(newGreen = value) },
                        onBlueChanged = { value -> updateRgbColor(newBlue = value) },
                        onHueChanged = { value -> updateHsvColor(newHue = value) },
                        onSaturationChanged = { value -> updateHsvColor(newSaturation = value) },
                        onValueChanged = { value -> updateHsvColor(newValue = value) },
                        onColorSelected = { r, g, b ->
                            updateRgbColor(
                                newRed = r,
                                newGreen = g,
                                newBlue = b
                            )
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }

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
                }
            }
        }
    }
}

@Composable
fun ColorModeSelector(
    modifier: Modifier = Modifier,
    colorMode: MutableState<ColorMode>,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf(
            "RGB" to ColorMode.RGB,
            "HSV" to ColorMode.HSV,
            "16C" to ColorMode.PALETTE
        ).forEach { (label, mode) ->
            val selected = mode == colorMode.value

            Box(
                modifier = Modifier
                    .background(
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .clickable { colorMode.value = mode },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }

}

@Composable
fun ColorSettingContent(
    colorMode: MutableState<ColorMode>,
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

    if (colorMode.value != ColorMode.PALETTE)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = modifier
                    .fillMaxHeight()
                    .weight(0.4f)
                    .align(Alignment.Top),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(backgroundColor)
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                )

                if (colorMode.value == ColorMode.RGB)
                    Column {

                        Text(
                            text = "${stringResource(R.string.red)}:${
                                red.toString().padStart(3)
                            }",
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${stringResource(R.string.green)}:${
                                green.toString().padStart(3)
                            }",
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${stringResource(R.string.blue)}:${
                                blue.toString().padStart(3)
                            }",
                            fontFamily = FontFamily.Monospace
                        )
                    }
                else
                    Column {
                        Text(
                            text = "${stringResource(R.string.hsv_h)}:${
                                hue.toString().padStart(3)
                            }°",
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${stringResource(R.string.hsv_s)}:${
                                saturation.toString().padStart(3)
                            }%",
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${stringResource(R.string.hsv_v)}:${
                                value.toString().padStart(3)
                            }%",
                            fontFamily = FontFamily.Monospace
                        )
                    }
            }

            if (colorMode.value == ColorMode.RGB)
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .align(alignment = Alignment.Top),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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
                }
            else
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .align(alignment = Alignment.Top),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HueDial(
                        label = stringResource(R.string.hsv_h),
                        value = hue,
                        colorAtValue = { hue -> hsvToColor(hue, 100, 100) },
                        onValueChange = onHueChanged
                    )

                    ColorPad(
                        values = IntOffset(saturation, value),
                        labels = stringResource(R.string.hsv_s) to stringResource(R.string.hsv_v),
                        colorAtValues = { saturation, value ->
                            hsvToColor(
                                hue,
                                saturation,
                                value
                            )
                        },
                        onValuesChange = { (saturation, value) ->
                            onSaturationChanged(saturation)
                            onValueChanged(value)
                        },
                    )
                }
        }
    else {

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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
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
                    shape = RoundedCornerShape(if (bgCol == backgroundColor) 24.dp else 4.dp),
                    modifier = Modifier
                        .height(48.dp)
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
    val outline = MaterialTheme.colorScheme.outline

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
            val oneThirdWidth = size.width / 2f
            val visibleUnits = (oneThirdWidth / pixelsPerUnit).toInt() // + tickInterval

            drawBarArea(
                value = value,
                leftValue = maxOf(0, value - visibleUnits),
                rightValue = minOf(maxValue, value + visibleUnits),
                visibleUnits = visibleUnits,
                tickInterval = tickInterval,
                majorTickInterval = majorTickInterval,
                pixelsPerUnit = pixelsPerUnit,
                tickColor = tickColor,
                activeColor = activeColor,
                outlineColor = outline,
                colorAtValue = colorAtValue
            )
        }

        Text(
            text = "◀ $label ▶",
            style = MaterialTheme.typography.labelMedium,
        )
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
    val outline = MaterialTheme.colorScheme.outline

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
            val thirdWidth = size.width / 2f
            val visibleUnits = (thirdWidth / pixelsPerUnit).toInt() // + tickInterval / 2

            drawBarArea(
                value = value,
                leftValue = value - visibleUnits,
                rightValue = value + visibleUnits,
                visibleUnits = visibleUnits,
                tickInterval = tickInterval,
                majorTickInterval = majorTickInterval,
                pixelsPerUnit = pixelsPerUnit,
                tickColor = tickColor,
                activeColor = activeColor,
                outlineColor = outline,
                colorAtValue = colorAtValue
            )
        }

        Text(
            text = "◀ $label ▶",
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun ColorPad(
    values: IntOffset,
    labels: Pair<String, String>,
    onValuesChange: (IntOffset) -> Unit,
    colorAtValues: (Int, Int) -> Color,
    modifier: Modifier = Modifier,
) {
    val maxValue = 100
    val dpPerUnit = 2.dp
    val tickInterval = 5
    val majorTickInterval = 20

    val density = LocalDensity.current
    val pixelsPerUnit = with(density) { dpPerUnit.toPx() }

    val currentValues by rememberUpdatedState(values)
    val currentOnValuesChange by rememberUpdatedState(onValuesChange)
    val currentColorAtValues by rememberUpdatedState(colorAtValues)
    var dragRemainder by remember { mutableStateOf(Offset.Zero) }
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
    val activeColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
                                val delta = IntOffset(
                                    dragRemainder.x.toInt(),
                                    dragRemainder.y.toInt()
                                )

                                val newValues = IntOffset(
                                    (currentValues + delta).x.coerceIn(0, maxValue),
                                    (currentValues - delta).y.coerceIn(0, maxValue)
                                )

                                if (newValues != currentValues) {
                                    currentOnValuesChange(newValues)
                                }

                                dragRemainder -= Offset(delta.x.toFloat(), delta.y.toFloat())
                            }
                        },
                    )
                },
        ) {
            val oneThirdWidth = size.width / 2f
            val visibleUnits = (oneThirdWidth / pixelsPerUnit).toInt() // + tickInterval

            drawSquareArea(
                values = values,
                topLeftValues = IntOffset(
                    maxOf(0, values.x - visibleUnits),
                    maxOf(0, values.y - visibleUnits)
                ),
                bottomRightValues = IntOffset(
                    minOf(maxValue, values.x + visibleUnits),
                    minOf(maxValue, values.y + visibleUnits)
                ),
                visibleUnits = visibleUnits,
                tickInterval = tickInterval,
                majorTickInterval = majorTickInterval,
                pixelsPerUnit = pixelsPerUnit,
                tickColor = tickColor,
                activeColor = activeColor,
                outlineColor = outlineColor,
                currentColorAtValues = currentColorAtValues
            )
        }

        Text(
            text = "◀ ${labels.first} ▶ | ▲ ${labels.second} ▼",
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

private fun DrawScope.drawBarArea(
    value: Int,
    leftValue: Int,
    rightValue: Int,
    visibleUnits: Int,
    tickInterval: Int,
    majorTickInterval: Int,
    pixelsPerUnit: Float,
    tickColor: Color,
    activeColor: Color,
    outlineColor: Color,
    colorAtValue: (Int) -> Color,
) {
    translate(left = center.x, top = center.y) {
        val left = (leftValue - value) * pixelsPerUnit
        val right = (rightValue - value) * pixelsPerUnit

        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(colorAtValue(leftValue), colorAtValue(rightValue)),
                startX = left,
                endX = right,
            ),
            topLeft = Offset(left, -size.height / 2f),
            size = Size(right - left, size.height)
        )

        for (tickValue in leftValue..rightValue) {
            if (tickValue % tickInterval != 0) continue

            val x = (tickValue - value) * pixelsPerUnit

            val isMajor = tickValue % majorTickInterval == 0
            val tickHeight = if (isMajor) 24.dp else 12.dp
            val y = tickHeight.toPx() / 2f

            drawLine(
                color = tickColor,
                start = Offset(x, -y),
                end = Offset(x, y),
                strokeWidth = 1.dp.toPx(),
            )
        }

        drawLine(
            color = activeColor,
            start = Offset(0f, -size.height / 2f),
            end = Offset(0f, size.height / 2f),
            strokeWidth = 2.dp.toPx(),
        )

        drawBorder(
            areaSize = Size(visibleUnits * 2f * pixelsPerUnit, size.height),
            color = outlineColor
        )
    }
}

private fun DrawScope.drawSquareArea(
    values: IntOffset,
    topLeftValues: IntOffset,
    bottomRightValues: IntOffset,
    visibleUnits: Int,
    tickInterval: Int,
    majorTickInterval: Int,
    pixelsPerUnit: Float,
    tickColor: Color,
    activeColor: Color,
    outlineColor: Color,
    currentColorAtValues: (Int, Int) -> Color
) {
    translate(center.x, center.y) {
        val topLeft = (topLeftValues - values).run {
            Offset(x * pixelsPerUnit, -y * pixelsPerUnit)
        }

        val bottomRight = (bottomRightValues - values).run {
            Offset(x * pixelsPerUnit, -y * pixelsPerUnit)
        }

        for (tickValue in topLeftValues.y..bottomRightValues.y) {
            val y = -(tickValue - values.y) * pixelsPerUnit

            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        currentColorAtValues(topLeftValues.x, tickValue),
                        currentColorAtValues(bottomRightValues.x, tickValue)
                    ),
                    startX = topLeft.x,
                    endX = bottomRight.x,
                ),
                topLeft = Offset(topLeft.x, y),
                size = Size((bottomRight - topLeft).x, pixelsPerUnit * 1.1f)
            )
        }

        for (tickValue in topLeftValues.x..bottomRightValues.x) {
            if (tickValue % tickInterval != 0) continue

            val isMajor = tickValue % majorTickInterval == 0
            val strokeWidth = (if (isMajor) 1f else 0.5f).dp.toPx()

            val x = (tickValue - values.x) * pixelsPerUnit

            drawLine(
                color = tickColor,
                start = Offset(x, topLeft.y),
                end = Offset(x, bottomRight.y),
                strokeWidth = strokeWidth,
            )
        }

        for (tickValue in topLeftValues.y..bottomRightValues.y) {
            if (tickValue % tickInterval != 0) continue

            val isMajor = tickValue % majorTickInterval == 0
            val strokeWidth = (if (isMajor) 1f else 0.5f).dp.toPx()

            val y = -(tickValue - values.y) * pixelsPerUnit

            drawLine(
                color = tickColor,
                start = Offset(topLeft.x, y),
                end = Offset(bottomRight.x, y),
                strokeWidth = strokeWidth,
            )
        }

        drawCircle(
            color = activeColor,
            center = Offset.Zero,
            radius = 3.dp.toPx(),
            style = Stroke(width = 1.dp.toPx())
        )

        drawCircle(
            color = tickColor,
            center = Offset.Zero,
            radius = 3.dp.toPx(),
            style = Stroke(width = 2.dp.toPx())
        )

        drawBorder(
            areaSize = (visibleUnits * 2f * pixelsPerUnit).let { Size(it, it) },
            color = outlineColor
        )
    }

}

private fun DrawScope.drawBorder(areaSize: Size, color: Color) {
    drawRect(
        color = color,
        topLeft = -areaSize.center,
        size = areaSize,
        style = Stroke(width = 0.5.dp.toPx())
    )
}

private fun rgbToHsv(color: Int): IntArray {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(color, hsv)
    return intArrayOf(hsv[0].toInt(), (hsv[1] * 100f).toInt(), (hsv[2] * 100f).toInt())
}

private fun hsvToColor(hue: Int, saturation: Int, value: Int): Color {
    val hsv = floatArrayOf(hue.toFloat(), saturation / 100f, value / 100f)
    return Color(AndroidColor.HSVToColor(hsv))
}

@Preview(showBackground = true)
@Composable
fun ColorSettingContentPreview() {
    CwpTheme {
        ColorSettingContent(
            colorMode = remember { mutableStateOf(ColorMode.RGB) },
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