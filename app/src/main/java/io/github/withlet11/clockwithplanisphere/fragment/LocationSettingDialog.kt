/**
 * LocationSettingDialog.kt
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

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.google.android.gms.location.*
import io.github.withlet11.clockwithplanisphere.CwpTheme
import io.github.withlet11.clockwithplanisphere.DEFAULT_LATITUDE
import io.github.withlet11.clockwithplanisphere.DEFAULT_LONGITUDE
import io.github.withlet11.clockwithplanisphere.LATITUDE_KEY
import io.github.withlet11.clockwithplanisphere.LONGITUDE_KEY
import io.github.withlet11.clockwithplanisphere.PREFERENCES_NAME
import io.github.withlet11.clockwithplanisphere.R

private const val MAXIMUM_UPDATE_INTERVAL = 10000L
private const val MINIMUM_UPDATE_INTERVAL = 5000L

@Composable
fun LocationSettingDialog(
    onDismiss: () -> Unit,
    onLocationSaved: () -> Unit,
) {
    val context = LocalContext.current

    var currentLatitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var currentLongitude by rememberSaveable { mutableStateOf<Double?>(null) }

    var latitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var longitude by rememberSaveable { mutableStateOf<Double?>(null) }

    var latitudeText by rememberSaveable { mutableStateOf("") }
    var longitudeText by rememberSaveable { mutableStateOf("") }

    var isLatitudeError by rememberSaveable { mutableStateOf(false) }
    var isLongitudeError by rememberSaveable { mutableStateOf(false) }

    var statusMessage by rememberSaveable { mutableStateOf<Int?>(null) }
    var isGetLocationEnabled by rememberSaveable { mutableStateOf(true) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val currentOnLocation by rememberUpdatedState(
        newValue = { location: Location ->
            latitude = location.latitude
            longitude = location.longitude
            latitudeText = "%+f".format(location.latitude)
            longitudeText = "%+f".format(location.longitude)
            isLatitudeError = false
            isLongitudeError = false
            isGetLocationEnabled = true
            statusMessage = null
        }
    )

    val currentOnError by rememberUpdatedState(
        newValue = {
            isGetLocationEnabled = true
            statusMessage = R.string.pleaseCheckIfGPSIsOn
        }
    )

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation

                if (location != null) {
                    currentOnLocation(location)
                } else {
                    currentOnError()
                }

                fusedLocationClient.removeLocationUpdates(this)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            startLocationUpdates(
                context = context,
                fusedLocationClient = fusedLocationClient,
                locationCallback = locationCallback,
                onError = {
                    isGetLocationEnabled = true
                    statusMessage = R.string.pleaseCheckIfGPSIsOn
                }
            )
        } else {
            isGetLocationEnabled = true
            statusMessage = R.string.no_permission_to_access_location_permanent
        }
    }

    fun startLocation(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        permissionLauncher: ManagedActivityResultLauncher<
                Array<String>,
                Map<String, Boolean>
                >,
        onStart: () -> Unit,
        onGpsDisabled: () -> Unit,
        locationCallback: LocationCallback
    ) {
        onStart()

        val fineLocationGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationGranted && !coarseLocationGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        startLocationUpdates(
            context = context,
            fusedLocationClient = fusedLocationClient,
            locationCallback = locationCallback,
            onError = onGpsDisabled
        )
    }

    LaunchedEffect(Unit) {
        val preferences = context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

        latitude = preferences.getFloat(
            LATITUDE_KEY,
            DEFAULT_LATITUDE.toFloat()
        ).toDouble()

        longitude = preferences.getFloat(
            LONGITUDE_KEY,
            DEFAULT_LONGITUDE.toFloat()
        ).toDouble()

        latitudeText = "%+f".format(latitude)
        longitudeText = "%+f".format(longitude)

        currentLatitude = latitude
        currentLongitude = longitude
    }

    val isPositiveButtonEnabled =
        latitude != null && longitude != null &&
                (latitude != currentLatitude || longitude != currentLongitude)

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
                Text(
                    text = stringResource(R.string.locationSettings),
                    style = MaterialTheme.typography.titleLarge,
                )

                LocationSettingContent(
                    latitudeText = latitudeText,
                    onLatitudeChanged = { text ->
                        latitudeText = text

                        val parsed = text
                            .replace(',', '.')
                            .toDoubleOrNull()

                        if (parsed != null && parsed in -90.0..90.0) {
                            latitude = parsed
                            isLatitudeError = false
                        } else {
                            latitude = null
                            isLatitudeError = true
                        }
                    },
                    isLatitudeError = isLatitudeError,
                    longitudeText = longitudeText,
                    onLongitudeChanged = { text ->
                        longitudeText = text

                        val parsed = text
                            .replace(',', '.')
                            .toDoubleOrNull()

                        if (parsed != null && parsed in -180.0..180.0) {
                            longitude = parsed
                            isLongitudeError = false
                        } else {
                            longitude = null
                            isLongitudeError = true
                        }
                    },
                    isLongitudeError = isLongitudeError,
                    statusMessage = statusMessage?.let { stringResource(it) } ?: "",
                    isGetLocationEnabled = isGetLocationEnabled,
                    onGetLocationClick = {
                        startLocation(
                            context = context,
                            fusedLocationClient = fusedLocationClient,
                            permissionLauncher = permissionLauncher,
                            locationCallback = locationCallback,
                            onStart = {
                                isGetLocationEnabled = false
                                statusMessage = R.string.inGettingLocation
                            },
                            onGpsDisabled = {
                                isGetLocationEnabled = true
                                statusMessage = R.string.pleaseCheckIfGPSIsOn
                            }
                        )
                    }
                )
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
                                putFloat(
                                    LATITUDE_KEY,
                                    latitude!!.toFloat()
                                )
                                putFloat(
                                    LONGITUDE_KEY,
                                    longitude!!.toFloat()
                                )
                                commit()
                            }

                            onLocationSaved()
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
fun LocationSettingContent(
    latitudeText: String,
    onLatitudeChanged: (String) -> Unit,
    isLatitudeError: Boolean,
    longitudeText: String,
    onLongitudeChanged: (String) -> Unit,
    isLongitudeError: Boolean,
    statusMessage: String,
    isGetLocationEnabled: Boolean,
    onGetLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(320.dp)
            .wrapContentSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.latitude),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
            OutlinedTextField(
                value = latitudeText,
                onValueChange = onLatitudeChanged,
                modifier = Modifier.weight(1f),
                isError = isLatitudeError,
                singleLine = true,
                placeholder = { Text(stringResource(R.string.latitudeHint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.longitude),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
            OutlinedTextField(
                value = longitudeText,
                onValueChange = onLongitudeChanged,
                modifier = Modifier.weight(1f),
                isError = isLongitudeError,
                singleLine = true,
                placeholder = { Text(stringResource(R.string.longitudeHint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Button(
            onClick = onGetLocationClick,
            enabled = isGetLocationEnabled,
            modifier = Modifier.width(180.dp)
        ) {
            Text(stringResource(R.string.gps))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = statusMessage,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LocationSettingContentPreview() {
    CwpTheme {
        LocationSettingContent(
            latitudeText = "+35.6895",
            onLatitudeChanged = {},
            isLatitudeError = false,
            longitudeText = "+139.6917",
            onLongitudeChanged = {},
            isLongitudeError = false,
            statusMessage = "",
            isGetLocationEnabled = true,
            onGetLocationClick = {}
        )
    }
}

@SuppressLint("MissingPermission")
private fun startLocationUpdates(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    locationCallback: LocationCallback,
    onError: () -> Unit
) {
    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        MAXIMUM_UPDATE_INTERVAL
    )
        .setMinUpdateIntervalMillis(MINIMUM_UPDATE_INTERVAL)
        .setWaitForAccurateLocation(true)
        .build()

    val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    } else {
        onError()
    }
}
