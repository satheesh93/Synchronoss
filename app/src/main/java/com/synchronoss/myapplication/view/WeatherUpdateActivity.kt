package com.synchronoss.myapplication.view

import android.annotation.SuppressLint
import android.content.IntentSender
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.synchronoss.myapplication.R
import com.synchronoss.myapplication.databinding.ActivityWeatherUpdateBinding
import com.synchronoss.myapplication.model.WeatherResponseModel
import com.synchronoss.myapplication.viewmodel.WeatherUpdateViewModel

class WeatherUpdateActivity : AppCompatActivity() {
    lateinit var binding: ActivityWeatherUpdateBinding
    lateinit var weatherUpdateViewModel: WeatherUpdateViewModel

    var latitude: Double? = null
    var longitude: Double? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_CHECK_SETTINGS = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_weather_update)

        init()
    }

    private fun init() {
        setViewModel()
        setUI()

        weatherUpdateViewModel.startWeatherUpdates()
        setWorkerListener()
    }

    private fun setUI() {
        setSupportActionBar(binding.toolbar)
    }

    private fun setViewModel() {
        weatherUpdateViewModel = ViewModelProvider(this).get(WeatherUpdateViewModel::class.java)
        binding.viewModel = weatherUpdateViewModel
        binding.lifecycleOwner = this

        weatherUpdateViewModel.onWeatherUpdate.observe(this,weatherObserver)
    }

    private fun setWorkerListener() {
        WorkManager.getInstance(this).getWorkInfosByTagLiveData("WeatherWorkerPeriodic")
            .observe(this, {

                if (latitude != null && longitude != null) {
                    getCurrentLocation()
                    weatherUpdateViewModel.getWeatherUpdate(latitude!!, longitude!!)
                }
            })
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(): Boolean {

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

                    weatherUpdateViewModel.getWeatherUpdate(latitude!!, longitude!!)
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

    val weatherObserver = Observer<WeatherResponseModel> {
        binding.city.text = it.name
        binding.temperature.text = it.main?.temp.toString()
        binding.pressure.text = it.main?.pressure.toString()
        binding.humidity.text = it.main?.humidity.toString()
    }

}