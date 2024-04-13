package com.rahuldharmkar.phonepeqrintegration.data

import com.google.gson.annotations.SerializedName

data class QRInitResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: QRData?
)
