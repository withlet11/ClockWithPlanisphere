package io.github.withlet11.clockwithplanisphere.ui

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import io.github.withlet11.clockwithplanisphere.view.*

@Composable
fun ClockScreen() {
    AndroidView(
        factory = { context ->
            FrameLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                val clockBasePanel = ClockBasePanel(context, null).apply { 
                    currentDate = java.time.LocalDate.now()
                }
                val skyPanel = SkyPanel(context, null)
                val sunPanel = SunPanel(context, null)
                val moonPanel = SunAndMoonPanel(context, null)
                val horizonPanel = HorizonPanel(context, null)
                val clockHandsPanel = ClockHandsPanel(context, null).apply { 
                    localTime = java.time.LocalTime.MIDNIGHT
                }

                val lp = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.CENTER
                }

                addView(clockBasePanel, lp)
                addView(skyPanel, lp)
                addView(sunPanel, lp)
                addView(moonPanel, lp)
                addView(horizonPanel, lp)
                addView(clockHandsPanel, lp)
            }
        }
    )
}

@Composable
fun ClockContent(
    clockBasePanel: ClockBasePanel,
    skyPanel: SkyPanel,
    sunPanel: SunPanel,
    sunAndMoonPanel: SunAndMoonPanel,
    horizonPanel: HorizonPanel,
    clockHandsPanel: ClockHandsPanel,
    onFrameLayoutCreated: (FrameLayout) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { context ->
                FrameLayout(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    val lp = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = android.view.Gravity.CENTER
                    }

                    addView(clockBasePanel, lp)
                    addView(skyPanel, lp)
                    addView(sunPanel, lp)
                    addView(sunAndMoonPanel, lp)
                    addView(horizonPanel, lp)
                    addView(clockHandsPanel, lp)

                    onFrameLayoutCreated(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
fun ClockScreenPreview() {
    ClockScreen()
}
