package com.synchronoss.myapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.synchronoss.myapplication.api.APIClient
import com.synchronoss.myapplication.api.APIInterface
import com.synchronoss.myapplication.model.WeatherResponseModel
import com.synchronoss.myapplication.workers.GetCurrentLocationWorker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class WeatherUpdateViewModel(application: Application) : AndroidViewModel(application) {
    private val workManager: WorkManager = WorkManager.getInstance(application.applicationContext)
    val appID = "18330d542449b18eb5cf6ea8d97a1d57"

    private val _isViewLoading = MutableLiveData<Boolean>()
    val isViewLoading: LiveData<Boolean> = _isViewLoading

    private val _onWeatherUpdate = MutableLiveData<WeatherResponseModel>()
    val onWeatherUpdate: LiveData<WeatherResponseModel> = _onWeatherUpdate

    fun startWeatherUpdates() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()

        val checkDBStatusWorkerBuilder =
            PeriodicWorkRequestBuilder<GetCurrentLocationWorker>(15, TimeUnit.MINUTES)

//        val weatherDataBuilder = Data.Builder()
//        weatherDataBuilder.putString("Location", location)
//        weatherWorkerBuilder.setInputData(weatherDataBuilder.build())

        val startWeatherUpdateWorker = checkDBStatusWorkerBuilder
            .setConstraints(constraints)
            .addTag("WeatherWorkerPeriodic")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "WeatherWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            startWeatherUpdateWorker
        )
    }

    fun getWeatherUpdate(lat: Double, lon: Double) {
        val apiInterface: APIInterface = APIClient.client.create(APIInterface::class.java)

        _isViewLoading.value = true
        apiInterface.getWeatherDetails(lat, lon, appID)
            .enqueue(object : Callback<WeatherResponseModel> {
                override fun onResponse(
                    call: Call<WeatherResponseModel>,
                    response: Response<WeatherResponseModel>
                ) {
                    _isViewLoading.value = false

                    if (response.isSuccessful) {
                        _onWeatherUpdate.value = response.body()
                    } else {
                        if (response.code() == 401) {

                        } else {
                            Log.i("ERRRRROR response", response.errorBody()!!.string() + "")
                        }
                    }
                }

                override fun onFailure(call: Call<WeatherResponseModel>, t: Throwable) {
                    _isViewLoading.value = false

                }
            })
    }

}