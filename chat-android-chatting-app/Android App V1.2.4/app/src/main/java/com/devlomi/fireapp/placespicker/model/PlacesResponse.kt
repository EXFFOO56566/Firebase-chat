package com.devlomi.fireapp.activities.placespicker.model

import com.google.gson.annotations.SerializedName

data class PlacesResponse(
        @SerializedName("meta")
        val meta: Meta,
        @SerializedName("response")
        val response: Response
)