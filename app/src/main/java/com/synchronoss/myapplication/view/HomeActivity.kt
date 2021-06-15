package com.synchronoss.myapplication.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.synchronoss.myapplication.R
import com.synchronoss.myapplication.databinding.ActivityHomeBinding
import com.synchronoss.myapplication.utils.Utils
import com.synchronoss.myapplication.viewmodel.HomeViewModel

class HomeActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var homeViewModel: HomeViewModel

    var latitude: Double? = null
    var longitude: Double? = null

    private var locationAvailable = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_GPS_PERMISSION = 100
    private val REQUEST_CHECK_SETTINGS = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        init()
    }

    private fun init() {
        setViewModel()
        setUI()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocation()
    }

    private fun setUI() {
        setSupportActionBar(binding.toolbar)

        binding.getWeatherUpdates.setOnClickListener(this)
    }

    private fun setViewModel() {

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        binding.viewModel = homeViewModel
        binding.lifecycleOwner = this
//        homeViewModel.vehicleTonnage.observe(this, vehicleTonnageObserver)
    }

    private fun getCurrentLocation(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Utils.Util.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), REQUEST_GPS_PERMISSION
            )
            return false
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            Log.i("locationSettingResponse", " ${locationSettingsResponse.locationSettingsStates}")
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.i("sendEx", " $sendEx")
                }
            }
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    latitude = location.latitude
                    longitude = location.longitude
                    fusedLocationClient.removeLocationUpdates(this)
                    break
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_GPS_PERMISSION -> {
                if (!Utils.Util.isAllPermissionsGranted(
                        this,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                ) {
                    Toast.makeText(
                        this,
                        "Please provide access to GPS to proceed further",
                        Toast.LENGTH_SHORT
                    ).show()
                    locationAvailable = getCurrentLocation()
                } else {
                    locationAvailable = getCurrentLocation()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> {
                if (resultCode != RESULT_OK) {
                    Toast.makeText(
                        this, "Please Switch-On the GPS to proceed further",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.get_weather_updates -> {
                if (Utils.Util.isAllPermissionsGranted(
                        this,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    ) && locationAvailable
                ) {
                    startActivity(Intent(this, WeatherUpdateActivity::class.java))

                } else {
                    locationAvailable = getCurrentLocation()
                }
            }
        }
    }


}
