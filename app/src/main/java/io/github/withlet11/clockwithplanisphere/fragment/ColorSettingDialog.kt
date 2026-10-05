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
import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import io.github.withlet11.clockwithplanisphere.CwpTheme
import io.github.withlet11.clockwithplanisphere.R

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

    var red by rememberSaveable {
        mutableIntStateOf(Color.red(backgroundColor))
    }
    var green by rememberSaveable {
        mutableIntStateOf(Color.green(backgroundColor))
    }
    var blue by rememberSaveable {
        mutableIntStateOf(Color.blue(backgroundColor))
    }

    var isPositiveButtonEnabled by rememberSaveable {
        mutableStateOf(true)
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

        red = Color.red(backgroundColor)
        green = Color.green(backgroundColor)
        blue = Color.blue(backgroundColor)
    }

    fun updateColor(
        newRed: Int = red,
        newGreen: Int = green,
        newBlue: Int = blue,
    ) {
        red = newRed
        green = newGreen
        blue = newBlue
        backgroundColor = Color.rgb(red, green, blue)
        isPositiveButtonEnabled = true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.bg_color))
        },
        text = {
            ColorSettingContent(
                red = red,
                green = green,
                blue = blue,
                onRedChanged = { value ->
                    updateColor(newRed = value)
                },
                onGreenChanged = { value ->
                    updateColor(newGreen = value)
                },
                onBlueChanged = { value ->
                    updateColor(newBlue = value)
                },
                onColorSelected = { r, g, b ->
                    updateColor(
                        newRed = r,
                        newGreen = g,
                        newBlue = b
                    )
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    context.getSharedPreferences(
                        PREFERENCES_NAME,
                        Context.MODE_PRIVATE
                    ).edit {
                        putInt(
                            BACKGROUND_COLOR_KEY,
                            backgroundColor
                        )
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
            TextButton(
                onClick = onDismiss
            ) {
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
    onRedChanged: (Int) -> Unit,
    onGreenChanged: (Int) -> Unit,
    onBlueChanged: (Int) -> Unit,
    onColorSelected: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = ComposeColor(red, green, blue)

    Column(
        modifier = modifier
            .padding(16.dp)
            .width(320.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .background(backgroundColor)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline
                    )
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ColorSliderRow(
                    label = stringResource(R.string.red),
                    value = red,
                    onValueChange = onRedChanged
                )

                ColorSliderRow(
                    label = stringResource(R.string.green),
                    value = green,
                    onValueChange = onGreenChanged
                )

                ColorSliderRow(
                    label = stringResource(R.string.blue),
                    value = blue,
                    onValueChange = onBlueChanged
                )
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
fun ColorSliderRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodySmall
        )

        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..255f,
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
        )

        Text(
            text = "$value",
            modifier = Modifier.width(30.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ColorSettingContentPreview() {
    CwpTheme {
        ColorSettingContent(
            red = 192,
            green = 224,
            blue = 255,
            onRedChanged = {},
            onGreenChanged = {},
            onBlueChanged = {},
            onColorSelected = { _, _, _ -> }
        )
    }
}