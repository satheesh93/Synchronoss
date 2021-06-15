package com.synchronoss.myapplication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Wind(val speed: Float?, val deg: Int?):Parcelable
