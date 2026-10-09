/*
 * MainScreen.kt
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

import android.content.Context.MODE_PRIVATE
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import io.github.withlet11.clockwithplanisphere.CwpTopAppBar
import io.github.withlet11.clockwithplanisphere.R
import io.github.withlet11.clockwithplanisphere.fragment.ColorSettingDialog
import io.github.withlet11.clockwithplanisphere.fragment.LocationSettingDialog
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

private const val DEFAULT_LATITUDE = 45.0
private const val DEFAULT_LONGITUDE = 0.0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onPrivacyPolicyClick: () -> Unit,
    onLicensesClick: () -> Unit,
    onCreditsClick: () -> Unit,
) {
    val context = LocalContext.current

    val defaultColor = colorResource(R.color.defaultBackGround).toArgb()

    var latitude by remember { mutableDoubleStateOf(DEFAULT_LATITUDE) }
    var longitude by remember { mutableDoubleStateOf(DEFAULT_LONGITUDE) }
    var backgroundColor by remember { mutableIntStateOf(0) }
    var isSouthernSky by remember { mutableStateOf(false) }

    fun loadPreviousSettings() {
        val previous = context.getSharedPreferences("observation_position", MODE_PRIVATE)

        try {
            latitude = previous.getFloat("latitude", DEFAULT_LATITUDE.toFloat()).toDouble()
            longitude = previous.getFloat("longitude", DEFAULT_LONGITUDE.toFloat()).toDouble()
            isSouthernSky = previous.getBoolean("isSouthernSky", false)
            backgroundColor = previous.getInt( "backgroundColor", defaultColor )
        } catch (_: ClassCastException) {
            latitude = DEFAULT_LATITUDE
            longitude = DEFAULT_LONGITUDE
            isSouthernSky = false
            backgroundColor = defaultColor
        }
    }

    fun loadPreviousPosition() {
        val previous = context.getSharedPreferences("observation_position", MODE_PRIVATE)

        try {
            latitude = previous.getFloat("latitude", DEFAULT_LATITUDE.toFloat()).toDouble()
            longitude = previous.getFloat("longitude", DEFAULT_LONGITUDE.toFloat()).toDouble()
        } catch (_: ClassCastException) {
            latitude = DEFAULT_LATITUDE
            longitude = DEFAULT_LONGITUDE
        }
    }

    fun loadColorSettings() {
        val previous = context.getSharedPreferences("observation_position", MODE_PRIVATE)

        backgroundColor = try {
            previous.getInt("backgroundColor", defaultColor)
        } catch (_: ClassCastException) {
            defaultColor
        }
    }

    loadPreviousSettings()

    var menuExpanded by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showAd by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(10L.seconds)
        showAd = false
    }

    Scaffold(
        topBar = {
            CwpTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(60.dp)
                    )
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.north_label),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = isSouthernSky,
                            onCheckedChange = {
                                isSouthernSky = it
                                context.getSharedPreferences("observation_position", MODE_PRIVATE)
                                    .edit {
                                        putBoolean("isSouthernSky", isSouthernSky)
                                        putInt("backgroundColor", backgroundColor)
                                    }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                uncheckedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                checkedBorderColor = MaterialTheme.colorScheme.primary,
                                uncheckedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            thumbContent = {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                            },
                        )
                        Text(
                            text = stringResource(R.string.south_label),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Menu"
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.locationSettings)) },
                                    onClick = {
                                        menuExpanded = false
                                        showLocationDialog = true
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.bg_color)) },
                                    onClick = {
                                        menuExpanded = false
                                        showColorDialog = true
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.privacy_policy_header)) },
                                    onClick = {
                                        menuExpanded = false
                                        onPrivacyPolicyClick()
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.license)) },
                                    onClick = {
                                        menuExpanded = false
                                        onLicensesClick()
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.opensource_licenses)) },
                                    onClick = {
                                        menuExpanded = false
                                        onCreditsClick()
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showLocationDialog) {
                LocationSettingDialog(
                    onDismiss = {
                        showLocationDialog = false
                    },
                    onLocationSaved = {
                        showLocationDialog = false
                        loadPreviousPosition()
                    },
                )
            }
            if (showColorDialog) {
                ColorSettingDialog(
                    onDismiss = {
                        showColorDialog = false
                    },
                    onColorSaved = {
                        showColorDialog = false
                        loadColorSettings()
                    },
                )
            }

            CwpScreen(
                isSouthernSky = isSouthernSky,
                latitude = latitude,
                longitude = longitude,
                backgroundColor = backgroundColor,
                modifier = Modifier.fillMaxSize()
            )

            if (showAd) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    contentAlignment = Alignment.Center,
                ) {
                    AndroidView(
                        factory = { context ->
                            AdView(context).apply {
                                setAdSize(AdSize.BANNER)
                                adUnitId = "ca-app-pub-6502278727709781/9103220433"
//                                adUnitId = "ca-app-pub-3940256099942544/6300978111" // Test ad
                                loadAd(AdRequest.Builder().build())
                            }
                        },
                    )
                }
            }
        }
    }
}
