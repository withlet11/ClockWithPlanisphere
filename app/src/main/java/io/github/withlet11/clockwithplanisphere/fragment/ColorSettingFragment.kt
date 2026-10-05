/*
 * ColorSettingFragment.kt
 *
 * Copyright 2021-2026 Yasuhiro Yamakawa <withlet11@gmail.com>
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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.DialogFragment
import io.github.withlet11.clockwithplanisphere.CwpTheme
import io.github.withlet11.clockwithplanisphere.R

class ColorSettingFragment : DialogFragment() {
    companion object {
        const val DEFAULT_BACKGROUND_COLOR = 0xffc0e0ffu
    }

    private var backgroundColor = DEFAULT_BACKGROUND_COLOR.toInt()

    private var redState by mutableStateOf(0)
    private var greenState by mutableIntStateOf(0)
    private var blueState by mutableIntStateOf(0)
    private var isPositiveButtonEnabledState by mutableStateOf(true)

    interface BackgroundColorSettingDialogListener {
        fun onColorDialogPositiveClick(dialog: DialogFragment)
        fun onColorDialogNegativeClick(dialog: DialogFragment)
    }

    private var listener: BackgroundColorSettingDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as BackgroundColorSettingDialogListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        getPreviousValues()
        redState = Color.red(backgroundColor)
        greenState = Color.green(backgroundColor)
        blueState = Color.blue(backgroundColor)

        return ComposeView(requireContext()).apply {
            setContent {
                CwpTheme {
                    AlertDialog(
                        onDismissRequest = {
                            listener?.onColorDialogNegativeClick(this@ColorSettingFragment)
                            dismiss()
                        },
                        title = {
                            Text(text = stringResource(R.string.bg_color))
                        },
                        text = {
                            ColorSettingContent(
                                red = redState,
                                green = greenState,
                                blue = blueState,
                                onRedChanged = { r ->
                                    redState = r
                                    backgroundColor = Color.rgb(redState, greenState, blueState)
                                    isPositiveButtonEnabledState = true
                                },
                                onGreenChanged = { g ->
                                    greenState = g
                                    backgroundColor = Color.rgb(redState, greenState, blueState)
                                    isPositiveButtonEnabledState = true
                                },
                                onBlueChanged = { b ->
                                    blueState = b
                                    backgroundColor = Color.rgb(redState, greenState, blueState)
                                    isPositiveButtonEnabledState = true
                                },
                                onColorSelected = { r, g, b ->
                                    redState = r
                                    greenState = g
                                    blueState = b
                                    backgroundColor = Color.rgb(redState, greenState, blueState)
                                    isPositiveButtonEnabledState = true
                                }
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    context?.getSharedPreferences("observation_position", Context.MODE_PRIVATE)?.edit()
                                        ?.run {
                                            putInt("backgroundColor", backgroundColor)
                                            commit()
                                        }
                                    listener?.onColorDialogPositiveClick(this@ColorSettingFragment)
                                    dismiss()
                                },
                                enabled = isPositiveButtonEnabledState
                            ) {
                                Text(stringResource(R.string.modify))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    listener?.onColorDialogNegativeClick(this@ColorSettingFragment)
                                    dismiss()
                                }
                            ) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    private fun getPreviousValues() {
        context?.getSharedPreferences("observation_position", Context.MODE_PRIVATE)?.run {
            backgroundColor = getInt("backgroundColor", DEFAULT_BACKGROUND_COLOR.toInt())
        }
    }
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
                    .border(1.dp, MaterialTheme.colorScheme.outline)
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
