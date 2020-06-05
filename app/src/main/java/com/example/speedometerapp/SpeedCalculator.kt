package com.example.speedometerapp

import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*

class SpeedCalculator(private val context: Context) {

    private var fusedLocation: FusedLocationProviderClient
    private var locationCallback: LocationCallback
    private var startLocation: Location? = null
    private var endLocation: Location? = null
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var numOfCalculations = 0
    private var isLocationReachedTo10KM = false
    private var isLocationReachedTo30KM = false

    private lateinit var updateSpeed: (String) -> Unit
    private lateinit var updateSpeedFrom10To30: (String) -> Unit
    private lateinit var updateSpeedFrom30To10: (String) -> Unit

    init {
        fusedLocation = LocationServices.getFusedLocationProviderClient(context)
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
    }

    fun doCalculations(func1: (String) -> Unit, func2: (String) -> Unit, func3: (String) -> Unit) {
        updateSpeed = func1
        updateSpeedFrom10To30 = func2
        updateSpeedFrom30To10 = func3
        requestLocationData()
    }

    private fun requestLocationData() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 100
        locationRequest.fastestInterval = 200
        fusedLocation = LocationServices.getFusedLocationProviderClient(context)
        fusedLocation.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private fun calculateTakenTime(start: Location, end: Location) {
        updateSpeed("15")
        if (isLocationReachedTo10KM) {
            endTime = System.currentTimeMillis()
            updateSpeed("${(getDistance(start, end) / 1000) / ((startTime - endTime) / 3_600_000)}")
            if (isLocationReachedTo30KM) {
                val takenTime = (endTime - startTime) / 1000
                ++numOfCalculations

                if (numOfCalculations == 1) {
                    updateSpeedFrom10To30("$takenTime")
                } else if (numOfCalculations == 2) {
                    updateSpeedFrom30To10("$takenTime")
                    fusedLocation.removeLocationUpdates(locationCallback)
                }
            } else {
                val distance = getDistance(start, end)
                if (distance / 1000 >= 30) {
                    isLocationReachedTo30KM = true
                    startLocation = endLocation
                    startTime = 0
                    endTime = 0
                }
            }
        } else {
            val distance: Float = getDistance(start, end)
            if (distance / 1000 >= 10) {
                isLocationReachedTo10KM = true
                startLocation = end
                startTime = System.currentTimeMillis()
            }
        }
    }

    private fun getDistance(start: Location, end: Location): Float {
        return start.distanceTo(end)
    }
}