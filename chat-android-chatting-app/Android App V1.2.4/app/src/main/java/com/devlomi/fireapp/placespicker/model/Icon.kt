package com.devlomi.fireapp.activities.placespicker.model

import com.google.gson.annotations.SerializedName

data class Icon(
        @SerializedName("prefix")
        val prefix: String,
        @SerializedName("suffix")
        val suffix: String
) {
    fun getIcon(size: Int) = "$prefix$size$suffix"


}

