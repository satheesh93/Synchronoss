package com.synchronoss.myapplication.api

import com.synchronoss.myapplication.model.WeatherResponseModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface APIInterface {

    @GET("data/2.5/weather?")
    fun getWeatherDetails(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("APPID") appID: String
    ): Call<WeatherResponseModel>

}
