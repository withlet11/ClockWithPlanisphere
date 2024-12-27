/**
 * ColorSettingFragment.kt
 *
 * Copyright 2021-2024 Yasuhiro Yamakawa <withlet11@gmail.com>
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
import android.graphics.Color.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.fragment.app.DialogFragment
import io.github.withlet11.clockwithplanisphere.R

class ColorSettingFragment : DialogFragment() {
    companion object {
        const val DEFAULT_BACKGROUND_COLOR = 0xffc0e0ffu
    }

    private lateinit var colorPreview: View
    private lateinit var bgColorRSeekBar: SeekBar
    private lateinit var bgColorGSeekBar: SeekBar
    private lateinit var bgColorBSeekBar: SeekBar
    private lateinit var bgColorRField: TextView
    private lateinit var bgColorGField: TextView
    private lateinit var bgColorBField: TextView

    private var backgroundColor = DEFAULT_BACKGROUND_COLOR.toInt()

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
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val backgroundColorSettingView = inflater.inflate(R.layout.fragment_color_setting, null)
        builder.setView(backgroundColorSettingView)
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
                listener?.onColorDialogNegativeClick(
                    this
                )
            }

        getPreviousValues()
        prepareGUIComponents(backgroundColorSettingView)

        return builder.create().also { dialog = it }
    }

    override fun onStart() {
        super.onStart()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    private fun prepareGUIComponents(backgroundColorSettingView: View) {
        colorPreview = backgroundColorSettingView.findViewById<View>(R.id.colorPreview).apply {
            setBackgroundColor(backgroundColor)
        }
        bgColorRSeekBar =
            backgroundColorSettingView.findViewById<SeekBar>(R.id.bgColorRSeekBar).apply {
                min = 0
                max = 255
                progress = red(backgroundColor)
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        bgColorRField.text = "$progress"
                        backgroundColor =
                            rgb(progress, green(backgroundColor), blue(backgroundColor))
                        colorPreview.setBackgroundColor(backgroundColor)
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        // do nothing
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        // do nothing
                    }
                })

            }

        bgColorRField =
            backgroundColorSettingView.findViewById<TextView>(R.id.bgColorRField).apply {
                text = "%d".format(red(backgroundColor))
            }

        bgColorGSeekBar =
            backgroundColorSettingView.findViewById<SeekBar>(R.id.bgColorGSeekBar).apply {
                min = 0
                max = 255
                progress = green(backgroundColor)
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        bgColorGField.text = "$progress"
                        backgroundColor = rgb(red(backgroundColor), progress, blue(backgroundColor))
                        colorPreview.setBackgroundColor(backgroundColor)
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        // do nothing
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        // do nothing
                    }
                })

            }

        bgColorGField =
            backgroundColorSettingView.findViewById<TextView>(R.id.bgColorGField).apply {
                text = "%d".format(green(backgroundColor))
            }

        bgColorBSeekBar =
            backgroundColorSettingView.findViewById<SeekBar>(R.id.bgColorBSeekBar).apply {
                min = 0
                max = 255
                progress = blue(backgroundColor)
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        bgColorBField.text = "$progress"
                        backgroundColor =
                            rgb(red(backgroundColor), green(backgroundColor), progress)
                        colorPreview.setBackgroundColor(backgroundColor)
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        // do nothing
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        // do nothing
                    }
                })

            }

        bgColorBField =
            backgroundColorSettingView.findViewById<TextView>(R.id.bgColorBField).apply {
                text = "%d".format(blue(backgroundColor))
            }

        listOf(
            R.id.color01Button, R.id.color02Button, R.id.color03Button, R.id.color04Button,
            R.id.color05Button, R.id.color06Button, R.id.color07Button, R.id.color08Button,
            R.id.color09Button, R.id.color10Button, R.id.color11Button, R.id.color12Button,
            R.id.color13Button, R.id.color14Button, R.id.color15Button, R.id.color16Button
        ).forEach { id ->
            backgroundColorSettingView.findViewById<Button>(id).apply {
                val color = (background as ColorDrawable).color
                setOnClickListener {
                    bgColorRSeekBar.progress = color.red
                    bgColorGSeekBar.progress = color.green
                    bgColorBSeekBar.progress = color.blue
                }
            }
        }
    }

    private fun getPreviousValues() {
        context?.getSharedPreferences("observation_position", Context.MODE_PRIVATE)?.run {
            backgroundColor = getInt("backgroundColor", DEFAULT_BACKGROUND_COLOR.toInt())
        }
    }

}