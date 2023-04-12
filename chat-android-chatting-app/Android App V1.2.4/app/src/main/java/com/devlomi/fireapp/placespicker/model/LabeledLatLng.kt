package com.devlomi.fireapp.activities.placespicker.model

import com.google.gson.annotations.SerializedName

data class LabeledLatLng(
        @SerializedName("label")
        val label: String,
        @SerializedName("lat")
        val lat: Double,
        @SerializedName("lng")
        val lng: Double
)