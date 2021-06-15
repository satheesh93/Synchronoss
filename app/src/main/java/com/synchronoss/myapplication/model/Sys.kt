package com.synchronoss.myapplication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Sys(
    val type: Number?,
    val id: Number?,
    val country: String?,
    val sunrise: Number?,
    val sunset: Number?
):Parcelable
