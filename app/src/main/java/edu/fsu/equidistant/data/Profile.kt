package edu.fsu.equidistant.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
class Profile(
        val uuid:String="",
        val username: String = "",
        val photoRef:String=""
    ) : Parcelable