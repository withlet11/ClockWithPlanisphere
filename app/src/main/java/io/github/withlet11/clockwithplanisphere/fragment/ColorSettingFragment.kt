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

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.DialogFragment
import io.github.withlet11.clockwithplanisphere.R

class ColorSettingFragment : DialogFragment() {
    companion object {
        const val DEFAULT_BACKGROUND_COLOR = 0xffc0e0ffu
    }

    private var backgroundColor = DEFAULT_BACKGROUND_COLOR.toInt()

    private var redState by mutableStateOf(0)
    private var greenState by mutableStateOf(0)
    private var blueState by mutableStateOf(0)
    private var isPositiveButtonEnabledState by mutableStateOf(false)

    private lateinit var dialog: AlertDialog

    interface BackgroundColorSettingDialogListener {
        fun onColorDialogPositiveClick(dialog: DialogFragment)
        fun onColorDialogNegativeClick(dialog: DialogFragment)
    }

    private var listener: BackgroundColorSettingDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as BackgroundColorSettingDialogListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        getPreviousValues()
        redState = Color.red(backgroundColor)
        greenState = Color.green(backgroundColor)
        blueState = Color.blue(backgroundColor)

        val composeView = ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ColorSettingContent(
                        red = redState,
                        green = greenState,
                        blue = blueState,
                        onRedChanged = { r ->
                            redState = r
                            backgroundColor = Color.rgb(redState, greenState, blueState)
                            isPositiveButtonEnabledState = true
                            updatePositiveButtonState()
                        },
                        onGreenChanged = { g ->
                            greenState = g
                            backgroundColor = Color.rgb(redState, greenState, blueState)
                            isPositiveButtonEnabledState = true
                            updatePositiveButtonState()
                        },
                        onBlueChanged = { b ->
                            blueState = b
                            backgroundColor = Color.rgb(redState, greenState, blueState)
                            isPositiveButtonEnabledState = true
                            updatePositiveButtonState()
                        },
                        onColorSelected = { r, g, b ->
                            redState = r
                            greenState = g
                            blueState = b
                            backgroundColor = Color.rgb(redState, greenState, blueState)
                            isPositiveButtonEnabledState = true
                            updatePositiveButtonState()
                        }
                    )
                }
            }
        }

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(composeView)
            .setTitle(R.string.bg_color)
            .setPositiveButton(context?.getText(R.string.modify)) { _, _ ->
                context?.getSharedPreferences("observation_position", Context.MODE_PRIVATE)?.edit()
                    ?.run {
                        putInt("backgroundColor", backgroundColor)
                        commit()
                    }
                listener?.onColorDialogPositiveClick(this)
            }
            .setNegativeButton(context?.getText(R.string.cancel)) { _, _ ->
                listener?.onColorDialogNegativeClick(this)
            }

        return builder.create().also {
            dialog = it
            it.setOnShowListener {
                updatePositiveButtonState()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        updatePositiveButtonState()
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    private fun updatePositiveButtonState() {
        if (::dialog.isInitialized) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = isPositiveButtonEnabledState
        }
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
            Triple(R.string.white, ComposeColor.White, ComposeColor.Black),
            Triple(R.string.silver, ComposeColor(0xFFC0C0C0), ComposeColor.Black),
            Triple(R.string.gray, ComposeColor.Gray, ComposeColor.Black),
            Triple(R.string.black, ComposeColor.Black, ComposeColor.White),
            Triple(R.string.red, ComposeColor.Red, ComposeColor.Black),
            Triple(R.string.maroon, ComposeColor(0xFF800000), ComposeColor.White),
            Triple(R.string.yellow, ComposeColor.Yellow, ComposeColor.Black),
            Triple(R.string.olive, ComposeColor(0xFF808000), ComposeColor.Black),
            Triple(R.string.lime, ComposeColor(0xFF00FF00), ComposeColor.Black),
            Triple(R.string.green, ComposeColor.Green, ComposeColor.White),
            Triple(R.string.aqua, ComposeColor.Cyan, ComposeColor.Black),
            Triple(R.string.teal, ComposeColor(0xFF008080), ComposeColor.White),
            Triple(R.string.blue, ComposeColor.Blue, ComposeColor.White),
            Triple(R.string.navy, ComposeColor(0xFF000080), ComposeColor.White),
            Triple(R.string.fuchsia, ComposeColor.Magenta, ComposeColor.Black),
            Triple(R.string.purple, ComposeColor(0xFF800080), ComposeColor.White),
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
            items(paletteColors) { (stringRes, bgCol, textCol) ->
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
    MaterialTheme {
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
