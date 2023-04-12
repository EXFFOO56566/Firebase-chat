package com.devlomi.fireapp.activities.placespicker

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Place(val name: String, val address: String, val iconUrl: String?, val latLng: LatLng) : Parcelable{
    companion object{
        const val EXTRA_PLACE = "place"
    }
}



