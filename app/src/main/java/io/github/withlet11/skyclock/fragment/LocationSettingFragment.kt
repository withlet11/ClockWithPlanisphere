/**
 * LocationSettingFragment.kt
 *
 * Copyright 2021 Yasuhiro Yamakawa <withlet11@gmail.com>
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

package io.github.withlet11.skyclock.fragment

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.google.android.gms.location.*
import io.github.withlet11.skyclock.R


class LocationSettingFragment : DialogFragment() {
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0
    private lateinit var latitudeField: TextView
    private lateinit var longitudeField: TextView

    private lateinit var getLocationButton: Button
    private lateinit var statusField: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var dialog: AlertDialog

    private var normalTextColor = 0
    private var warningTextColor = 0

    companion object {
        private const val MAXIMUM_UPDATE_INTERVAL = 10000L
        private const val MINIMUM_UPDATE_INTERVAL = 5000L
        private const val REQUEST_PERMISSION = 1000
    }

    interface LocationSettingDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    private var listener: LocationSettingDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as LocationSettingDialogListener
        normalTextColor = context.getColor(R.color.black)
        warningTextColor = context.getColor(R.color.red)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
        val inflater = activity!!.layoutInflater
        val locationSettingView = inflater.inflate(R.layout.fragment_location_setting, null)
        builder.setView(locationSettingView)
            .setTitle(R.string.locationSettings)
            .setPositiveButton(context?.getText(R.string.modify)) { _, _ ->
                context?.getSharedPreferences("observation_position", Context.MODE_PRIVATE)?.edit()
                    ?.run {
                        putFloat("latitude", latitude?.toFloat() ?: 0f)
                        putFloat("longitude", longitude?.toFloat() ?: 0f)
                        commit()
                    }
                listener?.onDialogPositiveClick(this)
            }
            .setNegativeButton(context?.getText(R.string.cancel)) { _, _ -> listener?.onDialogNegativeClick(this) }

        getPreviousValues()
        prepareGUIComponents(locationSettingView)

        return builder.create().also { dialog = it }
    }

    override fun onStart() {
        super.onStart()
        locationRequest = LocationRequest.create().apply {
            interval = MAXIMUM_UPDATE_INTERVAL
            fastestInterval = MINIMUM_UPDATE_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                val location = locationResult?.lastLocation ?: return

                latitude = location.latitude
                longitude = location.longitude
                latitudeField.text = "%+f".format(latitude)
                longitudeField.text = "%+f".format(longitude)
                unlockViewItems()
                statusField.text = ""

                fusedLocationClient.removeLocationUpdates(this)
            }
        }
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    private fun getPreviousValues() {
        context?.getSharedPreferences("observation_position", Context.MODE_PRIVATE)?.run {
            latitude = getFloat("latitude", 0f).toDouble()
            longitude = getFloat("longitude", 0f).toDouble()
        }
    }

    private fun prepareGUIComponents(locationSettingView: View) {
        latitudeField = locationSettingView.findViewById<TextView>(R.id.latitudeField).apply {
            keyListener = DigitsKeyListener.getInstance("0123456789.,+-")
            setAutofillHints("%+.4f".format(23.4567))
            hint = "%+.4f".format(23.4567)
            text = "%+f".format(latitude)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    latitude = latitudeField.text.toString().replace(',', '.').toDoubleOrNull()
                    latitude?.let { if (it > 90.0 || it < -90.0) latitude = null }
                    latitudeField.setTextColor(if (latitude == null) warningTextColor else normalTextColor)
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                        latitude != null && longitude != null
                }
            })
        }

        longitudeField = locationSettingView.findViewById<TextView>(R.id.longitudeField).apply {
            keyListener = DigitsKeyListener.getInstance("0123456789.,+-")
            setAutofillHints("%+.3f".format(123.456))
            hint = "%+.3f".format(123.456)
            text = "%+f".format(longitude)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    longitude = longitudeField.text.toString().replace(',', '.').toDoubleOrNull()
                    longitude?.let { if (it > 180.0 || it < -180.0) longitude = null }
                    longitudeField.setTextColor(if (longitude == null) warningTextColor else normalTextColor)
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                        latitude != null && longitude != null
                }
            })
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)

        getLocationButton = locationSettingView.findViewById<Button>(R.id.getLocationButton).apply {
            setOnClickListener { startGPS() }
        }

        statusField = locationSettingView.findViewById(R.id.statusField)

    }

    private fun lockViewItems() {
        latitudeField.isEnabled = false
        longitudeField.isEnabled = false
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        getLocationButton.isEnabled = false
    }

    private fun unlockViewItems() {
        latitudeField.isEnabled = true
        longitudeField.isEnabled = true
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
            latitude != null && longitude != null
        getLocationButton.isEnabled = true
    }

    private fun startGPS() {
        context?.let { _context ->
            lockViewItems()
            statusField.text = getString(R.string.inGettingLocation)
            val isPermissionFineLocation = ActivityCompat.checkSelfPermission(
                _context, Manifest.permission.ACCESS_FINE_LOCATION
            )
            val isPermissionCoarseLocation = ActivityCompat.checkSelfPermission(
                _context, Manifest.permission.ACCESS_COARSE_LOCATION
            )

            if (isPermissionFineLocation != PackageManager.PERMISSION_GRANTED &&
                isPermissionCoarseLocation != PackageManager.PERMISSION_GRANTED
            ) {
                unlockViewItems()
                requestLocationPermission()
            } else {
                val locationManager: LocationManager =
                    _context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                } else {
                    unlockViewItems()
                    statusField.text = getString(R.string.pleaseCheckIfGPSIsOn)
                }
            }
        }
    }

    private fun requestLocationPermission() {
        activity?.let { _activity ->
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    _activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                statusField.text = getString(R.string.no_permission_to_access_location_permanent)
            } else {
                ActivityCompat.requestPermissions(
                    _activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION
                )
            }
        }
    }
}