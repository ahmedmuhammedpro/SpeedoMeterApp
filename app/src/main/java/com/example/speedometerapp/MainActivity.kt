package com.example.speedometerapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
        private const val REQUEST_PERMISSION_ID = 10
    }

    private lateinit var fusedLocation: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var startLocation: Location? = null
    private var endLocation: Location? = null

    private var startTime: Long = 0
    private var endTime: Long = 0

    private var numOfCalculations = 0

    private var isLocationReachedTo10KM = false
    private var isLocationReachedTo30KM = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                endLocation = locationResult?.lastLocation
                if (startLocation == null) {
                    startLocation = endLocation
                }
                calculateTakenTime(startLocation!!, endLocation!!)
            }
        }

        getLastLocation()
    }

    private fun getLastLocation() {
        if (checkPermission()) {
            if (isGPSEnabled()) {
                requestLocationData()
            } else {
                Toast.makeText(this, "Enable GPS", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermission()
        }
    }

    private fun requestLocationData() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 100
        locationRequest.fastestInterval = 200
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        fusedLocation.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private fun checkPermission(): Boolean {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION_ID)
    }

    private fun isGPSEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun calculateTakenTime(start: Location, end: Location) {
        if (isLocationReachedTo10KM) {
            endTime = System.currentTimeMillis()
            currentSpeed.text = "${(getDistance(start, end) / 1000) / ((startTime - endTime) / 3_600_000)}"
            if (isLocationReachedTo30KM) {
                val takenTime = (endTime - startTime) / 1000
                ++numOfCalculations

                if (numOfCalculations == 1) {
                    speedFrom10To30.text = "$takenTime"
                } else if (numOfCalculations == 2) {
                    speedFrom30To10.text = "$takenTime"
                    fusedLocation.removeLocationUpdates(locationCallback)
                }
            } else {
                val distance = getDistance(start, end)
                if (distance / 1000 >= 30) {
                    isLocationReachedTo30KM = true
                    startLocation = endLocation
                    startTime = 0
                    endTime = 0
                    Toast.makeText(this, "Return now", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            val distance: Float = getDistance(start, end)
            if (distance / 1000 >= 10) {
                isLocationReachedTo10KM = true
                startLocation = end
                startTime = System.currentTimeMillis()
                Log.i(TAG, "distance = $distance, reached good work")
            }
        }
    }

    private fun getDistance(start: Location, end: Location): Float {
        return start.distanceTo(end)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_ID) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (checkPermission())
            getLastLocation()
    }
}
