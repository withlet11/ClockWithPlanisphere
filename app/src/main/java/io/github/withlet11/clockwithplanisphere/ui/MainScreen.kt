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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import io.github.withlet11.clockwithplanisphere.CwpTopAppBar
import io.github.withlet11.clockwithplanisphere.R
import io.github.withlet11.clockwithplanisphere.dialogs.ColorSettingDialog
import io.github.withlet11.clockwithplanisphere.dialogs.LayoutSettingDialog
import io.github.withlet11.clockwithplanisphere.dialogs.LocationSettingDialog
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onPrivacyPolicyClick: () -> Unit,
    onLicensesClick: () -> Unit,
    onCreditsClick: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val defaultColor = colorResource(R.color.defaultBackGround).toArgb()

    viewModel.initialize(defaultColor)

    val latitude = viewModel.latitude
    val longitude = viewModel.longitude
    val isSouthernSky = viewModel.isSouthernSky
    val backgroundColor = if (viewModel.isInitialized) viewModel.backgroundColor else defaultColor
    val horizontalPosition = viewModel.horizontalPosition
    val verticalPosition = viewModel.verticalPosition

    var menuExpanded by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showLayoutDialog by remember { mutableStateOf(false) }
    var showAd by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(10L.seconds)
        showAd = false
    }

    Scaffold(
        topBar = {
            CwpTopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewModel.updateSouthernSky(!isSouthernSky) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(
                                stringResource(R.string.north_label),
                                stringResource(R.string.south_label)
                            ).forEachIndexed { index, label ->
                                val selected = (index == 1) == isSouthernSky

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
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null
                                        )
                                    },
                                    text = { Text(stringResource(R.string.locationSettings)) },
                                    onClick = {
                                        menuExpanded = false
                                        showLocationDialog = true
                                    },
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Palette,
                                            contentDescription = null
                                        )
                                    },
                                    text = { Text(stringResource(R.string.bg_color)) },
                                    onClick = {
                                        menuExpanded = false
                                        showColorDialog = true
                                    },
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = null
                                        )
                                    },
                                    text = { Text(stringResource(R.string.layoutSettings)) },
                                    onClick = {
                                        menuExpanded = false
                                        showLayoutDialog = true
                                    },
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 0.5.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.PrivacyTip,
                                            contentDescription = null
                                        )
                                    },
                                    text = { Text(stringResource(R.string.privacy_policy_header)) },
                                    onClick = {
                                        menuExpanded = false
                                        onPrivacyPolicyClick()
                                    },
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null
                                        )
                                    },
                                    text = { Text(stringResource(R.string.license)) },
                                    onClick = {
                                        menuExpanded = false
                                        onLicensesClick()
                                    },
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null
                                        )
                                    },
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
                        viewModel.loadPreviousPosition()
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
                        viewModel.loadColorSettings(defaultColor)
                    },
                )
            }
            if (showLayoutDialog) {
                LayoutSettingDialog(
                    onDismiss = {
                        showLayoutDialog = false
                    },
                    onLayoutSaved = {
                        showLayoutDialog = false
                        viewModel.loadLayoutSettings()
                    },
                )
            }

            CwpScreen(
                isSouthernSky = isSouthernSky,
                latitude = latitude,
                longitude = longitude,
                backgroundColor = backgroundColor,
                horizontalPosition = horizontalPosition,
                verticalPosition = verticalPosition
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
