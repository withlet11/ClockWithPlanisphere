/**
 * LocationSettingFragment.kt
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

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.google.android.gms.location.*
import io.github.withlet11.clockwithplanisphere.R

class LocationSettingFragment : DialogFragment() {
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0

    private var latitudeTextState by mutableStateOf("")
    private var longitudeTextState by mutableStateOf("")
    private var isLatitudeErrorState by mutableStateOf(false)
    private var isLongitudeErrorState by mutableStateOf(false)
    private var statusMessageState by mutableStateOf("")
    private var isGetLocationEnabledState by mutableStateOf(true)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var dialog: AlertDialog

    companion object {
        private const val MAXIMUM_UPDATE_INTERVAL = 10000L
        private const val MINIMUM_UPDATE_INTERVAL = 5000L
        private const val REQUEST_PERMISSION = 1000
        const val DEFAULT_LATITUDE = 45.0
        const val DEFAULT_LONGITUDE = 0.0
    }

    interface LocationSettingDialogListener {
        fun onLocationDialogPositiveClick(dialog: DialogFragment)
        fun onLocationDialogNegativeClick(dialog: DialogFragment)
    }

    private var listener: LocationSettingDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as LocationSettingDialogListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getPreviousValues()

        latitudeTextState = "%+f".format(latitude)
        longitudeTextState = "%+f".format(longitude)

        val composeView = ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    LocationSettingContent(
                        latitudeText = latitudeTextState,
                        onLatitudeChanged = { text ->
                            latitudeTextState = text
                            val parsed = text.replace(',', '.').toDoubleOrNull()
                            if (parsed != null && parsed >= -90.0 && parsed <= 90.0) {
                                latitude = parsed
                                isLatitudeErrorState = false
                            } else {
                                latitude = null
                                isLatitudeErrorState = true
                            }
                            updatePositiveButtonState()
                        },
                        isLatitudeError = isLatitudeErrorState,
                        longitudeText = longitudeTextState,
                        onLongitudeChanged = { text ->
                            longitudeTextState = text
                            val parsed = text.replace(',', '.').toDoubleOrNull()
                            if (parsed != null && parsed >= -180.0 && parsed <= 180.0) {
                                longitude = parsed
                                isLongitudeErrorState = false
                            } else {
                                longitude = null
                                isLongitudeErrorState = true
                            }
                            updatePositiveButtonState()
                        },
                        isLongitudeError = isLongitudeErrorState,
                        statusMessage = statusMessageState,
                        isGetLocationEnabled = isGetLocationEnabledState,
                        onGetLocationClick = { startGPS() }
                    )
                }
            }
        }

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(composeView)
            .setTitle(R.string.locationSettings)
            .setPositiveButton(context?.getText(R.string.modify)) { _, _ ->
                context?.getSharedPreferences("observation_position", Context.MODE_PRIVATE)?.edit()
                    ?.run {
                        putFloat("latitude", latitude?.toFloat() ?: DEFAULT_LATITUDE.toFloat())
                        putFloat("longitude", longitude?.toFloat() ?: DEFAULT_LONGITUDE.toFloat())
                        commit()
                    }
                listener?.onLocationDialogPositiveClick(this)
            }
            .setNegativeButton(context?.getText(R.string.cancel)) { _, _ ->
                listener?.onLocationDialogNegativeClick(this)
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
        locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, MAXIMUM_UPDATE_INTERVAL)
                .setMinUpdateIntervalMillis(MINIMUM_UPDATE_INTERVAL)
                .setWaitForAccurateLocation(true)
                .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation

                latitude = location?.latitude
                longitude = location?.longitude
                latitudeTextState = latitude?.let { "%+f".format(it) } ?: ""
                longitudeTextState = longitude?.let { "%+f".format(it) } ?: ""
                isLatitudeErrorState = false
                isLongitudeErrorState = false
                unlockViewItems()
                statusMessageState = ""

                fusedLocationClient.removeLocationUpdates(this)
            }
        }
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    private fun getPreviousValues() {
        context?.getSharedPreferences("observation_position", Context.MODE_PRIVATE)?.run {
            latitude = getFloat("latitude", DEFAULT_LATITUDE.toFloat()).toDouble()
            longitude = getFloat("longitude", DEFAULT_LONGITUDE.toFloat()).toDouble()
        }
    }

    private fun updatePositiveButtonState() {
        if (::dialog.isInitialized) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled =
                latitude != null && longitude != null
        }
    }

    private fun lockViewItems() {
        isGetLocationEnabledState = false
        updatePositiveButtonState()
    }

    private fun unlockViewItems() {
        isGetLocationEnabledState = true
        updatePositiveButtonState()
    }

    private fun startGPS() {
        context?.let {
            lockViewItems()
            statusMessageState = getString(R.string.inGettingLocation)
            val isPermissionFineLocation = ActivityCompat.checkSelfPermission(
                it, Manifest.permission.ACCESS_FINE_LOCATION
            )
            val isPermissionCoarseLocation = ActivityCompat.checkSelfPermission(
                it, Manifest.permission.ACCESS_COARSE_LOCATION
            )

            if (isPermissionFineLocation != PackageManager.PERMISSION_GRANTED &&
                isPermissionCoarseLocation != PackageManager.PERMISSION_GRANTED
            ) {
                unlockViewItems()
                requestLocationPermission()
            } else {
                val locationManager: LocationManager =
                    it.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                } else {
                    unlockViewItems()
                    statusMessageState = getString(R.string.pleaseCheckIfGPSIsOn)
                }
            }
        }
    }

    private fun requestLocationPermission() {
        activity?.let {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                statusMessageState = getString(R.string.no_permission_to_access_location_permanent)
            } else {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION
                )
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
            .padding(16.dp)
            .wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        Row(
//            modifier = Modifier.width(320.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
            Column(
                modifier = Modifier.width(320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
            // }

            Button(
                onClick = onGetLocationClick,
                enabled = isGetLocationEnabled,
                modifier = Modifier.width(320.dp)
            ) {
                Text(stringResource(R.string.gps))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = statusMessage,
            modifier = Modifier
                .width(320.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LocationSettingContentPreview() {
    MaterialTheme {
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
