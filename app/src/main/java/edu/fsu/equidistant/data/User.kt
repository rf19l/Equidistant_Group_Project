package edu.fsu.equidistant.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val username: String = "",
    val email: String = "",
    var token: String = "",
    val longitude: Double = 0.0,
    val latitude: Double = 0.0
) : Parcelable
