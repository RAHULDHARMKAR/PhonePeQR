package com.rahuldharmkar.phonepeqrintegration.data

import com.google.gson.annotations.SerializedName

data class QRData(
    @SerializedName("qrString") val qrString: String
)
