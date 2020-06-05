package com.example.speedometerapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), UpdateView {

    companion object {
        const val TAG = "MainActivity"
        private const val REQUEST_PERMISSION_ID = 10
    }

    private val presenter: PresenterType = MainPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun checkPermissionAndGPS() {
        if (checkPermission()) {
            if (isGPSEnabled()) {
                presenter.calculateSpeed()
            } else {
                Toast.makeText(this, "Enable GPS", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermission()
        }
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_ID) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissionAndGPS()
            }
        }
    }

    override fun updateCurrentSpeedView(s: String) {
        currentSpeed.text = s
    }

    override fun updateFrom10To30SpeedView(s: String) {
        speedFrom10To30.text = s
    }

    override fun updateFrom30To10SpeedView(s: String) {
        speedFrom30To10.text = s
    }

    override fun getContext(): Context {
        return this
    }

    override fun onStart() {
        super.onStart()
        checkPermissionAndGPS()
    }

}

interface UpdateView {
    fun updateCurrentSpeedView(s: String)
    fun updateFrom10To30SpeedView(s: String)
    fun updateFrom30To10SpeedView(s: String)
    fun getContext(): Context
}
